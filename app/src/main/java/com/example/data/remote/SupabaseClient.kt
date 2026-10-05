package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.CachedCertificateType
import com.example.data.model.GeneratedCertificate
import com.example.data.model.UnionProfile
import com.example.data.model.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

sealed class AuthResult {
    data class Success(val user: UserProfile) : AuthResult()
    data class InactiveAccount(val message: String) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class SupabaseClient(private val sessionTokenProvider: () -> String?) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private val baseUrl: String
        get() = BuildConfig.SUPABASE_URL.trimEnd('/')

    private val anonKey: String
        get() = BuildConfig.SUPABASE_ANON_KEY

    val isConfigured: Boolean
        get() = baseUrl.isNotBlank() &&
                !baseUrl.contains("placeholder") &&
                !baseUrl.contains("your-project") &&
                anonKey.isNotBlank() &&
                !anonKey.contains("placeholder")

    private fun newRequestBuilder(authenticated: Boolean = true): Request.Builder {
        val builder = Request.Builder()
            .header("apikey", anonKey)
            .header("Content-Type", "application/json")

        val token = if (authenticated) sessionTokenProvider() ?: anonKey else anonKey
        builder.header("Authorization", "Bearer $token")
        return builder
    }

    suspend fun login(email: String, pass: String): AuthResult = withContext(Dispatchers.IO) {
        if (!isConfigured) {
            return@withContext AuthResult.Error("Supabase URL অথবা Anon Key এখনও কনফিগার করা হয়নি। আপনি অফলাইন মোড ব্যবহার করতে পারেন।")
        }

        try {
            val url = "$baseUrl/auth/v1/token?grant_type=password"
            val bodyObj = JSONObject().apply {
                put("email", email.trim())
                put("password", pass)
            }
            val request = newRequestBuilder(authenticated = false)
                .url(url)
                .post(bodyObj.toString().toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                val respStr = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    val errorMsg = try {
                        JSONObject(respStr).optString("error_description", "লগইন ব্যর্থ হয়েছে। ইমেইল বা পাসওয়ার্ড সঠিক নয়।")
                    } catch (_: Exception) {
                        "লগইন ব্যর্থ হয়েছে (কোড: ${response.code})"
                    }
                    return@withContext AuthResult.Error(errorMsg)
                }

                val json = JSONObject(respStr)
                val accessToken = json.getString("access_token")
                val userObj = json.getJSONObject("user")
                val userId = userObj.getString("id")
                val userEmail = userObj.optString("email", email)

                // Fetch Profile from public.profiles
                val profileResult = fetchUserProfile(userId, accessToken)
                if (profileResult is AuthResult.Success) {
                    val profile = profileResult.user
                    if (!profile.isActive) {
                        return@withContext AuthResult.InactiveAccount(
                            "আপনার একাউন্টটি বর্তমানে নিষ্ক্রিয় করা আছে। অনুগ্রহ করে ইউনিয়ন পরিষদ অ্যাডমিন বা সুপার অ্যাডমিনের সাথে যোগাযোগ করুন।"
                        )
                    }
                    return@withContext AuthResult.Success(profile.copy(accessToken = accessToken, email = userEmail))
                } else if (profileResult is AuthResult.InactiveAccount) {
                    return@withContext profileResult
                } else {
                    // Fallback to basic operator profile
                    val meta = userObj.optJSONObject("user_metadata")
                    val fullName = meta?.optString("full_name") ?: userEmail
                    return@withContext AuthResult.Success(
                        UserProfile(
                            id = userId,
                            email = userEmail,
                            fullName = fullName,
                            role = "operator",
                            isActive = true,
                            accessToken = accessToken
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("SupabaseClient", "Login error", e)
            AuthResult.Error("সার্ভারের সাথে সংযোগ স্থাপন করা সম্ভব হয়নি: ${e.localizedMessage}")
        }
    }

    suspend fun fetchUserProfile(userId: String, token: String): AuthResult = withContext(Dispatchers.IO) {
        try {
            val url = "$baseUrl/rest/v1/profiles?id=eq.$userId&select=*"
            val request = Request.Builder()
                .url(url)
                .header("apikey", anonKey)
                .header("Authorization", "Bearer $token")
                .header("Content-Type", "application/json")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                val respStr = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    return@withContext AuthResult.Error("প্রোফাইল লোড করা যায়নি")
                }
                val array = JSONArray(respStr)
                if (array.length() == 0) {
                    return@withContext AuthResult.Error("ব্যবহারকারীর তথ্য পাওয়া যায়নি")
                }
                val obj = array.getJSONObject(0)
                val isActive = obj.optBoolean("is_active", true)
                if (!isActive) {
                    return@withContext AuthResult.InactiveAccount("আপনার একাউন্টটি নিষ্ক্রিয় করা হয়েছে।")
                }
                val profile = UserProfile(
                    id = obj.getString("id"),
                    email = "",
                    fullName = obj.optString("full_name", "ইউপি ব্যবহারকারী"),
                    role = obj.optString("role", "operator"),
                    unionId = if (obj.isNull("union_id")) null else obj.optString("union_id"),
                    phone = if (obj.isNull("phone")) null else obj.optString("phone"),
                    isActive = isActive,
                    accessToken = token
                )
                AuthResult.Success(profile)
            }
        } catch (e: Exception) {
            AuthResult.Error(e.localizedMessage ?: "প্রোফাইল ত্রুটি")
        }
    }

    suspend fun fetchUnionProfile(unionId: String): UnionProfile? = withContext(Dispatchers.IO) {
        if (!isConfigured) return@withContext null
        try {
            val url = "$baseUrl/rest/v1/unions?id=eq.$unionId&select=*"
            val request = newRequestBuilder(authenticated = true).url(url).get().build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val array = JSONArray(response.body?.string() ?: "[]")
                if (array.length() == 0) return@withContext null
                val obj = array.getJSONObject(0)
                UnionProfile(
                    id = 1,
                    unionName = obj.optString("name_bn", ""),
                    upazila = obj.optString("upazila", ""),
                    district = obj.optString("district", ""),
                    chairmanName = obj.optString("chairman_name", ""),
                    unionPhone = obj.optString("phone", ""),
                    unionEmail = obj.optString("email", ""),
                    logoUri = if (obj.isNull("logo_url")) null else obj.optString("logo_url"),
                    isConfigured = true
                )
            }
        } catch (_: Exception) {
            null
        }
    }

    suspend fun fetchCertificateTypes(): List<CachedCertificateType> = withContext(Dispatchers.IO) {
        if (!isConfigured) return@withContext emptyList()
        try {
            val url = "$baseUrl/rest/v1/certificate_types?is_active=eq.true&select=*"
            val request = newRequestBuilder(authenticated = true).url(url).get().build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                val array = JSONArray(response.body?.string() ?: "[]")
                val list = mutableListOf<CachedCertificateType>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        CachedCertificateType(
                            id = obj.getString("id"),
                            titleBn = obj.getString("title_bn"),
                            englishName = if (obj.isNull("english_name")) null else obj.optString("english_name"),
                            category = obj.optString("category", "নাগরিক সেবা"),
                            fieldsJson = obj.opt("fields")?.toString() ?: "[]",
                            templateBn = obj.getString("template_bn"),
                            isActive = obj.optBoolean("is_active", true),
                            lastUpdated = System.currentTimeMillis()
                        )
                    )
                }
                list
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun generateServerSerialNo(unionId: String): String? = withContext(Dispatchers.IO) {
        if (!isConfigured) return@withContext null
        try {
            val url = "$baseUrl/rest/v1/rpc/generate_certificate_serial_no"
            val body = JSONObject().apply {
                put("p_union_id", unionId)
            }
            val request = newRequestBuilder(authenticated = true)
                .url(url)
                .post(body.toString().toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val str = response.body?.string()?.trim('"', ' ', '\n')
                str
            }
        } catch (_: Exception) {
            null
        }
    }

    suspend fun uploadCertificate(
        cert: GeneratedCertificate,
        unionId: String,
        userId: String
    ): String? = withContext(Dispatchers.IO) {
        if (!isConfigured) return@withContext null
        try {
            val payload = JSONObject().apply {
                put("union_id", unionId)
                put("type_id", cert.certificateTypeId)
                put("created_by", userId)
                put("serial_no", cert.serialNo)
                put("status", "generated")

                val dataObj = JSONObject().apply {
                    put("applicant_name", cert.applicantName)
                    put("father_husband_name", cert.fatherOrHusbandName)
                    put("mother_name", cert.motherName)
                    put("village", cert.village)
                    put("ward_no", cert.wardNo)
                    put("post_office", cert.postOffice)
                    put("upazila", cert.upazila)
                    put("district", cert.district)
                    put("nid_birth_no", cert.nidOrBirthNo)
                    put("issue_date_bn", cert.issueDateBangla)
                    put("generated_body_text", cert.generatedBodyText)
                    put("custom_fields", JSONObject(cert.customFieldsJson))
                    if (cert.heirsJson != null) {
                        put("heirs", JSONArray(cert.heirsJson))
                    }
                }
                put("data", dataObj)
            }

            val request = if (cert.remoteId.isNullOrBlank()) {
                val url = "$baseUrl/rest/v1/certificates"
                newRequestBuilder(authenticated = true)
                    .url(url)
                    .header("Prefer", "return=representation")
                    .post(payload.toString().toRequestBody(jsonMediaType))
                    .build()
            } else {
                val url = "$baseUrl/rest/v1/certificates?id=eq.${cert.remoteId}"
                newRequestBuilder(authenticated = true)
                    .url(url)
                    .header("Prefer", "return=representation")
                    .patch(payload.toString().toRequestBody(jsonMediaType))
                    .build()
            }

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e("SupabaseClient", "Upload cert failed: ${response.code}")
                    // Conflict Resolution: If 409 Conflict (e.g. duplicate serial_no), find existing record by serial_no and patch it
                    if (response.code == 409 || response.code == 400) {
                        val existingRemoteId = findCertificateIdBySerialNo(cert.serialNo, unionId)
                        if (!existingRemoteId.isNullOrBlank()) {
                            val patchUrl = "$baseUrl/rest/v1/certificates?id=eq.$existingRemoteId"
                            val patchReq = newRequestBuilder(authenticated = true)
                                .url(patchUrl)
                                .header("Prefer", "return=representation")
                                .patch(payload.toString().toRequestBody(jsonMediaType))
                                .build()
                            client.newCall(patchReq).execute().use { patchResp ->
                                if (patchResp.isSuccessful) {
                                    return@withContext existingRemoteId
                                }
                            }
                        }
                    }
                    return@withContext null
                }
                val respStr = response.body?.string() ?: ""
                val array = JSONArray(respStr)
                if (array.length() > 0) {
                    array.getJSONObject(0).getString("id")
                } else {
                    cert.remoteId
                }
            }
        } catch (e: Exception) {
            Log.e("SupabaseClient", "Upload cert error", e)
            null
        }
    }

    suspend fun findCertificateIdBySerialNo(serialNo: String, unionId: String): String? = withContext(Dispatchers.IO) {
        if (!isConfigured) return@withContext null
        try {
            val url = "$baseUrl/rest/v1/certificates?serial_no=eq.$serialNo&union_id=eq.$unionId&select=id"
            val request = newRequestBuilder(authenticated = true).url(url).get().build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val array = JSONArray(response.body?.string() ?: "[]")
                if (array.length() > 0) {
                    array.getJSONObject(0).getString("id")
                } else null
            }
        } catch (_: Exception) {
            null
        }
    }

    suspend fun softDeleteCertificate(remoteId: String): Boolean = withContext(Dispatchers.IO) {
        if (!isConfigured) return@withContext false
        try {
            val url = "$baseUrl/rest/v1/certificates?id=eq.$remoteId"
            val body = JSONObject().apply {
                put("deleted_at", "now()")
            }
            val request = newRequestBuilder(authenticated = true)
                .url(url)
                .patch(body.toString().toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                response.isSuccessful
            }
        } catch (_: Exception) {
            false
        }
    }

    suspend fun fetchRemoteCertificates(): List<JSONObject> = withContext(Dispatchers.IO) {
        if (!isConfigured) return@withContext emptyList()
        try {
            val url = "$baseUrl/rest/v1/certificates?deleted_at=is.null&order=created_at.desc&select=*"
            val request = newRequestBuilder(authenticated = true).url(url).get().build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                val array = JSONArray(response.body?.string() ?: "[]")
                val list = mutableListOf<JSONObject>()
                for (i in 0 until array.length()) {
                    list.add(array.getJSONObject(i))
                }
                list
            }
        } catch (_: Exception) {
            emptyList()
        }
    }
}

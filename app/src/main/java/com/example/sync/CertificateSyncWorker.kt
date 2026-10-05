package com.example.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.local.AppDatabase
import com.example.data.local.UserSessionManager
import com.example.data.model.GeneratedCertificate
import com.example.data.model.SyncStatus
import com.example.data.remote.SupabaseClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CertificateSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val sessionManager = UserSessionManager(applicationContext)
        val supabase = SupabaseClient(sessionTokenProvider = { sessionManager.getAccessToken() })

        if (!supabase.isConfigured || !sessionManager.isLoggedIn()) {
            return@withContext Result.success()
        }

        val user = sessionManager.currentUser.value ?: return@withContext Result.success()
        if (!user.isActive) {
            return@withContext Result.failure()
        }

        val db = AppDatabase.getInstance(applicationContext)
        val certDao = db.certificateDao()
        val typeDao = db.cachedCertificateTypeDao()

        try {
            // 1. Sync certificate types from Supabase
            val remoteTypes = supabase.fetchCertificateTypes()
            if (remoteTypes.isNotEmpty()) {
                typeDao.insertAll(remoteTypes)
            }

            // 2. Upload pending local certificates (excluding offline drafts)
            val pendingList = certDao.getPendingSyncCertificates()
            val unionId = user.unionId ?: "00000000-0000-0000-0000-000000000001"

            for (cert in pendingList) {
                if (cert.syncStatus == SyncStatus.DRAFT) continue
                try {
                    when (cert.syncStatus) {
                        SyncStatus.PENDING_DELETE -> {
                            if (!cert.remoteId.isNullOrBlank()) {
                                supabase.softDeleteCertificate(cert.remoteId)
                            }
                            certDao.markSoftDeletedSynced(cert.id)
                        }
                        SyncStatus.PENDING_INSERT, SyncStatus.PENDING_UPDATE, SyncStatus.FAILED -> {
                            val remoteId = supabase.uploadCertificate(cert, unionId, user.id)
                            if (remoteId != null) {
                                certDao.updateSyncStatus(cert.id, remoteId, SyncStatus.SYNCED)
                            } else {
                                // Mark as FAILED so user sees visual failure badge and retry button
                                certDao.markFailed(cert.id)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("SyncWorker", "Error syncing certificate id=${cert.id}", e)
                    certDao.markFailed(cert.id)
                }
            }

            // 3. Pull remote certificates into local cache with Conflict Resolution
            val localUnion = db.unionProfileDao().getUnionProfileSync()
            var currentUnionName = localUnion?.unionName?.ifBlank { null }
            var currentChairmanName = localUnion?.chairmanName?.ifBlank { null }

            // If local profile is missing but user has unionId, fetch from Supabase
            if ((currentUnionName == null || currentChairmanName == null) && !user.unionId.isNullOrBlank()) {
                val remoteUnion = supabase.fetchUnionProfile(user.unionId)
                if (remoteUnion != null) {
                    db.unionProfileDao().saveUnionProfile(remoteUnion)
                    currentUnionName = remoteUnion.unionName
                    currentChairmanName = remoteUnion.chairmanName
                }
            }

            val remoteCerts = supabase.fetchRemoteCertificates()
            for (remoteJson in remoteCerts) {
                try {
                    val remoteId = remoteJson.getString("id")
                    val serialNo = remoteJson.getString("serial_no")

                    // Conflict Resolution: Check both remoteId and serialNo to prevent duplicates
                    val existingByRemote = certDao.getCertificateByRemoteId(remoteId)
                    val existingBySerial = if (existingByRemote == null) certDao.getCertificateBySerialNo(serialNo) else null
                    val existing = existingByRemote ?: existingBySerial

                    if (existing != null) {
                        // Already exists locally: ensure it's marked as SYNCED with correct remoteId
                        if (existing.remoteId != remoteId || existing.syncStatus != SyncStatus.SYNCED) {
                            certDao.updateSyncStatus(existing.id, remoteId, SyncStatus.SYNCED)
                        }
                    } else {
                        val data = remoteJson.optJSONObject("data")
                        val typeId = remoteJson.getString("type_id")
                        val unionName = data?.optString("union_name")?.ifBlank { null }
                            ?: currentUnionName
                            ?: "ইউনিয়ন পরিষদ"
                        val chairmanName = data?.optString("chairman_name")?.ifBlank { null }
                            ?: currentChairmanName
                            ?: "চেয়ারম্যান"

                        val createdCert = GeneratedCertificate(
                            remoteId = remoteId,
                            syncStatus = SyncStatus.SYNCED,
                            certificateTypeId = typeId,
                            certificateTitle = data?.optString("certificate_title", "ইউপি সনদ") ?: "ইউপি সনদ",
                            applicantName = data?.optString("applicant_name", "") ?: "",
                            fatherOrHusbandName = data?.optString("father_husband_name", "") ?: "",
                            motherName = data?.optString("mother_name", "") ?: "",
                            village = data?.optString("village", "") ?: "",
                            wardNo = data?.optString("ward_no", "১") ?: "১",
                            postOffice = data?.optString("post_office", "") ?: "",
                            upazila = data?.optString("upazila", "") ?: "",
                            district = data?.optString("district", "") ?: "",
                            nidOrBirthNo = data?.optString("nid_birth_no", "") ?: "",
                            serialNo = serialNo,
                            issueDateBangla = data?.optString("issue_date_bn", "") ?: "",
                            generatedBodyText = data?.optString("generated_body_text", "") ?: "",
                            unionName = unionName,
                            chairmanName = chairmanName,
                            customFieldsJson = data?.optJSONObject("custom_fields")?.toString() ?: "{}",
                            heirsJson = data?.optJSONArray("heirs")?.toString()
                        )
                        certDao.insertCertificate(createdCert)
                    }
                } catch (e: Exception) {
                    Log.w("SyncWorker", "Error processing remote cert record", e)
                }
            }

            Result.success()
        } catch (e: Exception) {
            Log.e("SyncWorker", "Sync failed", e)
            Result.retry()
        }
    }
}

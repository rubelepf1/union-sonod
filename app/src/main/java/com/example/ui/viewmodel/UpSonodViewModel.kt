package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.UserSessionManager
import com.example.data.model.CertificateRegistry
import com.example.data.model.CertificateType
import com.example.data.model.GeneratedCertificate
import com.example.data.model.Heir
import com.example.data.model.SyncStatus
import com.example.data.model.UnionProfile
import com.example.data.model.UserProfile
import com.example.data.remote.AuthResult
import com.example.data.remote.SupabaseClient
import com.example.data.repository.CertificateRepository
import com.example.data.repository.UnionRepository
import com.example.sync.SyncManager
import com.example.util.BanglaHelper
import com.example.util.PdfGenerator
import com.example.util.TemplateEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

enum class ScreenState {
    LOGIN,
    SPLASH_SETUP,
    HOME,
    FORM,
    PREVIEW,
    HISTORY,
    SETTINGS
}

data class FormState(
    val applicantName: String = "",
    val fatherOrHusbandName: String = "",
    val motherName: String = "",
    val village: String = "",
    val wardNo: String = "১",
    val postOffice: String = "",
    val upazila: String = "",
    val district: String = "",
    val nidOrBirthNo: String = "",
    val applicantPhotoUri: String? = null,
    val customFields: Map<String, String> = emptyMap(),
    val heirs: List<Heir> = listOf(Heir(name = "", relation = "স্ত্রী", age = "")),
    val currentStep: Int = 0,
    val errors: Map<String, String> = emptyMap()
)

class UpSonodViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val unionRepo = UnionRepository(db.unionProfileDao())
    private val certRepo = CertificateRepository(db.certificateDao(), db.cachedCertificateTypeDao())
    val sessionManager = UserSessionManager(application)
    val supabase = SupabaseClient(sessionTokenProvider = { sessionManager.getAccessToken() })

    val currentUser: StateFlow<UserProfile?> = sessionManager.currentUser
    val isOfflineGuestMode: StateFlow<Boolean> = sessionManager.isOfflineGuestMode

    val unionProfile: StateFlow<UnionProfile?> = unionRepo.unionProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val certificatesList: StateFlow<List<GeneratedCertificate>> = certRepo.allCertificates
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalCertificatesCount: StateFlow<Int> = certRepo.totalCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val pendingSyncCount: StateFlow<Int> = certRepo.pendingSyncCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val dynamicCertificateTypes: StateFlow<List<CertificateType>> = certRepo.certificateTypes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CertificateRegistry.ALL_TYPES)

    private val _currentScreen = MutableStateFlow(
        if (!sessionManager.isLoggedIn() && !sessionManager.isOfflineGuestMode.value) {
            ScreenState.LOGIN
        } else {
            ScreenState.HOME
        }
    )
    val currentScreen: StateFlow<ScreenState> = _currentScreen.asStateFlow()

    private val _screenBackStack = MutableStateFlow<List<ScreenState>>(listOf(_currentScreen.value))

    private val _selectedType = MutableStateFlow<CertificateType?>(null)
    val selectedType: StateFlow<CertificateType?> = _selectedType.asStateFlow()

    private val _formState = MutableStateFlow(FormState())
    val formState: StateFlow<FormState> = _formState.asStateFlow()

    private val _previewCertificate = MutableStateFlow<GeneratedCertificate?>(null)
    val previewCertificate: StateFlow<GeneratedCertificate?> = _previewCertificate.asStateFlow()

    private val _generatedPdfFile = MutableStateFlow<File?>(null)
    val generatedPdfFile: StateFlow<File?> = _generatedPdfFile.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("সকল")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    // Auth State
    private val _isLoginLoading = MutableStateFlow(false)
    val isLoginLoading: StateFlow<Boolean> = _isLoginLoading.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    init {
        // Schedule periodic sync via WorkManager
        SyncManager.schedulePeriodicSync(application)

        viewModelScope.launch {
            if (!sessionManager.isLoggedIn() && !sessionManager.isOfflineGuestMode.value) {
                _currentScreen.value = ScreenState.LOGIN
                _screenBackStack.value = listOf(ScreenState.LOGIN)
            } else {
                checkUnionSetup()
            }
        }
    }

    private suspend fun checkUnionSetup() {
        val profile = unionRepo.getProfileSync()
        if (profile == null || !profile.isConfigured || profile.unionName.isBlank()) {
            _currentScreen.value = ScreenState.SPLASH_SETUP
            _screenBackStack.value = listOf(ScreenState.SPLASH_SETUP)
        } else {
            _currentScreen.value = ScreenState.HOME
            _screenBackStack.value = listOf(ScreenState.HOME)
        }
    }

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _isLoginLoading.value = true
            _loginError.value = null

            when (val result = supabase.login(email, pass)) {
                is AuthResult.Success -> {
                    sessionManager.saveSession(result.user)
                    _statusMessage.value = "স্বাগতম, ${result.user.fullName} (${result.user.roleTitleBn})"
                    _isLoginLoading.value = false

                    // If user belongs to a union, sync union info
                    result.user.unionId?.let { uId ->
                        launch {
                            val remoteUnion = supabase.fetchUnionProfile(uId)
                            if (remoteUnion != null) {
                                unionRepo.saveProfile(remoteUnion)
                            }
                        }
                    }

                    // Trigger immediate sync
                    triggerManualSync()
                    checkUnionSetup()
                }
                is AuthResult.InactiveAccount -> {
                    _isLoginLoading.value = false
                    _loginError.value = result.message
                }
                is AuthResult.Error -> {
                    _isLoginLoading.value = false
                    _loginError.value = result.message
                }
            }
        }
    }

    fun continueAsOfflineGuest() {
        sessionManager.setOfflineGuestMode(true)
        viewModelScope.launch {
            checkUnionSetup()
        }
    }

    fun logout() {
        sessionManager.clearSession()
        _currentScreen.value = ScreenState.LOGIN
        _screenBackStack.value = listOf(ScreenState.LOGIN)
        _statusMessage.value = "সফলভাবে লগআউট করা হয়েছে"
    }

    fun triggerManualSync() {
        viewModelScope.launch {
            _isSyncing.value = true
            SyncManager.triggerImmediateSync(getApplication())
            // Pull remote certificate types
            val types = supabase.fetchCertificateTypes()
            if (types.isNotEmpty()) {
                certRepo.saveCachedTypes(types)
            }
            kotlinx.coroutines.delay(1200)
            _isSyncing.value = false
            _statusMessage.value = "ক্লাউড সিঙ্ক সফলভাবে সম্পন্ন হয়েছে"
        }
    }

    fun navigateTo(screen: ScreenState) {
        val updated = _screenBackStack.value.toMutableList()
        updated.add(screen)
        _screenBackStack.value = updated
        _currentScreen.value = screen
    }

    fun navigateBack(): Boolean {
        val list = _screenBackStack.value.toMutableList()
        if (list.size > 1) {
            list.removeAt(list.size - 1)
            val prev = list.last()
            _screenBackStack.value = list
            _currentScreen.value = prev
            return true
        } else if (_currentScreen.value != ScreenState.HOME && _currentScreen.value != ScreenState.LOGIN) {
            _currentScreen.value = ScreenState.HOME
            _screenBackStack.value = listOf(ScreenState.HOME)
            return true
        }
        return false
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(cat: String) {
        _selectedCategory.value = cat
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun saveUnionProfile(
        unionName: String,
        upazila: String,
        district: String,
        chairmanName: String,
        unionEmail: String,
        unionPhone: String,
        logoUri: String?
    ) {
        viewModelScope.launch {
            val profile = UnionProfile(
                id = 1,
                unionName = unionName.trim(),
                upazila = upazila.trim(),
                district = district.trim(),
                chairmanName = chairmanName.trim(),
                unionEmail = unionEmail.trim(),
                unionPhone = unionPhone.trim(),
                logoUri = logoUri,
                isConfigured = true
            )
            unionRepo.saveProfile(profile)
            _statusMessage.value = "ইউনিয়ন পরিষদ তথ্য সফলভাবে সংরক্ষিত হয়েছে"
            _currentScreen.value = ScreenState.HOME
            _screenBackStack.value = listOf(ScreenState.HOME)
        }
    }

    fun startCertificateForm(type: CertificateType) {
        _selectedType.value = type
        val profile = unionProfile.value
        _formState.value = FormState(
            upazila = profile?.upazila ?: "",
            district = profile?.district ?: "",
            currentStep = 0,
            heirs = if (type.isSuccession) listOf(
                Heir(name = "", relation = "স্ত্রী", age = ""),
                Heir(name = "", relation = "পুত্র", age = "")
            ) else emptyList()
        )
        navigateTo(ScreenState.FORM)
    }

    fun duplicateCertificate(cert: GeneratedCertificate) {
        val type = dynamicCertificateTypes.value.find { it.id == cert.certificateTypeId }
            ?: CertificateRegistry.findById(cert.certificateTypeId) ?: return
        _selectedType.value = type
        val customMap = BanglaHelper.parseCustomFieldsJson(cert.customFieldsJson)
        val heirsList = cert.heirsJson?.let { BanglaHelper.parseHeirsJson(it) } ?: emptyList()

        _formState.value = FormState(
            applicantName = cert.applicantName,
            fatherOrHusbandName = cert.fatherOrHusbandName,
            motherName = cert.motherName,
            village = cert.village,
            wardNo = cert.wardNo,
            postOffice = cert.postOffice,
            upazila = cert.upazila,
            district = cert.district,
            nidOrBirthNo = cert.nidOrBirthNo,
            applicantPhotoUri = cert.applicantPhotoUri,
            customFields = customMap,
            heirs = heirsList,
            currentStep = 0
        )
        navigateTo(ScreenState.FORM)
    }

    fun openExistingPreview(cert: GeneratedCertificate) {
        _previewCertificate.value = cert
        viewModelScope.launch {
            val file = PdfGenerator.generateCertificatePdf(getApplication(), cert, logoUri = unionProfile.value?.logoUri)
            _generatedPdfFile.value = file
        }
        navigateTo(ScreenState.PREVIEW)
    }

    fun updateUnionLogo(newLogoUri: String?) {
        viewModelScope.launch {
            val current = unionProfile.value ?: UnionProfile(id = 1)
            val updated = current.copy(logoUri = newLogoUri)
            unionRepo.saveProfile(updated)
            _statusMessage.value = "ইউনিয়ন পরিষদ লোগো সফলভাবে পরিবর্তন করা হয়েছে"
            _previewCertificate.value?.let { cert ->
                val file = PdfGenerator.generateCertificatePdf(getApplication(), cert, logoUri = newLogoUri)
                _generatedPdfFile.value = file
            }
        }
    }

    fun deleteCertificate(cert: GeneratedCertificate) {
        viewModelScope.launch {
            certRepo.delete(cert.id)
            SyncManager.triggerImmediateSync(getApplication())
            _statusMessage.value = "সনদটি মুছে ফেলা হয়েছে"
        }
    }

    fun updateApplicantName(v: String) = updateFormField { it.copy(applicantName = v) }
    fun updateFatherOrHusbandName(v: String) = updateFormField { it.copy(fatherOrHusbandName = v) }
    fun updateMotherName(v: String) = updateFormField { it.copy(motherName = v) }
    fun updateVillage(v: String) = updateFormField { it.copy(village = v) }
    fun updateWardNo(v: String) = updateFormField { it.copy(wardNo = v) }
    fun updatePostOffice(v: String) = updateFormField { it.copy(postOffice = v) }
    fun updateUpazila(v: String) = updateFormField { it.copy(upazila = v) }
    fun updateDistrict(v: String) = updateFormField { it.copy(district = v) }
    fun updateNidOrBirthNo(v: String) = updateFormField { it.copy(nidOrBirthNo = v) }
    fun updateApplicantPhoto(uri: Uri?) = updateFormField { it.copy(applicantPhotoUri = uri?.toString()) }

    fun updateCustomField(key: String, value: String) {
        val current = _formState.value.customFields.toMutableMap()
        current[key] = value
        updateFormField { it.copy(customFields = current) }
    }

    fun addHeir() {
        val current = _formState.value.heirs.toMutableList()
        current.add(Heir(name = "", relation = "পুত্র", age = ""))
        updateFormField { it.copy(heirs = current) }
    }

    fun updateHeir(index: Int, heir: Heir) {
        val current = _formState.value.heirs.toMutableList()
        if (index in current.indices) {
            current[index] = heir
            updateFormField { it.copy(heirs = current) }
        }
    }

    fun removeHeir(index: Int) {
        val current = _formState.value.heirs.toMutableList()
        if (index in current.indices && current.size > 1) {
            current.removeAt(index)
            updateFormField { it.copy(heirs = current) }
        }
    }

    fun goToNextStep(): Boolean {
        val state = _formState.value
        val type = _selectedType.value ?: return false
        val errors = mutableMapOf<String, String>()

        when (state.currentStep) {
            0 -> {
                if (state.applicantName.isBlank()) errors["applicantName"] = "আবেদনকারী / ব্যক্তির নাম আবশ্যক"
                if (state.fatherOrHusbandName.isBlank()) errors["fatherOrHusbandName"] = "পিতা বা স্বামীর নাম আবশ্যক"
                if (state.motherName.isBlank()) errors["motherName"] = "মাতার নাম আবশ্যক"
            }
            1 -> {
                if (state.village.isBlank()) errors["village"] = "গ্রাম বা মহল্লার নাম আবশ্যক"
                if (state.wardNo.isBlank()) errors["wardNo"] = "ওয়ার্ড নং আবশ্যক"
                if (state.postOffice.isBlank()) errors["postOffice"] = "ডাকঘরের নাম আবশ্যক"
                if (state.upazila.isBlank()) errors["upazila"] = "উপজেলার নাম আবশ্যক"
                if (state.district.isBlank()) errors["district"] = "জেলার নাম আবশ্যক"
            }
            2 -> {
                for (field in type.specificFields) {
                    if (field.required) {
                        val v = state.customFields[field.key] ?: ""
                        if (v.isBlank()) {
                            errors[field.key] = "${field.label} আবশ্যক"
                        }
                    }
                }
                if (type.isSuccession) {
                    if (state.heirs.isEmpty() || state.heirs.all { it.name.isBlank() }) {
                        errors["heirs"] = "কমপক্ষে একজন ওয়ারিশের তথ্য প্রদান করুন"
                    }
                }
            }
        }

        if (errors.isNotEmpty()) {
            _formState.value = state.copy(errors = errors)
            return false
        }

        if (state.currentStep < 3) {
            _formState.value = state.copy(currentStep = state.currentStep + 1, errors = emptyMap())
            return true
        } else {
            submitAndGenerateCertificate()
            return true
        }
    }

    fun goToPreviousStep() {
        val state = _formState.value
        if (state.currentStep > 0) {
            _formState.value = state.copy(currentStep = state.currentStep - 1, errors = emptyMap())
        }
    }

    private fun submitAndGenerateCertificate() {
        viewModelScope.launch {
            val state = _formState.value
            val type = _selectedType.value ?: return@launch
            val profile = unionProfile.value ?: UnionProfile(unionName = "ইউনিয়ন পরিষদ কার্যালয়", chairmanName = "চেয়ারম্যান")

            val customJson = BanglaHelper.formatCustomFieldsJson(state.customFields)
            val heirsJson = if (type.isSuccession) BanglaHelper.formatHeirsJson(state.heirs) else null

            val bodyText = TemplateEngine.generateBodyText(
                type = type,
                applicantName = state.applicantName,
                fatherOrHusbandName = state.fatherOrHusbandName,
                motherName = state.motherName,
                village = state.village,
                wardNo = state.wardNo,
                postOffice = state.postOffice,
                upazila = state.upazila,
                district = state.district,
                customValues = state.customFields
            )

            // Fetch serial number from Supabase RPC generate_certificate_serial_no
            val user = sessionManager.currentUser.value
            val targetUnionId = user?.unionId ?: "00000000-0000-0000-0000-000000000001"
            val serverSerial = supabase.generateServerSerialNo(targetUnionId)
            val serialNo = serverSerial ?: BanglaHelper.generateReferenceNumber((totalCertificatesCount.value + 1).toLong())

            val issueDate = BanglaHelper.getCurrentDateBangla()

            val photoToSave = if (type.requiresPhoto) state.applicantPhotoUri else null

            val entity = GeneratedCertificate(
                certificateTypeId = type.id,
                certificateTitle = type.title,
                applicantName = state.applicantName.trim(),
                fatherOrHusbandName = state.fatherOrHusbandName.trim(),
                motherName = state.motherName.trim(),
                village = state.village.trim(),
                wardNo = state.wardNo.trim(),
                postOffice = state.postOffice.trim(),
                upazila = state.upazila.trim(),
                district = state.district.trim(),
                nidOrBirthNo = state.nidOrBirthNo.trim(),
                serialNo = serialNo,
                issueDateBangla = issueDate,
                applicantPhotoUri = photoToSave,
                customFieldsJson = customJson,
                heirsJson = heirsJson,
                generatedBodyText = bodyText,
                unionName = profile.unionName,
                chairmanName = profile.chairmanName,
                syncStatus = SyncStatus.PENDING_INSERT,
                unionRemoteId = user?.unionId,
                createdByRemoteId = user?.id
            )

            val newId = certRepo.insert(entity)
            val savedCert = entity.copy(id = newId)

            // Trigger sync in background
            SyncManager.triggerImmediateSync(getApplication())

            val pdf = PdfGenerator.generateCertificatePdf(getApplication(), savedCert, logoUri = profile.logoUri)
            _generatedPdfFile.value = pdf
            _previewCertificate.value = savedCert

            navigateTo(ScreenState.PREVIEW)
        }
    }

    private fun updateFormField(transform: (FormState) -> FormState) {
        _formState.value = transform(_formState.value)
    }
}

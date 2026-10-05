package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.FormScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.PreviewScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SetupScreen
import com.example.ui.theme.UpSonodTheme
import com.example.ui.viewmodel.ScreenState
import com.example.ui.viewmodel.UpSonodViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UpSonodTheme {
                val viewModel: UpSonodViewModel = viewModel()
                UpSonodApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun UpSonodApp(viewModel: UpSonodViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val isOfflineGuest by viewModel.isOfflineGuestMode.collectAsStateWithLifecycle()
    val unionProfile by viewModel.unionProfile.collectAsStateWithLifecycle()
    val certificatesList by viewModel.certificatesList.collectAsStateWithLifecycle()
    val totalCount by viewModel.totalCertificatesCount.collectAsStateWithLifecycle()
    val pendingSyncCount by viewModel.pendingSyncCount.collectAsStateWithLifecycle()
    val dynamicTypes by viewModel.dynamicCertificateTypes.collectAsStateWithLifecycle()
    val selectedType by viewModel.selectedType.collectAsStateWithLifecycle()
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val previewCert by viewModel.previewCertificate.collectAsStateWithLifecycle()
    val generatedPdf by viewModel.generatedPdfFile.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()

    val isLoginLoading by viewModel.isLoginLoading.collectAsStateWithLifecycle()
    val loginError by viewModel.loginError.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(statusMessage) {
        statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    // Custom back navigation handling
    BackHandler(enabled = currentScreen != ScreenState.HOME && currentScreen != ScreenState.LOGIN) {
        viewModel.navigateBack()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        modifier = Modifier.fillMaxSize()
    ) { _ ->
        when (currentScreen) {
            ScreenState.LOGIN -> {
                LoginScreen(
                    isSupabaseConfigured = viewModel.supabase.isConfigured,
                    isLoading = isLoginLoading,
                    errorMessage = loginError,
                    onLoginSubmit = { email, pass ->
                        viewModel.login(email, pass)
                    },
                    onContinueOffline = {
                        viewModel.continueAsOfflineGuest()
                    }
                )
            }
            ScreenState.SPLASH_SETUP -> {
                SetupScreen(
                    onSetupComplete = { name, upazila, district, chairman, email, phone, logo ->
                        viewModel.saveUnionProfile(name, upazila, district, chairman, email, phone, logo)
                    }
                )
            }
            ScreenState.HOME -> {
                HomeScreen(
                    unionProfile = unionProfile,
                    currentUser = currentUser,
                    certificateTypes = dynamicTypes,
                    pendingSyncCount = pendingSyncCount,
                    isSyncing = isSyncing,
                    totalCertificatesCount = totalCount,
                    searchQuery = searchQuery,
                    selectedCategory = selectedCategory,
                    onSearchChange = { viewModel.setSearchQuery(it) },
                    onCategoryChange = { viewModel.setSelectedCategory(it) },
                    onSelectCertificate = { type ->
                        viewModel.startCertificateForm(type)
                    },
                    onTriggerSync = {
                        viewModel.triggerManualSync()
                    },
                    onNavigate = { screen ->
                        viewModel.navigateTo(screen)
                    }
                )
            }
            ScreenState.FORM -> {
                FormScreen(
                    certificateType = selectedType,
                    formState = formState,
                    onApplicantNameChange = { viewModel.updateApplicantName(it) },
                    onFatherOrHusbandNameChange = { viewModel.updateFatherOrHusbandName(it) },
                    onMotherNameChange = { viewModel.updateMotherName(it) },
                    onVillageChange = { viewModel.updateVillage(it) },
                    onWardNoChange = { viewModel.updateWardNo(it) },
                    onPostOfficeChange = { viewModel.updatePostOffice(it) },
                    onUpazilaChange = { viewModel.updateUpazila(it) },
                    onDistrictChange = { viewModel.updateDistrict(it) },
                    onNidOrBirthNoChange = { viewModel.updateNidOrBirthNo(it) },
                    onPhotoSelected = { viewModel.updateApplicantPhoto(it) },
                    onCustomFieldChange = { k, v -> viewModel.updateCustomField(k, v) },
                    onAddHeir = { viewModel.addHeir() },
                    onUpdateHeir = { idx, h -> viewModel.updateHeir(idx, h) },
                    onRemoveHeir = { idx -> viewModel.removeHeir(idx) },
                    onNextStep = { viewModel.goToNextStep() },
                    onPreviousStep = { viewModel.goToPreviousStep() },
                    onBackToHome = { viewModel.navigateTo(ScreenState.HOME) }
                )
            }
            ScreenState.PREVIEW -> {
                PreviewScreen(
                    certificate = previewCert,
                    pdfFile = generatedPdf,
                    unionProfile = unionProfile,
                    onUpdateLogo = { newLogo ->
                        viewModel.updateUnionLogo(newLogo)
                    },
                    onEditClick = {
                        viewModel.navigateTo(ScreenState.FORM)
                    },
                    onBackClick = {
                        viewModel.navigateTo(ScreenState.HOME)
                    }
                )
            }
            ScreenState.HISTORY -> {
                HistoryScreen(
                    certificates = certificatesList,
                    pendingSyncCount = pendingSyncCount,
                    isSyncing = isSyncing,
                    onTriggerSync = {
                        viewModel.triggerManualSync()
                    },
                    onOpenPreview = { cert ->
                        viewModel.openExistingPreview(cert)
                    },
                    onDuplicate = { cert ->
                        viewModel.duplicateCertificate(cert)
                    },
                    onDelete = { cert ->
                        viewModel.deleteCertificate(cert)
                    },
                    onNavigate = { screen ->
                        viewModel.navigateTo(screen)
                    }
                )
            }
            ScreenState.SETTINGS -> {
                SettingsScreen(
                    unionProfile = unionProfile,
                    currentUser = currentUser,
                    isOfflineGuestMode = isOfflineGuest,
                    pendingSyncCount = pendingSyncCount,
                    isSyncing = isSyncing,
                    onTriggerSync = {
                        viewModel.triggerManualSync()
                    },
                    onLogout = {
                        viewModel.logout()
                    },
                    onGoToLogin = {
                        viewModel.navigateTo(ScreenState.LOGIN)
                    },
                    onSaveProfile = { name, upazila, district, chairman, email, phone, logo ->
                        viewModel.saveUnionProfile(name, upazila, district, chairman, email, phone, logo)
                    },
                    onNavigate = { screen ->
                        viewModel.navigateTo(screen)
                    }
                )
            }
        }
    }
}

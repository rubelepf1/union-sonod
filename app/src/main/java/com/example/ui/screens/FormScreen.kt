package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.CertificateField
import com.example.data.model.CertificateType
import com.example.data.model.FieldType
import com.example.data.model.Heir
import com.example.ui.components.BanglaInputField
import com.example.ui.components.UpTopAppBar
import com.example.ui.theme.BdGreenContainer
import com.example.ui.theme.BdGreenDark
import com.example.ui.theme.BdGreenPrimary
import com.example.ui.theme.BdRedAccent
import com.example.ui.viewmodel.FormState
import com.example.util.BanglaHelper

data class WizardStep(
    val title: String,
    val description: String,
    val icon: ImageVector
)

@Composable
fun FormScreen(
    certificateType: CertificateType?,
    formState: FormState,
    onApplicantNameChange: (String) -> Unit,
    onFatherOrHusbandNameChange: (String) -> Unit,
    onMotherNameChange: (String) -> Unit,
    onVillageChange: (String) -> Unit,
    onWardNoChange: (String) -> Unit,
    onPostOfficeChange: (String) -> Unit,
    onUpazilaChange: (String) -> Unit,
    onDistrictChange: (String) -> Unit,
    onNidOrBirthNoChange: (String) -> Unit,
    onPhotoSelected: (Uri?) -> Unit,
    onCustomFieldChange: (key: String, value: String) -> Unit,
    onAddHeir: () -> Unit,
    onUpdateHeir: (index: Int, heir: Heir) -> Unit,
    onRemoveHeir: (index: Int) -> Unit,
    onNextStep: () -> Unit,
    onPreviousStep: () -> Unit,
    onSaveDraft: () -> Unit = {},
    onBackToHome: () -> Unit
) {
    BackHandler {
        if (formState.currentStep > 0) {
            onPreviousStep()
        } else {
            onBackToHome()
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        onPhotoSelected(uri)
    }

    // 3-Step Wizard Definitions
    val steps = listOf(
        WizardStep("নাগরিক তথ্য", "মৌলিক পরিচিতি ও এনআইডি", Icons.Default.Person),
        WizardStep("ঠিকানা ও বিবরণ", "ইউপি এলাকা ও সনদের তথ্য", Icons.Default.HomeWork),
        WizardStep("যাচাই ও চূড়ান্ত", if (certificateType?.isSuccession == true) "ওয়ারিশান ও অনুমোদন" else "চূড়ান্ত পর্যালোচনা", Icons.Default.FactCheck)
    )

    Scaffold(
        topBar = {
            UpTopAppBar(
                title = certificateType?.title ?: "সনদ ফরম",
                subtitle = "ধাপ ${BanglaHelper.toBanglaDigits((formState.currentStep + 1).toString())}: ${steps.getOrNull(formState.currentStep)?.title ?: ""}",
                showBackButton = true,
                onBackClick = {
                    if (formState.currentStep > 0) onPreviousStep() else onBackToHome()
                }
            )
        },
        bottomBar = {
            EnterpriseFormBottomBar(
                currentStep = formState.currentStep,
                totalSteps = steps.size,
                onPrevious = onPreviousStep,
                onSaveDraft = onSaveDraft,
                onNext = onNextStep
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Animated 3-Step Wizard Indicator
            AnimatedWizardStepper(
                currentStep = formState.currentStep,
                steps = steps,
                modifier = Modifier.fillMaxWidth()
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                when (formState.currentStep) {
                    0 -> StepCitizenBasicInfo(
                        formState = formState,
                        requiresPhoto = certificateType?.requiresPhoto == true,
                        onApplicantNameChange = onApplicantNameChange,
                        onFatherOrHusbandNameChange = onFatherOrHusbandNameChange,
                        onMotherNameChange = onMotherNameChange,
                        onNidOrBirthNoChange = onNidOrBirthNoChange,
                        onPickPhoto = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        onRemovePhoto = { onPhotoSelected(null) }
                    )
                    1 -> StepAddressAndDetails(
                        certificateType = certificateType,
                        formState = formState,
                        onVillageChange = onVillageChange,
                        onWardNoChange = onWardNoChange,
                        onPostOfficeChange = onPostOfficeChange,
                        onUpazilaChange = onUpazilaChange,
                        onDistrictChange = onDistrictChange,
                        onCustomFieldChange = onCustomFieldChange
                    )
                    2 -> StepSuccessionAndReview(
                        certificateType = certificateType,
                        formState = formState,
                        onAddHeir = onAddHeir,
                        onUpdateHeir = onUpdateHeir,
                        onRemoveHeir = onRemoveHeir
                    )
                }
                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }
}

/**
 * Animated Horizontal Stepper with step icons, animated connecting lines, and status
 */
@Composable
fun AnimatedWizardStepper(
    currentStep: Int,
    steps: List<WizardStep>,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                steps.forEachIndexed { index, step ->
                    val isCompleted = index < currentStep
                    val isCurrent = index == currentStep

                    val circleColor by animateColorAsState(
                        targetValue = when {
                            isCurrent -> BdGreenPrimary
                            isCompleted -> Color(0xFF006A4E)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        label = "circle_color"
                    )

                    val contentColor by animateColorAsState(
                        targetValue = when {
                            isCurrent || isCompleted -> Color.White
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        label = "content_color"
                    )

                    // Step Indicator Circle & Icon
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(circleColor)
                                .border(
                                    width = if (isCurrent) 2.5.dp else 0.dp,
                                    color = if (isCurrent) Color(0xFFF59E0B) else Color.Transparent,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isCompleted) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "সম্পন্ন",
                                    tint = contentColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = step.icon,
                                    contentDescription = step.title,
                                    tint = contentColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = step.title,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.5.sp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                lineHeight = 14.sp
                            ),
                            color = if (isCurrent) BdGreenDark else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Connecting Line (Between steps)
                    if (index < steps.size - 1) {
                        val progressFraction by animateFloatAsState(
                            targetValue = if (index < currentStep) 1f else 0f,
                            label = "step_line_progress"
                        )
                        Box(
                            modifier = Modifier
                                .weight(0.7f)
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fraction = progressFraction)
                                    .height(3.dp)
                                    .background(BdGreenPrimary)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Step 1: Citizen Basic Information with Real-time Bangla input, NID validation badge and conditional photo upload
 */
@Composable
fun StepCitizenBasicInfo(
    formState: FormState,
    requiresPhoto: Boolean,
    onApplicantNameChange: (String) -> Unit,
    onFatherOrHusbandNameChange: (String) -> Unit,
    onMotherNameChange: (String) -> Unit,
    onNidOrBirthNoChange: (String) -> Unit,
    onPickPhoto: () -> Unit,
    onRemovePhoto: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(BdGreenContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = BdGreenPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "নাগরিকের মৌলিক তথ্য",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BdGreenDark
                    )
                    Text(
                        text = "জাতীয় পরিচয়পত্র অথবা জন্ম নিবন্ধন অনুযায়ী সঠিক তথ্য দিন",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Auto-clear photo if switching to a certificate that does not permit photos
            LaunchedEffect(requiresPhoto) {
                if (!requiresPhoto && formState.applicantPhotoUri != null) {
                    onRemovePhoto()
                }
            }

            // Conditional Photo Upload Card
            if (requiresPhoto) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(BdGreenContainer.copy(alpha = 0.45f))
                        .border(1.dp, BdGreenPrimary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp, 88.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .clickable { onPickPhoto() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (formState.applicantPhotoUri != null) {
                            AsyncImage(
                                model = formState.applicantPhotoUri,
                                contentDescription = "পাসপোর্ট ছবি",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.AddAPhoto,
                                    contentDescription = "ছবি যুক্ত করুন",
                                    tint = BdGreenPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "ছবি দিন",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "পাসপোর্ট সাইজ ছবি (প্রয়োজনীয়)",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = BdGreenDark)
                        )
                        Text(
                            text = "সনদের ওপরের ডান কোণে সরকারি ফরম্যাটে প্রিন্ট হবে",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedButton(
                                onClick = onPickPhoto,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .heightIn(min = 40.dp)
                                    .testTag("button_pick_photo")
                            ) {
                                Text(
                                    text = if (formState.applicantPhotoUri != null) "ছবি পরিবর্তন" else "ছবি বাছুন",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                            if (formState.applicantPhotoUri != null) {
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = onRemovePhoto,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "মুছুন", tint = BdRedAccent)
                                }
                            }
                        }
                    }
                }
            } else {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ℹ️ এই সনদের জন্য কোনো ছবির প্রয়োজন নেই (সনদে ছবি প্রিন্ট হবে না)।",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp, lineHeight = 16.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Applicant Name
            BanglaInputField(
                value = formState.applicantName,
                onValueChange = onApplicantNameChange,
                label = "আবেদনকারী / সনদগ্রহীতার নাম",
                hint = "যেমন: মোঃ রফিকুল ইসলাম",
                required = true,
                errorMessage = formState.errors["applicantName"],
                modifier = Modifier.testTag("input_form_applicant_name")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Father or Husband Name
            BanglaInputField(
                value = formState.fatherOrHusbandName,
                onValueChange = onFatherOrHusbandNameChange,
                label = "পিতা বা স্বামীর নাম",
                hint = "যেমন: মোঃ আব্দুল জলিল",
                required = true,
                errorMessage = formState.errors["fatherOrHusbandName"],
                modifier = Modifier.testTag("input_form_father_husband")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Mother Name
            BanglaInputField(
                value = formState.motherName,
                onValueChange = onMotherNameChange,
                label = "মাতার নাম",
                hint = "যেমন: মোসাঃ ফাতেমা বেগম",
                required = true,
                errorMessage = formState.errors["motherName"],
                modifier = Modifier.testTag("input_form_mother_name")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Specialized Real-time Bangla NID / Birth Certificate Input Field with Live Validation Badge
            NidOrBirthNoInputField(
                value = formState.nidOrBirthNo,
                onValueChange = onNidOrBirthNoChange,
                modifier = Modifier.testTag("input_form_nid")
            )
        }
    }
}

/**
 * Enterprise NID / Birth Registration Input with English-to-Bangla auto-conversion & live digit validation badge
 */
@Composable
fun NidOrBirthNoInputField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val cleanDigits = BanglaHelper.toEnglishDigits(value).filter { it in '0'..'9' }
    val digitCount = cleanDigits.length

    val isValidLength = digitCount == 10 || digitCount == 13 || digitCount == 17
    val isWarning = digitCount > 0 && !isValidLength && digitCount <= 17
    val isOverLimit = digitCount > 17

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "জাতীয় পরিচয়পত্র / জন্ম নিবন্ধন নম্বর",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )

            // Live Digit Counter Badge
            Surface(
                color = when {
                    isValidLength -> Color(0xFFE8F5E9)
                    isWarning -> Color(0xFFFEF3C7)
                    isOverLimit -> Color(0xFFFEE2E2)
                    else -> MaterialTheme.colorScheme.surfaceVariant
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "${BanglaHelper.toBanglaDigits(digitCount.toString())} / ১৭ ডিজিট",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = when {
                        isValidLength -> Color(0xFF1B5E20)
                        isWarning -> Color(0xFFB45309)
                        isOverLimit -> Color(0xFFDC2626)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = value,
            onValueChange = { input ->
                // Auto convert all English numbers to official Bangla numbers on the fly
                val banglaConverted = BanglaHelper.toBanglaDigits(input)
                onValueChange(banglaConverted)
            },
            placeholder = {
                Text(
                    text = "১০, ১৩ বা ১৭ ডিজিটের এনআইডি বা জন্ম নিবন্ধন নং",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            singleLine = true,
            trailingIcon = {
                when {
                    isValidLength -> Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "বৈধ", tint = BdGreenPrimary)
                    isOverLimit -> Icon(imageVector = Icons.Default.Warning, contentDescription = "অতিরিক্ত", tint = BdRedAccent)
                    isWarning -> Text(
                        text = "অসম্পূর্ণ",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, color = Color(0xFFB45309)),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            },
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (isValidLength) BdGreenPrimary else MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = if (isValidLength) BdGreenPrimary.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outline
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Dynamic Format Clarification Banner
        Row(verticalAlignment = Alignment.CenterVertically) {
            when {
                digitCount == 10 -> Text(
                    text = "✓ স্মার্ট জাতীয় পরিচয়পত্র (১০ ডিজিট)",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold),
                    color = Color(0xFF1B5E20)
                )
                digitCount == 13 -> Text(
                    text = "✓ পুরাতন জাতীয় পরিচয়পত্র (১৩ ডিজিট)",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold),
                    color = Color(0xFF1B5E20)
                )
                digitCount == 17 -> Text(
                    text = "✓ জন্ম নিবন্ধন / ১৭ ডিজিট এনআইডি নম্বর",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold),
                    color = Color(0xFF1B5E20)
                )
                isOverLimit -> Text(
                    text = "⚠️ জাতীয় পরিচয়পত্র বা জন্ম নিবন্ধন সর্বোচ্চ ১৭ ডিজিটের বেশি হতে পারে না",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                    color = BdRedAccent
                )
                else -> Text(
                    text = "ইংরেজি বা বাংলায় লিখুন (১০ ডিজিট স্মার্ট কার্ড, ১৩ ডিজিট এনআইডি বা ১৭ ডিজিট জন্ম নিবন্ধন)",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Step 2: Permanent / Present Address & Certificate Specific Details
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepAddressAndDetails(
    certificateType: CertificateType?,
    formState: FormState,
    onVillageChange: (String) -> Unit,
    onWardNoChange: (String) -> Unit,
    onPostOfficeChange: (String) -> Unit,
    onUpazilaChange: (String) -> Unit,
    onDistrictChange: (String) -> Unit,
    onCustomFieldChange: (String, String) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(BdGreenContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.HomeWork,
                        contentDescription = null,
                        tint = BdGreenPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "স্থায়ী ও বর্তমান ঠিকানার বিবরণ",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BdGreenDark
                    )
                    Text(
                        text = "সংশ্লিষ্ট ইউনিয়নের গ্রাম ও ওয়ার্ড নম্বর নির্ধারণ করুন",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Village
            BanglaInputField(
                value = formState.village,
                onValueChange = onVillageChange,
                label = "গ্রাম / পাড়া / মহল্লা",
                hint = "যেমন: উত্তর কাঞ্চনপুর",
                required = true,
                errorMessage = formState.errors["village"],
                modifier = Modifier.testTag("input_form_village")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Ward Selector with 1-9 Chips for 1-click selection
            Text(
                text = "ওয়ার্ড নং *",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
            )
            Spacer(modifier = Modifier.height(6.dp))

            val wardOptions = listOf("১", "২", "৩", "৪", "৫", "৬", "৭", "৮", "৯")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                wardOptions.take(5).forEach { ward ->
                    val isSelected = formState.wardNo == ward
                    FilterChip(
                        selected = isSelected,
                        onClick = { onWardNoChange(ward) },
                        label = { Text(text = "$ward নং", fontSize = 11.5.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BdGreenPrimary,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                wardOptions.drop(5).forEach { ward ->
                    val isSelected = formState.wardNo == ward
                    FilterChip(
                        selected = isSelected,
                        onClick = { onWardNoChange(ward) },
                        label = { Text(text = "$ward নং", fontSize = 11.5.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BdGreenPrimary,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Post Office
            BanglaInputField(
                value = formState.postOffice,
                onValueChange = onPostOfficeChange,
                label = "ডাকঘর",
                hint = "যেমন: রামগঞ্জ",
                required = true,
                errorMessage = formState.errors["postOffice"],
                modifier = Modifier.testTag("input_form_post_office")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Upazila & District (Side by side)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                BanglaInputField(
                    value = formState.upazila,
                    onValueChange = onUpazilaChange,
                    label = "উপজেলা",
                    hint = "উপজেলার নাম",
                    required = true,
                    errorMessage = formState.errors["upazila"],
                    modifier = Modifier.weight(1f).testTag("input_form_upazila")
                )

                BanglaInputField(
                    value = formState.district,
                    onValueChange = onDistrictChange,
                    label = "জেলা",
                    hint = "জেলার নাম",
                    required = true,
                    errorMessage = formState.errors["district"],
                    modifier = Modifier.weight(1f).testTag("input_form_district")
                )
            }

            // Certificate Specific Fields Section
            if (certificateType != null && certificateType.specificFields.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "${certificateType.title}-এর সুনির্দিষ্ট বিবরণ",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = BdGreenDark
                )
                Text(
                    text = "সনদের মূল বক্তব্যের জন্য প্রয়োজনীয় তথ্য পূরণ করুন",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                certificateType.specificFields.forEach { field ->
                    CustomFieldRenderer(
                        field = field,
                        currentValue = formState.customFields[field.key] ?: "",
                        errorMessage = formState.errors[field.key],
                        onValueChange = { onCustomFieldChange(field.key, it) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

/**
 * Step 3: Succession Heirs List (if applicable) and Final Review
 */
@Composable
fun StepSuccessionAndReview(
    certificateType: CertificateType?,
    formState: FormState,
    onAddHeir: () -> Unit,
    onUpdateHeir: (Int, Heir) -> Unit,
    onRemoveHeir: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // 1. Succession Heirs Editor (ONLY for succession certificates)
        if (certificateType?.isSuccession == true) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "ওয়ারিশগণের তালিকা",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = BdGreenDark
                            )
                            Text(
                                text = "মৃত ব্যক্তির সকল বৈধ উত্তরাধিকারীর বিবরণ",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = onAddHeir,
                            colors = ButtonDefaults.buttonColors(containerColor = BdGreenPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .heightIn(min = 40.dp)
                                .testTag("button_add_heir")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "ওয়ারিশ যোগ", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    formState.heirs.forEachIndexed { index, heir ->
                        HeirItemEditor(
                            index = index,
                            heir = heir,
                            canDelete = formState.heirs.size > 1,
                            onUpdate = { onUpdateHeir(index, it) },
                            onDelete = { onRemoveHeir(index) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        // 2. Final Review Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.FactCheck, contentDescription = null, tint = BdGreenPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "তথ্য যাচাই ও চূড়ান্ত পর্যালোচনা",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BdGreenDark
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    color = BdGreenContainer.copy(alpha = 0.45f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BdGreenPrimary.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        ReviewRow("সনদের ধরন", certificateType?.title ?: "")
                        ReviewRow("আবেদনকারীর নাম", formState.applicantName)
                        ReviewRow("পিতা/স্বামীর নাম", formState.fatherOrHusbandName)
                        ReviewRow("মাতার নাম", formState.motherName)
                        if (formState.nidOrBirthNo.isNotBlank()) {
                            ReviewRow("এনআইডি/জন্ম নিবন্ধন", formState.nidOrBirthNo)
                        }
                        ReviewRow("গ্রাম ও ওয়ার্ড", "${formState.village}, ওয়ার্ড নং: ${BanglaHelper.toBanglaDigits(formState.wardNo)}")
                        ReviewRow("ডাকঘর ও উপজেলা", "${formState.postOffice}, ${formState.upazila}, ${formState.district}")

                        if (certificateType?.requiresPhoto == true) {
                            ReviewRow("পাসপোর্ট সাইজ ছবি", if (formState.applicantPhotoUri != null) "সংযুক্ত আছে ✓" else "সংযুক্ত নেই")
                        }

                        if (certificateType?.isSuccession == true) {
                            ReviewRow("মোট ওয়ারিশের সংখ্যা", "${BanglaHelper.toBanglaDigits(formState.heirs.size.toString())} জন")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "আইনি প্রত্যয়ন ও ঘোষণা:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "উপরোক্ত সকল বিবরণী স্থানীয় তদন্ত ও নাগরিকের তথ্যের ভিত্তিতে প্রস্তুতকৃত। অনুমোদন পরবর্তীতে এটি ডিজিটালভাবে যাচাইযোগ্য A4 পেপারে প্রিন্ট হবে।",
                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun ReviewRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomFieldRenderer(
    field: CertificateField,
    currentValue: String,
    errorMessage: String?,
    onValueChange: (String) -> Unit
) {
    if (field.type == FieldType.DROPDOWN && field.options.isNotEmpty()) {
        var expanded by remember { mutableStateOf(false) }

        Column(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = field.label,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                )
                if (field.required) {
                    Text(text = " *", color = BdRedAccent, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(6.dp))

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = currentValue.ifEmpty { field.options.firstOrNull() ?: "" },
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BdGreenPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    field.options.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                onValueChange(option)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    } else {
        BanglaInputField(
            value = currentValue,
            onValueChange = {
                // If numeric field, automatically transform English numbers to Bangla digits
                val converted = if (field.type == FieldType.NUMBER) BanglaHelper.toBanglaDigits(it) else it
                onValueChange(converted)
            },
            label = field.label,
            hint = field.hint,
            helperText = field.helperText,
            isNumeric = field.type == FieldType.NUMBER,
            isMultiline = field.type == FieldType.MULTILINE,
            required = field.required,
            errorMessage = errorMessage
        )
    }
}

@Composable
fun HeirItemEditor(
    index: Int,
    heir: Heir,
    canDelete: Boolean,
    onUpdate: (Heir) -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ওয়ারিশ #${BanglaHelper.toBanglaDigits((index + 1).toString())}",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = BdGreenPrimary
                )
                if (canDelete) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "মুছুন", tint = BdRedAccent)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            BanglaInputField(
                value = heir.name,
                onValueChange = { onUpdate(heir.copy(name = it)) },
                label = "ওয়ারিশের নাম",
                hint = "পূর্ণ নাম",
                required = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BanglaInputField(
                    value = heir.relation,
                    onValueChange = { onUpdate(heir.copy(relation = it)) },
                    label = "সম্পর্ক",
                    hint = "যেমন: স্ত্রী / পুত্র",
                    required = true,
                    modifier = Modifier.weight(1f)
                )

                BanglaInputField(
                    value = heir.age,
                    onValueChange = { onUpdate(heir.copy(age = BanglaHelper.toBanglaDigits(it))) },
                    label = "বয়স",
                    hint = "যেমন: ৩৫",
                    isNumeric = true,
                    modifier = Modifier.weight(0.7f)
                )
            }
        }
    }
}

/**
 * Enterprise Form Bottom Action Bar with Previous, Save Draft, and Next/Generate actions
 */
@Composable
fun EnterpriseFormBottomBar(
    currentStep: Int,
    totalSteps: Int,
    onPrevious: () -> Unit,
    onSaveDraft: () -> Unit,
    onNext: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Previous button
            if (currentStep > 0) {
                OutlinedButton(
                    onClick = onPrevious,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("form_button_previous")
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "পূর্ববর্তী")
                }
            } else {
                Spacer(modifier = Modifier.width(4.dp))
            }

            // Save Draft button
            OutlinedButton(
                onClick = onSaveDraft,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = BdGreenPrimary
                ),
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("form_button_save_draft")
            ) {
                Icon(imageVector = Icons.Default.BookmarkBorder, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "খসড়া")
            }

            // Next or Generate button
            Button(
                onClick = onNext,
                colors = ButtonDefaults.buttonColors(containerColor = BdGreenPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("form_button_next")
            ) {
                Text(
                    text = if (currentStep == totalSteps - 1) "সনদ প্রস্তুত করুন" else "পরবর্তী ধাপ",
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = if (currentStep == totalSteps - 1) Icons.Default.CheckCircle else Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

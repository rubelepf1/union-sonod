package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.CertificateField
import com.example.data.model.CertificateType
import com.example.data.model.FieldType
import com.example.data.model.Heir
import com.example.ui.components.BanglaInputField
import com.example.ui.components.StepProgressBar
import com.example.ui.components.UpTopAppBar
import com.example.ui.theme.BdGreenContainer
import com.example.ui.theme.BdGreenDark
import com.example.ui.theme.BdGreenPrimary
import com.example.ui.theme.BdRedAccent
import com.example.ui.viewmodel.FormState
import com.example.util.BanglaHelper

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

    val stepTitles = listOf("আবেদনকারী", "ঠিকানা", "প্রত্যয়ন বিবরণ", "পর্যালোচনা")

    Scaffold(
        topBar = {
            UpTopAppBar(
                title = certificateType?.title ?: "সনদ ফরম",
                subtitle = "ধাপ ${BanglaHelper.toBanglaDigits((formState.currentStep + 1).toString())}: ${stepTitles.getOrElse(formState.currentStep) { "" }}",
                showBackButton = true,
                onBackClick = {
                    if (formState.currentStep > 0) onPreviousStep() else onBackToHome()
                }
            )
        },
        bottomBar = {
            FormBottomBar(
                currentStep = formState.currentStep,
                totalSteps = stepTitles.size,
                onPrevious = onPreviousStep,
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
            StepProgressBar(
                currentStep = formState.currentStep,
                steps = stepTitles
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                when (formState.currentStep) {
                    0 -> StepApplicantInfo(
                        formState = formState,
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
                    1 -> StepAddressInfo(
                        formState = formState,
                        onVillageChange = onVillageChange,
                        onWardNoChange = onWardNoChange,
                        onPostOfficeChange = onPostOfficeChange,
                        onUpazilaChange = onUpazilaChange,
                        onDistrictChange = onDistrictChange
                    )
                    2 -> StepSpecificDetails(
                        certificateType = certificateType,
                        formState = formState,
                        onCustomFieldChange = onCustomFieldChange,
                        onAddHeir = onAddHeir,
                        onUpdateHeir = onUpdateHeir,
                        onRemoveHeir = onRemoveHeir
                    )
                    3 -> StepReview(
                        certificateType = certificateType,
                        formState = formState
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun StepApplicantInfo(
    formState: FormState,
    onApplicantNameChange: (String) -> Unit,
    onFatherOrHusbandNameChange: (String) -> Unit,
    onMotherNameChange: (String) -> Unit,
    onNidOrBirthNoChange: (String) -> Unit,
    onPickPhoto: () -> Unit,
    onRemovePhoto: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(1.5.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "আবেদনকারীর প্রাথমিক তথ্য",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = BdGreenDark
            )
            Text(
                text = "সঠিক বানান ও জাতীয় পরিচয়পত্রের সাথে মিল রেখে তথ্য লিখুন",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Applicant Photo Upload Box
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(70.dp, 84.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .clickable { onPickPhoto() },
                    contentAlignment = Alignment.Center
                ) {
                    if (formState.applicantPhotoUri != null) {
                        AsyncImage(
                            model = formState.applicantPhotoUri,
                            contentDescription = "ছবি",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.AddAPhoto,
                                contentDescription = "ছবি বাছুন",
                                tint = BdGreenPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "ছবি",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "পাসপোর্ট সাইজ ছবি (ঐচ্ছিক)",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = "সনদের ওপর ডান কোণে প্রিন্ট হবে",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row {
                        OutlinedButton(
                            onClick = onPickPhoto,
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text(
                                text = if (formState.applicantPhotoUri != null) "পরিবর্তন" else "ছবি বাছুন",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                        if (formState.applicantPhotoUri != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = onRemovePhoto,
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "মুছুন", tint = BdRedAccent)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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

            BanglaInputField(
                value = formState.motherName,
                onValueChange = onMotherNameChange,
                label = "মাতার নাম",
                hint = "যেমন: মোসাঃ ফাতেমা বেগম",
                required = true,
                errorMessage = formState.errors["motherName"],
                modifier = Modifier.testTag("input_form_mother_name")
            )

            Spacer(modifier = Modifier.height(14.dp))

            BanglaInputField(
                value = formState.nidOrBirthNo,
                onValueChange = onNidOrBirthNoChange,
                label = "জাতীয় পরিচয়পত্র / জন্ম নিবন্ধন নং",
                hint = "১০, ১৩ বা ১৭ ডিজিটের এনআইডি বা জন্ম নিবন্ধন",
                helperText = "বাংলা বা ইংরেজি যেকোনো সংখ্যায় লিখতে পারেন",
                isNumeric = true,
                modifier = Modifier.testTag("input_form_nid")
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepAddressInfo(
    formState: FormState,
    onVillageChange: (String) -> Unit,
    onWardNoChange: (String) -> Unit,
    onPostOfficeChange: (String) -> Unit,
    onUpazilaChange: (String) -> Unit,
    onDistrictChange: (String) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(1.5.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "স্থায়ী ও বর্তমান ঠিকানার বিবরণ",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = BdGreenDark
            )
            Text(
                text = "ইউনিয়ন পরিষদের আওতাধীন সঠিক গ্রাম ও ওয়ার্ড নং উল্লেখ করুন",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

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

            // Ward Selector
            val wardOptions = listOf("১", "২", "৩", "৪", "৫", "৬", "৭", "৮", "৯")
            var wardExpanded by remember { mutableStateOf(false) }

            Text(
                text = "ওয়ার্ড নং *",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
            )
            Spacer(modifier = Modifier.height(6.dp))

            ExposedDropdownMenuBox(
                expanded = wardExpanded,
                onExpandedChange = { wardExpanded = !wardExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = "${formState.wardNo} নং ওয়ার্ড",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = wardExpanded) },
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BdGreenPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .testTag("dropdown_ward_no")
                )
                ExposedDropdownMenu(
                    expanded = wardExpanded,
                    onDismissRequest = { wardExpanded = false }
                ) {
                    wardOptions.forEach { ward ->
                        DropdownMenuItem(
                            text = { Text("$ward নং ওয়ার্ড") },
                            onClick = {
                                onWardNoChange(ward)
                                wardExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            BanglaInputField(
                value = formState.postOffice,
                onValueChange = onPostOfficeChange,
                label = "ডাকঘর",
                hint = "যেমন: কাঞ্চনপুর বাজার",
                required = true,
                errorMessage = formState.errors["postOffice"],
                modifier = Modifier.testTag("input_form_post_office")
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                BanglaInputField(
                    value = formState.upazila,
                    onValueChange = onUpazilaChange,
                    label = "উপজেলা",
                    hint = "উপজেলা",
                    required = true,
                    errorMessage = formState.errors["upazila"],
                    modifier = Modifier.weight(1f).testTag("input_form_upazila")
                )

                BanglaInputField(
                    value = formState.district,
                    onValueChange = onDistrictChange,
                    label = "জেলা",
                    hint = "জেলা",
                    required = true,
                    errorMessage = formState.errors["district"],
                    modifier = Modifier.weight(1f).testTag("input_form_district")
                )
            }
        }
    }
}

@Composable
fun StepSpecificDetails(
    certificateType: CertificateType?,
    formState: FormState,
    onCustomFieldChange: (String, String) -> Unit,
    onAddHeir: () -> Unit,
    onUpdateHeir: (Int, Heir) -> Unit,
    onRemoveHeir: (Int) -> Unit
) {
    if (certificateType == null) return

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(1.5.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "${certificateType.title}-এর সুনির্দিষ্ট বিবরণ",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = BdGreenDark
            )
            Text(
                text = "সনদের মূল বক্তব্যে অন্তর্ভুক্ত করার প্রয়োজনীয় তথ্যাবলি",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Dynamic fields
            certificateType.specificFields.forEach { field ->
                CustomFieldRenderer(
                    field = field,
                    currentValue = formState.customFields[field.key] ?: "",
                    errorMessage = formState.errors[field.key],
                    onValueChange = { onCustomFieldChange(field.key, it) }
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Succession Heirs List Editor
            if (certificateType.isSuccession) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ওয়ারিশগণের তালিকা",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = BdGreenDark
                        )
                        Text(
                            text = "সকল বৈধ উত্তরাধিকারীর নাম, সম্পর্ক ও বয়স দিন",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = onAddHeir,
                        colors = ButtonDefaults.buttonColors(containerColor = BdGreenPrimary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp).testTag("button_add_heir")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "ওয়ারিশ যোগ", style = MaterialTheme.typography.labelSmall)
                    }
                }

                if (formState.errors["heirs"] != null) {
                    Text(
                        text = formState.errors["heirs"]!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = BdRedAccent,
                        modifier = Modifier.padding(top = 4.dp)
                    )
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
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
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

        Column {
            Text(
                text = "${field.label}${if (field.required) " *" else ""}",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
            )
            Spacer(modifier = Modifier.height(6.dp))

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = if (currentValue.isNotEmpty()) currentValue else field.options.firstOrNull() ?: "",
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
                    field.options.forEach { opt ->
                        DropdownMenuItem(
                            text = { Text(opt) },
                            onClick = {
                                onValueChange(opt)
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
            onValueChange = onValueChange,
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
                        modifier = Modifier.size(28.dp)
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
                    onValueChange = { onUpdate(heir.copy(age = it)) },
                    label = "বয়স",
                    hint = "যেমন: ৩৫",
                    isNumeric = true,
                    modifier = Modifier.weight(0.7f)
                )
            }
        }
    }
}

@Composable
fun StepReview(
    certificateType: CertificateType?,
    formState: FormState
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(1.5.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = BdGreenPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "তথ্য যাচাই ও চূড়ান্ত অনুমোদন",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BdGreenDark
                )
            }
            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                color = BdGreenContainer.copy(alpha = 0.5f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    ReviewRow("সনদের ধরন", certificateType?.title ?: "")
                    ReviewRow("আবেদনকারীর নাম", formState.applicantName)
                    ReviewRow("পিতা/স্বামীর নাম", formState.fatherOrHusbandName)
                    ReviewRow("মাতার নাম", formState.motherName)
                    if (formState.nidOrBirthNo.isNotEmpty()) {
                        ReviewRow("এনআইডি/জন্ম নিবন্ধন", BanglaHelper.toBanglaDigits(formState.nidOrBirthNo))
                    }
                    ReviewRow("গ্রাম ও ওয়ার্ড", "${formState.village}, ওয়ার্ড নং: ${BanglaHelper.toBanglaDigits(formState.wardNo)}")
                    ReviewRow("ডাকঘর ও উপজেলা", "${formState.postOffice}, ${formState.upazila}, ${formState.district}")

                    if (certificateType?.isSuccession == true) {
                        ReviewRow("ওয়ারিশের সংখ্যা", "${BanglaHelper.toBanglaDigits(formState.heirs.size)} জন")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "ঘোষণা:",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "উপরে বর্ণিত সকল তথ্য সঠিক ও সত্য। সনদ প্রস্তুতের পর আপনি সরাসরি A4 সাইজে প্রিন্ট নিতে পারবেন এবং সংশ্লিষ্ট ইউনিয়ন পরিষদ চেয়ারম্যানের স্বাক্ষর গ্রহণ করতে পারবেন।",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
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

@Composable
fun FormBottomBar(
    currentStep: Int,
    totalSteps: Int,
    onPrevious: () -> Unit,
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
            if (currentStep > 0) {
                OutlinedButton(
                    onClick = onPrevious,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("form_button_previous")
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "পূর্ববর্তী")
                }
            } else {
                Spacer(modifier = Modifier.width(10.dp))
            }

            Button(
                onClick = onNext,
                colors = ButtonDefaults.buttonColors(containerColor = BdGreenPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("form_button_next")
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

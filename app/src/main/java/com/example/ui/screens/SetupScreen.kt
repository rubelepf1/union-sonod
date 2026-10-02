package com.example.ui.screens

import android.net.Uri
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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.components.BanglaInputField
import com.example.ui.theme.BdGreenDark
import com.example.ui.theme.BdGreenPrimary
import com.example.ui.theme.BdRedAccent

@Composable
fun SetupScreen(
    onSetupComplete: (
        unionName: String,
        upazila: String,
        district: String,
        chairmanName: String,
        unionEmail: String,
        unionPhone: String,
        logoUri: String?
    ) -> Unit
) {
    var unionName by remember { mutableStateOf("") }
    var upazila by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }
    var chairmanName by remember { mutableStateOf("") }
    var unionEmail by remember { mutableStateOf("") }
    var unionPhone by remember { mutableStateOf("") }
    var logoUri by remember { mutableStateOf<Uri?>(null) }
    var hasError by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        logoUri = uri
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Emblem Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(BdGreenPrimary),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(BdRedAccent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "ইউপি প্রতীক",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "ইউপি সনদ",
                style = MaterialTheme.typography.displayMedium,
                color = BdGreenPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "গণপ্রজাতন্ত্রী বাংলাদেশ সরকার অনুমোদিত ইউনিয়ন পরিষদ সনদ ব্যবস্থা",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Info Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "ইউনিয়ন পরিষদ প্রাথমিক সেটআপ",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BdGreenDark
                    )
                    Text(
                        text = "সনদে যে ইউনিয়ন পরিষদের নাম ও চেয়ারম্যানের পদবি প্রদর্শিত হবে তা নির্ধারণ করুন। পরবর্তীতে সেটিংস থেকে পরিবর্তন করা যাবে।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                    )

                    // Quick Demo Preset Button
                    OutlinedButton(
                        onClick = {
                            unionName = "৭নং কাঞ্চনপুর ইউনিয়ন পরিষদ"
                            upazila = "রামগঞ্জ"
                            district = "লক্ষ্মীপুর"
                            chairmanName = "মোঃ ইকবাল হোসেন"
                            unionPhone = "০১৮১১৯৮৭৬৫৪"
                            hasError = false
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = BdGreenPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("setup_preset_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = "নমুনা তথ্য",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "নমুনা তথ্য দিয়ে স্বয়ংক্রিয় পূরণ করুন",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    BanglaInputField(
                        value = unionName,
                        onValueChange = { unionName = it },
                        label = "ইউনিয়ন পরিষদের পূর্ণ নাম",
                        hint = "যেমন: ৭নং কাঞ্চনপুর ইউনিয়ন পরিষদ",
                        required = true,
                        errorMessage = if (hasError && unionName.isBlank()) "ইউনিয়ন পরিষদের নাম আবশ্যক" else null,
                        modifier = Modifier.testTag("input_union_name")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        BanglaInputField(
                            value = upazila,
                            onValueChange = { upazila = it },
                            label = "উপজেলা",
                            hint = "যেমন: রামগঞ্জ",
                            required = true,
                            errorMessage = if (hasError && upazila.isBlank()) "উপজেলা আবশ্যক" else null,
                            modifier = Modifier.weight(1f).testTag("input_upazila")
                        )

                        BanglaInputField(
                            value = district,
                            onValueChange = { district = it },
                            label = "জেলা",
                            hint = "যেমন: লক্ষ্মীপুর",
                            required = true,
                            errorMessage = if (hasError && district.isBlank()) "জেলা আবশ্যক" else null,
                            modifier = Modifier.weight(1f).testTag("input_district")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    BanglaInputField(
                        value = chairmanName,
                        onValueChange = { chairmanName = it },
                        label = "বর্তমান চেয়ারম্যানের নাম",
                        hint = "যেমন: মোঃ ইকবাল হোসেন",
                        required = true,
                        errorMessage = if (hasError && chairmanName.isBlank()) "চেয়ারম্যানের নাম আবশ্যক" else null,
                        modifier = Modifier.testTag("input_chairman_name")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    BanglaInputField(
                        value = unionPhone,
                        onValueChange = { unionPhone = it },
                        label = "মোবাইল নম্বর (ঐচ্ছিক)",
                        hint = "যেমন: ০১৭১২-৩৪৫৬৭৮",
                        isNumeric = true,
                        modifier = Modifier.testTag("input_union_phone")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Optional Logo Selection
                    Text(
                        text = "ইউনিয়ন পরিষদ লোগো / প্রতীক (ঐচ্ছিক)",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                            .padding(12.dp)
                    ) {
                        if (logoUri != null) {
                            AsyncImage(
                                model = logoUri,
                                contentDescription = "লোগো",
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(6.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "লোগো নির্বাচিত হয়েছে (পরিবর্তন করতে ক্লিক করুন)",
                                style = MaterialTheme.typography.bodySmall,
                                color = BdGreenPrimary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = "ছবি বাছুন",
                                tint = BdGreenPrimary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "ইউনিয়ন পরিষদ লোগো বা সিল আপলোড করতে ট্যাপ করুন",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (unionName.isBlank() || upazila.isBlank() || district.isBlank() || chairmanName.isBlank()) {
                                hasError = true
                            } else {
                                onSetupComplete(
                                    unionName,
                                    upazila,
                                    district,
                                    chairmanName,
                                    unionEmail,
                                    unionPhone,
                                    logoUri?.toString()
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BdGreenPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("setup_submit_button")
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "সেটআপ সম্পন্ন করে প্রবেশ করুন",
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 15.sp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

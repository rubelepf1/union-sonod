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
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.data.model.UnionProfile
import com.example.data.model.UserProfile
import com.example.ui.components.BanglaInputField
import com.example.ui.components.UpBottomNav
import com.example.ui.components.UpTopAppBar
import com.example.ui.theme.BdGreenContainer
import com.example.ui.theme.BdGreenDark
import com.example.ui.theme.BdGreenPrimary
import com.example.ui.theme.BdRedAccent
import com.example.ui.viewmodel.ScreenState
import com.example.util.BanglaHelper

@Composable
fun SettingsScreen(
    unionProfile: UnionProfile?,
    currentUser: UserProfile?,
    isOfflineGuestMode: Boolean,
    pendingSyncCount: Int,
    isSyncing: Boolean,
    onTriggerSync: () -> Unit,
    onLogout: () -> Unit,
    onGoToLogin: () -> Unit,
    onSaveProfile: (
        unionName: String,
        upazila: String,
        district: String,
        chairmanName: String,
        unionEmail: String,
        unionPhone: String,
        logoUri: String?
    ) -> Unit,
    onNavigate: (ScreenState) -> Unit
) {
    var unionName by remember(unionProfile) { mutableStateOf(unionProfile?.unionName ?: "") }
    var upazila by remember(unionProfile) { mutableStateOf(unionProfile?.upazila ?: "") }
    var district by remember(unionProfile) { mutableStateOf(unionProfile?.district ?: "") }
    var chairmanName by remember(unionProfile) { mutableStateOf(unionProfile?.chairmanName ?: "") }
    var unionEmail by remember(unionProfile) { mutableStateOf(unionProfile?.unionEmail ?: "") }
    var unionPhone by remember(unionProfile) { mutableStateOf(unionProfile?.unionPhone ?: "") }
    var logoUri by remember(unionProfile) { mutableStateOf(unionProfile?.logoUri?.let { Uri.parse(it) }) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        logoUri = uri
    }

    Scaffold(
        topBar = {
            UpTopAppBar(
                title = "ইউপি সেটিংস ও ক্লাউড",
                subtitle = "ইউনিয়ন পরিষদ তথ্য ও Supabase অ্যাকাউন্ট"
            )
        },
        bottomBar = {
            UpBottomNav(
                currentScreen = ScreenState.SETTINGS,
                onNavigate = onNavigate
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            // User Account / Supabase Cloud Status Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(1.5.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.AccountCircle, contentDescription = null, tint = BdGreenPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ক্লাউড অ্যাকাউন্ট ও সিঙ্ক",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = BdGreenDark
                            )
                        }

                        Surface(
                            color = if (currentUser != null) BdGreenContainer else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = if (currentUser != null) currentUser.roleTitleBn else "অফলাইন গেস্ট",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (currentUser != null) BdGreenDark else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (currentUser != null) {
                        Text(
                            text = "ব্যবহারকারী: ${currentUser.fullName}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "ইমেইল: ${currentUser.email}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (currentUser.phone != null) {
                            Text(
                                text = "মোবাইল: ${currentUser.phone}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = onTriggerSync,
                                enabled = !isSyncing,
                                colors = ButtonDefaults.buttonColors(containerColor = BdGreenPrimary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).testTag("settings_sync_button")
                            ) {
                                Icon(
                                    imageVector = if (isSyncing) Icons.Default.Sync else Icons.Default.CloudSync,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isSyncing) "সিঙ্ক হচ্ছে..." else "এখনই সিঙ্ক করুন",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }

                            OutlinedButton(
                                onClick = onLogout,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("settings_logout_button")
                            ) {
                                Icon(imageVector = Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = BdRedAccent, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "লগআউট", color = BdRedAccent, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    } else {
                        Text(
                            text = "আপনি বর্তমানে সম্পূর্ণ অফলাইন লোকাল মোডে আছেন।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = onGoToLogin,
                            colors = ButtonDefaults.buttonColors(containerColor = BdGreenPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().testTag("settings_login_button")
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.Login, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Supabase অ্যাকাউন্টে লগইন করুন")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Union Details Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(1.5.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = BdGreenPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ইউনিয়ন পরিষদের তথ্য হালনাগাদ",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BdGreenDark
                        )
                    }
                    Text(
                        text = "এখানে সংরক্ষিত তথ্য প্রস্তুতকৃত সকল সনদের হেডারে ও স্বাক্ষরে প্রদর্শিত হবে।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
                    )

                    BanglaInputField(
                        value = unionName,
                        onValueChange = { unionName = it },
                        label = "ইউনিয়ন পরিষদের নাম",
                        hint = "যেমন: ৭নং কাঞ্চনপুর ইউনিয়ন পরিষদ",
                        required = true,
                        modifier = Modifier.testTag("settings_union_name")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        BanglaInputField(
                            value = upazila,
                            onValueChange = { upazila = it },
                            label = "উপজেলা",
                            hint = "উপজেলা",
                            required = true,
                            modifier = Modifier.weight(1f).testTag("settings_upazila")
                        )

                        BanglaInputField(
                            value = district,
                            onValueChange = { district = it },
                            label = "জেলা",
                            hint = "জেলা",
                            required = true,
                            modifier = Modifier.weight(1f).testTag("settings_district")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    BanglaInputField(
                        value = chairmanName,
                        onValueChange = { chairmanName = it },
                        label = "বর্তমান চেয়ারম্যানের নাম",
                        hint = "চেয়ারম্যানের পূর্ণ নাম",
                        required = true,
                        modifier = Modifier.testTag("settings_chairman_name")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    BanglaInputField(
                        value = unionPhone,
                        onValueChange = { unionPhone = it },
                        label = "যোগাযোগ মোবাইল নম্বর",
                        hint = "মোবাইল নম্বর",
                        isNumeric = true,
                        modifier = Modifier.testTag("settings_union_phone")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Logo selector
                    Text(
                        text = "ইউনিয়ন পরিষদ লোগো",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

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
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(6.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "লোগো যুক্ত আছে (পরিবর্তন করতে ট্যাপ করুন)",
                                style = MaterialTheme.typography.bodySmall,
                                color = BdGreenPrimary
                            )
                        } else {
                            Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = null, tint = BdGreenPrimary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "লোগো যুক্ত করতে ট্যাপ করুন",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            if (unionName.isNotBlank() && upazila.isNotBlank() && district.isNotBlank() && chairmanName.isNotBlank()) {
                                onSaveProfile(
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
                        colors = ButtonDefaults.buttonColors(containerColor = BdGreenPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("settings_save_button")
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "সংরক্ষণ করুন", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Instructions & About Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(1.5.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = BdGreenPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ব্যবহার নির্দেশিকা ও সেবা পরিচিতি",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BdGreenDark
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    InstructionItem("১", "নাগরিকের সঠিক তথ্য ও ঠিকানার বিবরণ দিয়ে ফরমটি পূরণ করুন।")
                    InstructionItem("২", "প্রিভিউ স্ক্রিনে দেখে নিন সনদটি নিখুঁত A4 আকারে সজ্জিত হয়েছে।")
                    InstructionItem("৩", "সরাসরি প্রিন্টার দিয়ে প্রিন্ট করুন অথবা PDF আকারে সংরক্ষণ করে প্রিন্ট করান।")
                    InstructionItem("৪", "প্রিন্টআউট কপি নিয়ে ইউনিয়ন পরিষদ চেয়ারম্যানের নিকট থেকে স্বাক্ষর গ্রহণ করুন।")
                    InstructionItem("৫", "ইন্টারনেট থাকলে সনদ স্বয়ংক্রিয়ভাবে Supabase ক্লাউডে ব্যাকআপ থাকবে; ইন্টারনেট না থাকলেও অফলাইনে পূর্ণাঙ্গ সনদ তৈরি হবে।")

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "ইউপি সনদ (UP Sonod) - সংস্করণ ২.০.০ (Supabase ক্লাউড যুক্ত)",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "বাংলাদেশ ইউনিয়ন পরিষদ নাগরিক সনদপত্র তৈরিতে নিবেদিত।",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun InstructionItem(step: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(BdGreenPrimary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = step,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = BdGreenDark
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
    }
}

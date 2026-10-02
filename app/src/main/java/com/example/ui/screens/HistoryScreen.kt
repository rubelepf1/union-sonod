package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GeneratedCertificate
import com.example.data.model.SyncStatus
import com.example.ui.components.UpBottomNav
import com.example.ui.components.UpTopAppBar
import com.example.ui.theme.BdGreenContainer
import com.example.ui.theme.BdGreenDark
import com.example.ui.theme.BdGreenPrimary
import com.example.ui.theme.BdRedAccent
import com.example.ui.theme.GoldAccent
import com.example.ui.viewmodel.ScreenState
import com.example.util.BanglaHelper

@Composable
fun HistoryScreen(
    certificates: List<GeneratedCertificate>,
    pendingSyncCount: Int,
    isSyncing: Boolean,
    onTriggerSync: () -> Unit,
    onOpenPreview: (GeneratedCertificate) -> Unit,
    onDuplicate: (GeneratedCertificate) -> Unit,
    onDelete: (GeneratedCertificate) -> Unit,
    onNavigate: (ScreenState) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var certificateToDelete by remember { mutableStateOf<GeneratedCertificate?>(null) }

    val filteredList = certificates.filter { cert ->
        searchQuery.isBlank() ||
                cert.applicantName.contains(searchQuery, ignoreCase = true) ||
                cert.certificateTitle.contains(searchQuery, ignoreCase = true) ||
                cert.serialNo.contains(searchQuery, ignoreCase = true) ||
                cert.village.contains(searchQuery, ignoreCase = true)
    }

    if (certificateToDelete != null) {
        AlertDialog(
            onDismissRequest = { certificateToDelete = null },
            title = {
                Text(
                    text = "সনদ মুছে ফেলতে চান?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = "${certificateToDelete?.applicantName}-এর ${certificateToDelete?.certificateTitle} (${certificateToDelete?.serialNo}) তালিকা থেকে সম্পূর্ণ মুছে যাবে।",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        certificateToDelete?.let { onDelete(it) }
                        certificateToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BdRedAccent),
                    modifier = Modifier.testTag("confirm_delete_button")
                ) {
                    Text("মুছে ফেলুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { certificateToDelete = null }) {
                    Text("বাতিল")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            UpTopAppBar(
                title = "আমার তৈরি সনদ",
                subtitle = "মোট ${BanglaHelper.toBanglaDigits(certificates.size)} টি প্রস্তুতকৃত সনদ",
                actions = {
                    IconButton(
                        onClick = onTriggerSync,
                        modifier = Modifier.testTag("history_sync_button")
                    ) {
                        Icon(
                            imageVector = if (isSyncing) Icons.Default.Sync else Icons.Default.CloudSync,
                            contentDescription = "ক্লাউড সিঙ্ক",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        bottomBar = {
            UpBottomNav(
                currentScreen = ScreenState.HISTORY,
                onNavigate = onNavigate
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Input & Sync Status Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = "আবেদনকারীর নাম বা স্মারক নং...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "খুঁজুন", tint = BdGreenPrimary)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "মুছুন")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BdGreenPrimary,
                        cursorColor = BdGreenPrimary,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("history_search_input")
                )

                if (pendingSyncCount > 0) {
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = onTriggerSync,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${BanglaHelper.toBanglaDigits(pendingSyncCount)} সিঙ্ক",
                            style = MaterialTheme.typography.labelSmall.copy(color = GoldAccent)
                        )
                    }
                }
            }

            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = if (searchQuery.isEmpty()) "এখনো কোনো সনদ তৈরি করা হয়নি" else "উক্ত নামে কোনো সনদ খুঁজে পাওয়া যায়নি",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "নতুন সনদ তৈরি করতে 'সনদ সমূহ' ট্যাব থেকে পছন্দমতো সনদ নির্বাচন করুন।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Button(
                            onClick = { onNavigate(ScreenState.HOME) },
                            colors = ButtonDefaults.buttonColors(containerColor = BdGreenPrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("নতুন সনদ তৈরি করুন")
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredList, key = { it.id }) { cert ->
                        HistoryCertificateItem(
                            cert = cert,
                            onOpen = { onOpenPreview(cert) },
                            onDuplicate = { onDuplicate(cert) },
                            onDelete = { certificateToDelete = cert }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryCertificateItem(
    cert: GeneratedCertificate,
    onOpen: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(1.5.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() }
            .testTag("history_item_${cert.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = BdGreenContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = cert.certificateTitle,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = BdGreenDark,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Sync status indicator pill
                val isSynced = cert.syncStatus == SyncStatus.SYNCED
                Surface(
                    color = if (isSynced) BdGreenPrimary.copy(alpha = 0.1f) else GoldAccent.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = if (isSynced) Icons.Default.CloudDone else Icons.Default.CloudUpload,
                            contentDescription = null,
                            tint = if (isSynced) BdGreenPrimary else GoldAccent,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (isSynced) "ক্লাউড" else "লোকাল",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                color = if (isSynced) BdGreenPrimary else GoldAccent
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = cert.issueDateBangla,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = cert.applicantName,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "পিতা/স্বামী: ${cert.fatherOrHusbandName} • গ্রাম: ${cert.village}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "স্মারক নং: ${cert.serialNo}",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium),
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onDuplicate,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "অনুরূপ তৈরি", style = MaterialTheme.typography.labelSmall)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onOpen,
                    colors = ButtonDefaults.buttonColors(containerColor = BdGreenPrimary),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(imageVector = Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "দেখুন ও প্রিন্ট", style = MaterialTheme.typography.labelSmall)
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "মুছুন", tint = BdRedAccent)
                }
            }
        }
    }
}

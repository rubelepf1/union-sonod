package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CertificateType
import com.example.data.model.GeneratedCertificate
import com.example.data.model.UnionProfile
import com.example.data.model.UserProfile
import com.example.ui.components.CertificateSyncBadge
import com.example.ui.components.GlobalSyncBar
import com.example.ui.components.UpBottomNav
import com.example.ui.components.UpTopAppBar
import com.example.ui.theme.BdGreenContainer
import com.example.ui.theme.BdGreenDark
import com.example.ui.theme.BdGreenPrimary
import com.example.ui.theme.BdRedAccent
import com.example.ui.viewmodel.ScreenState
import com.example.util.BanglaHelper

@Composable
fun HomeScreen(
    unionProfile: UnionProfile?,
    currentUser: UserProfile?,
    certificateTypes: List<CertificateType>,
    recentCertificates: List<GeneratedCertificate> = emptyList(),
    pendingSyncCount: Int,
    isSyncing: Boolean,
    totalCertificatesCount: Int,
    searchQuery: String,
    selectedCategory: String,
    onSearchChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onSelectCertificate: (CertificateType) -> Unit,
    onOpenCertificate: (GeneratedCertificate) -> Unit = {},
    onRetrySync: (GeneratedCertificate) -> Unit = {},
    onTriggerSync: () -> Unit,
    onNavigate: (ScreenState) -> Unit
) {
    val categories = listOf("সকল", "নাগরিক সেবা", "উত্তরাধিকার", "আর্থিক সেবা", "সামাজিক সুরক্ষা", "বিশেষ প্রত্যয়ন", "বাণিজ্যিক সেবা")

    val filteredList = certificateTypes.filter { cert ->
        val matchesCategory = selectedCategory == "সকল" || cert.category == selectedCategory
        val matchesSearch = searchQuery.isBlank() ||
                cert.title.contains(searchQuery, ignoreCase = true) ||
                cert.englishName.contains(searchQuery, ignoreCase = true) ||
                cert.description.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    // Sync rotation animation
    val infiniteTransition = rememberInfiniteTransition(label = "sync")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing)
        ),
        label = "rotation"
    )

    Scaffold(
        topBar = {
            UpTopAppBar(
                title = unionProfile?.unionName?.ifBlank { "ইউনিয়ন পরিষদ" } ?: "ইউপি সনদ",
                subtitle = "উপজেলা: ${unionProfile?.upazila ?: ""}, জেলা: ${unionProfile?.district ?: ""}",
                actions = {
                    IconButton(
                        onClick = onTriggerSync,
                        modifier = Modifier.testTag("appbar_sync_button")
                    ) {
                        Box(contentAlignment = Alignment.TopEnd) {
                            Icon(
                                imageVector = if (isSyncing) Icons.Default.Sync else Icons.Default.CloudSync,
                                contentDescription = "ক্লাউড সিঙ্ক",
                                tint = Color.White,
                                modifier = if (isSyncing) Modifier.rotate(angle) else Modifier
                            )
                            if (pendingSyncCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(BdRedAccent)
                                )
                            }
                        }
                    }
                }
            )
        },
        bottomBar = {
            UpBottomNav(
                currentScreen = ScreenState.HOME,
                onNavigate = onNavigate
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 320.dp),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = innerPadding.calculateTopPadding() + 12.dp,
                bottom = innerPadding.calculateBottomPadding() + 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Header Stats Banner
            item(span = { GridItemSpan(maxLineSpan) }) {
                HomeBanner(
                    unionProfile = unionProfile,
                    currentUser = currentUser,
                    pendingSyncCount = pendingSyncCount,
                    totalCount = totalCertificatesCount
                )
            }

            // Top Global Sync Bar (Real-time pending sync status & trigger)
            item(span = { GridItemSpan(maxLineSpan) }) {
                GlobalSyncBar(
                    pendingSyncCount = pendingSyncCount,
                    isSyncing = isSyncing,
                    onTriggerSync = onTriggerSync
                )
            }

            // Recent Generated Certificates (if any, with exact sync status badges)
            if (recentCertificates.isNotEmpty() && searchQuery.isBlank()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "সাম্প্রতিক তৈরি সনদ",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Surface(
                                color = BdGreenPrimary.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.clickable { onNavigate(ScreenState.HISTORY) }
                            ) {
                                Text(
                                    text = "সবগুলো দেখুন (${BanglaHelper.toBanglaDigits(totalCertificatesCount)}) →",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = BdGreenPrimary,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            recentCertificates.forEach { cert ->
                                RecentCertificateHomeCard(
                                    cert = cert,
                                    onClick = { onOpenCertificate(cert) },
                                    onRetrySync = { onRetrySync(cert) }
                                )
                            }
                        }
                    }
                }
            }

            // Search Bar
            item(span = { GridItemSpan(maxLineSpan) }) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = {
                        Text(
                            text = "সনদ খুঁজুন (যেমন: নাগরিকত্ব, ওয়ারিশান, মৃত্যু...)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "খুঁজুন",
                            tint = BdGreenPrimary
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchChange("") }) {
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
                        .fillMaxWidth()
                        .testTag("home_search_input")
                )
            }

            // Category Chips
            item(span = { GridItemSpan(maxLineSpan) }) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { cat ->
                        val isSelected = selectedCategory == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { onCategoryChange(cat) },
                            label = {
                                Text(
                                    text = cat,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BdGreenPrimary,
                                selectedLabelColor = Color.White,
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.testTag("category_chip_$cat")
                        )
                    }
                }
            }

            // Title indicator
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "সনদের তালিকা (${BanglaHelper.toBanglaDigits(filteredList.size)} টি প্রাপ্ত)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Cards Grid
            if (filteredList.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "কোনো সনদ পাওয়া যায়নি",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredList, key = { it.id }) { cert ->
                    CertificateCard(
                        cert = cert,
                        onClick = { onSelectCertificate(cert) }
                    )
                }
            }
        }
    }
}

@Composable
fun HomeBanner(
    unionProfile: UnionProfile?,
    currentUser: UserProfile?,
    pendingSyncCount: Int,
    totalCount: Int
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = BdGreenPrimary),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (currentUser != null) currentUser.roleTitleBn else "অফলাইন গেস্ট মোড",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    if (pendingSyncCount > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = BdRedAccent,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "${BanglaHelper.toBanglaDigits(pendingSyncCount)} টি সিঙ্ক বাকি",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (currentUser != null) currentUser.fullName else (unionProfile?.chairmanName?.ifBlank { "চেয়ারম্যান" } ?: "চেয়ারম্যান"),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "সহজে ফরম পূরণ করে নিখুঁত A4 প্রত্যয়নপত্র প্রিন্ট নিন",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Total generated stat
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text = BanglaHelper.toBanglaDigits(totalCount),
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Text(
                    text = "তৈরি সনদ",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
fun CertificateCard(
    cert: CertificateType,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(1.5.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("cert_card_${cert.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val (catBg, catTint) = getCategoryColor(cert.category)
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(catBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getIconForCategory(cert.category),
                        contentDescription = cert.title,
                        tint = catTint,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = cert.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = cert.englishName,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Category Tag
                Surface(
                    color = catBg,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = cert.category,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold),
                        color = catTint,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = cert.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "সনদ তৈরি করুন",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = BdGreenPrimary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "তৈরি",
                    tint = BdGreenPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

fun getIconForCategory(category: String): ImageVector {
    return when (category) {
        "নাগরিক সেবা" -> Icons.Default.VerifiedUser
        "উত্তরাধিকার" -> Icons.Default.FamilyRestroom
        "আর্থিক সেবা" -> Icons.Default.MonetizationOn
        "সামাজিক সুরক্ষা" -> Icons.Default.Favorite
        "বাণিজ্যিক সেবা" -> Icons.Default.Business
        "বিশেষ প্রত্যয়ন" -> Icons.Default.CardMembership
        else -> Icons.Default.Description
    }
}

fun getCategoryColor(category: String): Pair<Color, Color> {
    return when (category) {
        "নাগরিক সেবা" -> Pair(Color(0xFFE8F5E9), Color(0xFF1B5E20))
        "উত্তরাধিকার" -> Pair(Color(0xFFE3F2FD), Color(0xFF0D47A1))
        "আর্থিক সেবা" -> Pair(Color(0xFFFFF8E1), Color(0xFFE65100))
        "সামাজিক সুরক্ষা" -> Pair(Color(0xFFFCE4EC), Color(0xFFC2185B))
        "বাণিজ্যিক সেবা" -> Pair(Color(0xFFEDE7F6), Color(0xFF512DA8))
        "বিশেষ প্রত্যয়ন" -> Pair(Color(0xFFE0F2F1), Color(0xFF004D40))
        else -> Pair(Color(0xFFECEFF1), Color(0xFF37474F))
    }
}

@Composable
fun RecentCertificateHomeCard(
    cert: GeneratedCertificate,
    onClick: () -> Unit,
    onRetrySync: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("home_recent_cert_${cert.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = cert.certificateTitle,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = BdGreenDark
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• ${cert.issueDateBangla}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = cert.applicantName,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "স্মারক নং: ${cert.serialNo}",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp),
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Sync Status Badge
            CertificateSyncBadge(
                syncStatus = cert.syncStatus,
                onRetry = onRetrySync
            )
        }
    }
}

package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SyncStatus
import com.example.ui.theme.BdGreenDark
import com.example.ui.theme.BdGreenPrimary
import com.example.ui.theme.BdRedAccent
import com.example.util.BanglaHelper

/**
 * Reusable visual status badge for GeneratedCertificate based on its syncStatus:
 * 1. "অফলাইন ড্রাফট" (ধূসর ব্যাজ)
 * 2. "সিঙ্ক পেন্ডিং" (হলুদ/অ্যাম্বার ব্যাজ ও স্পিনিং আইকন)
 * 3. "অনলাইনে সংরক্ষিত" (গাঢ় সবুজ ব্যাজ ও টিক চিহ্ন)
 * 4. "সিঙ্ক ব্যর্থ" (লাল ব্যাজ ও 'পুনরায় চেষ্টা করুন' বাটন)
 */
@Composable
fun CertificateSyncBadge(
    syncStatus: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sync_spin")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing)
        ),
        label = "sync_angle"
    )

    when (syncStatus) {
        SyncStatus.DRAFT -> {
            // "অফলাইন ড্রাফট" (ধূসর ব্যাজ)
            Surface(
                color = Color(0xFFF1F5F9), // Light Slate Gray
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                modifier = modifier
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EditNote,
                        contentDescription = "অফলাইন ড্রাফট",
                        tint = Color(0xFF475569),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "অফলাইন ড্রাফট",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color(0xFF334155)
                    )
                }
            }
        }

        SyncStatus.PENDING_INSERT, SyncStatus.PENDING_UPDATE, SyncStatus.PENDING_DELETE -> {
            // "সিঙ্ক পেন্ডিং" (হলুদ/অ্যাম্বার ব্যাজ ও স্পিনিং আইকন)
            Surface(
                color = Color(0xFFFEF3C7), // Amber Light Container
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCD34D)),
                modifier = modifier
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "সিঙ্ক পেন্ডিং",
                        tint = Color(0xFFD97706),
                        modifier = Modifier
                            .size(13.dp)
                            .rotate(angle)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "সিঙ্ক পেন্ডিং",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color(0xFFB45309)
                    )
                }
            }
        }

        SyncStatus.FAILED -> {
            // "সিঙ্ক ব্যর্থ" (লাল ব্যাজ ও 'পুনরায় চেষ্টা করুন' বাটন)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = modifier
            ) {
                Surface(
                    color = Color(0xFFFEE2E2), // Soft Red Container
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = "সিঙ্ক ব্যর্থ",
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "সিঙ্ক ব্যর্থ",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = Color(0xFFDC2626)
                        )
                    }
                }

                if (onRetry != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = Color(0xFFDC2626),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .clickable { onRetry() }
                            .testTag("retry_sync_badge_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "পুনরায় চেষ্টা করুন",
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "পুনরায় চেষ্টা করুন",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        else -> {
            // "অনলাইনে সংরক্ষিত" (গাঢ় সবুজ ব্যাজ ও টিক চিহ্ন)
            Surface(
                color = Color(0xFFE8F5E9), // Light Green
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA5D6A7)),
                modifier = modifier
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "অনলাইনে সংরক্ষিত",
                        tint = Color(0xFF006A4E),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "অনলাইনে সংরক্ষিত",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color(0xFF006A4E)
                    )
                }
            }
        }
    }
}

/**
 * Top Global Sync Bar showing how many items are awaiting sync with action button.
 */
@Composable
fun GlobalSyncBar(
    pendingSyncCount: Int,
    isSyncing: Boolean,
    onTriggerSync: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "global_sync_spin")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing)
        ),
        label = "global_sync_angle"
    )

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSyncing -> Color(0xFFEFF6FF) // Soft Blue
                pendingSyncCount > 0 -> Color(0xFFFFFBEB) // Warm Amber Light
                else -> Color(0xFFF0FDF4) // Soft Mint Green
            }
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = when {
                isSyncing -> Color(0xFF93C5FD)
                pendingSyncCount > 0 -> Color(0xFFFCD34D)
                else -> Color(0xFF86EFAC)
            }
        ),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isSyncing -> Color(0xFF3B82F6).copy(alpha = 0.15f)
                            pendingSyncCount > 0 -> Color(0xFFF59E0B).copy(alpha = 0.18f)
                            else -> Color(0xFF10B981).copy(alpha = 0.15f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when {
                        isSyncing -> Icons.Default.Sync
                        pendingSyncCount > 0 -> Icons.Default.CloudUpload
                        else -> Icons.Default.CloudDone
                    },
                    contentDescription = null,
                    tint = when {
                        isSyncing -> Color(0xFF2563EB)
                        pendingSyncCount > 0 -> Color(0xFFD97706)
                        else -> Color(0xFF059669)
                    },
                    modifier = Modifier
                        .size(20.dp)
                        .then(if (isSyncing) Modifier.rotate(angle) else Modifier)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Text Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = when {
                        isSyncing -> "ক্লাউড সিঙ্ক প্রক্রিয়াধীন রয়েছে..."
                        pendingSyncCount > 0 -> "${BanglaHelper.toBanglaDigits(pendingSyncCount)} টি সনদের তথ্য ক্লাউডে সিঙ্ক বাকি"
                        else -> "সকল ডাটা ক্লাউডে সুরক্ষিত ও সংরক্ষিত আছে"
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp
                    ),
                    color = when {
                        isSyncing -> Color(0xFF1E40AF)
                        pendingSyncCount > 0 -> Color(0xFF92400E)
                        else -> Color(0xFF065F46)
                    }
                )

                Text(
                    text = when {
                        isSyncing -> "রুম ডাটাবেজ থেকে সুপাবেসে ডাটা পুশ হচ্ছে"
                        pendingSyncCount > 0 -> "ইন্টারনেট পাওয়ার সাথে সাথে স্বয়ংক্রিয়ভাবে সিঙ্ক সম্পন্ন হবে"
                        else -> "ডিভাইস ও কেন্দ্রীয় সার্ভার সম্পূর্ণ আপডেট"
                    },
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp),
                    color = when {
                        isSyncing -> Color(0xFF3B82F6)
                        pendingSyncCount > 0 -> Color(0xFFB45309)
                        else -> Color(0xFF047857)
                    }
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Action Button
            if (pendingSyncCount > 0 || isSyncing) {
                Button(
                    onClick = onTriggerSync,
                    enabled = !isSyncing,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (pendingSyncCount > 0) Color(0xFFD97706) else Color(0xFF2563EB),
                        disabledContainerColor = Color(0xFF94A3B8)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier
                        .height(38.dp)
                        .testTag("global_sync_now_button")
                ) {
                    Text(
                        text = if (isSyncing) "সিঙ্ক হচ্ছে..." else "এখনই সিঙ্ক",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            } else {
                IconButton(
                    onClick = onTriggerSync,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("global_sync_refresh_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "রিফ্রেশ করুন",
                        tint = Color(0xFF059669),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

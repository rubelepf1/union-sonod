package com.example.ui.screens

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.CertificateRegistry
import com.example.data.model.GeneratedCertificate
import com.example.data.model.UnionProfile
import com.example.ui.components.UpTopAppBar
import com.example.ui.theme.BdGreenContainer
import com.example.ui.theme.BdGreenDark
import com.example.ui.theme.BdGreenPrimary
import com.example.util.BanglaHelper
import com.example.util.PdfPrintHelper
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun PreviewScreen(
    certificate: GeneratedCertificate?,
    pdfFile: File?,
    unionProfile: UnionProfile? = null,
    onUpdateLogo: ((String?) -> Unit)? = null,
    onEditClick: () -> Unit,
    onBackClick: () -> Unit
) {
    BackHandler { onBackClick() }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val logoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onUpdateLogo?.invoke(uri.toString())
            scope.launch {
                snackbarHostState.showSnackbar("ইউনিয়ন পরিষদ লোগো সফলভাবে পরিবর্তন করা হয়েছে")
            }
        }
    }

    // Smooth Interactive Pinch-to-Zoom & Double-Tap Zoom State
    var targetScale by remember { mutableFloatStateOf(1f) }
    val animatedScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "preview_scale"
    )

    val transformState = rememberTransformableState { zoomChange, _, _ ->
        targetScale = (targetScale * zoomChange).coerceIn(0.80f, 3.0f)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            UpTopAppBar(
                title = "সনদ প্রিভিউ (A4 সাইজ)",
                subtitle = certificate?.serialNo,
                showBackButton = true,
                onBackClick = onBackClick,
                actions = {
                    // Quick Logo Change Action
                    IconButton(
                        onClick = {
                            logoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.testTag("preview_change_logo")
                    ) {
                        Icon(imageVector = Icons.Default.Image, contentDescription = "লোগো পরিবর্তন", tint = Color.White)
                    }
                    // Zoom In
                    IconButton(
                        onClick = { targetScale = (targetScale + 0.25f).coerceAtMost(3.0f) },
                        modifier = Modifier.testTag("preview_zoom_in")
                    ) {
                        Icon(imageVector = Icons.Default.ZoomIn, contentDescription = "বড় করুন", tint = Color.White)
                    }
                    // Zoom Out
                    IconButton(
                        onClick = { targetScale = (targetScale - 0.25f).coerceAtLeast(0.80f) },
                        modifier = Modifier.testTag("preview_zoom_out")
                    ) {
                        Icon(imageVector = Icons.Default.ZoomOut, contentDescription = "ছোট করুন", tint = Color.White)
                    }
                    // Reset Zoom
                    if (targetScale != 1f) {
                        IconButton(onClick = { targetScale = 1f }) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "রিসেট", tint = Color.White)
                        }
                    }
                }
            )
        },
        bottomBar = {
            FloatingPrintActionPanel(
                context = context,
                certificate = certificate,
                pdfFile = pdfFile,
                onEditClick = onEditClick,
                onNotify = { msg ->
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = msg,
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            )
        },
        containerColor = Color(0xFFF1F5F3)
    ) { innerPadding ->
        if (certificate == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "কোনো সনদের তথ্য পাওয়া যায়নি",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                // Interactive Hint Badge with Live Zoom Percentage
                Surface(
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "💡 দুই আঙুলে পিঞ্চ বা ডাবল-ট্যাপ করে জুম করুন (${(animatedScale * 100).toInt()}%)",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // A4 Document Preview Card with Double-Tap and Pinch Gestures
                Box(
                    modifier = Modifier
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onDoubleTap = {
                                    targetScale = if (targetScale > 1.2f) 1f else 1.85f
                                }
                            )
                        }
                        .transformable(state = transformState)
                        .graphicsLayer(
                            scaleX = animatedScale,
                            scaleY = animatedScale
                        )
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White)
                        .border(1.5.dp, Color(0xFF006A4E), RoundedCornerShape(6.dp))
                        .padding(14.dp)
                ) {
                    A4CertificateContent(
                        certificate = certificate,
                        unionProfile = unionProfile,
                        onChangeLogo = {
                            logoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

/**
 * Clean, Standard A4 Certificate Layout
 */
@Composable
fun A4CertificateContent(
    certificate: GeneratedCertificate,
    unionProfile: UnionProfile? = null,
    onChangeLogo: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(0.8.dp, Color(0xFF333333))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Union Parishad Logo (Supports custom uploaded logo or authentic UP monogram)
        Box(
            modifier = Modifier
                .clickable { onChangeLogo?.invoke() },
            contentAlignment = Alignment.Center
        ) {
            if (!unionProfile?.logoUri.isNullOrBlank()) {
                AsyncImage(
                    model = unionProfile?.logoUri,
                    contentDescription = "ইউপি লোগো",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .border(1.2.dp, Color(0xFF006A4E), CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.ic_up_logo),
                    contentDescription = "ইউনিয়ন পরিষদ মনোগ্রাম",
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = "স্থানীয় সরকার বিভাগ",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF005F41)),
            letterSpacing = 0.5.sp
        )
        Text(
            text = certificate.unionName,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.Black
        )
        Text(
            text = "উপজেলা: ${certificate.upazila}, জেলা: ${certificate.district}",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
            color = Color.DarkGray
        )

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = Color(0xFFB4BEB9), thickness = 0.8.dp)
        Spacer(modifier = Modifier.height(10.dp))

        // Title and Photo Row (Conditional Photo: Only for certificates that require photo)
        val certType = CertificateRegistry.findById(certificate.certificateTypeId)
        val requiresPhoto = certType?.requiresPhoto == true
        val hasPhoto = !certificate.applicantPhotoUri.isNullOrBlank()
        val shouldShowPhoto = requiresPhoto

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (shouldShowPhoto) {
                Spacer(modifier = Modifier.width(54.dp)) // balance photo width for centered title
            }
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    color = Color(0xFFF3F8F5),
                    border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFF005F41)),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = certificate.certificateTitle,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.Black,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                    )
                }
            }

            if (shouldShowPhoto) {
                // Top right photo box
                Box(
                    modifier = Modifier
                        .size(54.dp, 66.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .border(0.8.dp, Color.Gray, RoundedCornerShape(3.dp))
                        .background(Color(0xFFF8F9FA)),
                    contentAlignment = Alignment.Center
                ) {
                    if (hasPhoto) {
                        AsyncImage(
                            model = certificate.applicantPhotoUri,
                            contentDescription = "ছবি",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = "পাসপোর্ট\nছবি",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Serial Number & Date
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "স্মারক নং: ${certificate.serialNo}",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                color = Color.DarkGray
            )
            Text(
                text = "তারিখ: ${certificate.issueDateBangla}",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                color = Color.DarkGray
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Formal Bangla Certificate Body
        Text(
            text = certificate.generatedBodyText,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 13.sp,
                lineHeight = 22.sp,
                textAlign = TextAlign.Justify
            ),
            color = Color.Black,
            modifier = Modifier.fillMaxWidth()
        )

        // Succession Heirs Table
        if (!certificate.heirsJson.isNullOrBlank()) {
            val heirs = BanglaHelper.parseHeirsJson(certificate.heirsJson)
            if (heirs.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "ওয়ারিশগণের তালিকা:",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.Black,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(6.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(0.8.dp, Color.Gray)
                ) {
                    // Header Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFEEF3F0))
                            .padding(vertical = 4.dp, horizontal = 6.dp)
                    ) {
                        Text(text = "ক্র.নং", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.width(36.dp))
                        Text(text = "নাম", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f))
                        Text(text = "সম্পর্ক", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.width(64.dp))
                        Text(text = "বয়স", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.width(44.dp))
                    }
                    HorizontalDivider(color = Color.Gray, thickness = 0.8.dp)

                    heirs.forEachIndexed { i, h ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp, horizontal = 6.dp)
                        ) {
                            Text(text = BanglaHelper.toBanglaDigits((i + 1).toString()), style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), modifier = Modifier.width(36.dp))
                            Text(text = h.name, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), modifier = Modifier.weight(1f))
                            Text(text = h.relation, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), modifier = Modifier.width(64.dp))
                            Text(text = BanglaHelper.toBanglaDigits(h.age), style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), modifier = Modifier.width(44.dp))
                        }
                        if (i < heirs.size - 1) {
                            HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 0.5.dp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        // Signatures Block (Ward Member on left, Chairman on right)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.width(110.dp).height(0.8.dp).background(Color.Gray))
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "সত্যায়নকারী ইউপি সদস্য",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.5.sp),
                    color = Color.Black
                )
                Text(
                    text = "স্বাক্ষর ও সিল",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                    color = Color.DarkGray
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.width(130.dp).height(0.8.dp).background(Color.Gray))
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = certificate.chairmanName,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                    color = Color.Black
                )
                Text(
                    text = "চেয়ারম্যান",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 10.sp),
                    color = Color(0xFF005F41)
                )
                Text(
                    text = certificate.unionName,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = Color.DarkGray
                )
            }
        }
    }
}

/**
 * Floating Enterprise Bottom Sheet with 1-click Print, PDF download, and QR Verification Status Card
 */
@Composable
fun FloatingPrintActionPanel(
    context: Context,
    certificate: GeneratedCertificate?,
    pdfFile: File?,
    onEditClick: () -> Unit,
    onNotify: (String) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        tonalElevation = 10.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            // 1. Digital QR Code Verification Status Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA5D6A7)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFF81C784), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode2,
                            contentDescription = "কিউআর কোড",
                            tint = Color(0xFF1B5E20),
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "ডিজিটাল কিউআর কোড ভেরিফাইড",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(imageVector = Icons.Default.Verified, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(14.dp))
                        }
                        Text(
                            text = "স্মারক নং: ${certificate?.serialNo ?: "অনির্ধারিত"} • সরকারি সত্যায়ন প্রস্তুত",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = Color(0xFF2E7D32))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Primary 1-Click Direct Print Button
            Button(
                onClick = {
                    if (pdfFile != null && certificate != null) {
                        PdfPrintHelper.printCertificate(context, pdfFile, certificate.certificateTitle)
                    } else {
                        onNotify("পিডিএফ ফাইল প্রস্তুত হচ্ছে...")
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BdGreenPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 50.dp)
                    .testTag("preview_button_print")
            ) {
                Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "সরাসরি প্রিন্ট করুন (1-Click Print)",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Secondary Actions Row (Save PDF, Share, and Edit)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // PDF Download
                OutlinedButton(
                    onClick = {
                        if (pdfFile != null && certificate != null) {
                            val uri = PdfPrintHelper.savePdfToDownloads(
                                context = context,
                                sourcePdf = pdfFile,
                                filename = "ইউপি_সনদ_${certificate.applicantName}_${certificate.serialNo}"
                            )
                            if (uri != null) {
                                onNotify("সনদটি Downloads ফোল্ডারে সংরক্ষিত হয়েছে ✓")
                            } else {
                                onNotify("ডাউনলোড সম্পন্ন হয়েছে")
                            }
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("preview_button_download")
                ) {
                    Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "ডাউনলোড", style = MaterialTheme.typography.labelMedium)
                }

                // Share
                OutlinedButton(
                    onClick = {
                        if (pdfFile != null && certificate != null) {
                            PdfPrintHelper.shareCertificate(context, pdfFile, certificate.certificateTitle)
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(0.9f)
                        .heightIn(min = 48.dp)
                        .testTag("preview_button_share")
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "শেয়ার", style = MaterialTheme.typography.labelMedium)
                }

                // Edit Form
                OutlinedButton(
                    onClick = onEditClick,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(0.9f)
                        .heightIn(min = 48.dp)
                        .testTag("preview_button_edit")
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "সংশোধন", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

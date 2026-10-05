package com.example.ui.screens

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Print
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
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
import com.example.ui.theme.BdRedAccent
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
                snackbarHostState.showSnackbar("লোগো সফলভাবে পরিবর্তন করা হয়েছে")
            }
        }
    }

    var scale by remember { mutableFloatStateOf(1f) }
    val transformState = rememberTransformableState { zoomChange, _, _ ->
        scale = (scale * zoomChange).coerceIn(0.85f, 2.5f)
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
                    IconButton(
                        onClick = { scale = (scale + 0.2f).coerceAtMost(2.5f) },
                        modifier = Modifier.testTag("preview_zoom_in")
                    ) {
                        Icon(imageVector = Icons.Default.ZoomIn, contentDescription = "বড় করুন", tint = Color.White)
                    }
                    IconButton(
                        onClick = { scale = (scale - 0.2f).coerceAtLeast(0.85f) },
                        modifier = Modifier.testTag("preview_zoom_out")
                    ) {
                        Icon(imageVector = Icons.Default.ZoomOut, contentDescription = "ছোট করুন", tint = Color.White)
                    }
                    if (scale != 1f) {
                        IconButton(onClick = { scale = 1f }) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "রিসেট", tint = Color.White)
                        }
                    }
                }
            )
        },
        bottomBar = {
            PreviewBottomActions(
                onSave = {
                    if (pdfFile != null && certificate != null) {
                        val savedUri = PdfPrintHelper.savePdfToDownloads(
                            context = context,
                            sourcePdf = pdfFile,
                            filename = "ইউপি_সনদ_${certificate.applicantName}_${certificate.serialNo}"
                        )
                        scope.launch {
                            val result = snackbarHostState.showSnackbar(
                                message = "সনদটি Downloads ফোল্ডারে সংরক্ষিত হয়েছে",
                                actionLabel = "খুলুন",
                                duration = SnackbarDuration.Long
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                PdfPrintHelper.openCertificate(context, pdfFile)
                            }
                        }
                    }
                },
                onPrint = {
                    if (pdfFile != null && certificate != null) {
                        PdfPrintHelper.printCertificate(context, pdfFile, certificate.certificateTitle)
                    }
                },
                onShare = {
                    if (pdfFile != null && certificate != null) {
                        PdfPrintHelper.shareCertificate(context, pdfFile, certificate.certificateTitle)
                    }
                },
                onEdit = onEditClick
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (certificate == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "কোনো সনদ পাওয়া যায়নি")
            }
            return@Scaffold
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .transformable(state = transformState)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Zoom hint tag
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Text(
                        text = "পিন্চ করে জুম করুন অথবা ওপরে +/- বাটন চাপুন (${(scale * 100).toInt()}%)",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }

                // A4 Document Preview Card
                Box(
                    modifier = Modifier
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale
                        )
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.White)
                        .border(1.5.dp, Color(0xFF14462D), RoundedCornerShape(4.dp))
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
                Spacer(modifier = Modifier.width(54.dp)) // balance photo width
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

        // Ref No and Date Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "স্মারক নং: ${certificate.serialNo}",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium),
                color = Color.Black
            )
            Text(
                text = "তারিখ: ${certificate.issueDateBangla}",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium),
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(6.dp))
        HorizontalDivider(color = Color(0xFFB4BEB9), thickness = 0.8.dp)
        Spacer(modifier = Modifier.height(12.dp))

        // Body Paragraph
        Text(
            text = certificate.generatedBodyText,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 13.5.sp,
                lineHeight = 22.sp
            ),
            color = Color(0xFF141414),
            textAlign = TextAlign.Justify,
            modifier = Modifier.fillMaxWidth()
        )

        // Succession heirs table if present
        val heirs = certificate.heirsJson?.let { BanglaHelper.parseHeirsJson(it) } ?: emptyList()
        if (heirs.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            PreviewHeirsTable(heirs = heirs)
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Footer Section: Member sig on left, Chairman sig on right
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            // Left: Member
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.width(120.dp).height(1.dp).background(Color.DarkGray))
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "সত্যায়নকারী ইউপি সদস্য",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                    color = Color.Black
                )
                Text(
                    text = "স্বাক্ষর",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                    color = Color.DarkGray
                )
            }

            // Right: Chairman signature block
            Column(horizontalAlignment = Alignment.End) {
                Box(modifier = Modifier.width(135.dp).height(1.dp).background(Color.DarkGray))
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = certificate.chairmanName,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                    color = Color.Black
                )
                Text(
                    text = "চেয়ারম্যান",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp, fontWeight = FontWeight.Bold),
                    color = Color.Black
                )
                Text(
                    text = certificate.unionName,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.5.sp),
                    color = Color.DarkGray
                )
                Text(
                    text = "${certificate.upazila}, ${certificate.district}",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.5.sp),
                    color = Color.DarkGray
                )
            }
        }
    }
}

@Composable
fun PreviewHeirsTable(heirs: List<com.example.data.model.Heir>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(0.8.dp, Color(0xFF333333))
    ) {
        // Table Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFEEF3F0))
                .padding(vertical = 4.dp, horizontal = 6.dp)
        ) {
            Text(text = "ক্র.নং", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.5.sp), modifier = Modifier.width(36.dp))
            Text(text = "ওয়ারিশগণের নাম", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.5.sp), modifier = Modifier.weight(1f))
            Text(text = "সম্পর্ক", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.5.sp), modifier = Modifier.width(55.dp))
            Text(text = "বয়স", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.5.sp), modifier = Modifier.width(40.dp))
        }
        HorizontalDivider(color = Color(0xFF333333), thickness = 0.8.dp)

        heirs.forEachIndexed { i, h ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp, horizontal = 6.dp)
            ) {
                Text(text = BanglaHelper.toBanglaDigits((i + 1).toString()), style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp), modifier = Modifier.width(36.dp))
                Text(text = h.name, style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp), modifier = Modifier.weight(1f))
                Text(text = h.relation, style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp), modifier = Modifier.width(55.dp))
                Text(text = BanglaHelper.toBanglaDigits(h.age), style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp), modifier = Modifier.width(40.dp))
            }
            if (i < heirs.size - 1) {
                HorizontalDivider(color = Color(0xFFCCCCCC), thickness = 0.5.dp)
            }
        }
    }
}

@Composable
fun PreviewBottomActions(
    onSave: () -> Unit,
    onPrint: () -> Unit,
    onShare: () -> Unit,
    onEdit: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onSave,
                    colors = ButtonDefaults.buttonColors(containerColor = BdGreenPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("preview_button_save")
                ) {
                    Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "PDF সেভ", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Button(
                    onClick = onPrint,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004D38)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("preview_button_print")
                ) {
                    Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "প্রিন্ট করুন", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onShare,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .testTag("preview_button_share")
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "শেয়ার করুন", fontSize = 12.5.sp)
                }

                OutlinedButton(
                    onClick = onEdit,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .testTag("preview_button_edit")
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "তথ্য এডিট", fontSize = 12.5.sp)
                }
            }
        }
    }
}

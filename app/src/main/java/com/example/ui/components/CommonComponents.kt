package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BdGreenPrimary
import com.example.ui.theme.BdRedAccent
import com.example.ui.viewmodel.ScreenState
import com.example.util.BanglaHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpTopAppBar(
    title: String,
    subtitle: String? = null,
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {},
    actions: @Composable () -> Unit = {}
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                        color = Color.White.copy(alpha = 0.85f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        navigationIcon = {
            if (showBackButton) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.testTag("appbar_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "ফিরে যান",
                        tint = Color.White
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .padding(start = 12.dp, end = 8.dp)
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(BdRedAccent)
                    )
                }
            }
        },
        actions = { actions() },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = BdGreenPrimary,
            titleContentColor = Color.White,
            actionIconContentColor = Color.White
        )
    )
}

@Composable
fun UpBottomNav(
    currentScreen: ScreenState,
    onNavigate: (ScreenState) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        val items = listOf(
            Triple(ScreenState.HOME, "সনদ সমূহ", Icons.Default.Home),
            Triple(ScreenState.HISTORY, "আমার তৈরি সনদ", Icons.Default.History),
            Triple(ScreenState.SETTINGS, "ইউপি সেটিংস", Icons.Default.Settings)
        )

        items.forEach { (screen, label, icon) ->
            val selected = currentScreen == screen
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(screen) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = BdGreenPrimary,
                    selectedTextColor = BdGreenPrimary,
                    indicatorColor = BdGreenPrimary.copy(alpha = 0.12f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.testTag("nav_item_${screen.name.lowercase()}")
            )
        }
    }
}

@Composable
fun BanglaInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    hint: String = "",
    helperText: String? = null,
    errorMessage: String? = null,
    isNumeric: Boolean = false,
    isMultiline: Boolean = false,
    required: Boolean = false,
    readOnly: Boolean = false,
    trailingIcon: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )
            if (required) {
                Text(
                    text = " *",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = BdRedAccent
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                if (hint.isNotEmpty()) {
                    Text(
                        text = hint,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            },
            isError = !errorMessage.isNullOrBlank(),
            readOnly = readOnly,
            singleLine = !isMultiline,
            minLines = if (isMultiline) 3 else 1,
            maxLines = if (isMultiline) 5 else 1,
            keyboardOptions = KeyboardOptions(
                keyboardType = if (isNumeric) KeyboardType.Number else KeyboardType.Text
            ),
            trailingIcon = trailingIcon,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BdGreenPrimary,
                focusedLabelColor = BdGreenPrimary,
                cursorColor = BdGreenPrimary,
                errorBorderColor = BdRedAccent
            ),
            modifier = Modifier.fillMaxWidth()
        )

        if (!errorMessage.isNullOrBlank()) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = BdRedAccent,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        } else if (!helperText.isNullOrBlank()) {
            Text(
                text = helperText,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }
    }
}

@Composable
fun StepProgressBar(
    currentStep: Int,
    steps: List<String>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            steps.forEachIndexed { index, title ->
                val isCompleted = index < currentStep
                val isCurrent = index == currentStep

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isCompleted -> BdGreenPrimary
                                    isCurrent -> BdGreenPrimary
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            )
                            .border(
                                width = if (isCurrent) 2.dp else 0.dp,
                                color = if (isCurrent) BdRedAccent else Color.Transparent,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCompleted) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "সম্পন্ন",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Text(
                                text = BanglaHelper.toBanglaDigits((index + 1).toString()),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isCurrent) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.5.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = if (isCurrent) BdGreenPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        val progress = (currentStep.toFloat() + 0.1f) / steps.size.toFloat()
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = BdGreenPrimary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

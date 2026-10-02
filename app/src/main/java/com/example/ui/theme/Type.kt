package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val NotoSansBengaliFamily = FontFamily(
    Font(R.font.noto_sans_bengali, FontWeight.Normal)
)

val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = NotoSansBengaliFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp
    ),
    displayMedium = TextStyle(
        fontFamily = NotoSansBengaliFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 34.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = NotoSansBengaliFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 30.sp
    ),
    titleLarge = TextStyle(
        fontFamily = NotoSansBengaliFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 19.sp,
        lineHeight = 26.sp
    ),
    titleMedium = TextStyle(
        fontFamily = NotoSansBengaliFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 23.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = NotoSansBengaliFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 23.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = NotoSansBengaliFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 19.sp
    ),
    labelLarge = TextStyle(
        fontFamily = NotoSansBengaliFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelMedium = TextStyle(
        fontFamily = NotoSansBengaliFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelSmall = TextStyle(
        fontFamily = NotoSansBengaliFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 15.sp
    )
)

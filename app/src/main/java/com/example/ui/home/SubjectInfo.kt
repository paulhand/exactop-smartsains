package com.example.ui.home

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class SubjectInfo(
    val name: String,
    val description: String,
    val icon: ImageVector,
    val accentColor: Color,
    val gradientColors: List<Color>,
    val testTag: String
)

package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*

@Composable
fun NeonButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    accentColor: Color = ElectricCyan,
    icon: ImageVector? = null,
    testTag: String = ""
) {
    val buttonModifier = if (testTag.isNotBlank()) modifier.testTag(testTag) else modifier
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = buttonModifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = accentColor,
            contentColor = TextDark,
            disabledContainerColor = SurfaceDarkElevated,
            disabledContentColor = TextMuted
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = if (enabled) TextDark else TextMuted
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (enabled) TextDark else TextMuted
            )
        }
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    borderColor: Color = SurfaceBorder,
    backgroundColor: Color = SurfaceDark,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(1.dp, borderColor)
    ) {
        content()
    }
}

@Composable
fun QuizTimerCircle(
    remainingSeconds: Int,
    totalSeconds: Int = 15,
    modifier: Modifier = Modifier
) {
    val progress = (remainingSeconds.toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "timerProgress")
    val timerColor = when {
        remainingSeconds <= 3 -> ErrorRed
        remainingSeconds <= 7 -> WarningOrange
        else -> ElectricCyan
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(64.dp)
    ) {
        CircularProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.fillMaxSize(),
            color = timerColor,
            trackColor = SurfaceDarkElevated,
            strokeWidth = 6.dp
        )
        Text(
            text = "$remainingSeconds",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = timerColor
        )
    }
}

@Composable
fun FeedbackResultDialog(
    isCorrect: Boolean,
    isTimeout: Boolean,
    scoreEarned: Int,
    message: String,
    correctAnswer: String,
    explanation: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("feedback_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = BorderStroke(
                2.dp,
                if (isCorrect) SuccessGreen else if (isTimeout) WarningOrange else ErrorRed
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = if (isCorrect) Icons.Filled.CheckCircle else if (isTimeout) Icons.Filled.Timer else Icons.Filled.Close,
                    contentDescription = null,
                    tint = if (isCorrect) SuccessGreen else if (isTimeout) WarningOrange else ErrorRed,
                    modifier = Modifier.size(54.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (isCorrect) "BENAR! +$scoreEarned" else if (isTimeout) "WAKTU HABIS!" else "SALAH! +0",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isCorrect) SuccessGreen else if (isTimeout) WarningOrange else ErrorRed
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = message,
                    fontSize = 14.sp,
                    color = TextWhite,
                    textAlign = TextAlign.Center
                )
                if (!isCorrect) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = SurfaceDarkElevated
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Kunci Jawaban: $correctAnswer",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = AcidLime
                            )
                            if (explanation.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = explanation,
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
                NeonButton(
                    text = "Lanjut",
                    onClick = onDismiss,
                    accentColor = if (isCorrect) SuccessGreen else ElectricCyan,
                    testTag = "feedback_dialog_continue_button"
                )
            }
        }
    }
}

package com.example.ui.quiz

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Member
import com.example.ui.components.FeedbackResultDialog
import com.example.ui.components.GlassCard
import com.example.ui.components.NeonButton
import com.example.ui.components.QuizTimerCircle
import com.example.ui.theme.*

@Composable
fun QuizScreen(
    member: Member,
    quizViewModel: QuizViewModel,
    onQuizCancelled: () -> Unit,
    onQuizCompleted: () -> Unit
) {
    val uiState by quizViewModel.uiState.collectAsState()

    when (val state = uiState) {
        is QuizUiState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DarkCanvas),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = ElectricCyan)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Memuat Soal Kuis...", color = TextWhite, fontSize = 15.sp)
                }
            }
        }

        is QuizUiState.Error -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DarkCanvas)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.widthIn(max = 400.dp)
                ) {
                    Text(
                        text = "Gagal Memuat Kuis",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = ErrorRed
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.message,
                        fontSize = 14.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    NeonButton(
                        text = "Kembali ke Beranda",
                        onClick = onQuizCancelled,
                        accentColor = ElectricCyan,
                        testTag = "quiz_error_back_button"
                    )
                }
            }
        }

        is QuizUiState.Completed -> {
            QuizSummaryScreen(
                completedState = state,
                member = member,
                onBackToHome = onQuizCompleted
            )
        }

        is QuizUiState.InProgress -> {
            val currentQuestion = state.questions[state.currentIndex]
            val feedback = state.currentFeedback

            if (feedback != null) {
                FeedbackResultDialog(
                    isCorrect = feedback.isCorrect,
                    isTimeout = feedback.isTimeout,
                    scoreEarned = feedback.scoreEarned,
                    message = feedback.message,
                    correctAnswer = feedback.correctAnswer,
                    explanation = feedback.explanation,
                    onDismiss = { quizViewModel.nextQuestion(member) }
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DarkCanvas)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top Bar: Subject, Index, Timer, Close
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onQuizCancelled,
                            modifier = Modifier.testTag("quiz_close_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Batalkan Kuis",
                                tint = TextMuted
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = state.subject.uppercase(),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = ElectricCyan
                            )
                            Text(
                                text = "Soal ${state.currentIndex + 1} dari ${state.questions.size}",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }

                        QuizTimerCircle(
                            remainingSeconds = state.remainingSeconds,
                            totalSeconds = state.totalSecondsForCurrent,
                            modifier = Modifier.testTag("quiz_timer_indicator")
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Question Card
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("quiz_question_card"),
                        borderColor = SurfaceBorder,
                        backgroundColor = SurfaceDark
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = SurfaceDarkElevated
                                ) {
                                    Text(
                                        text = "Grade ${currentQuestion.grade}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AcidLime,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Text(
                                    text = "+${currentQuestion.skor} Poin • ${state.totalSecondsForCurrent}s",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = ElectricYellow
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = currentQuestion.pertanyaan,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextWhite,
                                lineHeight = 24.sp,
                                modifier = Modifier.testTag("question_text")
                            )

                            // Question image if available in image1['quest']
                            val questionImageUrl = currentQuestion.parsedImages.questUrl
                            if (!questionImageUrl.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(14.dp))
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .testTag("question_image_container"),
                                    color = Color.White,
                                    border = BorderStroke(1.dp, SurfaceBorder)
                                ) {
                                    AsyncImage(
                                        model = questionImageUrl,
                                        contentDescription = "Gambar Soal",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 120.dp, max = 260.dp)
                                            .padding(6.dp)
                                            .testTag("question_image"),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 4 Options Buttons
                    OptionButton(
                        key = "A",
                        text = currentQuestion.opsi_a,
                        imageUrl = currentQuestion.parsedImages.ansAUrl,
                        accentColor = ElectricCyan,
                        onClick = { quizViewModel.submitAnswer("A") },
                        testTag = "option_button_a"
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OptionButton(
                        key = "B",
                        text = currentQuestion.opsi_b,
                        imageUrl = currentQuestion.parsedImages.ansBUrl,
                        accentColor = BrightViolet,
                        onClick = { quizViewModel.submitAnswer("B") },
                        testTag = "option_button_b"
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OptionButton(
                        key = "C",
                        text = currentQuestion.opsi_c,
                        imageUrl = currentQuestion.parsedImages.ansCUrl,
                        accentColor = AcidLime,
                        onClick = { quizViewModel.submitAnswer("C") },
                        testTag = "option_button_c"
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OptionButton(
                        key = "D",
                        text = currentQuestion.opsi_d,
                        imageUrl = currentQuestion.parsedImages.ansDUrl,
                        accentColor = ElectricYellow,
                        onClick = { quizViewModel.submitAnswer("D") },
                        testTag = "option_button_d"
                    )
                }
            }
        }

        else -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DarkCanvas),
                contentAlignment = Alignment.Center
            ) {
                Text("Menyiapkan kuis...", color = TextWhite)
            }
        }
    }
}

@Composable
private fun OptionButton(
    key: String,
    text: String,
    imageUrl: String? = null,
    accentColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = SurfaceDarkElevated,
            contentColor = TextWhite
        ),
        border = BorderStroke(1.dp, SurfaceBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = accentColor.copy(alpha = 0.2f),
                border = BorderStroke(1.dp, accentColor)
            ) {
                Text(
                    text = key,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = accentColor,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                if (text.isNotBlank()) {
                    Text(
                        text = text,
                        fontSize = 14.sp,
                        color = TextWhite,
                        modifier = Modifier.testTag("option_text_$key")
                    )
                }
                if (!imageUrl.isNullOrBlank()) {
                    if (text.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp)),
                        color = Color.White,
                        border = BorderStroke(1.dp, SurfaceBorder)
                    ) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = "Gambar Opsi $key",
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 60.dp, max = 180.dp)
                                .padding(4.dp)
                                .testTag("option_image_$key"),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }
        }
    }
}

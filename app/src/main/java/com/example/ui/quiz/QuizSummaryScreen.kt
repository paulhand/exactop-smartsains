package com.example.ui.quiz

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Member
import com.example.ui.components.GlassCard
import com.example.ui.components.NeonButton
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun QuizSummaryScreen(
    completedState: QuizUiState.Completed,
    member: Member,
    onBackToHome: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkCanvas)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 20.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header card
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = SuccessGreen,
                    backgroundColor = SurfaceDark
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Kuis Selesai! 🎉",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = SuccessGreen
                        )
                        Text(
                            text = "Mata Pelajaran: ${completedState.subject}",
                            fontSize = 14.sp,
                            color = TextMuted,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "${completedState.sessionTotalScore}",
                            fontSize = 44.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ElectricCyan,
                            modifier = Modifier.testTag("summary_session_score_text")
                        )
                        Text(
                            text = "Total Skor Sesi Ini",
                            fontSize = 12.sp,
                            color = TextWhite
                        )
                    }
                }
            }

            // Error banner if update failed
            if (completedState.updateError != null) {
                item {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("update_error_card"),
                        borderColor = ErrorRed,
                        backgroundColor = SurfaceDark
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Gagal Update Skor ke Server Supabase",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = ErrorRed
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = completedState.updateError.errorMessage,
                                fontSize = 12.sp,
                                color = TextWhite
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Query: ${completedState.updateError.queryString}",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = TextMuted
                            )
                        }
                    }
                }
            }

            // Stored skor array card
            item {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("summary_stored_skor_card"),
                    borderColor = ElectricCyan.copy(alpha = 0.5f),
                    backgroundColor = SurfaceDark
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Format Array Tersimpan di Kolom \"skor_akumulasi\"",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyan
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = DarkCanvas,
                            border = BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.25f))
                        ) {
                            Text(
                                text = completedState.rawStoredSkor.ifBlank {
                                    "[${completedState.allCumulativeScores.joinToString(", ") { if (it % 1.0 == 0.0) it.toLong().toString() else String.format(Locale.US, "%.1f", it) }}]"
                                },
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                color = AcidLime,
                                modifier = Modifier
                                    .padding(10.dp)
                                    .fillMaxWidth()
                                    .testTag("summary_stored_skor_text")
                            )
                        }
                    }
                }
            }

            // Updated stats grid
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Statistik Akumulasi Diperbarui",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatBox(
                            label = "TOTAL",
                            value = if (completedState.totalCumulativeScore % 1.0 == 0.0) completedState.totalCumulativeScore.toLong().toString() else String.format(Locale.US, "%.1f", completedState.totalCumulativeScore),
                            color = ElectricCyan,
                            modifier = Modifier.weight(1f)
                        )
                        StatBox(
                            label = "NUMSESI",
                            value = completedState.numSesi.toString(),
                            color = BrightViolet,
                            modifier = Modifier.weight(1f)
                        )
                        StatBox(
                            label = "MEAN",
                            value = String.format(Locale.US, "%.2f", completedState.meanScore),
                            color = AcidLime,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatBox(
                            label = "MODUS",
                            value = if (completedState.modusScore % 1.0 == 0.0) completedState.modusScore.toLong().toString() else String.format(Locale.US, "%.2f", completedState.modusScore),
                            color = ElectricYellow,
                            modifier = Modifier.weight(1f)
                        )
                        StatBox(
                            label = "MEDIAN",
                            value = if (completedState.medianScore % 1.0 == 0.0) completedState.medianScore.toLong().toString() else String.format(Locale.US, "%.2f", completedState.medianScore),
                            color = CyberPink,
                            modifier = Modifier.weight(1f)
                        )
                        StatBox(
                            label = "RANK",
                            value = "#${completedState.userRank} / ${completedState.totalMembers}",
                            color = WarningOrange,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Results breakdown
            item {
                Text(
                    text = "Rincian Jawaban (${completedState.results.count { it.isCorrect }} / ${completedState.results.size} Benar)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            itemsIndexed(completedState.results) { index, result ->
                QuestionResultRow(index = index, result = result)
            }

            // Back to Home button
            item {
                Spacer(modifier = Modifier.height(8.dp))
                NeonButton(
                    text = "Kembali ke Beranda",
                    onClick = onBackToHome,
                    icon = Icons.Filled.Home,
                    accentColor = ElectricCyan,
                    testTag = "summary_back_to_home_button"
                )
            }
        }
    }
}

@Composable
private fun StatBox(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = SurfaceDarkElevated,
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Black, color = color)
            Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
        }
    }
}

@Composable
private fun QuestionResultRow(
    index: Int,
    result: QuestionResult
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = if (result.isCorrect) SuccessGreen.copy(alpha = 0.5f) else ErrorRed.copy(alpha = 0.5f),
        backgroundColor = SurfaceDark
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (result.isCorrect) Icons.Filled.CheckCircle else if (result.isTimeout) Icons.Filled.Timer else Icons.Filled.Close,
                contentDescription = null,
                tint = if (result.isCorrect) SuccessGreen else if (result.isTimeout) WarningOrange else ErrorRed,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "No. ${index + 1}: ${result.question.pertanyaan}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextWhite,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (result.isCorrect) "Benar! Jawaban: ${result.question.jawaban_benar}" else "Jawaban Anda: ${result.selectedOption ?: "Tidak menjawab"} | Kunci: ${result.question.jawaban_benar}",
                    fontSize = 11.sp,
                    color = if (result.isCorrect) SuccessGreen else TextMuted
                )
            }

            Text(
                text = "+${result.scoreEarned}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (result.isCorrect) SuccessGreen else TextMuted
            )
        }
    }
}

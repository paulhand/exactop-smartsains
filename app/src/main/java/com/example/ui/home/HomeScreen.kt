package com.example.ui.home

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Member
import com.example.data.model.SkorAkumulasi
import com.example.ui.auth.AuthViewModel
import com.example.ui.components.GlassCard
import com.example.ui.components.NeonButton
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun HomeScreen(
    member: Member,
    authViewModel: AuthViewModel,
    onStartQuiz: (subject: String) -> Unit,
    onOpenAdmin: () -> Unit,
    onLogout: () -> Unit
) {
    var selectedSubject by remember { mutableStateOf("Matematika") }

    val currentMemberFromVm by authViewModel.currentMember.collectAsState()
    val isRefreshing by authViewModel.isRefreshing.collectAsState()
    val allMembers by authViewModel.allMembers.collectAsState()

    // Reload data each time Beranda is displayed
    LaunchedEffect(member.email) {
        authViewModel.refreshCurrentMember(member.email)
    }

    val activeMember = currentMemberFromVm ?: member
    val rawSkorAkumulasi = activeMember.skor_akumulasi ?: "[]"
    val stats = activeMember.parsedSkorAkumulasi

    // Compute rank
    val rankIndex = allMembers
        .sortedByDescending { if (it.email.equals(activeMember.email, ignoreCase = true)) stats.mean else it.parsedSkorAkumulasi.mean }
        .indexOfFirst { it.email.equals(activeMember.email, ignoreCase = true) }
    val displayRank = if (rankIndex >= 0) rankIndex + 1 else 1
    val totalMembers = maxOf(allMembers.size, 1)

    val infiniteTransition = rememberInfiniteTransition(label = "refreshRotation")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotationAngle"
    )

    val subjects = listOf(
        SubjectInfo(
            name = "Matematika",
            description = "Aljabar, Geometri, Skala, dan Logika Bilangan",
            icon = Icons.Filled.Calculate,
            accentColor = MathColor,
            gradientColors = listOf(Color(0xFF0077B6), Color(0xFF00F0FF)),
            testTag = "subject_card_matematika"
        ),
        SubjectInfo(
            name = "Fisika",
            description = "Mekanika, Kalor, Kelistrikan, dan Perubahan Wujud",
            icon = Icons.Filled.Science,
            accentColor = PhysicsColor,
            gradientColors = listOf(Color(0xFF5A189A), Color(0xFF9D4EDD)),
            testTag = "subject_card_fisika"
        ),
        SubjectInfo(
            name = "Kimia",
            description = "Reaksi Kimia, Senyawa, Asam Basa, dan Unsur",
            icon = Icons.Filled.Science,
            accentColor = ChemistryColor,
            gradientColors = listOf(Color(0xFF38B000), Color(0xFF70E000)),
            testTag = "subject_card_kimia"
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkCanvas)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ExacTop",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = ElectricCyan,
                            modifier = Modifier.testTag("home_app_name_text")
                        )
                        Text(
                            text = "Smart Sains Academy Learning Assistant",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AcidLime,
                            modifier = Modifier
                                .padding(vertical = 1.dp)
                                .testTag("home_subtitle_text")
                        )
                        Text(
                            text = "Halo, ${activeMember.nama.ifBlank { "Siswa" }} (K-${activeMember.grade})",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextWhite
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(
                            onClick = { authViewModel.refreshCurrentMember(activeMember.email) },
                            modifier = Modifier.testTag("home_refresh_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "Reload Data",
                                tint = ElectricCyan,
                                modifier = if (isRefreshing) Modifier.rotate(rotationAngle) else Modifier
                            )
                        }

                        IconButton(
                            onClick = onOpenAdmin,
                            modifier = Modifier.testTag("home_admin_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AdminPanelSettings,
                                contentDescription = "Panel Admin",
                                tint = BrightViolet
                            )
                        }

                        IconButton(
                            onClick = onLogout,
                            modifier = Modifier.testTag("home_logout_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Logout,
                                contentDescription = "Logout",
                                tint = ErrorRed
                            )
                        }
                    }
                }
            }

            // RAW SKOR AKUMULASI CARD (Requested by user)
            item {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("raw_skor_akumulasi_card"),
                    borderColor = ElectricCyan.copy(alpha = 0.6f),
                    backgroundColor = SurfaceDark
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Kolom \"skor_akumulasi\" (Tabel member)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElectricCyan
                            )
                            if (isRefreshing) {
                                Text(
                                    text = "Mengambil data...",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = DarkCanvas,
                            border = BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = rawSkorAkumulasi,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Medium,
                                color = AcidLime,
                                modifier = Modifier
                                    .padding(10.dp)
                                    .fillMaxWidth()
                                    .testTag("raw_skor_json_text")
                            )
                        }

                        if (stats.scores.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Rincian ${stats.scores.size} Sesi Kuis (Array):",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextMuted
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val recentScores = stats.scores.takeLast(6)
                                recentScores.forEachIndexed { idx, sVal ->
                                    val sessionIdx = stats.scores.size - (recentScores.size - 1 - idx)
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = SurfaceDarkElevated,
                                        border = BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.35f))
                                    ) {
                                        Text(
                                            text = "S$sessionIdx: ${if (sVal % 1.0 == 0.0) sVal.toLong().toString() else String.format(Locale.US, "%.1f", sVal)}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextWhite,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // METRICS GRID: TOTAL, MEAN, MODUS, MEDIAN, HIGHEST, LOWEST, NUMSESI, RANK
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Statistik Akumulasi Query",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Row 1: Total & Mean & Modus
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ParameterBox(
                            label = "total",
                            subLabel = "Total Skor",
                            value = if (stats.total % 1.0 == 0.0) stats.total.toLong().toString() else String.format(Locale.US, "%.1f", stats.total),
                            color = ElectricCyan,
                            modifier = Modifier.weight(1f)
                        )
                        ParameterBox(
                            label = "mean",
                            subLabel = "Rata-rata",
                            value = String.format(Locale.US, "%.2f", stats.mean),
                            color = AcidLime,
                            modifier = Modifier.weight(1f)
                        )
                        ParameterBox(
                            label = "modus",
                            subLabel = "Modus Skor",
                            value = if (stats.modus % 1.0 == 0.0) stats.modus.toLong().toString() else String.format(Locale.US, "%.2f", stats.modus),
                            color = ElectricYellow,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Row 2: Median & Highest (Skor Tertinggi) & Lowest (Skor Terendah)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ParameterBox(
                            label = "median",
                            subLabel = "Median Skor",
                            value = if (stats.median % 1.0 == 0.0) stats.median.toLong().toString() else String.format(Locale.US, "%.2f", stats.median),
                            color = CyberPink,
                            modifier = Modifier.weight(1f)
                        )
                        ParameterBox(
                            label = "highest",
                            subLabel = "Skor Tertinggi",
                            value = if (stats.highest % 1.0 == 0.0) stats.highest.toLong().toString() else String.format(Locale.US, "%.1f", stats.highest),
                            color = SuccessGreen,
                            modifier = Modifier.weight(1f)
                        )
                        ParameterBox(
                            label = "lowest",
                            subLabel = "Skor Terendah",
                            value = if (stats.lowest % 1.0 == 0.0) stats.lowest.toLong().toString() else String.format(Locale.US, "%.1f", stats.lowest),
                            color = WarningOrange,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Row 3: Numsesi & Rank
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ParameterBox(
                            label = "numsesi",
                            subLabel = "Sesi Kuis",
                            value = stats.numsesi.toString(),
                            color = BrightViolet,
                            modifier = Modifier.weight(1f)
                        )
                        ParameterBox(
                            label = "rank",
                            subLabel = "Peringkat",
                            value = "#$displayRank / $totalMembers",
                            color = ElectricCyan,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // USER PROFILE CARD
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = SurfaceBorder,
                    backgroundColor = SurfaceDark
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Informasi Profil Siswa",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Email", color = TextMuted, fontSize = 12.sp)
                            Text(activeMember.email ?: "-", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("NIK", color = TextMuted, fontSize = 12.sp)
                            Text(activeMember.nik ?: "-", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Tanggal Lahir", color = TextMuted, fontSize = 12.sp)
                            Text(activeMember.tgl_lahir.ifBlank { "-" }, color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            // SECTION STEM QUIZ
            item {
                Text(
                    text = "Pilih Mata Pelajaran STEM",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(subjects.size) { index ->
                val subject = subjects[index]
                SubjectCard(
                    subject = subject,
                    isSelected = selectedSubject == subject.name,
                    onClick = { selectedSubject = subject.name }
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                NeonButton(
                    text = "Mulai Kuis $selectedSubject (10 Soal)",
                    onClick = { onStartQuiz(selectedSubject) },
                    icon = Icons.Filled.PlayArrow,
                    accentColor = when (selectedSubject) {
                        "Matematika" -> MathColor
                        "Fisika" -> PhysicsColor
                        else -> ChemistryColor
                    },
                    testTag = "start_quiz_button"
                )
            }
        }
    }
}

@Composable
private fun ParameterBox(
    label: String,
    subLabel: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.testTag("param_box_$label"),
        shape = RoundedCornerShape(12.dp),
        color = SurfaceDarkElevated,
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label.uppercase(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                color = color
            )
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextWhite,
                modifier = Modifier
                    .padding(vertical = 2.dp)
                    .testTag("param_value_$label")
            )
            Text(
                text = subLabel,
                fontSize = 10.sp,
                color = TextMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SubjectCard(
    subject: SubjectInfo,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag(subject.testTag),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) SurfaceDarkElevated else SurfaceDark,
        border = BorderStroke(
            if (isSelected) 2.dp else 1.dp,
            if (isSelected) subject.accentColor else SurfaceBorder
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(subject.gradientColors)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = subject.icon,
                    contentDescription = null,
                    tint = TextDark,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = subject.name,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) subject.accentColor else TextWhite
                )
                Text(
                    text = subject.description,
                    fontSize = 12.sp,
                    color = TextMuted,
                    maxLines = 2
                )
            }

            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = subject.accentColor,
                    unselectedColor = TextMuted
                )
            )
        }
    }
}

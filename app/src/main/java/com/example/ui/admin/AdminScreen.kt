package com.example.ui.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.QuestionItem
import com.example.ui.components.GlassCard
import com.example.ui.components.NeonButton
import com.example.ui.theme.*

@Composable
fun AdminScreen(
    adminViewModel: AdminViewModel,
    onBackToHome: () -> Unit
) {
    val uiState by adminViewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingQuestion by remember { mutableStateOf<QuestionItem?>(null) }

    val subjects = listOf("Matematika", "Fisika", "Kimia")
    val currentSuccess = uiState as? AdminUiState.Success
    val selectedSubject = currentSuccess?.subject ?: "Matematika"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkCanvas)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackToHome,
                    modifier = Modifier.testTag("admin_back_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Kembali ke Beranda",
                        tint = ElectricCyan
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Panel Admin Bank Soal",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextWhite,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = { adminViewModel.loadData() },
                    modifier = Modifier.testTag("admin_refresh_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Muat Ulang Soal",
                        tint = BrightViolet
                    )
                }
            }

            // Subject Tabs
            TabRow(
                selectedTabIndex = subjects.indexOf(selectedSubject).coerceAtLeast(0),
                containerColor = SurfaceDark,
                contentColor = ElectricCyan,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                subjects.forEach { subj ->
                    val isSelected = selectedSubject == subj
                    Tab(
                        selected = isSelected,
                        onClick = { adminViewModel.setSubject(subj) },
                        text = {
                            Text(
                                text = subj,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) ElectricCyan else TextMuted
                            )
                        }
                    )
                }
            }

            // Search Bar & Add Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = currentSuccess?.searchQuery ?: "",
                    onValueChange = { adminViewModel.setSearchQuery(it) },
                    placeholder = { Text("Cari kata kunci...", fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("admin_search_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = SurfaceBorder,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    )
                )

                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = BrightViolet),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .height(52.dp)
                        .testTag("admin_add_question_button")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, tint = TextWhite)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tambah", color = TextWhite, fontWeight = FontWeight.Bold)
                }
            }

            // Grade Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val currentGrade = currentSuccess?.selectedGradeFilter
                FilterChip(
                    selected = currentGrade == null,
                    onClick = { adminViewModel.setGradeFilter(null) },
                    label = { Text("Semua", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ElectricCyan,
                        selectedLabelColor = TextDark,
                        containerColor = SurfaceDarkElevated,
                        labelColor = TextWhite
                    )
                )
                listOf(7, 8, 9, 10, 11, 12).forEach { g ->
                    FilterChip(
                        selected = currentGrade == g,
                        onClick = { adminViewModel.setGradeFilter(g) },
                        label = { Text("K-$g", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricCyan,
                            selectedLabelColor = TextDark,
                            containerColor = SurfaceDarkElevated,
                            labelColor = TextWhite
                        )
                    )
                }
            }

            // Content List
            when (val state = uiState) {
                is AdminUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = ElectricCyan)
                    }
                }
                is AdminUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = state.message, color = ErrorRed, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = { adminViewModel.loadData() }) {
                                Text("Coba Lagi")
                            }
                        }
                    }
                }
                is AdminUiState.Success -> {
                    if (state.questions.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "Tidak ada soal yang sesuai.",
                                color = TextMuted,
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            items(state.questions) { q ->
                                QuestionAdminCard(
                                    question = q,
                                    onEdit = { editingQuestion = q },
                                    onDelete = {
                                        q.id?.let { id -> adminViewModel.deleteQuestion(id, q.pertanyaan) }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        QuestionFormDialog(
            title = "Tambah Soal Baru ($selectedSubject)",
            initial = QuestionItem(grade = 10, skor = 10),
            onDismiss = { showAddDialog = false },
            onConfirm = { newQ ->
                adminViewModel.addQuestion(newQ)
                showAddDialog = false
            }
        )
    }

    editingQuestion?.let { q ->
        QuestionFormDialog(
            title = "Edit Soal #${q.id}",
            initial = q,
            onDismiss = { editingQuestion = null },
            onConfirm = { updatedQ ->
                adminViewModel.updateQuestion(updatedQ)
                editingQuestion = null
            }
        )
    }
}

@Composable
private fun QuestionAdminCard(
    question: QuestionItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = SurfaceBorder,
        backgroundColor = SurfaceDark
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = SurfaceDarkElevated
                ) {
                    Text(
                        text = "ID: ${question.id ?: "-"} | Kelas: ${question.grade} | Skor: ${question.skor}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyan,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = AcidLime, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.Delete, contentDescription = "Hapus", tint = ErrorRed, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = question.pertanyaan,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextWhite
            )

            Spacer(modifier = Modifier.height(6.dp))

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("A. ${question.opsi_a}", fontSize = 12.sp, color = if (question.jawaban_benar.equals("A", true)) AcidLime else TextMuted)
                Text("B. ${question.opsi_b}", fontSize = 12.sp, color = if (question.jawaban_benar.equals("B", true)) AcidLime else TextMuted)
                Text("C. ${question.opsi_c}", fontSize = 12.sp, color = if (question.jawaban_benar.equals("C", true)) AcidLime else TextMuted)
                Text("D. ${question.opsi_d}", fontSize = 12.sp, color = if (question.jawaban_benar.equals("D", true)) AcidLime else TextMuted)
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Kunci: ${question.jawaban_benar}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = ElectricYellow
            )
            if (!question.pembahasan.isNullOrBlank()) {
                Text(
                    text = "Pembahasan: ${question.pembahasan}",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        }
    }
}

@Composable
private fun QuestionFormDialog(
    title: String,
    initial: QuestionItem,
    onDismiss: () -> Unit,
    onConfirm: (QuestionItem) -> Unit
) {
    var pertanyaan by remember { mutableStateOf(initial.pertanyaan) }
    var opsiA by remember { mutableStateOf(initial.opsi_a) }
    var opsiB by remember { mutableStateOf(initial.opsi_b) }
    var opsiC by remember { mutableStateOf(initial.opsi_c) }
    var opsiD by remember { mutableStateOf(initial.opsi_d) }
    var jawabanBenar by remember { mutableStateOf(initial.jawaban_benar.ifBlank { "A" }) }
    var pembahasan by remember { mutableStateOf(initial.pembahasan ?: "") }
    var gradeStr by remember { mutableStateOf(initial.grade.toString()) }
    var skorStr by remember { mutableStateOf(initial.skor.toString()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = BorderStroke(1.dp, ElectricCyan)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ElectricCyan)
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = pertanyaan,
                    onValueChange = { pertanyaan = it },
                    label = { Text("Teks Pertanyaan") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = opsiA,
                    onValueChange = { opsiA = it },
                    label = { Text("Opsi A") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = opsiB,
                    onValueChange = { opsiB = it },
                    label = { Text("Opsi B") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = opsiC,
                    onValueChange = { opsiC = it },
                    label = { Text("Opsi C") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = opsiD,
                    onValueChange = { opsiD = it },
                    label = { Text("Opsi D") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text("Kunci Jawaban:", fontSize = 12.sp, color = TextWhite)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("A", "B", "C", "D").forEach { opt ->
                        FilterChip(
                            selected = jawabanBenar.equals(opt, true),
                            onClick = { jawabanBenar = opt },
                            label = { Text(opt) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = pembahasan,
                    onValueChange = { pembahasan = it },
                    label = { Text("Pembahasan") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = gradeStr,
                        onValueChange = { gradeStr = it },
                        label = { Text("Kelas (Grade)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = skorStr,
                        onValueChange = { skorStr = it },
                        label = { Text("Bobot Skor") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Batal", color = TextMuted)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val grade = gradeStr.toIntOrNull() ?: 10
                            val skor = skorStr.toIntOrNull() ?: 10
                            val updated = initial.copy(
                                pertanyaan = pertanyaan,
                                opsi_a = opsiA,
                                opsi_b = opsiB,
                                opsi_c = opsiC,
                                opsi_d = opsiD,
                                jawaban_benar = jawabanBenar,
                                pembahasan = pembahasan,
                                grade = grade,
                                skor = skor
                            )
                            onConfirm(updated)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)
                    ) {
                        Text("Simpan", color = TextDark, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

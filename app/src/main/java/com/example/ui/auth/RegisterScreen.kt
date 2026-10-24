package com.example.ui.auth

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.components.NeonButton
import com.example.ui.theme.*
import java.util.Calendar

@Composable
fun RegisterScreen(
    authViewModel: AuthViewModel,
    onNavigateBackToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    val context = LocalContext.current
    var nama by remember { mutableStateOf("") }
    var tglLahir by remember { mutableStateOf("") }
    var nik by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var konfirmasiPassword by remember { mutableStateOf("") }
    var gradeStr by remember { mutableStateOf("10") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    val authState by authViewModel.authState.collectAsState()

    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val monthStr = (month + 1).toString().padStart(2, '0')
            val dayStr = dayOfMonth.toString().padStart(2, '0')
            tglLahir = "$year-$monthStr-$dayStr"
        },
        calendar.get(Calendar.YEAR) - 16,
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            onRegisterSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkCanvas)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 500.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Daftar Akun Baru",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = ElectricCyan,
                modifier = Modifier.testTag("register_title_text")
            )
            Text(
                text = "Lengkapi data diri untuk memulai kuis dan melacak skor akumulasi",
                fontSize = 13.sp,
                color = TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
            )

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = SurfaceBorder,
                backgroundColor = SurfaceDark
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    OutlinedTextField(
                        value = nama,
                        onValueChange = { nama = it },
                        label = { Text("Nama Lengkap") },
                        placeholder = { Text("Contoh: Budi Santoso") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("register_nama_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = SurfaceBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedLabelColor = ElectricCyan,
                            unfocusedLabelColor = TextMuted
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = tglLahir,
                        onValueChange = { tglLahir = it },
                        label = { Text("Tanggal Lahir (YYYY-MM-DD)") },
                        placeholder = { Text("2008-05-14") },
                        singleLine = true,
                        trailingIcon = {
                            IconButton(onClick = { datePickerDialog.show() }) {
                                Icon(
                                    imageVector = Icons.Filled.CalendarToday,
                                    contentDescription = "Pilih Tanggal",
                                    tint = ElectricCyan
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("register_tgl_lahir_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = SurfaceBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedLabelColor = ElectricCyan,
                            unfocusedLabelColor = TextMuted
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = nik,
                        onValueChange = { nik = it },
                        label = { Text("NIK (Nomor Induk Kependudukan)") },
                        placeholder = { Text("16 digit angka") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("register_nik_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = SurfaceBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedLabelColor = ElectricCyan,
                            unfocusedLabelColor = TextMuted
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        placeholder = { Text("email@gmail.com") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("register_email_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = SurfaceBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedLabelColor = ElectricCyan,
                            unfocusedLabelColor = TextMuted
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = "Toggle password",
                                    tint = TextMuted
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("register_password_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = SurfaceBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedLabelColor = ElectricCyan,
                            unfocusedLabelColor = TextMuted
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = konfirmasiPassword,
                        onValueChange = { konfirmasiPassword = it },
                        label = { Text("Konfirmasi Password") },
                        singleLine = true,
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(
                                    imageVector = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = "Toggle password",
                                    tint = TextMuted
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("register_confirm_password_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = SurfaceBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedLabelColor = ElectricCyan,
                            unfocusedLabelColor = TextMuted
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Tingkat Kelas (Grade):",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextWhite
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("7", "8", "9", "10", "11", "12").forEach { g ->
                            val selected = gradeStr == g
                            FilterChip(
                                selected = selected,
                                onClick = { gradeStr = g },
                                label = { Text("K-$g", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ElectricCyan,
                                    selectedLabelColor = TextDark,
                                    containerColor = SurfaceDarkElevated,
                                    labelColor = TextWhite
                                )
                            )
                        }
                    }

                    val currentError = (authState as? AuthState.Error)?.message
                    if (currentError != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = currentError,
                            color = ErrorRed,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_error_text")
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    val isLoading = authState is AuthState.Loading
                    NeonButton(
                        text = if (isLoading) "Mendaftarkan..." else "Daftar Sekarang",
                        onClick = {
                            authViewModel.register(
                                nama = nama,
                                tglLahir = tglLahir,
                                nik = nik,
                                email = email,
                                clearTextPass = password,
                                confirmPass = konfirmasiPassword,
                                gradeStr = gradeStr
                            )
                        },
                        enabled = !isLoading,
                        accentColor = ElectricCyan,
                        testTag = "register_submit_button"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    TextButton(
                        onClick = onNavigateBackToLogin,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("register_back_to_login_button")
                    ) {
                        Text(
                            text = "Sudah punya akun? Masuk",
                            color = BrightViolet,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

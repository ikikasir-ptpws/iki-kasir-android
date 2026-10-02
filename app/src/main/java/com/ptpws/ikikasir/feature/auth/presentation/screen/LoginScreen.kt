package com.ptpws.ikikasir.feature.auth.presentation.screen

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.text.TextStyle
import com.ptpws.ikikasir.R
import com.ptpws.ikikasir.commond.interfamily

import androidx.compose.material.icons.outlined.Lock

private enum class ForgotStep { INPUT, SUCCESS }

@Composable
fun LoginScreen(
    isLoading: Boolean = false,
    onLoginClick: (email: String, password: String) -> Unit = { _, _ -> },
    onSendPasswordReset: (email: String, onResult: (Boolean, String) -> Unit) -> Unit = { _, _ -> }
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passVisible by remember { mutableStateOf(false) }

    var showForgotDialog by remember { mutableStateOf(false) }
    var resetEmailInput by remember { mutableStateOf("") }
    var isResetLoading by remember { mutableStateOf(false) }
    var forgotStep by remember { mutableStateOf(ForgotStep.INPUT) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp).background(color = Color.White),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            item {
                Image(
                    painter = painterResource(R.drawable.logoikikasir),
                    contentDescription = "logo", modifier = Modifier.size(220.dp)
                )
                Spacer(modifier = Modifier.height(30.dp))
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 28.dp),
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        Text(
                            text = "Masuk ke Akun",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = interfamily,
                            color = Color(0xFF111827)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "EMAIL",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = interfamily,
                            letterSpacing = 0.8.sp,
                            color = Color(0xFF6B7280)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            shape = RoundedCornerShape(15.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F3F5)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                        ) {
                            BasicTextField(
                                value = email,
                                onValueChange = { email = it },
                                singleLine = true,
                                textStyle = TextStyle(color = Color.Black, fontSize = 12.sp),
                                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                                decorationBox = { inner ->
                                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.CenterStart) {
                                        if (email.isEmpty()) Text("Masukkan Email Anda", fontSize = 12.sp, color = Color(0x80474747))
                                        inner()
                                    }
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "KATA SANDI",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = interfamily,
                                letterSpacing = 0.8.sp,
                                color = Color(0xFF6B7280)
                            )
                            TextButton(
                                onClick = {
                                    val trimmed = email.trim()
                                    resetEmailInput = trimmed
                                    errorMessage = if (trimmed.isBlank()) {
                                        "Silakan masukkan alamat email Anda pada kolom Email terlebih dahulu."
                                    } else null
                                    forgotStep = ForgotStep.INPUT
                                    showForgotDialog = true
                                },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(
                                    text = "LUPA?",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = interfamily,
                                    letterSpacing = 0.8.sp,
                                    color = Color(0xFF4F46E5)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            shape = RoundedCornerShape(15.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F3F5)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                        ) {
                            BasicTextField(
                                value = password,
                                onValueChange = { password = it },
                                singleLine = true,
                                visualTransformation = if (passVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                textStyle = TextStyle(color = Color.Black, fontSize = 12.sp),
                                modifier = Modifier.fillMaxSize(),
                                decorationBox = { inner ->
                                    Row(
                                        Modifier.fillMaxSize().padding(start = 16.dp, end = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                                            if (password.isEmpty()) Text("Masukkan Kata Sandi", fontSize = 12.sp, color = Color(0x80474747))
                                            inner()
                                        }
                                        IconButton(onClick = { passVisible = !passVisible }) {
                                            Icon(
                                                imageVector = if (passVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                contentDescription = null,
                                                tint = Color(0xFF6B7280)
                                            )
                                        }
                                    }
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(28.dp))
                        Button(
                            onClick = { onLoginClick(email, password) },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4F46E5),
                                contentColor = Color.White,
                                disabledContainerColor = Color(0xFF9CA3AF),
                                disabledContentColor = Color(0xFFE5E7EB)
                            ),
                            enabled = !isLoading,
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 8.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Text(text = "MASUK", fontSize = 15.sp, fontWeight = FontWeight.Bold, fontFamily = interfamily, letterSpacing = 2.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
                Spacer(modifier = Modifier.height(48.dp))
            }
        }

        // ─── Dialog Lupa Kata Sandi — Multi-Step Professional ───
        if (showForgotDialog) {
            androidx.compose.ui.window.Dialog(
                onDismissRequest = { if (!isResetLoading) showForgotDialog = false }
            ) {
                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                ) {
                    AnimatedContent(
                        targetState = forgotStep,
                        transitionSpec = {
                            (slideInHorizontally { it } + fadeIn()) togetherWith
                                    (slideOutHorizontally { -it } + fadeOut())
                        },
                        label = "forgot_step"
                    ) { step ->
                        when (step) {

                            // ── STEP 1: Input Email (Read-Only / Lock Demi Keamanan) ──
                            ForgotStep.INPUT -> Column(
                                modifier = Modifier.padding(28.dp),
                                verticalArrangement = Arrangement.spacedBy(0.dp)
                            ) {
                                Box(
                                    modifier = Modifier.size(56.dp).clip(CircleShape).background(Color(0xFFEEF2FF)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Email,
                                        contentDescription = null,
                                        tint = Color(0xFF4F46E5),
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Lupa Kata Sandi?",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = interfamily,
                                    color = Color(0xFF111827)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Kami akan mengirimkan tautan reset kata sandi resmi dari Firebase/Google ke alamat email akun yang terdaftar di bawah ini.",
                                    fontSize = 13.sp,
                                    fontFamily = interfamily,
                                    color = Color(0xFF6B7280),
                                    lineHeight = 20.sp
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                OutlinedTextField(
                                    value = resetEmailInput,
                                    onValueChange = {},
                                    readOnly = true,
                                    enabled = false,
                                    label = { Text("Alamat Email Akun", fontFamily = interfamily, fontSize = 13.sp) },
                                    leadingIcon = {
                                        Icon(Icons.Default.Email, null, tint = Color(0xFF4F46E5), modifier = Modifier.size(18.dp))
                                    },
                                    trailingIcon = {
                                        Icon(Icons.Outlined.Lock, "Email Terkunci", tint = Color(0xFF6B7280), modifier = Modifier.size(18.dp))
                                    },
                                    singleLine = true,
                                    isError = errorMessage != null,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledBorderColor = Color(0xFFE5E7EB),
                                        disabledTextColor = Color(0xFF111827),
                                        disabledLabelColor = Color(0xFF4F46E5),
                                        disabledContainerColor = Color(0xFFF9FAFB),
                                        disabledLeadingIconColor = Color(0xFF4F46E5),
                                        disabledTrailingIconColor = Color(0xFF6B7280),
                                        focusedBorderColor = Color(0xFF4F46E5),
                                        unfocusedBorderColor = Color(0xFFE5E7EB),
                                        errorBorderColor = Color(0xFFDC2626)
                                    ),
                                    textStyle = TextStyle(fontFamily = interfamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                )
                                AnimatedVisibility(visible = errorMessage != null) {
                                    Column {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "⚠ ${errorMessage.orEmpty()}",
                                            fontSize = 12.sp,
                                            fontFamily = interfamily,
                                            color = Color(0xFFDC2626),
                                            lineHeight = 17.sp
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                                Button(
                                    onClick = {
                                        val trimmed = resetEmailInput.trim()
                                        if (trimmed.isBlank()) {
                                            errorMessage = "Silakan tutup dialog dan masukkan email akun pada layar login terlebih dahulu."
                                            return@Button
                                        }
                                        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(trimmed).matches()) {
                                            errorMessage = "Format email pada layar login tidak valid."
                                            return@Button
                                        }
                                        isResetLoading = true
                                        errorMessage = null
                                        onSendPasswordReset(trimmed) { success, message ->
                                            isResetLoading = false
                                            if (success) {
                                                forgotStep = ForgotStep.SUCCESS
                                            } else {
                                                errorMessage = message
                                            }
                                        }
                                    },
                                    enabled = !isResetLoading && resetEmailInput.isNotBlank(),
                                    modifier = Modifier.fillMaxWidth().height(50.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF4F46E5),
                                        disabledContainerColor = Color(0xFFE5E7EB)
                                    )
                                ) {
                                    if (isResetLoading) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                                            Text("Mengirim...", fontFamily = interfamily, color = Color.White, fontSize = 14.sp)
                                        }
                                    } else {
                                        Text("Kirim Tautan Reset", fontFamily = interfamily, fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 15.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                TextButton(
                                    onClick = { showForgotDialog = false },
                                    enabled = !isResetLoading,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Batal", fontFamily = interfamily, color = Color(0xFF6B7280), fontSize = 14.sp)
                                }
                            }

                            // ── STEP 2: Sukses ───────────────────────────────────
                            ForgotStep.SUCCESS -> Column(
                                modifier = Modifier.padding(28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier.size(72.dp).clip(CircleShape).background(Color(0xFFDCFCE7)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF16A34A),
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Email Instruksi Terkirim!",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = interfamily,
                                    color = Color(0xFF111827),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Tautan reset kata sandi telah dikirim ke:",
                                    fontSize = 12.sp,
                                    fontFamily = interfamily,
                                    color = Color(0xFF6B7280),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = resetEmailInput,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = interfamily,
                                    color = Color(0xFF4F46E5),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9FF))
                                ) {
                                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        ForgotInfoRow("📧", "Buka aplikasi Gmail dari akun: $resetEmailInput")
                                        ForgotInfoRow("🔍", "Cek folder Spam / Sampah / Promosi jika tidak ada di Utama.")
                                        ForgotInfoRow("⏱", "Email biasanya masuk dalam 1–3 menit.")
                                        ForgotInfoRow("🛡️", "Dikirim oleh pengirim resmi Firebase (noreply@...).")
                                    }
                                }
                                Spacer(modifier = Modifier.height(20.dp))
                                Button(
                                    onClick = { showForgotDialog = false },
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
                                ) {
                                    Text("Oke, Saya Paham", fontFamily = interfamily, fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 15.sp)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ForgotInfoRow(emoji: String, text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
        Text(emoji, fontSize = 13.sp)
        Text(text = text, fontSize = 12.sp, fontFamily = interfamily, color = Color(0xFF0369A1), lineHeight = 17.sp)
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    MaterialTheme {
        LoginScreen()
    }
}

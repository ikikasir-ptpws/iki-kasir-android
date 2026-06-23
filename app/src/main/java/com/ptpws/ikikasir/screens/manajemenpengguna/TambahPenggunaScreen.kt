package com.ptpws.ikikasir.screens.manajemenpengguna

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.PointOfSale
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAddAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ptpws.ikikasir.commond.interfamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TambahPenggunaScreen(
    onBack: () -> Unit = {},
    onSimpanPengguna: () -> Unit = {}
) {
    var namaLengkap by remember { mutableStateOf("") }
    var alamatEmail by remember { mutableStateOf("") }
    var peranDipilih by remember { mutableStateOf("Kasir") }
    var kataSandi by remember { mutableStateOf("") }
    var kataSandiTerlihat by remember { mutableStateOf(false) }
    var statusAkunAktif by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color(0xFFF3F4F6),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Tambah Pengguna",
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = interfamily,
                        fontSize = 20.sp, color = Color.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color(0xFF4F46E5)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF3F4F6),
                    titleContentColor = Color(0xFF111827),
                    navigationIconContentColor = Color(0xFF4F46E5)
                )
            )
        },

        ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Upload Foto Profil
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box {
                        Box(
                            modifier = Modifier
                                .size(84.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF3F4F6))
                                .border(1.5.dp, Color(0xFFD1D5DB), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonAddAlt,
                                contentDescription = "Unggah Foto Profil",
                                tint = Color(0xFF9CA3AF),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4F46E5))
                                .border(2.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Ubah Foto",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Text(
                        text = "Unggah Foto Profil",
                        fontFamily = interfamily,
                        fontSize = 13.sp,
                        color = Color(0xFF6B7280)
                    )
                }
            }

            // ── Card Form
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {

                        // Nama Lengkap
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Nama Lengkap",
                                fontFamily = interfamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF374151)
                            )
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = Color(0xFF9CA3AF),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    BasicTextField(
                                        value = namaLengkap,
                                        onValueChange = { namaLengkap = it },
                                        singleLine = true,
                                        textStyle = TextStyle(
                                            color = Color.Black,
                                            fontSize = 14.sp,
                                            fontFamily = interfamily
                                        ),
                                        modifier = Modifier.fillMaxWidth(),
                                        decorationBox = { innerTextField ->
                                            if (namaLengkap.isEmpty()) {
                                                Text(
                                                    text = "Contoh: Budi Santoso",
                                                    fontSize = 14.sp,
                                                    fontFamily = interfamily,
                                                    color = Color(0xFF9CA3AF)
                                                )
                                            }
                                            innerTextField()
                                        }
                                    )
                                }
                            }
                        }

                        // Alamat Email
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Alamat Email",
                                fontFamily = interfamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF374151)
                            )
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Email,
                                        contentDescription = null,
                                        tint = Color(0xFF9CA3AF),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    BasicTextField(
                                        value = alamatEmail,
                                        onValueChange = { alamatEmail = it },
                                        singleLine = true,
                                        textStyle = TextStyle(
                                            color = Color.Black,
                                            fontSize = 14.sp,
                                            fontFamily = interfamily
                                        ),
                                        modifier = Modifier.fillMaxWidth(),
                                        decorationBox = { innerTextField ->
                                            if (alamatEmail.isEmpty()) {
                                                Text(
                                                    text = "email@domain.com",
                                                    fontSize = 14.sp,
                                                    fontFamily = interfamily,
                                                    color = Color(0xFF9CA3AF)
                                                )
                                            }
                                            innerTextField()
                                        }
                                    )
                                }
                            }
                        }

                        // Peran (Role)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Peran (Role)",
                                fontFamily = interfamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF374151)
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {

                                // Owner
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (peranDipilih == "Owner") Color(0xFFEEF2FF) else Color(0xFFF9FAFB))
                                        .border(
                                            width = if (peranDipilih == "Owner") 1.5.dp else 1.dp,
                                            color = if (peranDipilih == "Owner") Color(0xFF4F46E5) else Color(0xFFE5E7EB),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable { peranDipilih = "Owner" }
                                        .padding(vertical = 12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.AdminPanelSettings,
                                        contentDescription = "Owner",
                                        tint = if (peranDipilih == "Owner") Color(0xFF4F46E5) else Color(0xFF6B7280),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text(
                                        text = "Owner",
                                        fontFamily = interfamily,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (peranDipilih == "Owner") Color(0xFF4F46E5) else Color(0xFF374151)
                                    )
                                }

                                // Kasir
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (peranDipilih == "Kasir") Color(0xFFEEF2FF) else Color(0xFFF9FAFB))
                                        .border(
                                            width = if (peranDipilih == "Kasir") 1.5.dp else 1.dp,
                                            color = if (peranDipilih == "Kasir") Color(0xFF4F46E5) else Color(0xFFE5E7EB),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable { peranDipilih = "Kasir" }
                                        .padding(vertical = 12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.PointOfSale,
                                        contentDescription = "Kasir",
                                        tint = if (peranDipilih == "Kasir") Color(0xFF4F46E5) else Color(0xFF6B7280),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text(
                                        text = "Kasir",
                                        fontFamily = interfamily,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (peranDipilih == "Kasir") Color(0xFF4F46E5) else Color(0xFF374151)
                                    )
                                }

                                // Gudang
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (peranDipilih == "Gudang") Color(0xFFEEF2FF) else Color(0xFFF9FAFB))
                                        .border(
                                            width = if (peranDipilih == "Gudang") 1.5.dp else 1.dp,
                                            color = if (peranDipilih == "Gudang") Color(0xFF4F46E5) else Color(0xFFE5E7EB),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable { peranDipilih = "Gudang" }
                                        .padding(vertical = 12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Inventory2,
                                        contentDescription = "Gudang",
                                        tint = if (peranDipilih == "Gudang") Color(0xFF4F46E5) else Color(0xFF6B7280),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text(
                                        text = "Gudang",
                                        fontFamily = interfamily,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (peranDipilih == "Gudang") Color(0xFF4F46E5) else Color(0xFF374151)
                                    )
                                }
                            }
                        }

                        // Kata Sandi
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Kata Sandi",
                                fontFamily = interfamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF374151)
                            )
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = Color(0xFF9CA3AF),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    BasicTextField(
                                        value = kataSandi,
                                        onValueChange = { kataSandi = it },
                                        singleLine = true,
                                        textStyle = TextStyle(
                                            color = Color.Black,
                                            fontSize = 14.sp,
                                            fontFamily = interfamily
                                        ),
                                        visualTransformation = if (kataSandiTerlihat) VisualTransformation.None else PasswordVisualTransformation(),
                                        modifier = Modifier.weight(1f),
                                        decorationBox = { innerTextField ->
                                            if (kataSandi.isEmpty()) {
                                                Text(
                                                    text = "Min. 8 karakter",
                                                    fontSize = 14.sp,
                                                    fontFamily = interfamily,
                                                    color = Color(0xFF9CA3AF)
                                                )
                                            }
                                            innerTextField()
                                        }
                                    )
                                    Icon(
                                        imageVector = if (kataSandiTerlihat) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Tampilkan Kata Sandi",
                                        tint = Color(0xFF9CA3AF),
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clickable { kataSandiTerlihat = !kataSandiTerlihat }
                                    )
                                }
                            }
                        }

                        // Status Akun
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF9FAFB))
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Status Akun",
                                    fontFamily = interfamily,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF111827)
                                )
                                Text(
                                    text = "Aktifkan untuk memberikan akses segera",
                                    fontFamily = interfamily,
                                    fontSize = 12.sp,
                                    color = Color(0xFF6B7280)
                                )
                            }
                            Switch(
                                checked = statusAkunAktif,
                                onCheckedChange = { statusAkunAktif = it },
                                colors = SwitchDefaults.colors(
                                    checkedTrackColor = Color(0xFF4F46E5),
                                    checkedThumbColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // ── Info Verifikasi
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 28.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "ⓘ",
                        fontFamily = interfamily,
                        fontSize = 13.sp,
                        color = Color(0xFF9CA3AF)
                    )
                    Text(
                        text = "Pastikan alamat email yang didaftarkan aktif untuk keperluan verifikasi dan pemulihan kata sandi di masa mendatang.",
                        fontFamily = interfamily,
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280)
                    )
                }
            }

            // ── Tombol Simpan Pengguna
            item {
                Button(
                    onClick = onSimpanPengguna,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 102.dp)
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4F46E5)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Simpan Pengguna",
                        fontFamily = interfamily,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// ── Preview

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TambahPenggunaScreenPreview() {
    MaterialTheme {
        TambahPenggunaScreen()
    }
}
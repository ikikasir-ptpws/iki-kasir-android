package com.ptpws.ikikasir.screens.kategori

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ptpws.ikikasir.commond.interfamily
import com.ptpws.ikikasir.feature.kategori.presentation.util.KategoriIconHelper
import com.ptpws.ikikasir.feature.kategori.presentation.viewmodel.TambahKategoriViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TambahKategoriScreen(
    onBack: () -> Unit = {},
    onSimpanKategori: () -> Unit = {},
    viewModel: TambahKategoriViewModel = hiltViewModel()
) {
    val formState by viewModel.formState.collectAsState()
    val context = LocalContext.current
    var showIconPicker by remember { mutableStateOf(false) }

    val selectedIconOption = remember(formState.iconName) {
        KategoriIconHelper.getIconOption(formState.iconName)
    }

    LaunchedEffect(formState.isSuccess) {
        if (formState.isSuccess) {
            Toast.makeText(context, "Kategori berhasil disimpan", Toast.LENGTH_SHORT).show()
            viewModel.resetSuccess()
            onSimpanKategori()
        }
    }

    LaunchedEffect(formState.errorMessage) {
        formState.errorMessage?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        containerColor = Color(0xFFF3F4F6),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (formState.isEditMode) "Edit Kategori" else "Tambah Kategori Baru",
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = interfamily,
                        fontSize = 20.sp,
                        color = Color.Black
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
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            item {
                // Card Utama: Icon Kategori + Form
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {

                        // Icon Kategori + Badge Edit
                        Box(
                            modifier = Modifier
                                .padding(bottom = 4.dp)
                                .clickable { showIconPicker = true }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(selectedIconOption.bgColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = selectedIconOption.icon,
                                    contentDescription = selectedIconOption.label,
                                    tint = selectedIconOption.tintColor,
                                    modifier = Modifier.size(36.dp)
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
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Ubah Icon",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        Text(
                            text = "Klik untuk memilih icon",
                            fontFamily = interfamily,
                            fontSize = 12.sp,
                            color = Color(0xFF6B7280)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Input Nama Kategori
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Nama Kategori *",
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
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                border = BorderStroke(
                                    1.dp,
                                    if (formState.errorMessage != null && formState.nama.isBlank())
                                        Color(0xFFEF4444)
                                    else Color(0xFFE5E7EB)
                                )
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 14.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    BasicTextField(
                                        value = formState.nama,
                                        onValueChange = { viewModel.onNamaChange(it) },
                                        singleLine = true,
                                        textStyle = TextStyle(
                                            color = Color.Black,
                                            fontSize = 14.sp,
                                            fontFamily = interfamily
                                        ),
                                        modifier = Modifier.fillMaxWidth(),
                                        decorationBox = { innerTextField ->
                                            if (formState.nama.isEmpty()) {
                                                Text(
                                                    text = "Contoh: Minuman Dingin",
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

                        Spacer(modifier = Modifier.height(14.dp))

                        // Input Deskripsi Kategori
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Deskripsi Kategori (Opsional)",
                                fontFamily = interfamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF374151)
                            )
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(90.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 14.dp, vertical = 12.dp)
                                ) {
                                    BasicTextField(
                                        value = formState.deskripsi,
                                        onValueChange = { viewModel.onDeskripsiChange(it) },
                                        textStyle = TextStyle(
                                            color = Color.Black,
                                            fontSize = 14.sp,
                                            fontFamily = interfamily
                                        ),
                                        modifier = Modifier.fillMaxSize(),
                                        decorationBox = { innerTextField ->
                                            if (formState.deskripsi.isEmpty()) {
                                                Text(
                                                    text = "Berikan penjelasan singkat mengenai kategori ini...",
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
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Tip Kategori
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = Color(0xFFC0C1FF),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEEF2FF)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Tip",
                            tint = Color(0xFF4F46E5),
                            modifier = Modifier.size(18.dp)
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Tip Kategori",
                                fontFamily = interfamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF111827)
                            )
                            Text(
                                text = "Gunakan nama kategori yang singkat dan mudah diingat oleh kasir untuk mempercepat proses transaksi.",
                                fontFamily = interfamily,
                                fontSize = 12.sp,
                                color = Color(0xFF4F46E5)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Tombol Simpan
                Button(
                    onClick = { viewModel.simpanKategori() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = !formState.isLoading,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4F46E5),
                        disabledContainerColor = Color(0xFFA5B4FC)
                    )
                ) {
                    if (formState.isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Menyimpan...",
                            fontFamily = interfamily,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (formState.isEditMode) "Perbarui Kategori" else "Simpan Kategori",
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

    // Modal Dialog Pemilihan Icon Kategori
    if (showIconPicker) {
        AlertDialog(
            onDismissRequest = { showIconPicker = false },
            title = {
                Text(
                    text = "Pilih Icon Kategori",
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = interfamily,
                    fontSize = 18.sp
                )
            },
            text = {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(KategoriIconHelper.availableIcons) { option ->
                        val isSelected = option.name == formState.iconName
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) Color(0xFFEEF2FF) else Color(0xFFF9FAFB))
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) Color(0xFF4F46E5) else Color(0xFFE5E7EB),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    viewModel.onIconChange(option.name, option.colorHex)
                                    showIconPicker = false
                                }
                                .padding(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(option.bgColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = option.icon,
                                    contentDescription = option.label,
                                    tint = option.tintColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = option.label,
                                fontSize = 11.sp,
                                fontFamily = interfamily,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) Color(0xFF4F46E5) else Color(0xFF374151)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showIconPicker = false }) {
                    Text("Tutup", color = Color(0xFF4F46E5))
                }
            }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TambahKategoriScreenPreview() {
    MaterialTheme {
        TambahKategoriScreen()
    }
}

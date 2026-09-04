package com.ptpws.ikikasir.screens.kategori

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ptpws.ikikasir.R
import com.ptpws.ikikasir.commond.interfamily
import com.ptpws.ikikasir.feature.kategori.domain.model.Kategori
import com.ptpws.ikikasir.feature.kategori.presentation.util.KategoriIconHelper
import com.ptpws.ikikasir.feature.kategori.presentation.viewmodel.KategoriViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DaftarKategoriScreen(
    navController: NavController,
    onTambahPromo: () -> Unit = {},
    viewModel: KategoriViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(state.userMessage) {
        state.userMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearUserMessage()
        }
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearUserMessage()
        }
    }

    Scaffold(
        containerColor = Color(0xFFF3F4F6),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Kategori Produk",
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = interfamily,
                            color = Color.Black,
                            fontSize = 20.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (navController.currentDestination?.route == "kategori_produk") {
                            navController.popBackStack()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                },
                actions = {
                    // Network & Sync Indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (state.isOnline) Color(0xFFDCFCE7) else Color(0xFFFEF3C7),
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (state.isOnline) Icons.Default.CloudDone else Icons.Default.CloudOff,
                                    contentDescription = if (state.isOnline) "Online" else "Offline",
                                    tint = if (state.isOnline) Color(0xFF16A34A) else Color(0xFFD97706),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = if (state.isOnline) "Online" else "Offline",
                                    fontSize = 11.sp,
                                    fontFamily = interfamily,
                                    fontWeight = FontWeight.Medium,
                                    color = if (state.isOnline) Color(0xFF16A34A) else Color(0xFFD97706)
                                )
                            }
                        }

                        IconButton(
                            onClick = { viewModel.syncData() },
                            enabled = !state.isSyncing
                        ) {
                            if (state.isSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = Color(0xFF4F46E5)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = "Sinkronisasi",
                                    tint = Color(0xFF4F46E5)
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF3F4F6),
                    titleContentColor = Color(0xFF111827),
                    navigationIconContentColor = Color(0xFF4F46E5)
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onTambahPromo,
                containerColor = Color(0xFF4F46E5),
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Tambah Kategori"
                )
            }
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 80.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Search Bar
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(15.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF2F3F5)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    BasicTextField(
                        value = state.searchQuery,
                        onValueChange = { viewModel.onSearchQueryChange(it) },
                        singleLine = true,
                        textStyle = TextStyle(
                            color = Color.Black,
                            fontSize = 13.sp,
                            fontFamily = interfamily
                        ),
                        modifier = Modifier.fillMaxSize(),
                        decorationBox = { innerTextField ->
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(start = 16.dp, end = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Search,
                                    contentDescription = "Cari",
                                    tint = Color(0x80474747)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Box(
                                    modifier = Modifier.weight(1f),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (state.searchQuery.isEmpty()) {
                                        Text(
                                            text = "Cari Kategori...",
                                            fontFamily = interfamily,
                                            fontSize = 13.sp,
                                            color = Color(0x80474747)
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        }
                    )
                }
            }

            // Loading state
            if (state.isLoading && state.kategoriList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF4F46E5))
                    }
                }
            }

            // Empty state
            if (!state.isLoading && state.filteredList.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (state.searchQuery.isNotEmpty())
                                "Kategori \"${state.searchQuery}\" tidak ditemukan"
                            else
                                "Belum ada kategori",
                            fontFamily = interfamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = Color(0xFF374151)
                        )
                        Text(
                            text = if (state.searchQuery.isNotEmpty())
                                "Coba cari dengan kata kunci lain"
                            else
                                "Ketuk tombol + di bawah untuk menambahkan kategori baru",
                            fontFamily = interfamily,
                            fontSize = 13.sp,
                            color = Color(0xFF6B7280),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Dynamic Category Cards
            items(
                items = state.filteredList,
                key = { it.id }
            ) { item ->
                val iconOption = KategoriIconHelper.getIconOption(item.iconName)

                KategoriCardItem(
                    kategori = item,
                    icon = iconOption.icon,
                    iconBgColor = iconOption.bgColor,
                    iconTintColor = iconOption.tintColor,
                    onEdit = {
                        val intent = Intent(context, TambahKategoriActivity::class.java).apply {
                            putExtra("kategoriId", item.id)
                            putExtra("kategoriNama", item.nama)
                            putExtra("kategoriDeskripsi", item.deskripsi)
                            putExtra("kategoriIcon", item.iconName)
                            putExtra("kategoriColor", item.colorHex)
                        }
                        context.startActivity(intent)
                    },
                    onDelete = {
                        viewModel.requestDeleteKategori(item)
                    }
                )
            }
        }
    }

    // Confirmation Delete Dialog
    state.kategoriToDelete?.let { kategori ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteDialog() },
            title = {
                Text(
                    text = "Hapus Kategori",
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = interfamily
                )
            },
            text = {
                Text(
                    text = "Apakah Anda yakin ingin menghapus kategori \"${kategori.nama}\"?",
                    fontFamily = interfamily,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.deleteKategori(kategori.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Hapus", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDeleteDialog() }) {
                    Text("Batal", color = Color(0xFF6B7280))
                }
            }
        )
    }
}

// Kategori Card Item
@Composable
fun KategoriCardItem(
    kategori: Kategori,
    icon: ImageVector,
    iconBgColor: Color,
    iconTintColor: Color,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 6.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Icon Kategori
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = iconBgColor,
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = kategori.nama,
                    tint = iconTintColor,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Nama & Deskripsi / Jumlah Produk
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = kategori.nama,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = interfamily,
                        fontSize = 15.sp,
                        color = Color(0xFF111827)
                    )

                    // Offline pending sync tag
                    if (!kategori.isSynced) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFFEF3C7)
                        ) {
                            Text(
                                text = "Pending",
                                fontSize = 10.sp,
                                fontFamily = interfamily,
                                color = Color(0xFFD97706),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Text(
                    text = if (kategori.deskripsi.isNotBlank())
                        kategori.deskripsi
                    else
                        "${kategori.jumlahProduk} Produk",
                    fontSize = 12.sp,
                    fontFamily = interfamily,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF6B7280),
                    maxLines = 1
                )
            }

            // Tombol Edit & Hapus
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = Color(0xFF4F46E5),
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Hapus",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DaftarKategoriScreenPreview() {
    MaterialTheme {
        DaftarKategoriScreen(navController = rememberNavController())
    }
}
package com.ptpws.ikikasir.screens.manajemenpengguna

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ptpws.ikikasir.commond.interfamily
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ManajemenPenggunaScreen(
    navController: NavController,
    onBack: () -> Unit = {},
    onTambahUser: () -> Unit = {}
) {
    val context = LocalContext.current
    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        containerColor = Color(0xFFF3F4F6),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Manajemen Pengguna",
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = interfamily,
                        color = Color(0xFF111827),
                        fontSize = 16.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (navController.currentDestination?.route == "pengguna") {
                            navController.popBackStack()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color(0xFF4F46E5)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF3F4F6),
                    titleContentColor = Color(0xFF111827)
                )
            )
        },
        floatingActionButton = {
            if (pagerState.currentPage == 0) {
                FloatingActionButton(
                    onClick = {
                        context.startActivity(Intent(context, TambahPenggunaActivity::class.java))
                    },
                    containerColor = Color(0xFF4F46E5),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    elevation = FloatingActionButtonDefaults.elevation(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah User")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header section: big title + subtitle
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 16.dp)
            ) {
                Text(
                    text = "Manajemen pengguna",
                    fontWeight = FontWeight.Bold,
                    fontFamily = interfamily,
                    color = Color(0xFF111827),
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Kelola pengguna dan role secara realtime",
                    fontFamily = interfamily,
                    color = Color(0xFF6B7280),
                    fontSize = 13.sp
                )
            }

            // Custom pill/toggle tab buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE5E7EB))
                    .padding(4.dp)
            ) {
                listOf("DAFTAR USER", "ROLE & IZIN").forEachIndexed { index, title ->
                    val selected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (selected) Color(0xFF4F46E5) else Color.Transparent
                            )
                            .clickable {
                                coroutineScope.launch { pagerState.animateScrollToPage(index) }
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            fontFamily = interfamily,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp,
                            color = if (selected) Color.White else Color(0xFF6B7280)
                        )
                    }
                }
            }

            // Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> DaftarUserScreen(
                        onEditUser = { userId ->
                            val intent = Intent(context, TambahPenggunaActivity::class.java).apply {
                                putExtra("USER_ID", userId)
                            }
                            context.startActivity(intent)
                        }
                    )
                    1 -> RoleIzinScreen(
                        onAddRoleClick = {
                            context.startActivity(Intent(context, TambahRoleActivity::class.java))
                        },
                        onEditRoleClick = { role ->
                            val intent = Intent(context, TambahRoleActivity::class.java).apply {
                                putExtra("ROLE_ID", role.id)
                            }
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}
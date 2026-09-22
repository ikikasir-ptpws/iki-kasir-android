package com.ptpws.ikikasir.screens.manajemenpengguna

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.ptpws.ikikasir.R
import com.ptpws.ikikasir.commond.interfamily
import com.ptpws.ikikasir.feature.manajemenpengguna.presentation.viewmodel.UserViewModel

@Composable
fun DaftarUserScreen(
    viewModel: UserViewModel = hiltViewModel(),
    onEditUser: (String) -> Unit = {}
) {
    val state by viewModel.listState.collectAsState()

    val users = state.filteredUsers
    val totalPengguna = state.users.size
    val penggunaAktif = state.users.count { it.isActive }
    val penggunaNonAktif = state.users.count { !it.isActive }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
        ) {
            // Search bar & Sync button
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = { viewModel.onSearchQueryChange(it) },
                        placeholder = {
                            Text(
                                "Cari nama atau role...",
                                fontFamily = interfamily,
                                color = Color(0xFF6B7280),
                                fontSize = 14.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search, contentDescription = null,
                                tint = Color(0xFF6B7280), modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            Icon(
                                Icons.Default.Tune, contentDescription = "Filter",
                                tint = Color(0xFF4F46E5), modifier = Modifier.size(20.dp)
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFFE5E7EB),
                            focusedBorderColor = Color(0xFF4F46E5),
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                    )

                    IconButton(
                        onClick = { viewModel.syncData() },
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                    ) {
                        if (state.isSyncing) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color(0xFF4F46E5), strokeWidth = 2.dp)
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Sinkronisasi",
                                tint = Color(0xFF4F46E5),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            if (state.isLoading && users.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF4F46E5))
                    }
                }
            } else if (users.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Belum ada data pengguna",
                            fontFamily = interfamily,
                            color = Color(0xFF6B7280),
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                // User cards
                items(users.size) { index ->
                    val user = users[index]
                    val inisial = user.inisial
                    val role = user.roleDisplayName
                    val aktif = user.isActive

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 2.dp
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            var imageLoadFailed by remember(user.photoUrl) { mutableStateOf(false) }

                            // Avatar circular
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (inisial.firstOrNull()?.toString()?.uppercase()) {
                                            "A" -> Color(0xFFE7E3FF)
                                            "B" -> Color(0xFFE7E3FF)
                                            "R" -> Color(0xFFFFE6DC)
                                            else -> Color(0xFFE5E7EB)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (user.photoUrl.isNotBlank() && !imageLoadFailed) {
                                    AsyncImage(
                                        model = user.photoUrl,
                                        contentDescription = user.fullName,
                                        contentScale = ContentScale.Crop,
                                        onError = { imageLoadFailed = true },
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Text(
                                        text = inisial,
                                        fontFamily = interfamily,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (inisial.firstOrNull()?.toString()?.uppercase()) {
                                            "R" -> Color(0xFF92400E)
                                            else -> Color(0xFF4F46E5)
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            // Nama dan Email
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = user.fullName,
                                        fontFamily = interfamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp,
                                        color = Color(0xFF111827),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                Color(0xFFE5E7EB),
                                                RoundedCornerShape(6.dp)
                                            )
                                            .padding(horizontal = 7.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = role,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF4B5563)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = user.email,
                                    fontFamily = interfamily,
                                    fontSize = 12.sp,
                                    color = Color(0xFF6B7280)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (aktif) Color(0xFFDDF7E5) else Color(0xFFF3F4F6),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 10.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = if (aktif) "AKTIF" else "NONAKTIF",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = interfamily,
                                        color = if (aktif) Color(0xFF16A34A) else Color(0xFF6B7280)
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    IconButton(
                                        onClick = { onEditUser(user.id) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.iconedit),
                                            contentDescription = "Edit User",
                                            tint = Color(0xFF4338CA),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.deleteUser(user.id) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.iconhapus),
                                            contentDescription = "Hapus User",
                                            tint = Color(0xFFEF4444),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Summary card biru
            item {
                Spacer(Modifier.height(4.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF4F46E5)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "TOTAL PENGGUNA",
                                fontFamily = interfamily,
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "$totalPengguna",
                                fontFamily = interfamily,
                                color = Color.White,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Icon(
                            painter = painterResource(R.drawable.pengguna),
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.45f),
                            modifier = Modifier.size(56.dp)
                        )
                    }
                }
            }

            // Mini stat cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    listOf(
                        "PENGGUNA AKTIF" to "$penggunaAktif",
                        "PENGGUNA NONAKTIF" to "$penggunaNonAktif"
                    ).forEach { (label, value) ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = label, fontFamily = interfamily,
                                    color = Color(0xFF6B7280), fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium, letterSpacing = 0.3.sp
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = value, fontFamily = interfamily,
                                    fontWeight = FontWeight.Bold, color = Color(0xFF111827), fontSize = 20.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
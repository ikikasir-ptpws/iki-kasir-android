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
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ptpws.ikikasir.R
import com.ptpws.ikikasir.commond.interfamily

@Composable
fun DaftarUserScreen() {
    var query by remember { mutableStateOf("") }

    // Data pengguna langsung di sini
    val users = remember {
        listOf(
            Triple("Ahmad Subardjo",  "ahmad@umkm.id",  Triple("A", "ADMIN",  true)),
            Triple("Siti Aminah",     "siti.a@umkm.id", Triple("S", "KASIR",  false)),
            Triple("Budi Santoso",    "budi.s@umkm.id", Triple("B", "GUDANG", true)),
            Triple("Rina Wijaya",     "rina.w@umkm.id", Triple("R", "KASIR",  true)),
        )
    }
    // Triple struktur: (nama, email, (inisial, role, aktif))

    val filtered = users.filter {
        query.isBlank() ||
                it.first.contains(query, ignoreCase = true) ||
                it.third.second.contains(query, ignoreCase = true)
    }

    val totalPengguna    = users.size
    val penggunaAktif    = users.count { it.third.third }
    val penggunaNonAktif = users.count { !it.third.third }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
    )
    {
        // Search bar
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = {
                    Text(
                        "Cari nama atau role...",
                        fontFamily = interfamily,
                        color = Color(0xFF6B7280),
                        fontSize = 14.sp
                    )
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null,
                        tint = Color(0xFF6B7280), modifier = Modifier.size(20.dp))
                },
                trailingIcon = {
                    Icon(Icons.Default.FilterList, contentDescription = "Filter",
                        tint = Color(0xFF4F46E5), modifier = Modifier.size(20.dp))
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
                    .fillMaxWidth()
                    .height(52.dp)
            )
        }

        // User cards
        items(filtered.size) { index ->
            val (nama, email, detail) = filtered[index]
            val (inisial, role, aktif) = detail

            val roleBg   = when (role) { "ADMIN" -> Color(0xFFE1E2E4); "KASIR" -> Color(0xFFE1E2E4); else -> Color(0xFFE1E2E4) }
            val roleText = when (role) { "ADMIN" -> Color(0xFF464555); "KASIR" -> Color(0xFF464555); else -> Color(0xFF464555) }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 3.dp
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                when (inisial) {
                                    "A", "B" -> Color(0xFFE7E3FF)
                                    "R" -> Color(0xFFFFE6DC)
                                    else -> Color(0xFFE5E7EB)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = inisial,
                            fontFamily = interfamily,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (inisial) {
                                "R" -> Color(0xFF92400E)
                                else -> Color(0xFF4F46E5)
                            }
                        )
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
                                text = nama,
                                fontFamily = interfamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color(0xFF111827),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )

                            Box(
                                modifier = Modifier
                                    .background(
                                        Color(0xFFE5E7EB),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .padding(
                                        horizontal = 8.dp,
                                    )
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
                            text = email,
                            fontFamily = interfamily,
                            fontSize = 13.sp,
                            color = Color(0xFF6B7280)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(
                        horizontalAlignment = Alignment.End
                    ) {

                        Box(
                            modifier = Modifier
                                .background(
                                    if (aktif)
                                        Color(0xFFDDF7E5)
                                    else
                                        Color(0xFFF3F4F6),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(
                                    horizontal = 10.dp,
                                )
                        ) {
                            Text(
                                text = if (aktif) "AKTIF" else "NONAKTIF",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = interfamily,
                                color = if (aktif)
                                    Color(0xFF16A34A)
                                else
                                    Color(0xFF6B7280)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row {

                            IconButton(
                                onClick = {},
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.iconedit),
                                    contentDescription = null,
                                    tint = Color(0xFF4338CA),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(Modifier.width(12.dp))

                            IconButton(
                                onClick = {},
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.iconhapus),
                                    contentDescription = null,
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(18.dp)
                                )
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
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color.White.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Group, contentDescription = null,
                            tint = Color.White, modifier = Modifier.size(28.dp))
                    }
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
                    "PENGGUNA AKTIF"    to "$penggunaAktif/20",
                    "PENGGUNA NONAKTIF" to "$penggunaNonAktif/20"
                ).forEach { (label, value) ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(text = label, fontFamily = interfamily,
                                color = Color(0xFF6B7280), fontSize = 10.sp,
                                fontWeight = FontWeight.Medium, letterSpacing = 0.3.sp)
                            Spacer(Modifier.height(4.dp))
                            Text(text = value, fontFamily = interfamily,
                                fontWeight = FontWeight.Bold, color = Color(0xFF111827), fontSize = 20.sp)
                        }
                    }
                }
            }
        }
    }
}
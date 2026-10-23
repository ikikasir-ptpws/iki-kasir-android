package com.ptpws.ikikasir.screens.manajemenpengguna

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.ptpws.ikikasir.R
import com.ptpws.ikikasir.commond.GlobalScrollPaginationFooter
import com.ptpws.ikikasir.commond.GlobalTextField
import com.ptpws.ikikasir.commond.interfamily
import com.ptpws.ikikasir.feature.manajemenpengguna.presentation.viewmodel.UserViewModel
import com.ptpws.ikikasir.feature.role.presentation.viewmodel.RoleViewModel

// Opsi filter yang tersedia untuk halaman Daftar Pengguna
data class UserFilterItem(
    val label: String,
    val icon: ImageVector,
    val roleValue: String = "",
    val statusValue: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DaftarUserScreen(
    viewModel: UserViewModel = hiltViewModel(),
    roleViewModel: RoleViewModel = hiltViewModel(),
    onEditUser: (String) -> Unit = {}
) {
    val state by viewModel.listState.collectAsState()
    val roleState by roleViewModel.listState.collectAsState()
    var showFilterDropdown by remember { mutableStateOf(false) }
    val userFilterOptions = buildList {
        add(UserFilterItem("Semua Pengguna", Icons.Default.People))
        add(UserFilterItem("Aktif", Icons.Default.CheckCircle, statusValue = "aktif"))
        add(UserFilterItem("Nonaktif", Icons.Default.PersonOff, statusValue = "nonaktif"))
        roleState.roles
            .asSequence()
            .map { it.name.trim() }
            .filter { it.isNotEmpty() }
            .distinctBy { it.lowercase() }
            .forEach { roleName ->
                add(UserFilterItem(roleName, Icons.Default.Category, roleValue = roleName))
            }
    }

    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.syncData()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val users = state.filteredUsers
    val isFilterActive = state.filterRole.isNotBlank() || state.filterStatus.isNotBlank()
    val pageSize = 10
    var displayedCount by remember(state.searchQuery, state.filterRole, state.filterStatus) {
        mutableIntStateOf(pageSize)
    }
    val displayedUsers = remember(users, displayedCount) {
        users.take(displayedCount)
    }

    val totalPengguna = state.users.size
    val penggunaAktif = state.users.count { it.isActive }
    val penggunaNonAktif = state.users.count { !it.isActive }

    var userToDelete by remember { mutableStateOf<com.ptpws.ikikasir.feature.manajemenpengguna.domain.model.User?>(null) }

    if (userToDelete != null) {
        AlertDialog(
            onDismissRequest = { userToDelete = null },
            title = {
                Text(
                    text = "Hapus Pengguna",
                    fontWeight = FontWeight.Bold,
                    fontFamily = interfamily,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "Apakah Anda yakin ingin menghapus akun \"${userToDelete?.fullName}\"? Data akun ini akan dihapus dari sistem.",
                    fontFamily = interfamily,
                    fontSize = 14.sp,
                    color = Color(0xFF4B5563)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        userToDelete?.let { viewModel.deleteUser(it.id) }
                        userToDelete = null
                    }
                ) {
                    Text(
                        text = "Hapus",
                        color = Color(0xFFEF4444),
                        fontWeight = FontWeight.Bold,
                        fontFamily = interfamily
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { userToDelete = null }) {
                    Text(
                        text = "Batal",
                        color = Color(0xFF6B7280),
                        fontFamily = interfamily
                    )
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
        ) {
            // Search bar and filter
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        GlobalTextField(
                            value = state.searchQuery,
                            onValueChange = { viewModel.onSearchQueryChange(it) },
                            placeholder = "Cari nama, email, atau role...",
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search, contentDescription = null,
                                    tint = Color(0xFF6B7280), modifier = Modifier.size(18.dp)
                                )
                            },
                            trailingIcon = if (state.searchQuery.isNotEmpty()) {
                                {
                                    IconButton(
                                        onClick = { viewModel.onSearchQueryChange("") },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Hapus",
                                            tint = Color(0xFF6B7280),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            } else null,
                            modifier = Modifier.weight(1f)
                        )

                        // Tombol filter
                        Box {
                            Surface(
                                onClick = { showFilterDropdown = !showFilterDropdown },
                                shape = RoundedCornerShape(16.dp),
                                color = if (isFilterActive) Color(0xFFEEF2FF) else Color.White,
                                border = BorderStroke(
                                    1.dp,
                                    if (isFilterActive) Color(0xFF4F46E5) else Color(0xFFE2E8F0)
                                ),
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = "Filter Pengguna",
                                        tint = if (isFilterActive) Color(0xFF4F46E5) else Color(0xFF334155),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            // Dropdown Filter List
                            DropdownMenu(
                                expanded = showFilterDropdown,
                                onDismissRequest = { showFilterDropdown = false },
                                modifier = Modifier.background(Color.White, RoundedCornerShape(16.dp))
                            ) {
                                userFilterOptions.forEachIndexed { index, option ->
                                    if (index == 3 && userFilterOptions.size > 3) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(vertical = 4.dp),
                                            color = Color(0xFFE5E7EB)
                                        )
                                    }
                                    val isSelected = when {
                                        option.roleValue.isBlank() && option.statusValue.isBlank() ->
                                            !isFilterActive
                                        option.roleValue.isNotBlank() ->
                                            state.filterRole.equals(option.roleValue, ignoreCase = true) && state.filterStatus.isBlank()
                                        option.statusValue.isNotBlank() ->
                                            state.filterStatus.equals(option.statusValue, ignoreCase = true) && state.filterRole.isBlank()
                                        else -> false
                                    }

                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = option.label,
                                                fontFamily = interfamily,
                                                fontSize = 13.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) Color(0xFF4F46E5) else Color(0xFF1E293B)
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = option.icon,
                                                contentDescription = null,
                                                tint = if (isSelected) Color(0xFF4F46E5) else Color(0xFF64748B),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        },
                                        trailingIcon = if (isSelected) {
                                            {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Terpilih",
                                                    tint = Color(0xFF4F46E5),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        } else null,
                                        onClick = {
                                            if (option.roleValue.isBlank() && option.statusValue.isBlank()) {
                                                viewModel.clearFilters()
                                            } else {
                                                viewModel.onFilterRoleChange(option.roleValue)
                                                viewModel.onFilterStatusChange(option.statusValue)
                                            }
                                            showFilterDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                    }

                    // Chip filter aktif (jika ada filter yang dipilih)
                    if (isFilterActive) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (state.filterRole.isNotBlank()) {
                                ActiveFilterChip(
                                    label = "Role: ${state.filterRole.replaceFirstChar { it.uppercase() }}",
                                    onRemove = { viewModel.onFilterRoleChange("") }
                                )
                            }
                            if (state.filterStatus.isNotBlank()) {
                                ActiveFilterChip(
                                    label = "Status: ${state.filterStatus.replaceFirstChar { it.uppercase() }}",
                                    onRemove = { viewModel.onFilterStatusChange("") }
                                )
                            }
                            TextButton(
                                onClick = { viewModel.clearFilters() },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                            ) {
                                Text(
                                    text = "Hapus Filter",
                                    fontSize = 11.sp,
                                    color = Color(0xFFEF4444),
                                    fontFamily = interfamily
                                )
                            }
                        }
                    }
                }
            }

            if (state.isLoading && users.isEmpty()) {
                items(6) {
                    com.ptpws.ikikasir.commond.UserCardShimmer()
                    Spacer(modifier = Modifier.height(10.dp))
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
                            text = if (isFilterActive || state.searchQuery.isNotBlank())
                                "Tidak ada pengguna yang sesuai filter"
                            else
                                "Belum ada data pengguna",
                            fontFamily = interfamily,
                            color = Color(0xFF6B7280),
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                // User cards
                items(displayedUsers.size) { index ->
                    val user = displayedUsers[index]
                    val inisial = user.inisial
                    val role = user.roleDisplayName
                    val aktif = user.isActive

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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

                            Column(modifier = Modifier.weight(1f)) {
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
                                            .background(Color(0xFFE5E7EB), RoundedCornerShape(6.dp))
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

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFEEF2FF)) {
                                        IconButton(
                                            onClick = { onEditUser(user.id) },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Edit,
                                                contentDescription = "Edit User",
                                                tint = Color(0xFF4F46E5),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFFFF1F2)) {
                                        IconButton(
                                            onClick = { userToDelete = user },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Delete,
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
            }

            // Scroll Pagination Footer
            if (!state.isLoading && users.isNotEmpty()) {
                item(key = "scroll_pagination_footer") {
                    GlobalScrollPaginationFooter(
                        displayedCount = displayedCount,
                        totalItems = users.size,
                        onLoadMore = {
                            displayedCount = minOf(displayedCount + pageSize, users.size)
                        },
                        itemName = "pengguna"
                    )
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

@Composable
private fun ActiveFilterChip(label: String, onRemove: () -> Unit) {
    Box(
        modifier = Modifier
            .background(Color(0xFFEEF2FF), RoundedCornerShape(20.dp))
            .padding(start = 10.dp, end = 4.dp, top = 4.dp, bottom = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontFamily = interfamily,
                color = Color(0xFF4F46E5),
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.width(4.dp))
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF4F46E5))
                    .clickable { onRemove() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Hapus filter",
                    tint = Color.White,
                    modifier = Modifier.size(10.dp)
                )
            }
        }
    }
}
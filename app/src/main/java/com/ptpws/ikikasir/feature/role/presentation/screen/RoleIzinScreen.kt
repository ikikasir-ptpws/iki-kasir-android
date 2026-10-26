package com.ptpws.ikikasir.feature.role.presentation.screen

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.ManageAccounts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ptpws.ikikasir.commond.GlobalScrollPaginationFooter
import com.ptpws.ikikasir.commond.interfamily
import com.ptpws.ikikasir.feature.role.domain.model.Role
import com.ptpws.ikikasir.feature.role.presentation.viewmodel.RoleViewModel

@Composable
fun RoleIzinScreen(
    viewModel: RoleViewModel = hiltViewModel(),
    onAddRoleClick: () -> Unit = {},
    onEditRoleClick: (Role) -> Unit = {},
    onEditPermissionsClick: (Role) -> Unit = {}
) {
    val state by viewModel.listState.collectAsState()
    val roles = state.roles

    // Scroll Pagination State (Tampilkan 10 data awal, muat 10 data berikutnya saat scroll)
    val pageSize = 10
    var displayedCount by remember { mutableIntStateOf(pageSize) }
    val displayedRoles = remember(roles, displayedCount) {
        roles.take(displayedCount)
    }

    var roleToDelete by remember { mutableStateOf<Role?>(null) }

    // Dialog Konfirmasi Hapus Role
    if (roleToDelete != null) {
        AlertDialog(
            onDismissRequest = { roleToDelete = null },
            title = {
                Text(
                    text = "Hapus Role",
                    fontWeight = FontWeight.Bold,
                    fontFamily = interfamily,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "Apakah Anda yakin ingin menghapus role \"${roleToDelete?.name}\"? Role ini dan hak aksesnya akan dihapus dari sistem.",
                    fontFamily = interfamily,
                    fontSize = 14.sp,
                    color = Color(0xFF4B5563)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        roleToDelete?.let { viewModel.deleteRole(it.id) }
                        roleToDelete = null
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
                TextButton(
                    onClick = { roleToDelete = null }
                ) {
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

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (state.isLoading && roles.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                repeat(5) {
                    com.ptpws.ikikasir.commond.RoleCardShimmer()
                }
            }
        } else if (roles.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Belum ada data role",
                    fontFamily = interfamily,
                    color = Color(0xFF6B7280),
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 12.dp,
                    bottom = 80.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Role items (Scroll pagination: 10 data awal + 10 data saat scroll)
                items(displayedRoles.size) { index ->
                    val role = displayedRoles[index]
                    RoleCard(
                        role = role,
                        onEdit = { onEditRoleClick(role) },
                        onShowPermissions = { onEditPermissionsClick(role) },
                        onDelete = { roleToDelete = role }
                    )
                }

                // Scroll Pagination Footer (CircularProgressIndicator saat discroll)
                if (roles.isNotEmpty()) {
                    item(key = "scroll_pagination_footer") {
                        GlobalScrollPaginationFooter(
                            displayedCount = displayedCount,
                            totalItems = roles.size,
                            onLoadMore = {
                                displayedCount = minOf(displayedCount + pageSize, roles.size)
                            },
                            itemName = "role"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RoleCard(
    role: Role,
    onEdit: () -> Unit = {},
    onShowPermissions: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon role
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color(0xFF4F46E5).copy(alpha = 0.08f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.AdminPanelSettings,
                    contentDescription = null,
                    tint = Color(0xFF4F46E5),
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(Modifier.width(10.dp))

            // Nama dan jumlah pengguna
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = role.name,
                    fontFamily = interfamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color(0xFF111827),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.ManageAccounts,
                        contentDescription = null,
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(
                        text = "${role.userCount} Pengguna",
                        fontFamily = interfamily,
                        fontSize = 12.sp,
                        color = Color(0xFF9CA3AF)
                    )
                }
            }

            Spacer(Modifier.width(6.dp))

            // Status badge
            Box(
                modifier = Modifier
                    .background(
                        if (role.isActive) Color(0xFFDDF7E5) else Color(0xFFF3F4F6),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (role.isActive) "AKTIF" else "NONAKTIF",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = interfamily,
                    color = if (role.isActive) Color(0xFF16A34A) else Color(0xFF6B7280)
                )
            }

            Spacer(Modifier.width(4.dp))

            // Action icons (Edit, Izin/Key, Hapus)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Edit
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Edit Role",
                        tint = Color(0xFF4338CA),
                        modifier = Modifier.size(25.dp)
                    )
                }
                // Izin/Permissions
                IconButton(
                    onClick = onShowPermissions,
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Key,
                        contentDescription = "Izin Role",
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(25.dp)
                    )
                }
                // Hapus
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Hapus Role",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(25.dp)
                    )
                }
            }
        }
    }
}
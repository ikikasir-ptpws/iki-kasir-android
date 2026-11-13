package com.ptpws.ikikasir.feature.manajemenpengguna.presentation.viewmodel

import com.ptpws.ikikasir.feature.manajemenpengguna.presentation.state.UserListState
import kotlinx.coroutines.flow.StateFlow

/**
 * Kontrak state & aksi untuk Daftar Pengguna.
 * Dipisah dari ViewModel supaya screen bisa di-preview tanpa Hilt/Firebase/Room.
 */
interface UserListUiProvider {
    val listState: StateFlow<UserListState>
    fun onSearchQueryChange(query: String)
    fun onFilterRoleChange(role: String)
    fun onFilterStatusChange(status: String)
    fun clearFilters()
    fun deleteUser(id: String)
    fun syncData()
}

/**
 * Adapter Hilt: meneruskan kontrak di atas ke UserViewModel asli.
 */
class HiltUserListUiProvider(private val viewModel: UserViewModel) : UserListUiProvider {
    override val listState: StateFlow<UserListState> get() = viewModel.listState
    override fun onSearchQueryChange(query: String) = viewModel.onSearchQueryChange(query)
    override fun onFilterRoleChange(role: String) = viewModel.onFilterRoleChange(role)
    override fun onFilterStatusChange(status: String) = viewModel.onFilterStatusChange(status)
    override fun clearFilters() = viewModel.clearFilters()
    override fun deleteUser(id: String) = viewModel.deleteUser(id)
    override fun syncData() = viewModel.syncData()
}
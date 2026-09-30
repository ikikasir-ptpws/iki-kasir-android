package com.ptpws.ikikasir.commond

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

enum class CrudResultType {
    SUCCESS,
    FAILURE
}

data class CrudResultDialogState(
    val id: String,
    val type: CrudResultType,
    val message: String
)

object GlobalCrudResultDialog {
    private val _state = MutableStateFlow<CrudResultDialogState?>(null)
    val state = _state.asStateFlow()

    fun success(message: String) {
        _state.value = CrudResultDialogState(UUID.randomUUID().toString(), CrudResultType.SUCCESS, message)
    }

    fun failure(message: String) {
        _state.value = CrudResultDialogState(UUID.randomUUID().toString(), CrudResultType.FAILURE, message)
    }

    fun dismiss() {
        _state.value = null
    }
}

@Composable
fun GlobalCrudResultDialogHost(showSuccess: Boolean = true) {
    val dialog by GlobalCrudResultDialog.state.collectAsState()
    val current = dialog ?: return
    if (!showSuccess && current.type == CrudResultType.SUCCESS) return

    val isSuccess = current.type == CrudResultType.SUCCESS
    val accentColor = if (isSuccess) Color(0xFF059669) else Color(0xFFDC2626)

    AlertDialog(
        onDismissRequest = GlobalCrudResultDialog::dismiss,
        title = {
            Text(
                text = if (isSuccess) "Berhasil" else "Gagal",
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
        },
        text = {
            Text(
                text = current.message,
                fontSize = 14.sp,
                color = Color(0xFF374151)
            )
        },
        confirmButton = {
            Button(
                onClick = GlobalCrudResultDialog::dismiss,
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Text("Tutup", color = Color.White)
            }
        },
        containerColor = Color.White,
        titleContentColor = MaterialTheme.colorScheme.onSurface
    )
}

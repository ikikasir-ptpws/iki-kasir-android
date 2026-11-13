package com.ptpws.ikikasir.commond

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BarcodeMismatchDialog(
    message: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = Color(0xFFDC2626),
                modifier = Modifier.size(44.dp)
            )
        },
        title = {
            Text(
                text = "Kode Tidak Cocok",
                fontFamily = interfamily,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        text = {
            Text(
                text = message,
                fontFamily = interfamily,
                fontSize = 14.sp,
                color = Color(0xFF475569),
                lineHeight = 20.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Mengerti",
                    fontFamily = interfamily,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp)
    )
}

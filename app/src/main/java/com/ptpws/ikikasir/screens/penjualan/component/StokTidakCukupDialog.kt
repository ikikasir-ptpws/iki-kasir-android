package com.ptpws.ikikasir.screens.penjualan.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ptpws.ikikasir.commond.interfamily

@Composable
fun StokTidakCukupDialog(
    namaProduk: String,
    stokTersedia: Int,
    onGunakanStok: () -> Unit,
    onUbahJumlah: () -> Unit,
    onDismiss: () -> Unit = onUbahJumlah
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Surface(
                modifier = Modifier.padding(top = 4.dp),
                shape = CircleShape,
                color = Color(0xFFFFF7ED)
            ) {
                Icon(
                    imageVector = Icons.Default.Inventory2,
                    contentDescription = null,
                    tint = Color(0xFFEA580C),
                    modifier = Modifier.padding(12.dp)
                )
            }
        },
        title = {
            Text(
                text = "Stok tidak mencukupi",
                fontFamily = interfamily,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = namaProduk,
                    fontFamily = interfamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF334155),
                    textAlign = TextAlign.Center,
                    // Responsif: nama produk panjang dibatasi 2 baris agar tombol tidak terdorong
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFF7ED)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Stok tersedia",
                            fontFamily = interfamily,
                            fontSize = 13.sp,
                            color = Color(0xFF7C2D12),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "$stokTersedia unit",
                            fontFamily = interfamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEA580C),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Jumlah pesanan melebihi stok yang tersedia. Gunakan jumlah stok maksimal atau ubah jumlah secara manual.",
                    fontFamily = interfamily,
                    fontSize = 13.sp,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center,
                    lineHeight = 19.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onGunakanStok,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B32D1))
            ) {
                Text(
                    text = "Gunakan Stok",
                    fontFamily = interfamily,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onUbahJumlah) {
                Text(
                    text = "Ubah Jumlah",
                    fontFamily = interfamily,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF475569)
                )
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(22.dp)
    )
}

// ---- PREVIEW (aktifkan satu per satu untuk tes) ----
// import androidx.compose.ui.tooling.preview.Preview
// import com.ptpws.ikikasir.ui.theme.IKIKASIRTheme
//
// @Preview(name = "HP kecil 320x568", device = "spec:width=320dp,height=568dp", showBackground = true)
// @Preview(name = "HP standar 360x800", device = "spec:width=360dp,height=800dp", showBackground = true)
// @Preview(name = "HP besar 411x891", device = "spec:width=411dp,height=891dp", showBackground = true)
// @Preview(name = "Font besar", device = "spec:width=360dp,height=800dp", fontScale = 1.3f, showBackground = true)
// @Composable
// private fun StokTidakCukupDialogResponsivePreview() {
//     IKIKASIRTheme {
//         StokTidakCukupDialog(
//             namaProduk = "Indomie Goreng Spesial",
//             stokTersedia = 5,
//             onGunakanStok = {},
//             onUbahJumlah = {}
//         )
//     }
// }

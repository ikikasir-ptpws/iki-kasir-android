package com.ptpws.ikikasir.screens.penjualan.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ptpws.ikikasir.R
import com.ptpws.ikikasir.commond.interfamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetPembayaran(
    onDismiss: () -> Unit,
    onProsesBayar: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        var selectedMethod by remember { mutableStateOf("CASH") }
        var namaPelanggan by remember { mutableStateOf("") }
        var promo by remember { mutableStateOf("") }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Title
            item {
                Text(
                    text = "METODE PEMBAYARAN",
                    fontSize = 10.sp,
                    fontFamily = interfamily,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6B7280),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    letterSpacing = 1.sp
                )
            }

            // Payment Methods
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PaymentMethodItem(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Money,
                        label = "CASH",
                        isSelected = selectedMethod == "CASH",
                        onClick = { selectedMethod = "CASH" }
                    )
                    PaymentMethodItem(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.QrCodeScanner,
                        label = "QRIS",
                        isSelected = selectedMethod == "QRIS",
                        onClick = { selectedMethod = "QRIS" }
                    )
                    PaymentMethodItem(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.AccountBalance,
                        label = "TRF",
                        isSelected = selectedMethod == "TRF",
                        onClick = { selectedMethod = "TRF" }
                    )
                    PaymentMethodItem(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Receipt,
                        label = "HUTANG",
                        isSelected = selectedMethod == "HUTANG",
                        onClick = { selectedMethod = "HUTANG" }
                    )
                }
            }

            // Total Pembayaran
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Total Pembayaran",
                        fontSize = 12.sp,
                        fontFamily = interfamily,
                        color = Color(0xFF6B7280)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Rp 15.000",
                            fontSize = 20.sp,
                            fontFamily = interfamily,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827)
                        )
                        Icon(
                            imageVector = Icons.Default.MoreHoriz,
                            contentDescription = "Detail",
                            tint = Color(0xFF4F46E5),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Inputs
            item {
                OutlinedTextField(
                    value = namaPelanggan,
                    onValueChange = { namaPelanggan = it },
                    placeholder = { Text("Nama Pelanggan", fontSize = 12.sp, fontFamily = interfamily) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF4F46E5),
                        unfocusedBorderColor = Color(0xFFE5E7EB),
                    ),
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, fontFamily = interfamily)
                )
            }

            item {
                OutlinedTextField(
                    value = promo,
                    onValueChange = { promo = it },
                    placeholder = { Text("Pilih Promo", fontSize = 12.sp, fontFamily = interfamily) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color(0xFF6B7280)) },
                    readOnly = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF4F46E5),
                        unfocusedBorderColor = Color(0xFFE5E7EB),
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, fontFamily = interfamily)
                )
            }

            // Order Item Summary
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F4F6)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.kopi), // dummy image
                            contentDescription = "Roti Bakar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFE5E7EB))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Roti Bakar Cokelat",
                                fontSize = 13.sp,
                                fontFamily = interfamily,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111827)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Jumlah: 1x",
                                fontSize = 11.sp,
                                fontFamily = interfamily,
                                color = Color(0xFF6B7280)
                            )
                        }
                        Text(
                            text = "Rp 15.000",
                            fontSize = 13.sp,
                            fontFamily = interfamily,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4F46E5)
                        )
                    }
                }
            }

            // Bayar Button
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onProsesBayar,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
                ) {
                    Text(
                        text = "BAYAR",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = interfamily,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PaymentMethodItem(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF4F46E5) else Color(0xFFE5E7EB)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) Color.White else Color(0xFF6B7280),
                modifier = Modifier.size(30.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontFamily = interfamily,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else Color(0xFF6B7280)
            )
        }
    }
}

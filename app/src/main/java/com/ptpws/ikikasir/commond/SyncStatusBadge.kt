package com.ptpws.ikikasir.commond

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SyncStatusBadge(
    label: String = "Pending",
    modifier: Modifier = Modifier
) {
    Text(
        text = label,
        fontSize = 9.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = interfamily,
        color = Color(0xFFB45309),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
            .background(Color(0xFFFFF7ED), RoundedCornerShape(6.dp))
            .padding(horizontal = 7.dp, vertical = 4.dp)
    )
}

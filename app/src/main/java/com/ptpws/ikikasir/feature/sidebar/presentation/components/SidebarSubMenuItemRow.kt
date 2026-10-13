package com.ptpws.ikikasir.feature.sidebar.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ptpws.ikikasir.commond.interfamily
import com.ptpws.ikikasir.feature.sidebar.domain.model.SidebarSubMenuItem

private val PrimaryRoyalBlue = Color(0xFF3B32D1)
private val SelectedBg = Color(0xFFEEF2FF)
private val TextDark = Color(0xFF1E293B)
private val TextMuted = Color(0xFF64748B)

/**
 * Komponen baris untuk sub-menu pada sidebar tablet (diberi indentasi).
 */
@Composable
fun SidebarSubMenuItemRow(
    subItem: SidebarSubMenuItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 4.dp, top = 2.dp, bottom = 2.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        color = if (isSelected) SelectedBg else Color.Transparent,
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Bullet Indicator atau Icon sub-menu
            if (subItem.iconRes != null) {
                Icon(
                    painter = painterResource(id = subItem.iconRes),
                    contentDescription = subItem.title,
                    tint = if (isSelected) PrimaryRoyalBlue else TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            } else if (subItem.iconVector != null) {
                Icon(
                    imageVector = subItem.iconVector,
                    contentDescription = subItem.title,
                    tint = if (isSelected) PrimaryRoyalBlue else TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) PrimaryRoyalBlue else Color(0xFFCBD5E1))
                )
            }

            Text(
                text = subItem.title,
                fontFamily = interfamily,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) PrimaryRoyalBlue else TextDark,
                modifier = Modifier.weight(1f)
            )

            // Active pill tick jika terpilih
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(PrimaryRoyalBlue)
                )
            }
        }
    }
}

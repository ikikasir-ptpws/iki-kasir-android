package com.ptpws.ikikasir.feature.sidebar.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ptpws.ikikasir.commond.interfamily
import com.ptpws.ikikasir.feature.sidebar.domain.model.SidebarMenuItem

private val PrimaryRoyalBlue = Color(0xFF3B32D1)
private val SelectedBg = Color(0xFFEEF2FF)
private val ActiveChildBg = Color(0xFFF8FAFC)
private val TextDark = Color(0xFF1E293B)
private val TextMuted = Color(0xFF64748B)

/**
 * Komponen baris untuk item menu utama pada sidebar tablet.
 */
@Composable
fun SidebarMenuItemRow(
    item: SidebarMenuItem,
    isExpanded: Boolean,
    isChildActive: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val chevronRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 250),
        label = "chevronRotation"
    )

    val backgroundColor = when {
        isSelected -> SelectedBg
        isChildActive -> ActiveChildBg
        else -> Color.Transparent
    }

    val contentColor = when {
        isSelected -> PrimaryRoyalBlue
        isChildActive -> PrimaryRoyalBlue
        else -> TextDark
    }

    val iconTintColor = when {
        isSelected -> PrimaryRoyalBlue
        isChildActive -> PrimaryRoyalBlue
        else -> TextMuted
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = backgroundColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Icon (bisa Drawable resource atau ImageVector)
                if (item.iconRes != null) {
                    Icon(
                        painter = painterResource(id = item.iconRes),
                        contentDescription = item.title,
                        tint = iconTintColor,
                        modifier = Modifier.size(20.dp)
                    )
                } else if (item.iconVector != null) {
                    Icon(
                        imageVector = item.iconVector,
                        contentDescription = item.title,
                        tint = iconTintColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = item.title,
                    fontFamily = interfamily,
                    fontSize = 13.5.sp,
                    fontWeight = if (isSelected || isChildActive) FontWeight.Bold else FontWeight.Medium,
                    color = contentColor
                )
            }

            // Chevron panah jika menu memiliki sub-items (accordion)
            if (item.isExpandable) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Tutup sub-menu" else "Buka sub-menu",
                    tint = if (isExpanded || isChildActive) PrimaryRoyalBlue else TextMuted,
                    modifier = Modifier
                        .size(18.dp)
                        .rotate(chevronRotation)
                )
            }
        }
    }
}

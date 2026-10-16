package com.ptpws.ikikasir.commond

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.LastPage
import androidx.compose.material.icons.filled.FirstPage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.max
import kotlin.math.min

/**
 * Komponen Indikator Loading Scroll Pagination (Infinite Scroll) dengan CircularProgressIndicator.
 * Ketika item ini masuk ke dalam viewport/layar saat discroll, otomatis memanggil onLoadMore()
 * setelah delay singkat untuk memberikan efek visual yang mulus dan profesional.
 */
@Composable
fun ScrollPaginationLoadingItem(
    modifier: Modifier = Modifier,
    onLoadMore: () -> Unit,
    displayedCount: Int? = null,
    totalItems: Int? = null,
    itemName: String = "data",
    loadDelayMs: Long = 400L
) {
    LaunchedEffect(displayedCount) {
        delay(loadDelayMs)
        onLoadMore()
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = Color(0xFF4F46E5),
                    strokeWidth = 2.4.dp
                )
                Text(
                    text = if (displayedCount != null && totalItems != null) {
                        "Memuat $itemName berikutnya... ($displayedCount/$totalItems)"
                    } else {
                        "Memuat data..."
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = interfamily,
                    color = Color(0xFF475569)
                )
            }
        }
    }
}

/**
 * Indikator akhir daftar ketika seluruh data telah selesai dimuat.
 */
@Composable
fun ScrollPaginationEndOfListItem(
    modifier: Modifier = Modifier,
    totalItems: Int,
    itemName: String = "data"
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(32.dp)
                    .height(1.dp)
                    .background(Color(0xFFCBD5E1))
            )
            Text(
                text = "Semua $totalItems $itemName telah dimuat",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = interfamily,
                color = Color(0xFF94A3B8)
            )
            Box(
                modifier = Modifier
                    .width(32.dp)
                    .height(1.dp)
                    .background(Color(0xFFCBD5E1))
            )
        }
    }
}

/**
 * Komponen Global Footer Scroll Pagination yang otomatis menampilkan CircularProgressIndicator
 * saat pengguna melakukan scroll ke bawah untuk memuat data berikutnya, dan menampilkan indikator
 * selesai jika semua data sudah dimuat.
 */
@Composable
fun GlobalScrollPaginationFooter(
    displayedCount: Int,
    totalItems: Int,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
    itemName: String = "data"
) {
    if (displayedCount < totalItems) {
        ScrollPaginationLoadingItem(
            displayedCount = displayedCount,
            totalItems = totalItems,
            itemName = itemName,
            onLoadMore = onLoadMore,
            modifier = modifier
        )
    } else if (totalItems > 10) {
        ScrollPaginationEndOfListItem(
            totalItems = totalItems,
            itemName = itemName,
            modifier = modifier
        )
    }
}

/**
 * Extension helper to paginate any List in Kotlin.
 *
 * @param page 1-based page index
 * @param pageSize Number of items per page (default 10)
 */
fun <T> List<T>.paginate(page: Int, pageSize: Int = 10): List<T> {
    if (isEmpty()) return emptyList()
    val safePage = page.coerceAtLeast(1)
    val fromIndex = ((safePage - 1) * pageSize).coerceAtLeast(0)
    if (fromIndex >= size) return emptyList()
    val toIndex = (fromIndex + pageSize).coerceAtMost(size)
    return subList(fromIndex, toIndex)
}

/**
 * Helper to calculate total pages from total items.
 */
fun calculateTotalPages(totalItems: Int, pageSize: Int = 10): Int {
    if (totalItems <= 0) return 1
    return ((totalItems - 1) / pageSize) + 1
}

/**
 * Global Pagination Component for Iki Kasir.
 * Provides a responsive, aesthetically pleasing pagination bar matching the Material 3 design system.
 */
@Composable
fun GlobalPagination(
    currentPage: Int,
    totalPages: Int,
    totalItems: Int,
    pageSize: Int = 10,
    onPageChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    itemName: String = "data",
    showItemCount: Boolean = true
) {
    if (totalItems <= 0) return

    val safeTotalPages = max(1, totalPages)
    val safeCurrentPage = currentPage.coerceIn(1, safeTotalPages)

    // Auto-correct page if current page exceeds safeTotalPages (e.g. after search filter)
    LaunchedEffect(safeTotalPages) {
        if (currentPage > safeTotalPages) {
            onPageChange(safeTotalPages)
        }
    }

    val fromIndex = ((safeCurrentPage - 1) * pageSize) + 1
    val toIndex = min(safeCurrentPage * pageSize, totalItems)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Summary Info Row
            if (showItemCount) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Menampilkan $fromIndex - $toIndex dari $totalItems $itemName",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = interfamily,
                        color = Color(0xFF64748B)
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFEEF2FF)
                    ) {
                        Text(
                            text = "Halaman $safeCurrentPage / $safeTotalPages",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = interfamily,
                            color = Color(0xFF4F46E5),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                HorizontalDivider(
                    thickness = 0.8.dp,
                    color = Color(0xFFF1F5F9)
                )
            }

            // Navigation Controls
            BoxWithConstraints(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                val isVeryCompact = maxWidth < 340.dp
                val canShowFirstLast = safeTotalPages > 4 && !isVeryCompact

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left navigation buttons: First & Prev
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (canShowFirstLast) {
                            PaginationIconButton(
                                icon = Icons.Default.FirstPage,
                                contentDescription = "Halaman Pertama",
                                isEnabled = safeCurrentPage > 1,
                                onClick = { onPageChange(1) }
                            )
                        }

                        PaginationIconButton(
                            icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Halaman Sebelumnya",
                            isEnabled = safeCurrentPage > 1,
                            onClick = { onPageChange(safeCurrentPage - 1) }
                        )
                    }

                    // Middle Section: Page Buttons or Compact Pill
                    if (isVeryCompact) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Text(
                                text = "$safeCurrentPage dari $safeTotalPages",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = interfamily,
                                color = Color(0xFF1E293B),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    } else {
                        // Dynamic Page Buttons with sliding window
                        val pageItems = remember(safeCurrentPage, safeTotalPages) {
                            buildPageList(safeCurrentPage, safeTotalPages)
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            pageItems.forEach { item ->
                                when (item) {
                                    is PageItem.Number -> {
                                        PageNumberButton(
                                            pageNumber = item.page,
                                            isSelected = item.page == safeCurrentPage,
                                            onClick = { onPageChange(item.page) }
                                        )
                                    }
                                    is PageItem.Ellipsis -> {
                                        Text(
                                            text = "...",
                                            fontSize = 12.sp,
                                            fontFamily = interfamily,
                                            color = Color(0xFF94A3B8),
                                            modifier = Modifier.padding(horizontal = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Right navigation buttons: Next & Last
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PaginationIconButton(
                            icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Halaman Selanjutnya",
                            isEnabled = safeCurrentPage < safeTotalPages,
                            onClick = { onPageChange(safeCurrentPage + 1) }
                        )

                        if (canShowFirstLast) {
                            PaginationIconButton(
                                icon = Icons.AutoMirrored.Filled.LastPage,
                                contentDescription = "Halaman Terakhir",
                                isEnabled = safeCurrentPage < safeTotalPages,
                                onClick = { onPageChange(safeTotalPages) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PaginationIconButton(
    icon: ImageVector,
    contentDescription: String,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isEnabled) Color(0xFFEEF2FF) else Color(0xFFF8FAFC),
        animationSpec = tween(150),
        label = "btnBg"
    )
    val iconColor by animateColorAsState(
        targetValue = if (isEnabled) Color(0xFF4F46E5) else Color(0xFFCBD5E1),
        animationSpec = tween(150),
        label = "btnIcon"
    )

    Surface(
        onClick = onClick,
        enabled = isEnabled,
        shape = RoundedCornerShape(10.dp),
        color = bgColor,
        border = BorderStroke(
            1.dp,
            if (isEnabled) Color(0xFFC7D2FE) else Color(0xFFE2E8F0)
        ),
        modifier = Modifier.size(34.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = iconColor,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun PageNumberButton(
    pageNumber: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF4F46E5) else Color.Transparent,
        animationSpec = tween(150),
        label = "pageNumBg"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else Color(0xFF475569),
        animationSpec = tween(150),
        label = "pageNumText"
    )

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = bgColor,
        border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.size(32.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = pageNumber.toString(),
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontFamily = interfamily,
                color = textColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

private sealed class PageItem {
    data class Number(val page: Int) : PageItem()
    data object Ellipsis : PageItem()
}

/**
 * Builds a sliding window of page numbers with ellipsis for clean navigation.
 */
private fun buildPageList(current: Int, total: Int): List<PageItem> {
    if (total <= 5) {
        return (1..total).map { PageItem.Number(it) }
    }

    val items = mutableListOf<PageItem>()
    items.add(PageItem.Number(1))

    if (current <= 3) {
        items.add(PageItem.Number(2))
        items.add(PageItem.Number(3))
        if (total > 4) items.add(PageItem.Number(4))
        items.add(PageItem.Ellipsis)
        items.add(PageItem.Number(total))
    } else if (current >= total - 2) {
        items.add(PageItem.Ellipsis)
        for (p in (total - 3)..total) {
            if (p > 1) items.add(PageItem.Number(p))
        }
    } else {
        items.add(PageItem.Ellipsis)
        items.add(PageItem.Number(current - 1))
        items.add(PageItem.Number(current))
        items.add(PageItem.Number(current + 1))
        items.add(PageItem.Ellipsis)
        items.add(PageItem.Number(total))
    }

    return items
}

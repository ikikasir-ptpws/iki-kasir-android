package com.ptpws.ikikasir.feature.kategori.presentation.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Cookie
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class KategoriIconOption(
    val name: String,
    val label: String,
    val icon: ImageVector,
    val bgColor: Color,
    val tintColor: Color,
    val colorHex: String
)

object KategoriIconHelper {

    val availableIcons = listOf(
        KategoriIconOption(
            name = "LocalCafe",
            label = "Kopi",
            icon = Icons.Default.LocalCafe,
            bgColor = Color(0xFFEDE9FE),
            tintColor = Color(0xFF4F46E5),
            colorHex = "#4F46E5"
        ),
        KategoriIconOption(
            name = "Restaurant",
            label = "Makanan",
            icon = Icons.Default.Restaurant,
            bgColor = Color(0xFFEDE9FE),
            tintColor = Color(0xFF4F46E5),
            colorHex = "#4F46E5"
        ),
        KategoriIconOption(
            name = "LocalBar",
            label = "Minuman",
            icon = Icons.Default.LocalBar,
            bgColor = Color(0xFFFFF0EB),
            tintColor = Color(0xFFEA580C),
            colorHex = "#EA580C"
        ),
        KategoriIconOption(
            name = "Cookie",
            label = "Snack",
            icon = Icons.Default.Cookie,
            bgColor = Color(0xFFEDE9FE),
            tintColor = Color(0xFF4F46E5),
            colorHex = "#4F46E5"
        ),
        KategoriIconOption(
            name = "Cake",
            label = "Dessert",
            icon = Icons.Default.Cake,
            bgColor = Color(0xFFEDE9FE),
            tintColor = Color(0xFF4F46E5),
            colorHex = "#4F46E5"
        ),
        KategoriIconOption(
            name = "Fastfood",
            label = "Fastfood",
            icon = Icons.Default.Fastfood,
            bgColor = Color(0xFFFEF3C7),
            tintColor = Color(0xFFD97706),
            colorHex = "#D97706"
        ),
        KategoriIconOption(
            name = "ShoppingBag",
            label = "Produk",
            icon = Icons.Default.ShoppingBag,
            bgColor = Color(0xFFDCFCE7),
            tintColor = Color(0xFF16A34A),
            colorHex = "#16A34A"
        ),
        KategoriIconOption(
            name = "Category",
            label = "Lainnya",
            icon = Icons.Default.Category,
            bgColor = Color(0xFFE0E7FF),
            tintColor = Color(0xFF4F46E5),
            colorHex = "#4F46E5"
        )
    )

    fun getIconOption(name: String): KategoriIconOption {
        return availableIcons.find { it.name.equals(name, ignoreCase = true) }
            ?: availableIcons.last()
    }
}

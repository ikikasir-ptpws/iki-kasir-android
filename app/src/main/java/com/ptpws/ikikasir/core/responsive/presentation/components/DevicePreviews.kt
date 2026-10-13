package com.ptpws.ikikasir.core.responsive.presentation.components

import androidx.compose.ui.tooling.preview.Preview

/**
 * Matriks Preview khusus untuk menguji tampilan pada 4 variasi ukuran HP:
 * 1. HP Kecil (320 × 640 dp)
 * 2. HP Standar (360 × 800 dp)
 * 3. HP Modern (393 × 852 dp)
 * 4. HP Besar (432 × 960 dp)
 */
@Preview(
    name = "1. HP Kecil (320x640)",
    group = "HP Phones",
    device = "spec:width=320dp,height=640dp,dpi=320",
    showBackground = true
)
@Preview(
    name = "2. HP Standar (360x800)",
    group = "HP Phones",
    device = "spec:width=360dp,height=800dp,dpi=400",
    showBackground = true
)
@Preview(
    name = "3. HP Modern (393x852)",
    group = "HP Phones",
    device = "spec:width=393dp,height=852dp,dpi=440",
    showBackground = true
)
@Preview(
    name = "4. HP Besar (432x960)",
    group = "HP Phones",
    device = "spec:width=432dp,height=960dp,dpi=420",
    showBackground = true
)
annotation class PhoneDevicePreviews

/**
 * Matriks Preview khusus untuk menguji tampilan pada 3 variasi ukuran Tablet:
 * 5. Tablet Kecil (600 × 960 dp)
 * 6. Tablet Standar (800 × 1280 dp)
 * 7. Tablet Besar (1024 × 1366 dp)
 */
@Preview(
    name = "5. Tablet Kecil (600x960)",
    group = "Tablets",
    device = "spec:width=600dp,height=960dp,dpi=240",
    showBackground = true
)
@Preview(
    name = "6. Tablet (800x1280)",
    group = "Tablets",
    device = "spec:width=800dp,height=1280dp,dpi=240",
    showBackground = true
)
@Preview(
    name = "7. Tablet Besar (1024x1366)",
    group = "Tablets",
    device = "spec:width=1024dp,height=1366dp,dpi=240",
    showBackground = true
)
annotation class TabletDevicePreviews

/**
 * Matriks Preview khusus untuk menguji tampilan pada 2 variasi Terminal POS & Layar Ekstra Lebar:
 * 8. Layar Lebar / POS (1280 × 800 dp Landscape)
 * 9. Layar Ekstra Lebar (1440 × 900 dp Landscape)
 */
@Preview(
    name = "8. POS Landscape (1280x800)",
    group = "POS / Wide Screen",
    device = "spec:width=1280dp,height=800dp,dpi=240",
    showBackground = true
)
@Preview(
    name = "9. Ekstra Lebar (1440x900)",
    group = "POS / Wide Screen",
    device = "spec:width=1440dp,height=900dp,dpi=240",
    showBackground = true
)
annotation class PosWideScreenPreviews

/**
 * Matriks Preview khusus untuk menguji Aksesibilitas Font Scaling Android OS:
 * Memverifikasi bahwa tata letak tidak rusak, teks tidak terpotong vertikal,
 * dan tombol tetap elastis saat pengguna memperbesar font sistem (1.3x & 1.5x).
 */
@Preview(
    name = "10. Font Besar 1.3x (Aksesibilitas)",
    group = "Accessibility Font Scale",
    device = "spec:width=360dp,height=800dp,dpi=400",
    fontScale = 1.3f,
    showBackground = true
)
@Preview(
    name = "11. Font Ekstra Besar 1.5x (Aksesibilitas)",
    group = "Accessibility Font Scale",
    device = "spec:width=360dp,height=800dp,dpi=400",
    fontScale = 1.5f,
    showBackground = true
)
annotation class FontAccessibilityPreviews

/**
 * Matriks Preview Lengkap (Mencakup variasi ukuran perangkat dan skala font sekaligus).
 * Gunakan anotasi ini pada Composable untuk memverifikasi responsivitas secara instan.
 */
@PhoneDevicePreviews
@TabletDevicePreviews
@PosWideScreenPreviews
@FontAccessibilityPreviews
annotation class AllDevicesResponsivePreviews


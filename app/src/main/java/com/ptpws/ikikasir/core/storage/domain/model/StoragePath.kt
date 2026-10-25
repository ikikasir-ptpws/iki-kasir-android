package com.ptpws.ikikasir.core.storage.domain.model

/**
 * Konstanta path folder Firebase Storage.
 * Setiap jenis upload gambar memiliki folder tersendiri
 * agar terstruktur dan mudah dikelola.
 *
 * Struktur di Firebase Storage:
 * ├── product_images/         → foto produk
 * ├── profile_photos/        → foto profil pemilik dan pengguna
 * ├── payment_proofs/         → bukti pembayaran
 * ├── store_logos/            → logo toko (struk)
 * └── category_images/        → gambar kategori
 */
object StoragePath {
    const val PRODUCT_IMAGES = "product_images"
    const val PROFILE_PHOTOS = "profile_photos"
    const val PAYMENT_PROOFS = "payment_proofs"
    const val STORE_LOGOS = "store_logos"
    const val CATEGORY_IMAGES = "category_images"
}

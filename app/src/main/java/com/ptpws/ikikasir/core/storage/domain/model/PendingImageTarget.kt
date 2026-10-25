package com.ptpws.ikikasir.core.storage.domain.model

enum class PendingImageTarget(
    val collection: String,
    val field: String,
    val storageFolder: String
) {
    PRODUCT("products", "imageUrl", StoragePath.PRODUCT_IMAGES),
    USER("users", "photoUrl", StoragePath.PROFILE_PHOTOS),
    TRANSACTION("transactions", "paymentProofUrl", StoragePath.PAYMENT_PROOFS),
    STORE_LOGO("nota_settings", "logoUrl", StoragePath.STORE_LOGOS),
    FINANCIAL_RECORD(
        "financial_records",
        "proofImageUrl",
        StoragePath.FINANCIAL_RECORD_PAYMENT_PROOFS
    )
}

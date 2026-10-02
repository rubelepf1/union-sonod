package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_certificate_types")
data class CachedCertificateType(
    @PrimaryKey val id: String,
    val titleBn: String,
    val englishName: String? = null,
    val category: String? = "নাগরিক সেবা",
    val fieldsJson: String = "[]",
    val templateBn: String,
    val isActive: Boolean = true,
    val lastUpdated: Long = System.currentTimeMillis()
)

package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "union_profile")
data class UnionProfile(
    @PrimaryKey val id: Int = 1,
    val unionName: String = "",
    val upazila: String = "",
    val district: String = "",
    val chairmanName: String = "",
    val unionEmail: String = "",
    val unionPhone: String = "",
    val logoUri: String? = null,
    val isConfigured: Boolean = false
)

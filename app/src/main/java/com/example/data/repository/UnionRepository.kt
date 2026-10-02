package com.example.data.repository

import com.example.data.local.UnionProfileDao
import com.example.data.model.UnionProfile
import kotlinx.coroutines.flow.Flow

class UnionRepository(private val dao: UnionProfileDao) {
    val unionProfile: Flow<UnionProfile?> = dao.getUnionProfile()

    suspend fun getProfileSync(): UnionProfile? = dao.getUnionProfileSync()

    suspend fun saveProfile(profile: UnionProfile) {
        dao.saveUnionProfile(profile.copy(id = 1, isConfigured = true))
    }
}

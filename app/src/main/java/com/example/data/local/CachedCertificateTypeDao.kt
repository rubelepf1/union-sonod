package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.CachedCertificateType
import kotlinx.coroutines.flow.Flow

@Dao
interface CachedCertificateTypeDao {
    @Query("SELECT * FROM cached_certificate_types WHERE isActive = 1 ORDER BY id ASC")
    fun getAllCachedTypes(): Flow<List<CachedCertificateType>>

    @Query("SELECT * FROM cached_certificate_types WHERE isActive = 1 ORDER BY id ASC")
    suspend fun getAllCachedTypesSync(): List<CachedCertificateType>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(types: List<CachedCertificateType>)

    @Query("DELETE FROM cached_certificate_types")
    suspend fun clearAll()
}

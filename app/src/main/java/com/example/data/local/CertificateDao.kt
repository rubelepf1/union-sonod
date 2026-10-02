package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.GeneratedCertificate
import kotlinx.coroutines.flow.Flow

@Dao
interface CertificateDao {
    @Query("SELECT * FROM generated_certificates ORDER BY timestamp DESC")
    fun getAllCertificates(): Flow<List<GeneratedCertificate>>

    @Query("SELECT * FROM generated_certificates WHERE id = :id")
    suspend fun getCertificateById(id: Long): GeneratedCertificate?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCertificate(certificate: GeneratedCertificate): Long

    @Update
    suspend fun updateCertificate(certificate: GeneratedCertificate)

    @Query("DELETE FROM generated_certificates WHERE id = :id")
    suspend fun deleteCertificateById(id: Long)

    @Query("SELECT COUNT(*) FROM generated_certificates")
    fun getCertificateCount(): Flow<Int>
}

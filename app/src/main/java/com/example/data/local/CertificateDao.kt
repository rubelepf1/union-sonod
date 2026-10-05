package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.GeneratedCertificate
import com.example.data.model.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface CertificateDao {
    @Query("SELECT * FROM generated_certificates WHERE isDeleted = 0 ORDER BY timestamp DESC")
    fun getAllCertificates(): Flow<List<GeneratedCertificate>>

    @Query("SELECT * FROM generated_certificates WHERE id = :id AND isDeleted = 0")
    suspend fun getCertificateById(id: Long): GeneratedCertificate?

    @Query("SELECT * FROM generated_certificates WHERE remoteId = :remoteId LIMIT 1")
    suspend fun getCertificateByRemoteId(remoteId: String): GeneratedCertificate?

    @Query("SELECT * FROM generated_certificates WHERE serialNo = :serialNo LIMIT 1")
    suspend fun getCertificateBySerialNo(serialNo: String): GeneratedCertificate?

    @Query("SELECT * FROM generated_certificates WHERE syncStatus = '${SyncStatus.PENDING_DELETE}' OR (syncStatus IN ('${SyncStatus.PENDING_INSERT}', '${SyncStatus.PENDING_UPDATE}', '${SyncStatus.FAILED}') AND isDeleted = 0)")
    suspend fun getPendingSyncCertificates(): List<GeneratedCertificate>

    @Query("SELECT COUNT(*) FROM generated_certificates WHERE syncStatus IN ('${SyncStatus.PENDING_INSERT}', '${SyncStatus.PENDING_UPDATE}', '${SyncStatus.PENDING_DELETE}', '${SyncStatus.FAILED}') AND isDeleted = 0")
    fun getPendingSyncCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCertificate(certificate: GeneratedCertificate): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(certificates: List<GeneratedCertificate>)

    @Update
    suspend fun updateCertificate(certificate: GeneratedCertificate)

    @Query("UPDATE generated_certificates SET isDeleted = 1, syncStatus = '${SyncStatus.PENDING_DELETE}' WHERE id = :id")
    suspend fun markDeletedLocally(id: Long)

    @Query("UPDATE generated_certificates SET isDeleted = 1, syncStatus = '${SyncStatus.SYNCED}' WHERE id = :id")
    suspend fun markSoftDeletedSynced(id: Long)

    @Query("UPDATE generated_certificates SET syncStatus = :newStatus, remoteId = :remoteId WHERE id = :id")
    suspend fun updateSyncStatus(id: Long, remoteId: String, newStatus: String)

    @Query("UPDATE generated_certificates SET syncStatus = :newStatus WHERE id = :id")
    suspend fun updateSyncStatusOnly(id: Long, newStatus: String)

    @Query("UPDATE generated_certificates SET syncStatus = '${SyncStatus.FAILED}' WHERE id = :id")
    suspend fun markFailed(id: Long)

    @Query("SELECT COUNT(*) FROM generated_certificates WHERE isDeleted = 0")
    fun getCertificateCount(): Flow<Int>
}

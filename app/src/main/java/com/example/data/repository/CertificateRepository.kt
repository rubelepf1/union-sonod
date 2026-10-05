package com.example.data.repository

import com.example.data.local.CachedCertificateTypeDao
import com.example.data.local.CertificateDao
import com.example.data.model.CachedCertificateType
import com.example.data.model.CertificateRegistry
import com.example.data.model.CertificateType
import com.example.data.model.GeneratedCertificate
import com.example.data.model.SyncStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CertificateRepository(
    private val dao: CertificateDao,
    private val cachedTypeDao: CachedCertificateTypeDao
) {
    val allCertificates: Flow<List<GeneratedCertificate>> = dao.getAllCertificates()
    val totalCount: Flow<Int> = dao.getCertificateCount()
    val pendingSyncCount: Flow<Int> = dao.getPendingSyncCount()

    // Dynamic types cached locally with fallback to built-in types
    val certificateTypes: Flow<List<CertificateType>> = cachedTypeDao.getAllCachedTypes().map { cachedList ->
        if (cachedList.isNotEmpty()) {
            cachedList.map { cached ->
                // Check if we have richer schema in registry
                val builtin = CertificateRegistry.findById(cached.id)
                CertificateType(
                    id = cached.id,
                    title = cached.titleBn,
                    englishName = cached.englishName ?: builtin?.englishName ?: "",
                    description = builtin?.description ?: "ইউনিয়ন পরিষদ প্রত্যয়নপত্র",
                    category = cached.category ?: builtin?.category ?: "নাগরিক সেবা",
                    specificFields = builtin?.specificFields ?: emptyList(),
                    bodyTemplate = cached.templateBn,
                    isSuccession = cached.id == "succession",
                    requiresPhoto = builtin?.requiresPhoto ?: false
                )
            }
        } else {
            CertificateRegistry.ALL_TYPES
        }
    }

    suspend fun getById(id: Long): GeneratedCertificate? = dao.getCertificateById(id)

    suspend fun insert(certificate: GeneratedCertificate): Long = dao.insertCertificate(
        certificate.copy(syncStatus = SyncStatus.PENDING_INSERT)
    )

    suspend fun update(certificate: GeneratedCertificate) = dao.updateCertificate(
        certificate.copy(syncStatus = SyncStatus.PENDING_UPDATE)
    )

    suspend fun delete(id: Long) = dao.markDeletedLocally(id)

    suspend fun saveCachedTypes(types: List<CachedCertificateType>) {
        if (types.isNotEmpty()) {
            cachedTypeDao.insertAll(types)
        }
    }
}

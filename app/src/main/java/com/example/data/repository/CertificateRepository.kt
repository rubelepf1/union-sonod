package com.example.data.repository

import com.example.data.local.CertificateDao
import com.example.data.model.GeneratedCertificate
import kotlinx.coroutines.flow.Flow

class CertificateRepository(private val dao: CertificateDao) {
    val allCertificates: Flow<List<GeneratedCertificate>> = dao.getAllCertificates()
    val totalCount: Flow<Int> = dao.getCertificateCount()

    suspend fun getById(id: Long): GeneratedCertificate? = dao.getCertificateById(id)

    suspend fun insert(certificate: GeneratedCertificate): Long = dao.insertCertificate(certificate)

    suspend fun update(certificate: GeneratedCertificate) = dao.updateCertificate(certificate)

    suspend fun delete(id: Long) = dao.deleteCertificateById(id)
}

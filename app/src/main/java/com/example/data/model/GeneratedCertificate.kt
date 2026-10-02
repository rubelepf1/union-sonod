package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

object SyncStatus {
    const val SYNCED = "SYNCED"
    const val PENDING_INSERT = "PENDING_INSERT"
    const val PENDING_UPDATE = "PENDING_UPDATE"
    const val PENDING_DELETE = "PENDING_DELETE"
}

@Entity(tableName = "generated_certificates")
data class GeneratedCertificate(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val remoteId: String? = null,
    val unionRemoteId: String? = null,
    val createdByRemoteId: String? = null,
    val syncStatus: String = SyncStatus.PENDING_INSERT,
    val isDeleted: Boolean = false,
    val certificateTypeId: String,
    val certificateTitle: String,
    val applicantName: String,
    val fatherOrHusbandName: String,
    val motherName: String,
    val village: String,
    val wardNo: String,
    val postOffice: String,
    val upazila: String,
    val district: String,
    val nidOrBirthNo: String = "",
    val serialNo: String,
    val issueDateBangla: String,
    val applicantPhotoUri: String? = null,
    val customFieldsJson: String = "{}",
    val heirsJson: String? = null,
    val generatedBodyText: String,
    val unionName: String,
    val chairmanName: String,
    val pdfPath: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

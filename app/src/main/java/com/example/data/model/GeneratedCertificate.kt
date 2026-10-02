package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "generated_certificates")
data class GeneratedCertificate(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
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

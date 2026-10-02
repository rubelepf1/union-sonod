package com.example.util

import com.example.data.model.CertificateType
import com.example.data.model.UnionProfile

object TemplateEngine {

    fun generateBodyText(
        type: CertificateType,
        applicantName: String,
        fatherOrHusbandName: String,
        motherName: String,
        village: String,
        wardNo: String,
        postOffice: String,
        upazila: String,
        district: String,
        customValues: Map<String, String>
    ): String {
        var text = type.bodyTemplate

        text = text.replace("{applicant_name}", applicantName.trim())
        text = text.replace("{father_husband_name}", fatherOrHusbandName.trim())
        text = text.replace("{mother_name}", motherName.trim())
        text = text.replace("{village}", village.trim())
        text = text.replace("{ward_no}", BanglaHelper.toBanglaDigits(wardNo.trim()))
        text = text.replace("{post_office}", postOffice.trim())
        text = text.replace("{upazila}", upazila.trim())
        text = text.replace("{district}", district.trim())

        for ((k, v) in customValues) {
            val placeholder = "{$k}"
            val replacement = if (k.contains("amount") || k.contains("no") || k.contains("date")) {
                BanglaHelper.toBanglaDigits(v.trim())
            } else {
                v.trim()
            }
            text = text.replace(placeholder, replacement)
        }

        return text
    }
}

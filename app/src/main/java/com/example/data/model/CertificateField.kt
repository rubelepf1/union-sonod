package com.example.data.model

enum class FieldType {
    TEXT,
    NUMBER,
    DATE,
    MULTILINE,
    DROPDOWN
}

data class CertificateField(
    val key: String,
    val label: String,
    val hint: String = "",
    val helperText: String? = null,
    val type: FieldType = FieldType.TEXT,
    val required: Boolean = true,
    val options: List<String> = emptyList(),
    val defaultValue: String = ""
)

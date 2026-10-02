package com.example.data.model

data class Heir(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String = "",
    val relation: String = "",
    val age: String = "",
    val remarks: String = ""
)

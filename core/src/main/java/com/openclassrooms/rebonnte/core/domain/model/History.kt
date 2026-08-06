package com.openclassrooms.rebonnte.core.domain.model

data class History(
    val medicineId: String = "",
    val userId: String = "",
    val user: User? = null,
    val timeStamp: Long = 0,
    val details: String = ""
)

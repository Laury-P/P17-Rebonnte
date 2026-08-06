package com.openclassrooms.rebonnte.data.model

import com.openclassrooms.rebonnte.core.domain.model.History

data class HistoryDto(
    val medicineId: String = "",
    val userId: String = "",
    val timeStamp: Long = 0,
    val details: String = ""
) {
    fun toDomain(): History {
        return History(
            medicineId = medicineId,
            userId = userId,
            user = null,
            timeStamp = timeStamp,
            details = details
        )
    }
}

fun History.toDto(): HistoryDto {
    return HistoryDto(
        medicineId = medicineId,
        userId = userId,
        timeStamp = timeStamp,
        details = details
    )
}

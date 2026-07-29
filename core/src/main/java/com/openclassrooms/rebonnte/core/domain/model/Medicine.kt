package com.openclassrooms.rebonnte.core.domain.model

data class Medicine(
    var medicineId : String = "",
    var name: String = "",
    var stock: Int = 0,
    var aisleId: String = "",
    var histories: List<History>? = null
)
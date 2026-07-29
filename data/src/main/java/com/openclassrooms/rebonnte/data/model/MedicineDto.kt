package com.openclassrooms.rebonnte.data.model

import com.openclassrooms.rebonnte.core.domain.model.History
import com.openclassrooms.rebonnte.core.domain.model.Medicine

data class MedicineDto(
    val medicineId: String = "",
    var name: String = "",
    var stock: Int = 0,
    var aisleId: String = ""
) {
    fun toDomain(histories : List<History>) : Medicine {
        return Medicine(
            medicineId = this.medicineId,
            name = this.name,
            aisleId = this.aisleId,
            stock = this.stock,
            histories = histories
        )
    }
}

fun Medicine.toDto() : MedicineDto {
    return MedicineDto(
        medicineId = this.medicineId,
        name = this.name,
        aisleId = this.aisleId,
        stock = this.stock
    )
}

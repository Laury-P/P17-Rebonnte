package com.openclassrooms.rebonnte.core.domain.model

import androidx.annotation.StringRes
import com.openclassrooms.rebonnte.core.R

enum class MedicineSortOption(@param:StringRes val labelRes: Int) {
    NONE(R.string.sort_none),
    NAME(R.string.sort_name),
    STOCK(R.string.sort_stock)
}

package com.openclassrooms.rebonnte.feature.medicine

import androidx.compose.runtime.Composable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.openclassrooms.rebonnte.core.designsystem.common.MedicineItem
import com.openclassrooms.rebonnte.core.domain.model.Medicine
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph

@Destination<RootGraph>
@Composable
fun MedicineScreen(viewModel: MedicineViewModel = viewModel()) {
    val medicines by viewModel.medicines.collectAsState(initial = emptyList())

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(medicines) { medicine ->
            MedicineItem(medicine = medicine, onClick = {
                //TODO navigate vers medecin detail screen
            })
        }
    }
}





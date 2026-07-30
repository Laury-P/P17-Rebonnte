package com.openclassrooms.rebonnte.feature.aisle.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.openclassrooms.rebonnte.core.domain.model.Medicine
import com.openclassrooms.rebonnte.feature.aisle.list.AisleViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.feature.destinations.MedicineDetailScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Destination<RootGraph>
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AisleDetailScreen(
    aisleId: String,
    viewModel: AisleDetailViewModel = hiltViewModel(),
    navigator: DestinationsNavigator) {

    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            if (state is UiState.Success) {
                TopAppBar(
                    title = { Text((state as UiState.Success).aisleName) }
                )
            }
        }
    ) { paddingValues ->
        when (state) {
            is UiState.Success ->
                LazyColumn(
                    contentPadding = paddingValues,
                    modifier = Modifier.fillMaxSize()
                ) {
                    if((state as UiState.Success).medicines.isEmpty()) {
                        item {
                            Text("No medicine yet")
                        }
                    } else {
                        items((state as UiState.Success).medicines) { medicine ->
                            MedicineItem(medicine = medicine, onClick = {
                                navigator.navigate(MedicineDetailScreenDestination(medicine.medicineId))
                            })
                        }
                    }
                }
            is UiState.Loading -> {
                CircularProgressIndicator()
            }
            is UiState.Error -> Text((state as UiState.Error).error)
        }


    }

}

@Composable
fun MedicineItem(medicine: Medicine, onClick: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(medicine.name) }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(text = medicine.name, fontWeight = FontWeight.Bold)
            Text(text = "Stock: ${medicine.stock}", color = Color.Gray)
        }
        Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = "Arrow")
    }
}

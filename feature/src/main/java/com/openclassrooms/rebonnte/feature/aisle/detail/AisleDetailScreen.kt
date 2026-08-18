package com.openclassrooms.rebonnte.feature.aisle.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.openclassrooms.rebonnte.core.designsystem.common.ErrorComponent
import com.openclassrooms.rebonnte.core.designsystem.common.LoadingComponent
import com.openclassrooms.rebonnte.core.designsystem.common.MedicineItem
import com.openclassrooms.rebonnte.core.domain.model.Medicine
import com.openclassrooms.rebonnte.feature.R
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.feature.destinations.AisleScreenDestination
import com.ramcosta.composedestinations.generated.feature.destinations.MedicineDetailScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.delay

data class AisleDetailNavArgs(
    val aisleId: String
)

@Destination<RootGraph>(navArgs = AisleDetailNavArgs::class)
@Composable
fun AisleDetailScreen(
    viewModel: AisleDetailViewModel = hiltViewModel(),
    navigator: DestinationsNavigator
) {
    val state by viewModel.uiState.collectAsState()

    AisleDetailContent(
        uiState = state,
        onMedicineClick = { medicineId ->
            navigator.navigate(MedicineDetailScreenDestination(medicineId))
        },
        onNavigateBack = {
            navigator.navigate(AisleScreenDestination)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AisleDetailContent(
    uiState: UiState,
    onMedicineClick: (String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val titleDefault = stringResource(R.string.aisle_detail_title_default)
    val backDesc = stringResource(R.string.aisle_back_description)

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    val title = if (uiState is UiState.Success) uiState.aisle.name else titleDefault
                    Text(
                        text = title,
                        modifier = Modifier
                            .testTag("aisle_detail_title")
                            .semantics { heading() }
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.semantics { contentDescription = backDesc }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (uiState) {
                is UiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (uiState.medicines.isEmpty()) {
                            item {
                                Text(
                                    text = stringResource(R.string.aisle_detail_empty),
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        } else {
                            items(uiState.medicines, key = { it.medicineId }) { medicine ->
                                MedicineItem(
                                    medicine = medicine,
                                    onClick = { onMedicineClick(medicine.medicineId) },
                                    onClickLabel = stringResource(R.string.aisle_detail_medicine_click_label, medicine.name)
                                )
                            }
                        }
                    }
                }

                is UiState.Loading -> {
                    LoadingComponent(modifier = Modifier.fillMaxSize())
                }

                is UiState.Error -> {
                    LaunchedEffect(uiState) {
                        delay(2500)
                        onNavigateBack()
                    }
                    ErrorComponent(
                        message = stringResource(R.string.aisle_detail_error_redirect, uiState.error),
                        modifier = Modifier.fillMaxSize().semantics {
                            liveRegion = LiveRegionMode.Polite
                        }
                    )
                }
            }
        }
    }
}



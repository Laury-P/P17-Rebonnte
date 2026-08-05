package com.openclassrooms.rebonnte.feature.aisle.detail

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.openclassrooms.rebonnte.core.designsystem.common.ErrorComponent
import com.openclassrooms.rebonnte.core.designsystem.common.LoadingComponent
import com.openclassrooms.rebonnte.core.designsystem.common.MedicineItem
import com.openclassrooms.rebonnte.feature.auth.AuthViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.feature.destinations.AisleScreenDestination
import com.ramcosta.composedestinations.generated.feature.destinations.LogScreenDestination
import com.ramcosta.composedestinations.generated.feature.destinations.MedicineDetailScreenDestination
import com.ramcosta.composedestinations.generated.feature.navgraphs.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.delay

@Destination<RootGraph>
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AisleDetailScreen(
    aisleId: String,
    viewModel: AisleDetailViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    navigator: DestinationsNavigator) {

    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (state is UiState.Success) {
                        Text((state as UiState.Success).aisle.name)
                    } else {
                        Text("Aisle Detail")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        authViewModel.signOut()
                        navigator.navigate(LogScreenDestination) {
                            popUpTo(RootNavGraph) { inclusive = true }
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Sign Out")
                    }
                }
            )
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
            is UiState.Loading -> { LoadingComponent() }
            is UiState.Error -> {
                LaunchedEffect(state) {
                    delay(2500)
                    navigator.navigate(AisleScreenDestination)
                }
                ErrorComponent(message = "${(state as UiState.Error).error}. \nYou will be redirected shortly")
            }
        }


    }

}

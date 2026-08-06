package com.openclassrooms.rebonnte.feature.medicine.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.openclassrooms.rebonnte.core.designsystem.common.ErrorComponent
import com.openclassrooms.rebonnte.core.designsystem.common.LoadingComponent
import com.openclassrooms.rebonnte.core.domain.model.History
import com.openclassrooms.rebonnte.core.domain.model.Medicine
import com.openclassrooms.rebonnte.core.domain.model.OperationState
import com.openclassrooms.rebonnte.feature.auth.AuthViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.feature.destinations.LogScreenDestination
import com.ramcosta.composedestinations.generated.feature.destinations.MedicineScreenDestination
import com.ramcosta.composedestinations.generated.feature.navgraphs.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Destination<RootGraph>
@Composable
fun MedicineDetailScreen(
    medicineId: String,
    viewModel: MedicineDetailViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    navigator: DestinationsNavigator
) {

    val medicineState by viewModel.uiState.collectAsState()

    val operationState by viewModel.operationState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(operationState) {
        if(operationState is OperationState.Error){
            val errorMessage = "Stock update failed: ${(operationState as OperationState.Error).error}"

            snackbarHostState.showSnackbar(
                message = errorMessage,
                withDismissAction = true
            )
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    if (medicineState is UiState.Success) {
                        Text((medicineState as UiState.Success).medicine.name) 
                    } else {
                        Text("Medicine Detail")
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
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        when (medicineState) {
            is UiState.Success -> {
                val medicine = (medicineState as UiState.Success).medicine
                Column {
                    SuccessContent(medicine,
                        modifier = Modifier.padding(innerPadding),
                        addOne = { viewModel.updateStock(medicine, true) },
                        removeOne = { viewModel.updateStock(medicine, false)},
                        operationState = operationState
                    )
                }

            }

            is UiState.Loading -> LoadingComponent()
            is UiState.Error -> {
                LaunchedEffect(medicineState) {
                    delay(2000)
                    navigator.navigate(MedicineScreenDestination)
                }
                ErrorComponent(message = (medicineState as UiState.Error).error)
            }
        }

    }
}

@Composable
fun SuccessContent(
    medicine: Medicine,
    modifier: Modifier,
    addOne: () -> Unit,
    removeOne: () -> Unit,
    operationState : OperationState
) {
    Column(
        modifier = modifier
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TextField(
            value = medicine.name,
            onValueChange = {},
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true
        )

        TextField(
            value = medicine.aisleName,
            onValueChange = {},
            label = { Text("Aisle") },
            readOnly = true,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = { removeOne() },
                enabled = (medicine.stock > 0) && (operationState !is OperationState.Loading)
            ) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = "Minus One"
                )
            }
            TextField(
                value = medicine.stock.toString(),
                onValueChange = {},
                label = { Text("Stock") },
                readOnly = true,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = { addOne() },
                enabled = operationState !is OperationState.Loading
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "Plus One"
                )
            }
        }
        Text(
            text = "History",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 8.dp)
        )

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            val list = medicine.histories ?: emptyList()
            if (list.isNotEmpty()) {
                if(operationState is OperationState.Loading) {
                    item { LoadingComponent() }
                }
                items(list) { history ->
                    HistoryItem(history = history, medicine.name)
                }
            }
        }
    }
}

@Composable
fun HistoryItem(history: History, name : String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "$name - ${history.medicineId}", fontWeight = FontWeight.Bold)
            val userDisplay = if (history.user != null) "${history.user!!.name} - ${history.user!!.email}" else history.userId
            Text(text = "User: $userDisplay")
            val formattedDate = remember(history.timeStamp) {
                SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(history.timeStamp))
            }
            Text(text = "Date: $formattedDate")
            Text(text = "Details: ${history.details}")
        }
    }
}

package com.openclassrooms.rebonnte.feature.medicine.detail

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.openclassrooms.rebonnte.core.designsystem.common.ErrorComponent
import com.openclassrooms.rebonnte.core.designsystem.common.LoadingComponent
import com.openclassrooms.rebonnte.core.domain.model.History
import com.openclassrooms.rebonnte.core.domain.model.Medicine
import com.openclassrooms.rebonnte.core.domain.model.OperationState
import com.openclassrooms.rebonnte.feature.R
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import java.text.SimpleDateFormat
import java.util.*

data class MedicineDetailNavArgs(
    val medicineId: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Destination<RootGraph>(navArgs = MedicineDetailNavArgs::class)
@Composable
fun MedicineDetailScreen(
    viewModel: MedicineDetailViewModel = hiltViewModel(),
    navigator: DestinationsNavigator
) {
    val medicineState by viewModel.uiState.collectAsStateWithLifecycle()
    val updateState by viewModel.updateState.collectAsStateWithLifecycle()
    val deleteState by viewModel.deleteState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val deleteSuccessMsg = stringResource(R.string.medicine_detail_delete_success)

    LaunchedEffect(deleteState) {
        if (deleteState is OperationState.Success) {
            Toast.makeText(context, deleteSuccessMsg, Toast.LENGTH_SHORT).show()
            navigator.navigateUp()
        }
    }

    MedicineDetailContent(
        uiState = medicineState,
        updateState = updateState,
        deleteState = deleteState,
        onUpdateStock = { medicine, isIncrease -> viewModel.updateStock(medicine, isIncrease) },
        onDeleteMedicine = { viewModel.deleteMedicine() },
        onNavigateBack = { navigator.navigateUp() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineDetailContent(
    uiState: UiState,
    updateState: OperationState,
    deleteState: OperationState,
    onUpdateStock: (Medicine, Boolean) -> Unit,
    onDeleteMedicine: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val showDeleteDialog = remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    
    val updateFailedMsg = stringResource(R.string.medicine_detail_update_failed)
    val deleteFailedMsg = stringResource(R.string.medicine_detail_delete_failed)
    val titleDefault = stringResource(R.string.medicine_detail_title_default)
    val backDesc = stringResource(R.string.aisle_back_description)
    val deleteDesc = stringResource(R.string.medicine_detail_delete_description)

    LaunchedEffect(updateState) {
        if (updateState is OperationState.Error) {
            snackbarHostState.showSnackbar(
                message = updateFailedMsg.format(updateState.error),
                withDismissAction = true
            )
        }
    }

    LaunchedEffect(deleteState) {
        if (deleteState is OperationState.Error) {
            snackbarHostState.showSnackbar(
                message = deleteFailedMsg.format(deleteState.error),
                withDismissAction = true
            )
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    val title = if (uiState is UiState.Success) uiState.medicine.name else titleDefault
                    Text(
                        text = title,
                        modifier = Modifier
                            .testTag("medicine_detail_title")
                            .semantics { heading() }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = backDesc
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showDeleteDialog.value = true },
                        enabled = uiState is UiState.Success && deleteState !is OperationState.Loading
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteForever,
                            contentDescription = deleteDesc
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        if (showDeleteDialog.value) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog.value = false },
                title = { Text(stringResource(R.string.medicine_detail_delete_dialog_title)) },
                text = { Text(stringResource(R.string.medicine_detail_delete_confirm)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onDeleteMedicine()
                            showDeleteDialog.value = false
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(stringResource(R.string.medicine_detail_delete_button))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog.value = false }) {
                        Text(stringResource(R.string.aisle_cancel))
                    }
                }
            )
        }

        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            if (deleteState is OperationState.Loading) {
                LoadingComponent(modifier = Modifier.fillMaxSize())
            } else {
                when (uiState) {
                    is UiState.Success -> {
                        SuccessContent(
                            medicine = uiState.medicine,
                            operationState = updateState,
                            onUpdateStock = onUpdateStock
                        )
                    }
                    is UiState.Loading -> LoadingComponent(modifier = Modifier.fillMaxSize())
                    is UiState.Error -> {
                        ErrorComponent(
                            message = uiState.error,
                            modifier = Modifier.fillMaxSize().semantics {
                                liveRegion = LiveRegionMode.Polite
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SuccessContent(
    medicine: Medicine,
    operationState: OperationState,
    onUpdateStock: (Medicine, Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Info Card
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = medicine.name,
                onValueChange = {},
                label = { Text(stringResource(R.string.medicine_detail_name_label)) },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.outline,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            OutlinedTextField(
                value = medicine.aisleName,
                onValueChange = {},
                label = { Text(stringResource(R.string.medicine_detail_aisle_label)) },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.outline,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
        }

        // Stock Control
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.medicine_detail_stock_label),
                    style = MaterialTheme.typography.titleMedium
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onUpdateStock(medicine, false) },
                        enabled = medicine.stock > 0 && operationState !is OperationState.Loading
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = stringResource(R.string.medicine_detail_stock_decrease)
                        )
                    }
                    
                    if (operationState is OperationState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp).padding(4.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = medicine.stock.toString(),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                    
                    IconButton(
                        onClick = { onUpdateStock(medicine, true) },
                        enabled = operationState !is OperationState.Loading
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = stringResource(R.string.medicine_detail_stock_increase)
                        )
                    }
                }
            }
        }

        Text(
            text = stringResource(R.string.medicine_detail_history_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.semantics { heading() }
        )

        val historyList = medicine.histories ?: emptyList()
        if (historyList.isEmpty()) {
            Text(
                text = stringResource(R.string.medicine_detail_history_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(historyList) { history ->
                    HistoryItem(history = history)
                }
            }
        }
    }
}


@Composable
fun HistoryItem(history: History) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            val formattedDate = remember(history.timeStamp) {
                SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(history.timeStamp))
            }
            
            Text(
                text = stringResource(R.string.medicine_detail_history_item_date, formattedDate),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
            
            val userDisplay = history.user?.name ?: history.userId
            Text(
                text = stringResource(R.string.medicine_detail_history_item_user, userDisplay),
                style = MaterialTheme.typography.bodySmall
            )
            
            Text(
                text = stringResource(R.string.medicine_detail_history_item_details, history.details),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

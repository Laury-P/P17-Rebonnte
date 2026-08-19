package com.openclassrooms.rebonnte.feature.medicine.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.openclassrooms.rebonnte.core.designsystem.common.AisleSelectorComponent
import com.openclassrooms.rebonnte.core.designsystem.common.ErrorComponent
import com.openclassrooms.rebonnte.core.designsystem.common.LoadingComponent
import com.openclassrooms.rebonnte.core.designsystem.common.MedicineItem
import com.openclassrooms.rebonnte.core.designsystem.common.SearchBarComponent
import com.openclassrooms.rebonnte.core.domain.model.Aisle
import com.openclassrooms.rebonnte.core.domain.model.OperationState
import com.openclassrooms.rebonnte.feature.R
import com.openclassrooms.rebonnte.feature.auth.AuthViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.feature.destinations.LogScreenDestination
import com.ramcosta.composedestinations.generated.feature.destinations.MedicineDetailScreenDestination
import com.ramcosta.composedestinations.generated.feature.navgraphs.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@OptIn(ExperimentalMaterial3Api::class)
@Destination<RootGraph>
@Composable
fun MedicineScreen(
    viewModel: MedicineViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    navigator: DestinationsNavigator,
) {
    val medicinesState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val aisles by viewModel.aisles.collectAsStateWithLifecycle()
    val operationState by viewModel.operationState.collectAsStateWithLifecycle()

    MedicineContent(
        uiState = medicinesState,
        searchQuery = searchQuery,
        aisles = aisles,
        operationState = operationState,
        onSearchQueryChange = { viewModel.filterByName(it) },
        onSortByName = { viewModel.sortByName() },
        onSortByStock = { viewModel.sortByStock() },
        onSortByNone = { viewModel.sortByNone() },
        onAddMedicine = { name, stock, aisleId -> viewModel.addMedicine(name, stock, aisleId) },
        onRetry = { viewModel.retry() },
        onMedicineClick = { id -> navigator.navigate(MedicineDetailScreenDestination(id)) },
        onSignOut = {
            authViewModel.signOut()
            navigator.navigate(LogScreenDestination) {
                popUpTo(RootNavGraph) { inclusive = true }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineContent(
    uiState: ListMedicinesState,
    searchQuery: String,
    aisles: List<Aisle>,
    operationState: OperationState,
    onSearchQueryChange: (String) -> Unit,
    onSortByName: () -> Unit,
    onSortByStock: () -> Unit,
    onSortByNone: () -> Unit,
    onAddMedicine: (String, Int, String) -> Unit,
    onRetry: () -> Unit,
    onMedicineClick: (String) -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isSearchActive by rememberSaveable { mutableStateOf(false) }
    val showNewMedicineDialog = remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    
    val addFailedMsg = stringResource(R.string.medicine_add_failed)
    val titleText = stringResource(R.string.medicine_title)
    val logoutDesc = stringResource(R.string.aisle_logout_description)
    val sortTitle = stringResource(R.string.medicine_sort_title)
    val addDesc = stringResource(R.string.medicine_add_description)

    LaunchedEffect(operationState) {
        if (operationState is OperationState.Error) {
            snackbarHostState.showSnackbar(
                message = addFailedMsg.format(operationState.error),
                withDismissAction = true
            )
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            Column {
                TopAppBar(
                    title = { 
                        Text(
                            text = titleText,
                            modifier = Modifier.semantics { heading() }
                        ) 
                    },
                    actions = {
                        IconButton(
                            onClick = onSignOut,
                            modifier = Modifier.semantics { 
                                contentDescription = logoutDesc
                            }
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null)
                        }
                        
                        var expanded by remember { mutableStateOf(false) }
                        Box {
                            IconButton(
                                onClick = { expanded = true },
                                modifier = Modifier.semantics { 
                                    contentDescription = sortTitle
                                }
                            ) {
                                Icon(Icons.Default.MoreVert, contentDescription = null)
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                offset = DpOffset(x = 0.dp, y = 0.dp)
                            ) {
                                DropdownMenuItem(
                                    onClick = { onSortByNone(); expanded = false },
                                    text = { Text(stringResource(R.string.medicine_sort_none)) }
                                )
                                DropdownMenuItem(
                                    onClick = { onSortByName(); expanded = false },
                                    text = { Text(stringResource(R.string.medicine_sort_name)) }
                                )
                                DropdownMenuItem(
                                    onClick = { onSortByStock(); expanded = false },
                                    text = { Text(stringResource(R.string.medicine_sort_stock)) }
                                )
                            }
                        }
                    }
                )
                SearchBarComponent(
                    query = searchQuery,
                    onQueryChange = onSearchQueryChange,
                    isSearchActive = isSearchActive,
                    onActiveChanged = { isSearchActive = it },
                    placeholder = stringResource(R.string.medicine_search_placeholder),
                    backDescription = stringResource(R.string.aisle_back_description)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showNewMedicineDialog.value = true },
                modifier = Modifier.semantics { 
                    contentDescription = addDesc
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }
    ) { innerPadding ->
        if (showNewMedicineDialog.value) {
            var name by rememberSaveable { mutableStateOf("") }
            var selectedAisle by remember { mutableStateOf(aisles.firstOrNull()) }
            var stock by rememberSaveable { mutableIntStateOf(0) }
            
            AlertDialog(
                onDismissRequest = { showNewMedicineDialog.value = false },
                title = { Text(stringResource(R.string.medicine_new_dialog_title)) },
                text = {
                    if (aisles.isNotEmpty()) {
                        NewMedicineForm(
                            name = name,
                            aisle = selectedAisle ?: aisles[0],
                            stock = stock,
                            onNameChanged = { name = it },
                            onAisleChanged = { selectedAisle = it },
                            onStockChanged = { stock = it },
                            aisles = aisles
                        )
                    } else {
                        LoadingComponent(modifier = Modifier.fillMaxWidth().height(100.dp))
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val aisleId = selectedAisle?.aisleId ?: aisles.firstOrNull()?.aisleId
                            if (aisleId != null) {
                                onAddMedicine(name, stock, aisleId)
                            }
                            showNewMedicineDialog.value = false
                        },
                        enabled = name.isNotBlank() && (selectedAisle != null || aisles.isNotEmpty())
                    ) {
                        Text(stringResource(R.string.aisle_save))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showNewMedicineDialog.value = false }) {
                        Text(stringResource(R.string.aisle_cancel))
                    }
                }
            )
        }

        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            if (operationState is OperationState.Loading) {
                LoadingComponent(modifier = Modifier.fillMaxWidth().padding(8.dp))
            }

            when (uiState) {
                is ListMedicinesState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.listMedicine, key = { it.medicineId }) { medicine ->
                            MedicineItem(
                                medicine = medicine,
                                onClick = { onMedicineClick(medicine.medicineId) },
                                onClickLabel = stringResource(R.string.aisle_detail_medicine_click_label, medicine.name)
                            )
                        }
                    }
                }

                is ListMedicinesState.Loading -> LoadingComponent(modifier = Modifier.fillMaxSize())
                
                is ListMedicinesState.Error -> {
                    ErrorComponent(
                        message = uiState.error,
                        withRetryButton = true,
                        onRetryClick = onRetry,
                        modifier = Modifier.fillMaxSize().semantics {
                            liveRegion = LiveRegionMode.Polite
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun NewMedicineForm(
    name: String,
    aisle: Aisle,
    stock: Int,
    onNameChanged: (String) -> Unit,
    onStockChanged: (Int) -> Unit,
    aisles: List<Aisle>,
    onAisleChanged: (Aisle) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedTextField(
            value = name,
            onValueChange = onNameChanged,
            label = { Text(stringResource(R.string.medicine_new_name_label)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        AisleSelectorComponent(
            selectedAisle = aisle,
            onAisleSelected = onAisleChanged,
            aisles = aisles,
            label = stringResource(R.string.medicine_new_aisle_label)
        )
        OutlinedTextField(
            value = if (stock == 0) "" else stock.toString(),
            onValueChange = { newValue ->
                val filteredValue = newValue.filter { it.isDigit() }
                onStockChanged(filteredValue.toIntOrNull() ?: 0)
            },
            label = { Text(stringResource(R.string.medicine_new_stock_label)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )
    }
}

package com.openclassrooms.rebonnte.feature.medicine.list

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.openclassrooms.rebonnte.core.designsystem.common.ErrorComponent
import com.openclassrooms.rebonnte.core.designsystem.common.LoadingComponent
import com.openclassrooms.rebonnte.core.designsystem.common.MedicineItem
import com.openclassrooms.rebonnte.core.domain.model.Aisle
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.feature.destinations.MedicineDetailScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@OptIn(ExperimentalMaterial3Api::class)
@Destination<RootGraph>
@Composable
fun MedicineScreen(
    viewModel: MedicineViewModel = hiltViewModel(),
    navigator: DestinationsNavigator,
) {
    val medicinesState by viewModel.uiState.collectAsState()

    var isSearchActive by rememberSaveable { mutableStateOf(false) }

    val searchQuery by viewModel.searchQuery.collectAsState()

    val showNewMedicineDialog = remember { mutableStateOf(false) }

    val aisles by viewModel.aisles.collectAsState()

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(text = "Medicines") },
                    actions = {
                        var expanded by remember { mutableStateOf(false) }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Box {
                                IconButton(onClick = { expanded = true }) {
                                    Icon(Icons.Default.MoreVert, contentDescription = null)
                                }
                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false },
                                    offset = DpOffset(x = 0.dp, y = 0.dp)
                                ) {
                                    DropdownMenuItem(
                                        onClick = {
                                            viewModel.sortByNone()
                                            expanded = false
                                        },
                                        text = { Text("Sort by None") }
                                    )
                                    DropdownMenuItem(
                                        onClick = {
                                            viewModel.sortByName()
                                            expanded = false
                                        },
                                        text = { Text("Sort by Name") }
                                    )
                                    DropdownMenuItem(
                                        onClick = {
                                            viewModel.sortByStock()
                                            expanded = false
                                        },
                                        text = { Text("Sort by Stock") }
                                    )
                                }
                            }
                        }
                    }
                )
                EmbeddedSearchBar(
                    query = searchQuery,
                    onQueryChange = {
                        viewModel.filterByName(it)
                    },
                    isSearchActive = isSearchActive,
                    onActiveChanged = { isSearchActive = it }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                showNewMedicineDialog.value = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { innerPadding ->
        if (showNewMedicineDialog.value) {
            var name by remember { mutableStateOf("") }
            var selectedAisle by remember { mutableStateOf(aisles.firstOrNull()) }
            var stock by remember { mutableIntStateOf(0) }
            AlertDialog(
                onDismissRequest = { showNewMedicineDialog.value = false },
                text = {
                    if (aisles.isNotEmpty()) {
                        NewMedicineForm(
                            name,
                            selectedAisle ?: aisles[0],
                            stock,
                            onNameChanged = { name = it },
                            onAisleChanged = { selectedAisle = it },
                            onStockChanged = { stock = it },
                            aisles = aisles
                        )
                    } else {
                        LoadingComponent()
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val aisleId = selectedAisle?.aisleId ?: aisles.firstOrNull()?.aisleId
                            if (aisleId != null) {
                                viewModel.addMedicine(name, stock, aisleId)
                            }
                            showNewMedicineDialog.value = false
                        },
                        enabled = name.isNotBlank() && (selectedAisle != null || aisles.isNotEmpty())
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showNewMedicineDialog.value = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
        when (val state = medicinesState) {
            is ListMedicinesState.Success -> {
                val medicines = state.listMedicine
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    items(medicines) { medicine ->
                        MedicineItem(medicine = medicine, onClick = {
                            navigator.navigate(MedicineDetailScreenDestination(medicine.medicineId))
                        })
                    }
                }
            }

            is ListMedicinesState.Loading -> LoadingComponent()
            is ListMedicinesState.Error -> {
                ErrorComponent(
                    message = state.error,
                    withRetryButton = true,
                    onRetryClick = { viewModel.retry() }
                )
            }
        }
    }

}

@Composable
fun EmbeddedSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    isSearchActive: Boolean,
    onActiveChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var searchQuery by rememberSaveable { mutableStateOf(query) }
    val activeChanged: (Boolean) -> Unit = { active ->
        searchQuery = ""
        onQueryChange("")
        onActiveChanged(active)
    }

    val shape: Shape = RoundedCornerShape(16.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(horizontal = 16.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isSearchActive) {
            IconButton(onClick = { activeChanged(false) }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        } else {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        BasicTextField(
            value = searchQuery,
            onValueChange = { query ->
                searchQuery = query
                onQueryChange(query)
            },
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            singleLine = true,
            decorationBox = { innerTextField ->
                if (searchQuery.isEmpty()) {
                    Text(
                        text = "Search",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                innerTextField()
            }
        )

        if (isSearchActive && searchQuery.isNotEmpty()) {
            IconButton(onClick = {
                searchQuery = ""
                onQueryChange("")
            }) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
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
    Column {
        OutlinedTextField(
            value = name,
            onValueChange = { onNameChanged(it) },
            label = { Text("Medicine name: ") },
        )
        AisleSelector(
            selectedAisle = aisle,
            aisles = aisles,
            onAisleSelected = onAisleChanged
        )

        OutlinedTextField(
            value = stock.toString(),
            onValueChange = { newValue ->
                val filteredValue = newValue.filter { it.isDigit() }
                onStockChanged(filteredValue.toIntOrNull() ?: 0)
            },
            label = { Text("Initial stock: ") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AisleSelector(
    selectedAisle: Aisle, onAisleSelected: (Aisle) -> Unit, aisles: List<Aisle>
) {
    val expandedState = remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expandedState.value,
        onExpandedChange = { expandedState.value = it },
        modifier = Modifier.fillMaxWidth()
    ) {
        TextField(
            modifier = Modifier.menuAnchor(
                ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                enabled = true
            ).fillMaxWidth(),
            readOnly = true,
            value = selectedAisle.name,
            onValueChange = {},
            label = {Text("Select the aisle")},
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedState.value)}
        )
        ExposedDropdownMenu(
            expanded = expandedState.value,
            onDismissRequest = { expandedState.value = false },
        ) {
            aisles.forEach { aisle ->
                DropdownMenuItem(
                    text = {Text(aisle.name)},
                    onClick = {
                        onAisleSelected(aisle)
                        expandedState.value = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}
package com.openclassrooms.rebonnte.feature.aisle.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.openclassrooms.rebonnte.core.domain.model.Aisle
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.feature.destinations.AisleDetailScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@OptIn(ExperimentalMaterial3Api::class)
@Destination<RootGraph>
@Composable
fun AisleScreen(
    modifier: Modifier = Modifier,
    viewModel: AisleViewModel = hiltViewModel(),
    navigator: DestinationsNavigator
) {

    val aislesState by viewModel.uiState.collectAsState()

    val showNewAisleDialog = remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = "Aisle") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                showNewAisleDialog.value = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add aisle")
            }
        }
    ) { innerPadding ->
        if (showNewAisleDialog.value) {
            var aisleName by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showNewAisleDialog.value = false },
                title = { Text("Add new Aisle") },
                text = {
                    OutlinedTextField(
                        value = aisleName,
                        onValueChange = { aisleName = it },
                        label = {Text("Aisle name: ")},
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.addAisle(aisleName)
                            showNewAisleDialog.value = false
                        },
                        enabled = aisleName.isNotEmpty()
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showNewAisleDialog.value = false }) {
                        Text("Cancel")
                    }
                }
            )

        }
        when (aislesState) {
            is ListAislesState.Success -> {
                val aisles = (aislesState as ListAislesState.Success).listAisle
                LazyColumn(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                ) {
                    if (aisles.isEmpty()) {
                        item {
                            Text(text = "No aisle yet")
                        }
                    } else {
                        items(aisles) { aisle ->
                            val id = aisle.aisleId
                            AisleItem(aisle = aisle, onClick = {
                                navigator.navigate(direction = AisleDetailScreenDestination(aisleId = id))
                            }
                            )
                        }
                    }

                }
            }

            is ListAislesState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is ListAislesState.Error -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = (aislesState as ListAislesState.Error).error)
                    // Todo gerer l'erreur
                }
            }

        }
    }
}

@Composable
fun AisleItem(aisle: Aisle, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = aisle.name, style = MaterialTheme.typography.bodyMedium)
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Arrow"
        )
    }
}


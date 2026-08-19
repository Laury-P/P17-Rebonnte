package com.openclassrooms.rebonnte.feature.aisle.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.openclassrooms.rebonnte.core.designsystem.common.ErrorComponent
import com.openclassrooms.rebonnte.core.designsystem.common.LoadingComponent
import com.openclassrooms.rebonnte.core.domain.model.Aisle
import com.openclassrooms.rebonnte.core.domain.model.OperationState
import com.openclassrooms.rebonnte.feature.auth.AuthViewModel
import com.openclassrooms.rebonnte.feature.R
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.feature.destinations.AisleDetailScreenDestination
import com.ramcosta.composedestinations.generated.feature.destinations.LogScreenDestination
import com.ramcosta.composedestinations.generated.feature.navgraphs.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Destination<RootGraph>
@Composable
fun AisleScreen(
    modifier: Modifier = Modifier,
    viewModel: AisleViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    navigator: DestinationsNavigator
) {
    val uiState by viewModel.uiState.collectAsState()
    val operationState by viewModel.operationState.collectAsState()

    AisleContent(
        modifier = modifier,
        uiState = uiState,
        operationState = operationState,
        onAddAisle = { viewModel.addAisle(it) },
        onRetry = { viewModel.retry() },
        onAisleClick = { id -> 
            navigator.navigate(direction = AisleDetailScreenDestination(aisleId = id))
        },
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
fun AisleContent(
    uiState: ListAislesState,
    operationState: OperationState,
    onAddAisle: (String) -> Unit,
    onRetry: () -> Unit,
    onAisleClick: (String) -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val showNewAisleDialog = remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val errorPrefix = stringResource(R.string.aisle_error_prefix)
    val logoutDesc = stringResource(R.string.aisle_logout_description)
    val addDesc = stringResource(R.string.aisle_add_description)

    LaunchedEffect(operationState) {
        if (operationState is OperationState.Error) {
            snackbarHostState.showSnackbar(
                message = errorPrefix.format(operationState.error),
                withDismissAction = true
            )
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = stringResource(R.string.aisle_title),
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
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showNewAisleDialog.value = true },
                modifier = Modifier.semantics { 
                    contentDescription = addDesc
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }
    ) { innerPadding ->
        if (showNewAisleDialog.value) {
            var aisleName by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showNewAisleDialog.value = false },
                title = { Text(stringResource(R.string.aisle_new_dialog_title)) },
                text = {
                    OutlinedTextField(
                        value = aisleName,
                        onValueChange = { aisleName = it },
                        label = { Text(stringResource(R.string.aisle_new_label)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onAddAisle(aisleName)
                            showNewAisleDialog.value = false
                        },
                        enabled = aisleName.isNotBlank()
                    ) {
                        Text(stringResource(R.string.aisle_save))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showNewAisleDialog.value = false }) {
                        Text(stringResource(R.string.aisle_cancel))
                    }
                }
            )
        }

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when (uiState) {
                is ListAislesState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Affiche le loader comme premier item de la liste si une opération est en cours
                        if (operationState is OperationState.Loading) {
                            item {
                                LoadingComponent(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                )
                            }
                        }

                        if (uiState.listAisle.isEmpty()) {
                            item {
                                Text(
                                    text = stringResource(R.string.aisle_empty),
                                    modifier = Modifier.padding(8.dp),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        } else {
                            items(
                                items = uiState.listAisle,
                                key = { it.aisleId }
                            ) { aisle ->
                                AisleItem(
                                    aisle = aisle,
                                    onClick = { onAisleClick(aisle.aisleId) }
                                )
                            }
                        }
                    }
                }

                is ListAislesState.Loading -> {
                    LoadingComponent(modifier = Modifier.fillMaxSize())
                }

                is ListAislesState.Error -> {
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
fun AisleItem(aisle: Aisle, onClick: () -> Unit) {
    val clickLabel = stringResource(R.string.aisle_item_click_label, aisle.name)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick,
                onClickLabel = clickLabel
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = aisle.name,
                style = MaterialTheme.typography.titleMedium
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

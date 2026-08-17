package com.openclassrooms.rebonnte.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.openclassrooms.rebonnte.core.domain.model.OperationState
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.feature.destinations.AisleScreenDestination
import com.ramcosta.composedestinations.generated.feature.destinations.LogScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.openclassrooms.rebonnte.feature.R

@Destination<RootGraph>(start = true)
@Composable
fun LogScreen(navigator: DestinationsNavigator, viewModel: LogViewModel = hiltViewModel()) {
    val operationState by viewModel.operationState.collectAsState()

    LaunchedEffect(operationState) {
        if (operationState is OperationState.Success) {
            navigator.navigate(AisleScreenDestination()) {
                popUpTo(LogScreenDestination) { inclusive = true }
            }
        }
    }

    LogContent(
        operationState = operationState,
        onSignIn = { email, password -> viewModel.signIn(email, password) },
        onSignUp = { email, password, name -> viewModel.signUp(email, password, name) }
    )
}

@Composable
fun LogContent(
    operationState: OperationState,
    onSignIn: (String, String) -> Unit,
    onSignUp: (String, String, String) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    var isLoginMode by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isLoginMode) stringResource(R.string.auth_login_title) else stringResource(R.string.auth_signup_title),
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.semantics { heading() }
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (!isLoginMode) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.auth_name_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = operationState is OperationState.Error,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(stringResource(R.string.auth_email_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = operationState is OperationState.Error,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(stringResource(R.string.auth_password_label)) },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                isError = operationState is OperationState.Error,
                trailingIcon = {
                    val image = if (passwordVisible)
                        Icons.Filled.Visibility
                    else Icons.Filled.VisibilityOff

                    val description = if (passwordVisible) stringResource(R.string.auth_password_hide) else stringResource(R.string.auth_password_show)

                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = description)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (email.isNotBlank() && password.isNotBlank() && (isLoginMode || name.isNotBlank())) {
                            if (isLoginMode) {
                                onSignIn(email, password)
                            } else {
                                onSignUp(email, password, name)
                            }
                            focusManager.clearFocus()
                        }
                    }
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (operationState is OperationState.Loading) {
                val loadingDesc = stringResource(R.string.auth_loading_desc)
                CircularProgressIndicator(
                    modifier = Modifier.semantics {
                        contentDescription = loadingDesc
                    }
                )
            } else {
                Button(
                    onClick = {
                        if (isLoginMode) {
                            onSignIn(email, password)
                        } else {
                            onSignUp(email, password, name)
                        }
                        focusManager.clearFocus()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = email.isNotBlank() && password.isNotBlank() && (isLoginMode || name.isNotBlank())
                ) {
                    Text(if (isLoginMode) stringResource(R.string.auth_login_button) else stringResource(R.string.auth_signup_button))
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = { isLoginMode = !isLoginMode }) {
                    Text(
                        if (isLoginMode) stringResource(R.string.auth_no_account)
                        else stringResource(R.string.auth_already_account)
                    )
                }
            }

            if (operationState is OperationState.Error) {
                Text(
                    text = (operationState as OperationState.Error).error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .semantics { liveRegion = LiveRegionMode.Polite }
                )
            }
        }
    }
}

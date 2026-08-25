package com.openclassrooms.rebonnte.core.designsystem.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun ErrorComponent(modifier : Modifier = Modifier, withRetryButton : Boolean = false, message: String, onRetryClick : () -> Unit = {}) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = (message))
        if (withRetryButton) {
            TextButton(onClick = { onRetryClick() }){
                Text("Retry")
            }
        }
    }
}
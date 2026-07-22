package com.openclassrooms.rebonnte.feature.auth

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph

@Destination<RootGraph>(start = true)
@Composable
fun LogScreen() {
    Text(text="This is the log screen")
}
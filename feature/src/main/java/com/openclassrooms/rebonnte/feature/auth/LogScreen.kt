package com.openclassrooms.rebonnte.feature.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.feature.destinations.AisleScreenDestination

@Destination<RootGraph>(start = true)
@Composable
fun LogScreen(navController: NavController) {
    Column() {
        Text(text="This is the log screen")
        TextButton(onClick = { navController.navigate(AisleScreenDestination.route) }) {
            Text("Go to aislescreen")
        }

    }

}
package com.openclassrooms.rebonnte

import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.runtime.internal.composableLambdaInstance
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.openclassrooms.rebonnte.core.ui.theme.RebonnteTheme
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.generated.feature.navgraphs.RootNavGraph
import com.ramcosta.composedestinations.generated.feature.destinations.AisleScreenDestination
import com.ramcosta.composedestinations.generated.feature.destinations.LogScreenDestination
import com.ramcosta.composedestinations.generated.feature.destinations.MedicineScreenDestination
import com.ramcosta.composedestinations.utils.currentDestinationAsState
import com.ramcosta.composedestinations.utils.startDestination
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RebonnteTheme {
                val navController = rememberNavController()
                val currentDestination = navController.currentDestinationAsState().value
                    ?: RootNavGraph.startDestination

                Scaffold(
                    modifier = Modifier,
                    bottomBar = {
                        if (currentDestination != LogScreenDestination) {
                            NavigationBar {
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                                    label = { Text("Aisle") },
                                    selected = currentDestination == AisleScreenDestination,
                                    onClick = { navController.navigate(AisleScreenDestination.route) }
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.List, contentDescription = null) },
                                    label = { Text("Medicine") },
                                    selected = currentDestination == MedicineScreenDestination,
                                    onClick = { navController.navigate(MedicineScreenDestination.route) }
                                )
                            }
                        }
                    },
                ) { innerPadding ->
                    DestinationsNavHost(
                        navGraph = RootNavGraph,
                        navController = navController,
                        modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
                    )
                }

            }
        }
    }
}
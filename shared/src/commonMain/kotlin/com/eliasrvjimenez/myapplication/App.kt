package com.eliasrvjimenez.myapplication

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.eliasrvjimenez.myapplication.ViewModel.AppViewModel
import com.eliasrvjimenez.myapplication.ViewModel.NavigationRoutes

@Composable
@Preview
fun App(
    navController: NavHostController = rememberNavController(), // Necessary for navigation on android and for navigation to behave as expected on other platforms
    viewModel: AppViewModel = viewModel { AppViewModel() } // ViewModel Necessary for handling navigation state and other app-wide state management
) {
    // Observe the current back stack entry to update the ViewModel with the current route
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    // Update the ViewModel with the current route whenever it changes
    LaunchedEffect(navBackStackEntry) {
        viewModel.onRouteChanged(navBackStackEntry?.destination?.route)
    }

    // Observe the FAB (FloatingActionButton) visibility state from the ViewModel
    val isFabVisible by viewModel.isFabVisible.collectAsStateWithLifecycle()

    MaterialTheme {
        Scaffold (
            floatingActionButton = { if (isFabVisible) CharacterListFAB(navController) },
            modifier = Modifier.fillMaxSize().safeContentPadding()
        ){ paddingValues ->
            NavHost(
                navController = navController,
                startDestination = NavigationRoutes.CHARACTER_LIST,
                modifier = Modifier.padding(paddingValues)
            ) {
                composable(NavigationRoutes.CHARACTER_LIST) {
                    CharacterListView()
                }
                composable(NavigationRoutes.CHARACTER_CREATE) {
                    CharacterCreateView(onCancel = {
                        navController.popBackStack()
                    })
                }
            }
        }
    }
}

@Composable
fun CharacterListFAB(
    navController: NavHostController
) {
    FloatingActionButton(onClick = { navController.navigate(NavigationRoutes.CHARACTER_CREATE) }) {
        Icon(Icons.Default.Add, contentDescription = "Add Character")
    }
}
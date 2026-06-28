package com.eliasrvjimenez.guildhall

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.eliasrvjimenez.guildhall.view.character.CharacterCreateView
import com.eliasrvjimenez.guildhall.view.character.CharacterListView
import com.eliasrvjimenez.guildhall.view.wiki.WikiClassDetailView
import com.eliasrvjimenez.guildhall.view.wiki.WikiHomeView
import com.eliasrvjimenez.guildhall.view.wiki.WikiListView
import com.eliasrvjimenez.guildhall.viewmodel.AppViewModel
import com.eliasrvjimenez.guildhall.viewmodel.NavigationRoutes
import org.koin.compose.viewmodel.koinViewModel

@Composable
@Preview
fun App(
    navController: NavHostController = rememberNavController(), // Necessary for navigation on android and for navigation to behave as expected on other platforms
    viewModel: AppViewModel = koinViewModel() // ViewModel Necessary for handling navigation state and other app-wide state management
) {
    // Observe the current back stack entry to update the ViewModel with the current route
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    // Update the ViewModel with the current route whenever it changes
    LaunchedEffect(navBackStackEntry) {
        viewModel.onRouteChanged(navBackStackEntry?.destination?.route)
    }

    // Observe the FAB (FloatingActionButton) visibility state from the ViewModel
    val isFabVisible by viewModel.isFabVisible.collectAsStateWithLifecycle()
    val currentRoute by viewModel.currentRoute.collectAsStateWithLifecycle()

    MaterialTheme {
        Scaffold (
            floatingActionButton = { if (isFabVisible) CharacterListFAB(navController) },
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute == NavigationRoutes.CHARACTER_LIST || currentRoute == NavigationRoutes.CHARACTER_CREATE,
                        onClick = {
                            navController.navigate(NavigationRoutes.CHARACTER_LIST) {
                                launchSingleTop = true
                                restoreState = true
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                            }
                        },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Characters") },
                        label = { Text("Characters") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == NavigationRoutes.WIKI_HOME ||
                                currentRoute?.startsWith("wiki_class") == true,
                        onClick = {
                            navController.navigate(NavigationRoutes.WIKI_HOME) {
                                launchSingleTop = true
                                restoreState = true
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                            }
                        },
                        icon = { Icon(Icons.Default.MenuBook, contentDescription = "Wiki") },
                        label = { Text("Wiki") }
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
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
                composable(NavigationRoutes.WIKI_HOME) {
                    WikiHomeView(onCategoryClick = { category ->
                        if (category == "classes") {
                            navController.navigate(NavigationRoutes.WIKI_CLASS_LIST)
                        }
                    })
                }
                composable(NavigationRoutes.WIKI_CLASS_LIST) {
                    WikiListView(
                        onClassClick = { classIndex ->
                            navController.navigate("wiki_class_detail/$classIndex")
                        },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(NavigationRoutes.WIKI_CLASS_DETAIL) {
                    WikiClassDetailView(
                        onBack = { navController.popBackStack() }
                    )
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
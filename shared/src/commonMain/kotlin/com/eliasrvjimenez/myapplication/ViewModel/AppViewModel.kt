package com.eliasrvjimenez.myapplication.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

object NavigationRoutes {
    const val CHARACTER_LIST = "character_list"
    const val CHARACTER_CREATE = "character_create"
}

class AppViewModel() : ViewModel() {
    private val _currentRoute = MutableStateFlow<String?>(null)

    val isFabVisible: StateFlow<Boolean> = _currentRoute.map { it == NavigationRoutes.CHARACTER_LIST }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    fun onRouteChanged(route: String?) {
        _currentRoute.value = route
    }
}
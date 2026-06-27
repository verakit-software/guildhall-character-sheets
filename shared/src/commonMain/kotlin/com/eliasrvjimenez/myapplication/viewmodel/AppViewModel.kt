package com.eliasrvjimenez.myapplication.viewmodel

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
    const val WIKI_HOME = "wiki_home"
    const val WIKI_CLASS_LIST = "wiki_class_list"
    const val WIKI_CLASS_DETAIL = "wiki_class_detail/{classIndex}"
}

class AppViewModel : ViewModel() {
    private val _currentRoute = MutableStateFlow<String?>(null)
    val currentRoute: StateFlow<String?> = _currentRoute

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
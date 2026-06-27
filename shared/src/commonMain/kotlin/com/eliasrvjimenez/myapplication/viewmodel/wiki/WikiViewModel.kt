package com.eliasrvjimenez.myapplication.viewmodel.wiki

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.ApolloClient
import com.eliasrvjimenez.myapplication.GetClassDetailQuery
import com.eliasrvjimenez.myapplication.GetClassesQuery
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class WikiUiState {
    object Loading : WikiUiState()
    data class Success(val classes: List<GetClassesQuery.Class>) : WikiUiState()
    data class DetailSuccess(val classDetail: GetClassDetailQuery.Class) : WikiUiState()
    data class Error(val message: String) : WikiUiState()
}

class WikiViewModel(
    private val apolloClient: ApolloClient,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _uiState = MutableStateFlow<WikiUiState>(WikiUiState.Loading)
    val uiState: StateFlow<WikiUiState> = _uiState

    init {
        val classIndex: String? = savedStateHandle["classIndex"]
        if (classIndex != null) {
            fetchClassDetail(classIndex)
        } else {
            fetchClasses()
        }
    }

    fun fetchClasses() {
        viewModelScope.launch {
            println("WikiViewModel: Fetching classes...")
            _uiState.value = WikiUiState.Loading
            try {
                val response = apolloClient.query(GetClassesQuery()).execute()
                println("WikiViewModel: Response received. hasErrors=${response.hasErrors()}")
                if (response.hasErrors()) {
                    val errorMsg =
                        response.errors?.joinToString { it.message } ?: "Unknown GraphQL error"
                    println("WikiViewModel: GraphQL errors: $errorMsg")
                    _uiState.value = WikiUiState.Error(errorMsg)
                } else {
                    val classes = response.data?.classes?.filterNotNull() ?: emptyList()
                    println("WikiViewModel: Successfully fetched ${classes.size} classes")
                    _uiState.value = WikiUiState.Success(classes)
                }
            } catch (e: Exception) {
                println("WikiViewModel: Exception during fetch: ${e.message}")
                e.printStackTrace()
                _uiState.value = WikiUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun fetchClassDetail(index: String) {
        viewModelScope.launch {
            println("WikiViewModel: Fetching detail for $index...")
            _uiState.value = WikiUiState.Loading
            try {
                val response = apolloClient.query(GetClassDetailQuery(index)).execute()
                println("WikiViewModel: Detail response received. hasErrors=${response.hasErrors()}")
                if (response.hasErrors()) {
                    val errorMsg =
                        response.errors?.joinToString { it.message } ?: "Unknown GraphQL error"
                    _uiState.value = WikiUiState.Error(errorMsg)
                } else {
                    val classDetail = response.data?.`class`
                    if (classDetail != null) {
                        _uiState.value = WikiUiState.DetailSuccess(classDetail)
                    } else {
                        _uiState.value = WikiUiState.Error("Class not found")
                    }
                }
            } catch (e: Exception) {
                println("WikiViewModel: Exception during detail fetch: ${e.message}")
                _uiState.value = WikiUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }
}

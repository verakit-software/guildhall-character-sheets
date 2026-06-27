package com.eliasrvjimenez.myapplication.viewmodel.wiki

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.ApolloClient
import com.eliasrvjimenez.myapplication.GetClassDetailQuery
import com.eliasrvjimenez.myapplication.GetClassesQuery
import com.eliasrvjimenez.myapplication.util.Logger
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
            Logger.d("WikiViewModel: Fetching classes...")
            _uiState.value = WikiUiState.Loading
            try {
                val response = apolloClient.query(GetClassesQuery()).execute()
                Logger.d("WikiViewModel: Response received. hasErrors=${response.hasErrors()}")
                if (response.hasErrors()) {
                    val errorMsg =
                        response.errors?.joinToString { it.message } ?: "Unknown GraphQL error"
                    Logger.d("WikiViewModel: GraphQL errors: $errorMsg")
                    _uiState.value = WikiUiState.Error(errorMsg)
                } else {
                    val classes = response.data?.classes?.filterNotNull() ?: emptyList()
                    Logger.d("WikiViewModel: Successfully fetched ${classes.size} classes")
                    _uiState.value = WikiUiState.Success(classes)
                }
            } catch (e: Exception) {
                Logger.e("WikiViewModel: Exception during fetch", e)
                _uiState.value = WikiUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun fetchClassDetail(index: String) {
        viewModelScope.launch {
            Logger.d("WikiViewModel: Fetching detail for $index...")
            _uiState.value = WikiUiState.Loading
            try {
                val response = apolloClient.query(GetClassDetailQuery(index)).execute()
                Logger.d("WikiViewModel: Detail response received. hasErrors=${response.hasErrors()}")
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
                Logger.e("WikiViewModel: Exception during detail fetch: ${e.message}", e)
                _uiState.value = WikiUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }
}

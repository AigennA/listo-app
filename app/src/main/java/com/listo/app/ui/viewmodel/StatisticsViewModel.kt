package com.listo.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.listo.app.domain.usecases.GetStatisticsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StatisticsUiState(
    val completedCount: Int = 0,
    val pendingCount: Int = 0,
    val completedThisWeek: Int = 0,
    val isLoading: Boolean = true
) {
    val totalCount get() = completedCount + pendingCount
    val completionRate get() =
        if (totalCount == 0) 0f else completedCount.toFloat() / totalCount
}

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val getStatisticsUseCase: GetStatisticsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                getStatisticsUseCase.completedCount(),
                getStatisticsUseCase.pendingCount(),
                getStatisticsUseCase.completedThisWeek()
            ) { completed, pending, week ->
                StatisticsUiState(
                    completedCount = completed,
                    pendingCount = pending,
                    completedThisWeek = week,
                    isLoading = false
                )
            }.collect { state -> _uiState.value = state }
        }
    }
}

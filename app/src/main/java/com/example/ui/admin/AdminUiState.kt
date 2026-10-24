package com.example.ui.admin

import com.example.data.model.QuestionItem

sealed interface AdminUiState {
    object Loading : AdminUiState
    data class Success(
        val subject: String,
        val questions: List<QuestionItem>,
        val selectedGradeFilter: Int? = null,
        val searchQuery: String = "",
        val isSubmitting: Boolean = false,
        val message: String? = null
    ) : AdminUiState
    data class Error(val message: String) : AdminUiState
}

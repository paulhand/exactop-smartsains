package com.example.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.QuestionItem
import com.example.data.repository.SupabaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminViewModel(
    private val repository: SupabaseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AdminUiState>(AdminUiState.Loading)
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    private var currentSubject: String = "Matematika"
    private var currentGradeFilter: Int? = null
    private var currentSearch: String = ""

    init {
        loadData(currentSubject, currentGradeFilter, currentSearch)
    }

    fun setSubject(subject: String) {
        currentSubject = subject
        loadData(currentSubject, currentGradeFilter, currentSearch)
    }

    fun setGradeFilter(grade: Int?) {
        currentGradeFilter = grade
        loadData(currentSubject, currentGradeFilter, currentSearch)
    }

    fun setSearchQuery(query: String) {
        currentSearch = query
        loadData(currentSubject, currentGradeFilter, currentSearch)
    }

    fun loadData(
        subject: String = currentSubject,
        grade: Int? = currentGradeFilter,
        search: String = currentSearch
    ) {
        viewModelScope.launch {
            _uiState.value = AdminUiState.Loading
            val result = repository.getQuestions(subject, grade, search)
            result.onSuccess { list ->
                _uiState.value = AdminUiState.Success(
                    subject = subject,
                    questions = list,
                    selectedGradeFilter = grade,
                    searchQuery = search,
                    isSubmitting = false,
                    message = null
                )
            }.onFailure { err ->
                _uiState.value = AdminUiState.Error(err.message ?: "Gagal memuat daftar soal")
            }
        }
    }

    fun addQuestion(question: QuestionItem) {
        val current = _uiState.value as? AdminUiState.Success ?: return
        viewModelScope.launch {
            _uiState.value = current.copy(isSubmitting = true)
            val result = repository.insertQuestion(current.subject, question)
            result.onSuccess {
                loadData(current.subject, current.selectedGradeFilter, current.searchQuery)
            }.onFailure { err ->
                _uiState.value = current.copy(isSubmitting = false, message = "Error: ${err.message}")
            }
        }
    }

    fun updateQuestion(question: QuestionItem) {
        val current = _uiState.value as? AdminUiState.Success ?: return
        viewModelScope.launch {
            _uiState.value = current.copy(isSubmitting = true)
            val result = repository.updateQuestion(current.subject, question)
            result.onSuccess {
                loadData(current.subject, current.selectedGradeFilter, current.searchQuery)
            }.onFailure { err ->
                _uiState.value = current.copy(isSubmitting = false, message = "Error: ${err.message}")
            }
        }
    }

    fun deleteQuestion(id: Long, questionText: String) {
        val current = _uiState.value as? AdminUiState.Success ?: return
        viewModelScope.launch {
            _uiState.value = current.copy(isSubmitting = true)
            val result = repository.deleteQuestion(current.subject, id, questionText)
            result.onSuccess {
                loadData(current.subject, current.selectedGradeFilter, current.searchQuery)
            }.onFailure { err ->
                _uiState.value = current.copy(isSubmitting = false, message = "Error: ${err.message}")
            }
        }
    }

    fun clearMessage() {
        val current = _uiState.value as? AdminUiState.Success ?: return
        _uiState.value = current.copy(message = null)
    }
}

package com.example.ui.quiz

import com.example.data.model.QuestionItem

sealed interface QuizUiState {
    object Idle : QuizUiState
    object Loading : QuizUiState

    data class InProgress(
        val subject: String,
        val questions: List<QuestionItem>,
        val currentIndex: Int,
        val remainingSeconds: Int,
        val totalSecondsForCurrent: Int = 60,
        val temporaryScores: List<Int>,
        val currentFeedback: QuestionFeedback? = null
    ) : QuizUiState

    data class Completed(
        val subject: String,
        val sessionTotalScore: Int,
        val sessionScores: List<Int>,
        val totalCumulativeScore: Double,
        val numSesi: Int,
        val meanScore: Double,
        val modusScore: Double,
        val medianScore: Double,
        val highestScore: Double = 0.0,
        val lowestScore: Double = 0.0,
        val userRank: Int,
        val totalMembers: Int,
        val results: List<QuestionResult>,
        val updateError: UpdateSkorErrorInfo? = null,
        val rawStoredSkor: String = "",
        val allCumulativeScores: List<Double> = emptyList()
    ) : QuizUiState

    data class Error(val message: String) : QuizUiState
}

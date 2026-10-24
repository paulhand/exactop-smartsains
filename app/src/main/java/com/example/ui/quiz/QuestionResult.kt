package com.example.ui.quiz

import com.example.data.model.QuestionItem

data class QuestionResult(
    val questionIndex: Int,
    val question: QuestionItem,
    val selectedOption: String?,
    val isCorrect: Boolean,
    val scoreEarned: Int,
    val isTimeout: Boolean
)

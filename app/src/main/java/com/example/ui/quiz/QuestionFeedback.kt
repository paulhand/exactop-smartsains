package com.example.ui.quiz

data class QuestionFeedback(
    val isCorrect: Boolean,
    val isTimeout: Boolean,
    val scoreEarned: Int,
    val message: String,
    val correctAnswer: String,
    val explanation: String
)

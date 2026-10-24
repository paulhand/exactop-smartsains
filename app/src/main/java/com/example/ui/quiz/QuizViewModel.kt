package com.example.ui.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Member
import com.example.data.model.QuestionItem
import com.example.data.repository.SupabaseRepository
import com.example.data.repository.SupabaseUpdateException
import com.example.util.StatsUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class QuizViewModel(
    private val repository: SupabaseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<QuizUiState>(QuizUiState.Idle)
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private val sessionResults = mutableListOf<QuestionResult>()

    fun startQuiz(subject: String, userMember: Member) {
        _uiState.value = QuizUiState.Loading
        sessionResults.clear()
        viewModelScope.launch {
            val result = repository.getRandom10Questions(subject, userMember.grade)
            result.onSuccess { questions ->
                _uiState.value = QuizUiState.InProgress(
                    subject = subject,
                    questions = questions,
                    currentIndex = 0,
                    remainingSeconds = 15,
                    temporaryScores = emptyList(),
                    currentFeedback = null
                )
                startTimer()
            }.onFailure { error ->
                _uiState.value = QuizUiState.Error(error.message ?: "Gagal memulai kuis")
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val current = _uiState.value as? QuizUiState.InProgress ?: break
                if (current.currentFeedback != null) break

                val newRemaining = current.remainingSeconds - 1
                if (newRemaining <= 0) {
                    handleTimeout()
                    break
                } else {
                    _uiState.value = current.copy(remainingSeconds = newRemaining)
                }
            }
        }
    }

    private fun handleTimeout() {
        val current = _uiState.value as? QuizUiState.InProgress ?: return
        val currentQuestion = current.questions[current.currentIndex]

        val result = QuestionResult(
            questionIndex = current.currentIndex,
            question = currentQuestion,
            selectedOption = null,
            isCorrect = false,
            scoreEarned = 0,
            isTimeout = true
        )
        sessionResults.add(result)

        val updatedScores = current.temporaryScores + 0
        val feedback = QuestionFeedback(
            isCorrect = false,
            isTimeout = true,
            scoreEarned = 0,
            message = "User tidak menjawab. Skor = 0",
            correctAnswer = currentQuestion.jawaban_benar,
            explanation = currentQuestion.pembahasan ?: ""
        )

        _uiState.value = current.copy(
            remainingSeconds = 0,
            temporaryScores = updatedScores,
            currentFeedback = feedback
        )
    }

    fun submitAnswer(optionKey: String) {
        val current = _uiState.value as? QuizUiState.InProgress ?: return
        if (current.currentFeedback != null) return

        timerJob?.cancel()
        val currentQuestion = current.questions[current.currentIndex]
        val isCorrect = checkAnswerMatch(optionKey, currentQuestion)
        val scoreEarned = if (isCorrect) currentQuestion.skor else 0

        val result = QuestionResult(
            questionIndex = current.currentIndex,
            question = currentQuestion,
            selectedOption = optionKey,
            isCorrect = isCorrect,
            scoreEarned = scoreEarned,
            isTimeout = false
        )
        sessionResults.add(result)

        val updatedScores = current.temporaryScores + scoreEarned
        val feedback = QuestionFeedback(
            isCorrect = isCorrect,
            isTimeout = false,
            scoreEarned = scoreEarned,
            message = if (isCorrect) "BENAR" else "SALAH",
            correctAnswer = currentQuestion.jawaban_benar,
            explanation = currentQuestion.pembahasan ?: ""
        )

        _uiState.value = current.copy(
            temporaryScores = updatedScores,
            currentFeedback = feedback
        )
    }

    private fun checkAnswerMatch(selectedKey: String, question: QuestionItem): Boolean {
        val key = selectedKey.trim().uppercase()
        val correct = question.jawaban_benar.trim()

        // Match option letter directly (A, B, C, D)
        if (key.equals(correct, ignoreCase = true)) return true

        // Match option text if database stores the full text in jawaban_benar
        val optionText = when (key) {
            "A" -> question.opsi_a
            "B" -> question.opsi_b
            "C" -> question.opsi_c
            "D" -> question.opsi_d
            else -> ""
        }
        return optionText.trim().equals(correct, ignoreCase = true)
    }

    fun nextQuestion(userMember: Member) {
        val current = _uiState.value as? QuizUiState.InProgress ?: return
        val nextIndex = current.currentIndex + 1

        if (nextIndex < current.questions.size) {
            _uiState.value = current.copy(
                currentIndex = nextIndex,
                remainingSeconds = 15,
                currentFeedback = null
            )
            startTimer()
        } else {
            finishQuizSession(current.subject, current.temporaryScores, userMember)
        }
    }

    private fun finishQuizSession(
        subject: String,
        sessionScores: List<Int>,
        userMember: Member
    ) {
        _uiState.value = QuizUiState.Loading
        viewModelScope.launch {
            val sessionTotal = sessionScores.sum()
            val currentSkorAkumulasi = userMember.parsedSkorAkumulasi
            val updatedSkor = StatsUtils.updateSkorAkumulasi(
                current = currentSkorAkumulasi,
                newSessionScore = sessionTotal.toDouble()
            )

            var updateError: UpdateSkorErrorInfo? = null
            val email = userMember.email
            val storedArrayString = updatedSkor.toStoredString()
            if (!email.isNullOrBlank()) {
                val updateResult = repository.updateMemberSkorAkumulasi(email, storedArrayString)
                updateResult.onFailure { err ->
                    if (err is SupabaseUpdateException) {
                        updateError = UpdateSkorErrorInfo(
                            errorMessage = err.message,
                            queryString = err.queryString,
                            payload = err.payload
                        )
                    } else {
                        updateError = UpdateSkorErrorInfo(
                            errorMessage = err.message ?: "Update gagal",
                            queryString = "PATCH member?email=eq.$email",
                            payload = "{\"skor_akumulasi\": \"$storedArrayString\"}"
                        )
                    }
                }
            }

            val allMembers = repository.getAllMembers()
            val sorted = allMembers.map {
                it.email to if (it.email.equals(userMember.email, ignoreCase = true)) updatedSkor.mean else it.parsedSkorAkumulasi.mean
            }.sortedByDescending { it.second }
            val rankIdx = sorted.indexOfFirst { it.first.equals(userMember.email, ignoreCase = true) }
            val rank = if (rankIdx >= 0) rankIdx + 1 else 1

            _uiState.value = QuizUiState.Completed(
                subject = subject,
                sessionTotalScore = sessionTotal,
                sessionScores = sessionScores,
                totalCumulativeScore = updatedSkor.total,
                numSesi = updatedSkor.numsesi,
                meanScore = updatedSkor.mean,
                modusScore = updatedSkor.modus,
                medianScore = updatedSkor.median,
                userRank = rank,
                totalMembers = maxOf(allMembers.size, 1),
                results = sessionResults.toList(),
                updateError = updateError,
                rawStoredSkor = storedArrayString,
                allCumulativeScores = updatedSkor.scores
            )
        }
    }

    fun reset() {
        timerJob?.cancel()
        _uiState.value = QuizUiState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}

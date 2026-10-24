package com.example.util

import com.example.data.model.SkorAkumulasi

object StatsUtils {
    /**
     * Calculates the time allotted for a question based on its score:
     * - Lowest score (10) gives 60 seconds.
     * - For each score increase of 10, increases time by 30 seconds.
     */
    fun calculateQuestionTimeSeconds(skor: Int): Int {
        val baseTime = 60
        if (skor <= 10) return baseTime
        val extraScore = skor - 10
        val extraSeconds = (extraScore * 30) / 10
        return baseTime + extraSeconds
    }

    fun updateSkorAkumulasi(
        current: SkorAkumulasi,
        newSessionScore: Double
    ): SkorAkumulasi {
        val newScores = if (current.scores.isNotEmpty()) {
            current.scores + newSessionScore
        } else if (current.numsesi > 0) {
            // Reconstruct history if previous record had only aggregated count & mean
            List(current.numsesi) { current.mean } + newSessionScore
        } else {
            listOf(newSessionScore)
        }

        val newNumSesi = newScores.size
        val newTotal = newScores.sum()
        val newMean = if (newNumSesi > 0) newTotal / newNumSesi else 0.0
        val modus = calculateModus(newScores)
        val median = calculateMedian(newScores)
        val highest = newScores.maxOrNull() ?: 0.0
        val lowest = newScores.minOrNull() ?: 0.0

        return SkorAkumulasi(
            scores = newScores,
            total = newTotal,
            numsesi = newNumSesi,
            mean = newMean,
            modus = modus,
            median = median,
            highest = highest,
            lowest = lowest
        )
    }

    fun calculateModus(numbers: List<Double>): Double {
        if (numbers.isEmpty()) return 0.0
        val frequencyMap = LinkedHashMap<Double, Int>()
        for (num in numbers) {
            frequencyMap[num] = (frequencyMap[num] ?: 0) + 1
        }
        var maxCount = 0
        var mode = numbers.first()
        for ((num, count) in frequencyMap) {
            if (count > maxCount) {
                maxCount = count
                mode = num
            }
        }
        return mode
    }

    fun calculateMedian(numbers: List<Double>): Double {
        if (numbers.isEmpty()) return 0.0
        val sorted = numbers.sorted()
        val size = sorted.size
        return if (size % 2 == 1) {
            sorted[size / 2]
        } else {
            (sorted[size / 2 - 1] + sorted[size / 2]) / 2.0
        }
    }
}

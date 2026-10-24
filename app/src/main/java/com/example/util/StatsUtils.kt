package com.example.util

import com.example.data.model.SkorAkumulasi

object StatsUtils {
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

        return SkorAkumulasi(
            scores = newScores,
            total = newTotal,
            numsesi = newNumSesi,
            mean = newMean,
            modus = modus,
            median = median
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

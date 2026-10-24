package com.example.data.model

import com.example.util.StatsUtils
import com.squareup.moshi.Json
import java.util.Locale

data class SkorAkumulasi(
    val scores: List<Double> = emptyList(),
    @Json(name = "total") val total: Double = 0.0,
    @Json(name = "numsesi") val numsesi: Int = 0,
    @Json(name = "mean") val mean: Double = 0.0,
    @Json(name = "modus") val modus: Double = 0.0,
    @Json(name = "median") val median: Double = 0.0
) {
    /**
     * Stored string representation in array format, delimited by comma: [180, 180, 260, 235, 265]
     */
    fun toStoredString(): String {
        return "[${scores.joinToString(", ") { formatNumber(it) }}]"
    }

    fun toJsonString(): String = toStoredString()

    companion object {
        fun formatNumber(num: Double): String {
            return if (num % 1.0 == 0.0) {
                num.toLong().toString()
            } else {
                String.format(Locale.US, "%.2f", num).trimEnd('0').trimEnd('.')
            }
        }

        /**
         * Parses skor_akumulasi from any of:
         * 1) Standard array format: "[180, 180, 260, 235, 265]"
         * 2) JSON object with key skor: {"skor": "180, 180, 260, 235, 265"} or {"skor": [180, 180, ...]}
         * 3) Bare comma-delimited: "180, 180, 260, 235, 265"
         * 4) Legacy JSON object: {"total": 1120, "numsesi": 5, "mean": 224.00, "modus": 213.75, "median": 213.75}
         */
        fun fromJsonString(raw: String?): SkorAkumulasi {
            if (raw.isNullOrBlank()) {
                return SkorAkumulasi()
            }
            val trimmed = raw.trim()

            // Check if it's legacy JSON format: {"total": ..., "numsesi": ...} without an array or "skor" key
            if (trimmed.startsWith("{") && trimmed.contains("\"total\"") && !trimmed.contains("\"skor\"")) {
                val total = extractNum(trimmed, "total") ?: 0.0
                val numsesi = (extractNum(trimmed, "numsesi") ?: 0.0).toInt()
                val mean = extractNum(trimmed, "mean") ?: (if (numsesi > 0) total / numsesi else 0.0)
                val modus = extractNum(trimmed, "modus") ?: mean
                val median = extractNum(trimmed, "median") ?: mean
                val legacyScores = if (numsesi > 0) List(numsesi) { mean } else emptyList()
                return SkorAkumulasi(
                    scores = legacyScores,
                    total = total,
                    numsesi = numsesi,
                    mean = mean,
                    modus = modus,
                    median = median
                )
            }

            var contentToParse = trimmed
            // Check for {"skor": "..."} or {"skor": [...]}
            if (trimmed.startsWith("{")) {
                val skorMatch = Regex("\"skor\"\\s*:\\s*(\"[^\"]*\"|\\[[^\\]]*\\])", RegexOption.IGNORE_CASE).find(trimmed)
                if (skorMatch != null) {
                    contentToParse = skorMatch.groupValues[1].trim('"', ' ')
                }
            }

            val cleanStr = contentToParse.trim().removeSurrounding("[", "]").trim()
            if (cleanStr.isBlank()) {
                return SkorAkumulasi()
            }

            val parsedScores = cleanStr.split(",")
                .mapNotNull { it.trim().toDoubleOrNull() }

            if (parsedScores.isEmpty()) {
                return SkorAkumulasi()
            }

            val total = parsedScores.sum()
            val numsesi = parsedScores.size
            val mean = total / numsesi
            val modus = StatsUtils.calculateModus(parsedScores)
            val median = StatsUtils.calculateMedian(parsedScores)

            return SkorAkumulasi(
                scores = parsedScores,
                total = total,
                numsesi = numsesi,
                mean = mean,
                modus = modus,
                median = median
            )
        }

        private fun extractNum(json: String, key: String): Double? {
            val regex = Regex("[\"']?$key[\"']?\\s*:\\s*[\"']?([0-9.eE+-]+)[\"']?", RegexOption.IGNORE_CASE)
            val match = regex.find(json)
            val str = match?.groupValues?.getOrNull(1) ?: return null
            return str.toDoubleOrNull()
        }
    }
}

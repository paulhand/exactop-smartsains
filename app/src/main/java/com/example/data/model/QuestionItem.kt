package com.example.data.model

import com.squareup.moshi.Json

data class QuestionImages(
    val questUrl: String? = null,
    val ansAUrl: String? = null,
    val ansBUrl: String? = null,
    val ansCUrl: String? = null,
    val ansDUrl: String? = null
) {
    val hasAnyImage: Boolean
        get() = questUrl != null || ansAUrl != null || ansBUrl != null || ansCUrl != null || ansDUrl != null

    companion object {
        const val BASE_URL = "https://aoplaetqkabshapkapwf.storage.supabase.co/storage/v1/object/public/ExacTop/"

        fun parse(image1: String?): QuestionImages {
            if (image1.isNullOrBlank()) {
                return QuestionImages()
            }
            val quest = extractUrl(image1, "quest")
            val ansA = extractUrl(image1, "ansA")
            val ansB = extractUrl(image1, "ansB")
            val ansC = extractUrl(image1, "ansC")
            val ansD = extractUrl(image1, "ansD")
            return QuestionImages(
                questUrl = quest,
                ansAUrl = ansA,
                ansBUrl = ansB,
                ansCUrl = ansC,
                ansDUrl = ansD
            )
        }

        private fun extractUrl(raw: String, key: String): String? {
            val regex = Regex("['\"]$key['\"]\\s*:\\s*['\"]([^'\"]*)['\"]", RegexOption.IGNORE_CASE)
            val match = regex.find(raw) ?: return null
            val path = match.groupValues.getOrNull(1)?.trim() ?: return null
            if (path.isBlank()) return null
            return if (path.startsWith("http://") || path.startsWith("https://")) {
                path
            } else {
                val cleanPath = path.removePrefix("/")
                "$BASE_URL$cleanPath"
            }
        }
    }
}

data class QuestionItem(
    @Json(name = "id") val id: Long? = null,
    @Json(name = "pertanyaan") val pertanyaan: String = "",
    @Json(name = "opsi_a") val opsi_a: String = "",
    @Json(name = "opsi_b") val opsi_b: String = "",
    @Json(name = "opsi_c") val opsi_c: String = "",
    @Json(name = "opsi_d") val opsi_d: String = "",
    @Json(name = "jawaban_benar") val jawaban_benar: String = "",
    @Json(name = "pembahasan") val pembahasan: String? = null,
    @Json(name = "mode") val mode: Int = 1,
    @Json(name = "skor") val skor: Int = 10,
    @Json(name = "grade") val grade: Int = 10,
    @Json(name = "image1") val image1: String? = null
) {
    val parsedImages: QuestionImages
        get() = QuestionImages.parse(image1)

    fun toInsertRequest(): QuestionInsertRequest {
        return QuestionInsertRequest(
            pertanyaan = pertanyaan,
            mode = mode,
            opsi_a = opsi_a,
            opsi_b = opsi_b,
            opsi_c = opsi_c,
            opsi_d = opsi_d,
            jawaban_benar = jawaban_benar,
            pembahasan = pembahasan ?: "",
            grade = grade,
            skor = skor,
            image1 = image1 ?: ""
        )
    }
}

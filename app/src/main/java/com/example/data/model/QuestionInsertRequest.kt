package com.example.data.model

import com.squareup.moshi.Json

data class QuestionInsertRequest(
    @Json(name = "pertanyaan") val pertanyaan: String = "",
    @Json(name = "mode") val mode: Int = 1,
    @Json(name = "opsi_a") val opsi_a: String = "",
    @Json(name = "opsi_b") val opsi_b: String = "",
    @Json(name = "opsi_c") val opsi_c: String = "",
    @Json(name = "opsi_d") val opsi_d: String = "",
    @Json(name = "jawaban_benar") val jawaban_benar: String = "",
    @Json(name = "pembahasan") val pembahasan: String = "",
    @Json(name = "grade") val grade: Int = 10,
    @Json(name = "skor") val skor: Int = 10,
    @Json(name = "image1") val image1: String = ""
)

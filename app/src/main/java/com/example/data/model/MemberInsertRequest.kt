package com.example.data.model

import com.squareup.moshi.Json

data class MemberInsertRequest(
    @Json(name = "nama") val nama: String = "",
    @Json(name = "tgl_lahir") val tgl_lahir: String = "",
    @Json(name = "nik") val nik: String? = null,
    @Json(name = "email") val email: String = "",
    @Json(name = "enc_pass") val enc_pass: String = "",
    @Json(name = "grade") val grade: Int = 10,
    @Json(name = "skor_akumulasi") val skor_akumulasi: String = "[]"
)

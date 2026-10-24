package com.example.data.model

import com.squareup.moshi.Json

data class Member(
    @Json(name = "id") val id: Long? = null,
    @Json(name = "nama") val nama: String = "",
    @Json(name = "tgl_lahir") val tgl_lahir: String = "",
    @Json(name = "nik") val nik: String? = null,
    @Json(name = "email") val email: String? = null,
    @Json(name = "enc_pass") val enc_pass: String = "",
    @Json(name = "grade") val grade: Int = 10,
    @Json(name = "skor_akumulasi") val skor_akumulasi: String? = "[]"
) {
    val parsedSkorAkumulasi: SkorAkumulasi
        get() = SkorAkumulasi.fromJsonString(skor_akumulasi)

    fun toInsertRequest(): MemberInsertRequest {
        return MemberInsertRequest(
            nama = nama,
            tgl_lahir = tgl_lahir,
            nik = nik,
            email = email ?: "",
            enc_pass = enc_pass,
            grade = grade,
            skor_akumulasi = skor_akumulasi ?: "[]"
        )
    }
}

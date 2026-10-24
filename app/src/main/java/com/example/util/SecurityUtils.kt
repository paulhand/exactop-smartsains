package com.example.util

import java.security.MessageDigest

object SecurityUtils {
    fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(input.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    fun isValidNama(nama: String): Boolean {
        if (nama.isBlank()) return false
        val regex = Regex("^[a-zA-Z\\s\\.\\'\\`\\-]+$")
        return regex.matches(nama.trim())
    }

    fun isValidNik(nik: String): Boolean {
        if (nik.isBlank()) return false
        val regex = Regex("^[0-9]+$")
        return regex.matches(nik.trim())
    }

    fun isValidGrade(gradeStr: String): Boolean {
        val num = gradeStr.trim().toIntOrNull()
        return num != null && num in 1..12
    }
}

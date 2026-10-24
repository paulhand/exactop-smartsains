package com.example.data.repository

data class SupabaseUpdateException(
    override val message: String,
    val queryString: String,
    val payload: String
) : Exception(message)

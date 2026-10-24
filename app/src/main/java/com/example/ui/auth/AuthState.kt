package com.example.ui.auth

import com.example.data.model.Member

sealed interface AuthState {
    object Idle : AuthState
    object Loading : AuthState
    data class Authenticated(val member: Member) : AuthState
    data class Error(val message: String) : AuthState
}

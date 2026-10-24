package com.example.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Member
import com.example.data.repository.SupabaseRepository
import com.example.util.SecurityUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: SupabaseRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentMember = MutableStateFlow<Member?>(null)
    val currentMember: StateFlow<Member?> = _currentMember.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _allMembers = MutableStateFlow<List<Member>>(emptyList())
    val allMembers: StateFlow<List<Member>> = _allMembers.asStateFlow()

    init {
        loadAllMembers()
    }

    fun setCurrentMember(member: Member?) {
        _currentMember.value = member
        if (member != null) {
            repository.setCurrentUser(member)
        }
    }

    fun refreshCurrentMember(email: String? = null) {
        val targetEmail = email
            ?: _currentMember.value?.email
            ?: repository.currentMember?.email

        if (!targetEmail.isNullOrBlank()) {
            viewModelScope.launch {
                _isRefreshing.value = true
                val result = repository.getMemberByEmail(targetEmail)
                result.onSuccess { freshMember ->
                    _currentMember.value = freshMember
                    if (_authState.value is AuthState.Authenticated) {
                        _authState.value = AuthState.Authenticated(freshMember)
                    }
                }
                val all = repository.getAllMembers()
                _allMembers.value = all
                _isRefreshing.value = false
            }
        } else {
            loadAllMembers()
        }
    }

    fun loadAllMembers() {
        viewModelScope.launch {
            val all = repository.getAllMembers()
            _allMembers.value = all
        }
    }

    fun login(email: String, clearTextPass: String) {
        if (email.isBlank()) {
            _authState.value = AuthState.Error("Field Email kosong! Anda harus mengisi field ini.")
            return
        }
        if (clearTextPass.isBlank()) {
            _authState.value = AuthState.Error("Field Password kosong! Anda harus mengisi field ini.")
            return
        }

        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = repository.login(email.trim(), clearTextPass)
            result.onSuccess { member ->
                _currentMember.value = member
                _authState.value = AuthState.Authenticated(member)
                loadAllMembers()
            }.onFailure { err ->
                _authState.value = AuthState.Error(err.message ?: "Login gagal")
            }
        }
    }

    fun register(
        nama: String,
        tglLahir: String,
        nik: String,
        email: String,
        clearTextPass: String,
        confirmPass: String,
        gradeStr: String
    ) {
        if (nama.isBlank() || tglLahir.isBlank() || nik.isBlank() || email.isBlank() || clearTextPass.isBlank()) {
            _authState.value = AuthState.Error("Semua field wajib diisi!")
            return
        }
        if (!SecurityUtils.isValidNama(nama)) {
            _authState.value = AuthState.Error("Format Nama tidak valid! Hanya huruf, spasi, dan tanda baca standar.")
            return
        }
        if (!SecurityUtils.isValidNik(nik)) {
            _authState.value = AuthState.Error("NIK harus berupa angka!")
            return
        }
        if (!email.contains("@")) {
            _authState.value = AuthState.Error("Format Email tidak valid!")
            return
        }
        if (clearTextPass != confirmPass) {
            _authState.value = AuthState.Error("Konfirmasi password tidak cocok!")
            return
        }
        val grade = gradeStr.toIntOrNull() ?: 10

        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = repository.register(nama, tglLahir, nik, email, clearTextPass, grade)
            result.onSuccess { member ->
                _currentMember.value = member
                _authState.value = AuthState.Authenticated(member)
                loadAllMembers()
            }.onFailure { err ->
                _authState.value = AuthState.Error(err.message ?: "Registrasi gagal")
            }
        }
    }

    fun logout() {
        repository.logout()
        _currentMember.value = null
        _authState.value = AuthState.Idle
    }

    fun clearError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.Idle
        }
    }
}

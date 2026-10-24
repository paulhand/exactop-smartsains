package com.example.data.repository

import com.example.data.api.SupabaseApi
import com.example.data.api.SupabaseClient
import com.example.data.model.Member
import com.example.data.model.MemberInsertRequest
import com.example.data.model.QuestionInsertRequest
import com.example.data.model.QuestionItem
import com.example.util.SecurityUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SupabaseRepository(
    private val api: SupabaseApi = SupabaseClient.api
) {
    var currentMember: Member? = null
        private set

    fun setCurrentUser(member: Member?) {
        currentMember = member
    }

    fun logout() {
        currentMember = null
    }

    suspend fun login(email: String, clearTextPass: String): Result<Member> = withContext(Dispatchers.IO) {
        try {
            val trimmedEmail = email.trim()
            val response = api.getMembers(emailFilter = "eq.$trimmedEmail")
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP ${response.code()} ${response.message()}: ${response.errorBody()?.string()}"))
            }
            val members = response.body()
            if (members.isNullOrEmpty()) {
                return@withContext Result.failure(Exception("Akun dengan email $trimmedEmail tidak ditemukan!"))
            }
            val member = members.first()
            val hashEntered = SecurityUtils.sha256(clearTextPass)
            if (member.enc_pass.equals(hashEntered, ignoreCase = true) || member.enc_pass == clearTextPass) {
                currentMember = member
                Result.success(member)
            } else {
                Result.failure(Exception("Password salah! Silakan coba lagi."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Koneksi gagal: ${e.localizedMessage ?: e.javaClass.simpleName}"))
        }
    }

    suspend fun getMemberByEmail(email: String): Result<Member> = withContext(Dispatchers.IO) {
        try {
            val trimmedEmail = email.trim()
            val response = api.getMembers(emailFilter = "eq.$trimmedEmail")
            if (response.isSuccessful) {
                val members = response.body()
                if (!members.isNullOrEmpty()) {
                    val freshMember = members.first()
                    currentMember = freshMember
                    return@withContext Result.success(freshMember)
                }
            }
            Result.failure(Exception("Member tidak ditemukan"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(
        nama: String,
        tglLahir: String,
        nik: String,
        email: String,
        clearTextPass: String,
        grade: Int
    ): Result<Member> = withContext(Dispatchers.IO) {
        try {
            val trimmedEmail = email.trim()
            val checkResp = api.getMembers(emailFilter = "eq.$trimmedEmail")
            if (checkResp.isSuccessful && !checkResp.body().isNullOrEmpty()) {
                return@withContext Result.failure(Exception("Email $trimmedEmail sudah terdaftar. Silakan login!"))
            }

            val encPass = SecurityUtils.sha256(clearTextPass)
            val req = MemberInsertRequest(
                nama = nama.trim(),
                tgl_lahir = tglLahir.trim(),
                nik = nik.trim(),
                email = trimmedEmail,
                enc_pass = encPass,
                grade = grade,
                skor_akumulasi = "[]"
            )

            val insertResp = api.insertMember(member = req)
            if (insertResp.isSuccessful) {
                val created = insertResp.body()?.firstOrNull() ?: Member(
                    nama = req.nama,
                    tgl_lahir = req.tgl_lahir,
                    nik = req.nik,
                    email = req.email,
                    enc_pass = req.enc_pass,
                    grade = req.grade,
                    skor_akumulasi = req.skor_akumulasi
                )
                currentMember = created
                Result.success(created)
            } else {
                Result.failure(Exception("Gagal registrasi: HTTP ${insertResp.code()} - ${insertResp.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Gagal registrasi: ${e.localizedMessage ?: e.javaClass.simpleName}"))
        }
    }

    suspend fun updateMemberSkorAkumulasi(email: String, newSkorValue: String): Result<Member> = withContext(Dispatchers.IO) {
        val trimmedEmail = email.trim()
        val updatePayload = mapOf("skor_akumulasi" to newSkorValue)
        val queryUrl = "https://aoplaetqkabshapkapwf.supabase.co/rest/v1/member?email=eq.$trimmedEmail"
        val payloadFormatted = "{\"skor_akumulasi\": \"$newSkorValue\"}"

        try {
            val response = api.updateMember(
                table = "member",
                emailFilter = "eq.$trimmedEmail",
                updatePayload = updatePayload
            )
            if (response.isSuccessful) {
                val list = response.body()
                if (!list.isNullOrEmpty()) {
                    val updated = list.first()
                    currentMember = updated
                    return@withContext Result.success(updated)
                }
            }

            val errBody = response.errorBody()?.string() ?: "Data respons kosong (tidak ada baris yang cocok / RLS terblokir)"
            val errMessage = "Gagal update: HTTP ${response.code()} ${response.message()} - $errBody"
            Result.failure(SupabaseUpdateException(errMessage, "PATCH $queryUrl", payloadFormatted))
        } catch (e: Exception) {
            val msg = "Koneksi Supabase error: ${e.localizedMessage ?: e.javaClass.simpleName}"
            Result.failure(SupabaseUpdateException(msg, "PATCH $queryUrl", payloadFormatted))
        }
    }

    suspend fun getAllMembers(): List<Member> = withContext(Dispatchers.IO) {
        try {
            val response = api.getMembers(table = "member", select = "*")
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun normalizeSubject(subject: String): String {
        return when (subject.trim().lowercase()) {
            "matematika" -> "matematika"
            "fisika" -> "fisika"
            "kimia" -> "kimia"
            else -> subject.trim().lowercase()
        }
    }

    suspend fun getQuestions(
        subject: String,
        gradeFilter: Int? = null,
        searchQuery: String = ""
    ): Result<List<QuestionItem>> = withContext(Dispatchers.IO) {
        try {
            val table = normalizeSubject(subject)
            val gradeQuery = gradeFilter?.let { "eq.$it" }
            val response = api.getQuestions(table = table, gradeFilter = gradeQuery)
            if (response.isSuccessful) {
                val list = response.body() ?: emptyList()
                val filtered = if (searchQuery.isNotBlank()) {
                    list.filter {
                        it.pertanyaan.contains(searchQuery, ignoreCase = true) ||
                        it.opsi_a.contains(searchQuery, ignoreCase = true) ||
                        it.opsi_b.contains(searchQuery, ignoreCase = true) ||
                        it.opsi_c.contains(searchQuery, ignoreCase = true) ||
                        it.opsi_d.contains(searchQuery, ignoreCase = true)
                    }
                } else {
                    list
                }
                Result.success(filtered)
            } else {
                Result.failure(Exception("Gagal mengambil soal: HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Gagal memuat soal: ${e.localizedMessage ?: e.javaClass.simpleName}"))
        }
    }

    suspend fun getRandom10Questions(subject: String, grade: Int): Result<List<QuestionItem>> = withContext(Dispatchers.IO) {
        try {
            val table = normalizeSubject(subject)
            val response = api.getQuestions(table = table, select = "*")
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Gagal mengambil soal dari server! HTTP ${response.code()}"))
            }
            val allQuestions = response.body().orEmpty()
            if (allQuestions.isEmpty()) {
                return@withContext Result.failure(Exception("Tidak ada soal tersedia untuk mata pelajaran $subject"))
            }
            // User is eligible for questions in their registered grade and up to 3 levels lower (e.g. Grade 9 is eligible for Grade 6, 7, 8, 9)
            val minGrade = (grade - 3).coerceAtLeast(1)
            val eligibleRange = minGrade..grade
            val eligibleQuestions = allQuestions.filter { it.grade in eligibleRange }
            val candidatePool = if (eligibleQuestions.size >= 5) eligibleQuestions else allQuestions
            val selected = candidatePool.shuffled().take(10)
            Result.success(selected)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal memuat soal kuis: ${e.localizedMessage ?: e.javaClass.simpleName}"))
        }
    }

    suspend fun insertQuestion(subject: String, question: QuestionItem): Result<QuestionItem> = withContext(Dispatchers.IO) {
        try {
            val table = normalizeSubject(subject)
            val response = api.insertQuestion(table = table, question = question.toInsertRequest())
            if (response.isSuccessful) {
                val created = response.body()?.firstOrNull() ?: question
                Result.success(created)
            } else {
                Result.failure(Exception("Gagal menambah soal: HTTP ${response.code()} - ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error tambah soal: ${e.localizedMessage ?: e.javaClass.simpleName}"))
        }
    }

    suspend fun updateQuestion(subject: String, question: QuestionItem): Result<QuestionItem> = withContext(Dispatchers.IO) {
        try {
            val id = question.id ?: return@withContext Result.failure(Exception("ID soal kosong!"))
            val table = normalizeSubject(subject)
            val response = api.updateQuestion(
                table = table,
                idFilter = "eq.$id",
                question = question.toInsertRequest()
            )
            if (response.isSuccessful) {
                val updated = response.body()?.firstOrNull() ?: question
                Result.success(updated)
            } else {
                Result.failure(Exception("Gagal memperbarui soal: HTTP ${response.code()} - ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error update soal: ${e.localizedMessage ?: e.javaClass.simpleName}"))
        }
    }

    suspend fun deleteQuestion(subject: String, id: Long, questionText: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val table = normalizeSubject(subject)
            val response = api.deleteQuestion(table = table, idFilter = "eq.$id")
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Gagal menghapus soal: HTTP ${response.code()} - ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error hapus soal: ${e.localizedMessage ?: e.javaClass.simpleName}"))
        }
    }
}

package com.example.data.api

import com.example.data.model.Member
import com.example.data.model.MemberInsertRequest
import com.example.data.model.QuestionInsertRequest
import com.example.data.model.QuestionItem
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface SupabaseApi {
    @GET("rest/v1/{table}")
    suspend fun getQuestions(
        @Path("table") table: String,
        @Query("select") select: String = "*",
        @Query("grade") gradeFilter: String? = null,
        @Query("limit") limit: Int? = null,
        @Query("order") order: String? = null
    ): Response<List<QuestionItem>>

    @POST("rest/v1/{table}")
    suspend fun insertQuestion(
        @Path("table") table: String,
        @Header("Prefer") prefer: String = "return=representation",
        @Body question: QuestionInsertRequest
    ): Response<List<QuestionItem>>

    @PATCH("rest/v1/{table}")
    suspend fun updateQuestion(
        @Path("table") table: String,
        @Query("id") idFilter: String,
        @Header("Prefer") prefer: String = "return=representation",
        @Body question: QuestionInsertRequest
    ): Response<List<QuestionItem>>

    @DELETE("rest/v1/{table}")
    suspend fun deleteQuestion(
        @Path("table") table: String,
        @Query("id") idFilter: String
    ): Response<ResponseBody>

    @GET("rest/v1/{table}")
    suspend fun getMembers(
        @Path("table") table: String = "member",
        @Query("select") select: String = "*",
        @Query("email") emailFilter: String? = null
    ): Response<List<Member>>

    @POST("rest/v1/{table}")
    suspend fun insertMember(
        @Path("table") table: String = "member",
        @Header("Prefer") prefer: String = "return=representation",
        @Body member: MemberInsertRequest
    ): Response<List<Member>>

    @PATCH("rest/v1/{table}")
    suspend fun updateMember(
        @Path("table") table: String = "member",
        @Query("email") emailFilter: String,
        @Header("Prefer") prefer: String = "return=representation",
        @Body updatePayload: Map<String, String>
    ): Response<List<Member>>
}

package com.novasec.secureauth.network

import com.novasec.secureauth.data.models.*
import retrofit2.http.*

interface ApiService {
    
    @POST("api/v1/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): AuthResponse

    @POST("api/v1/auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): AuthResponse

    @POST("api/v1/auth/refresh")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequest
    ): AuthResponse

    @POST("api/v1/auth/logout")
    suspend fun logout(
        @Header("Authorization") token: String
    ): Unit

    @GET("api/v1/auth/me")
    suspend fun getCurrentUser(
        @Header("Authorization") token: String
    ): User
}

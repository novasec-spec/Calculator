package com.novasec.secureauth.data.models

import com.google.gson.annotations.SerializedName

// Request Models
data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class RegisterRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("fullName") val fullName: String
)

data class RefreshTokenRequest(
    @SerializedName("refreshToken") val refreshToken: String
)

// Response Models
data class AuthResponse(
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("refreshToken") val refreshToken: String,
    @SerializedName("expiresIn") val expiresIn: Long,
    @SerializedName("user") val user: User
)

data class User(
    @SerializedName("id") val id: String,
    @SerializedName("email") val email: String,
    @SerializedName("fullName") val fullName: String,
    @SerializedName("createdAt") val createdAt: String
)

// Local Storage Models
data class AuthSession(
    val accessToken: String,
    val refreshToken: String,
    val expiresAt: Long,
    val user: User
)

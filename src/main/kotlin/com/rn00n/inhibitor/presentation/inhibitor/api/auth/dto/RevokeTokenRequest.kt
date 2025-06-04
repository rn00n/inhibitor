package com.rn00n.inhibitor.presentation.inhibitor.api.auth.dto

data class RevokeTokenRequest(
    val token: String,
    val token_type_hint: String,
)

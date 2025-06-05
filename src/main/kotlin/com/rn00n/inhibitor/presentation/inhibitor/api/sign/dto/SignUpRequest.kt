package com.rn00n.inhibitor.presentation.inhibitor.api.sign.dto

data class SignUpRequest(
    val username: String,
    val password: String,
    val name: String,
)
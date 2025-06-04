package com.rn00n.inhibitor.domain.accounts

enum class AccountStatus(
    val description: String,
) {
    ACTIVATE("활성"),
    BLOCK("정지"),
}

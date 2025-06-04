package com.rn00n.inhibitor.domain.accounts

import jakarta.persistence.*

@Entity
@Table(name = "accounts")
class Account(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val username: String,
    val password: String,
    val name: String,
    @Enumerated(EnumType.STRING)
    val status: AccountStatus,
)
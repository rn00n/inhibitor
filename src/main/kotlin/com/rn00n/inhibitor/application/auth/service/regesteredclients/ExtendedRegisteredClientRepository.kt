package com.rn00n.inhibitor.application.auth.service.regesteredclients

import org.springframework.security.oauth2.server.authorization.client.RegisteredClient
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository

interface ExtendedRegisteredClientRepository : RegisteredClientRepository {
    fun findAll(): List<RegisteredClient>
}
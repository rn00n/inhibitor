package com.rn00n.inhibitor.infrastructure.persistence.oauth2

import com.rn00n.inhibitor.commons.datasources.annotations.InhibitorRepository
import com.rn00n.inhibitor.domain.oauth2.OAuth2Authorization
import com.rn00n.inhibitor.domain.oauth2.repo.OAuth2AuthorizationRepository
import org.springframework.data.jpa.repository.JpaRepository

@InhibitorRepository
interface JpaOAuth2AuthorizationRepository : JpaRepository<OAuth2Authorization, String>, OAuth2AuthorizationRepository
package com.rn00n.inhibitor.domain.oauth2.repo

import com.rn00n.inhibitor.domain.oauth2.OAuth2Authorization
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface OAuth2AuthorizationRepository {
    fun findByRefreshTokenValue(token: String): OAuth2Authorization?
    fun deleteByRefreshTokenValue(token: String)
    fun delete(oAuth2Authorization: OAuth2Authorization)
    fun deleteByPrincipalName(username: String)
    fun findByPrincipalName(username: String): List<OAuth2Authorization>

    @Modifying
    @Query("DELETE FROM OAuth2Authorization a WHERE a.principalName = :principalName")
    fun deleteAllByPrincipalName(@Param("principalName") principalName: String): Int
}
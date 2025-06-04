package com.rn00n.inhibitor.commons.security.token

import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.oauth2.jwt.Jwt

/**
 * CustomJwtAuthenticationConverter
 *
 * JWT 토큰을 기반으로 Spring Security의 인증 객체(AbstractAuthenticationToken)를 생성하는 클래스.
 * 주로 사용자 인증 및 권한 부여 로직에서 사용됩니다.
 */
class CustomJwtAuthenticationConverter(
    private val userDetailsService: UserDetailsService,
) : Converter<Jwt, AbstractAuthenticationToken> {

    /**
     * Convert JWT token to AbstractAuthenticationToken
     *
     * JWT의 "sub" 클레임을 이용해 사용자 정보를 조회(UserDetailsService)하고,
     * Spring Security에서 사용하는 인증 토큰(UsernamePasswordAuthenticationToken)을 생성합니다.
     *
     * @param source JWT 토큰 객체
     * @return AbstractAuthenticationToken (인증 정보가 포함된 객체)
     * @throws IllegalArgumentException JWT에 'sub' 클레임이 없으면 예외 발생
     */
    override fun convert(source: Jwt): AbstractAuthenticationToken {
        // JWT의 'sub' 클레임에서 사용자 이름 추출
        val username: String = source.getClaim("sub")
            ?: throw IllegalArgumentException("JWT does not contain 'sub' claim") // 필수 클레임 "sub" 없을 시 예외 처리

        // UserDetailsService를 통해 사용자 정보 조회
        val userDetails: UserDetails = userDetailsService.loadUserByUsername(username)

        // 조회된 사용자 객체에서 권한 정보 가져오기
        val authorities: Collection<GrantedAuthority> = userDetails.authorities

        // 인증 토큰 생성 및 반환
        return UsernamePasswordAuthenticationToken(userDetails, source.tokenValue, authorities)
    }
}
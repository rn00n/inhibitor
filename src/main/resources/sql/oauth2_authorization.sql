CREATE TABLE oauth2_authorization
(
    id                            varchar(100) NOT NULL COMMENT 'Authorization 고유 식별자 (UUID 등, PK)',
    registered_client_id          varchar(100) NOT NULL COMMENT 'OAuth2 클라이언트 식별자 (Registered Client ID)',
    principal_name                varchar(200) NOT NULL COMMENT '인증 주체 식별자 (사용자 ID, username 등)',
    authorization_grant_type      varchar(100) NOT NULL COMMENT 'Authorization Grant Type (예: authorization_code, password, client_credentials)',
    authorized_scopes             varchar(1000) DEFAULT NULL COMMENT '권한 부여된 Scope 목록 (공백 또는 쉼표 구분)',
    attributes                    blob          DEFAULT NULL COMMENT '추가 인증 정보 (직렬화된 Map 형태)',

    state                         varchar(500)  DEFAULT NULL COMMENT 'Authorization 요청의 state 파라미터 (CSRF 방지 목적)',
    authorization_code_value      blob          DEFAULT NULL COMMENT '발급된 Authorization Code (암호화된 값)',
    authorization_code_issued_at  timestamp     DEFAULT NULL COMMENT 'Authorization Code 발급 시각',
    authorization_code_expires_at timestamp     DEFAULT NULL COMMENT 'Authorization Code 만료 시각',
    authorization_code_metadata   blob          DEFAULT NULL COMMENT 'Authorization Code 메타데이터 (직렬화된 Map)',

    access_token_value            blob          DEFAULT NULL COMMENT '발급된 Access Token 값 (JWT 또는 Opaque Token)',
    access_token_issued_at        timestamp     DEFAULT NULL COMMENT 'Access Token 발급 시각',
    access_token_expires_at       timestamp     DEFAULT NULL COMMENT 'Access Token 만료 시각',
    access_token_metadata         blob          DEFAULT NULL COMMENT 'Access Token 메타데이터 (Claim, 인증 컨텍스트 등)',
    access_token_type             varchar(100)  DEFAULT NULL COMMENT 'Access Token 타입 (예: Bearer)',
    access_token_scopes           varchar(1000) DEFAULT NULL COMMENT 'Access Token에 포함된 Scope 목록',

    oidc_id_token_value           blob          DEFAULT NULL COMMENT 'OIDC ID Token 값 (JWT)',
    oidc_id_token_issued_at       timestamp     DEFAULT NULL COMMENT 'OIDC ID Token 발급 시각',
    oidc_id_token_expires_at      timestamp     DEFAULT NULL COMMENT 'OIDC ID Token 만료 시각',
    oidc_id_token_metadata        blob          DEFAULT NULL COMMENT 'OIDC ID Token 메타데이터 (직렬화된 Claim 등)',

    refresh_token_value           blob          DEFAULT NULL COMMENT '발급된 Refresh Token 값',
    refresh_token_issued_at       timestamp     DEFAULT NULL COMMENT 'Refresh Token 발급 시각',
    refresh_token_expires_at      timestamp     DEFAULT NULL COMMENT 'Refresh Token 만료 시각',
    refresh_token_metadata        blob          DEFAULT NULL COMMENT 'Refresh Token 메타데이터 (토큰 발급 context 등)',

    user_code_value               blob          DEFAULT NULL COMMENT 'Device Code Flow에서 사용되는 User Code 값',
    user_code_issued_at           timestamp     DEFAULT NULL COMMENT 'User Code 발급 시각',
    user_code_expires_at          timestamp     DEFAULT NULL COMMENT 'User Code 만료 시각',
    user_code_metadata            blob          DEFAULT NULL COMMENT 'User Code 관련 메타데이터',

    device_code_value             blob          DEFAULT NULL COMMENT 'Device Authorization Grant의 Device Code 값',
    device_code_issued_at         timestamp     DEFAULT NULL COMMENT 'Device Code 발급 시각',
    device_code_expires_at        timestamp     DEFAULT NULL COMMENT 'Device Code 만료 시각',
    device_code_metadata          blob          DEFAULT NULL COMMENT 'Device Code 관련 메타데이터',

    PRIMARY KEY (id)
)
    COMMENT 'OAuth2 Authorization 저장 테이블'
    COLLATE = utf8mb4_general_ci;


CREATE INDEX idx_auth_principal_name ON oauth2_authorization (principal_name); -- users 기기초기화등 토큰 전체 무효화 용도
CREATE INDEX idx_auth_refresh_token_value ON oauth2_authorization (refresh_token_value(255)); -- refresh grant 에서 사용
CREATE INDEX idx_auth_refresh_token_expires_at ON oauth2_authorization (refresh_token_expires_at);
CREATE INDEX idx_auth_registered_client_id__access_token_expires_at ON oauth2_authorization (registered_client_id, access_token_expires_at);

# 추후 플랫폼별 초기화에서 사용 가능
# 현재 정책에서는 디바이스 초기화시 모든 플랫폼 전체 초기화여서 미사용
# CREATE INDEX idx_auth_registered_client_id ON oauth2_authorization (registered_client_id);

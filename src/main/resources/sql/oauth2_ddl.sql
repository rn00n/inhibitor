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
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='OAuth2 Authorization 저장 테이블';
CREATE INDEX idx_auth_principal_name ON oauth2_authorization (principal_name);
CREATE INDEX idx_auth_refresh_token_value ON oauth2_authorization (refresh_token_value(255));

CREATE TABLE oauth2_registered_client
(
    id                            varchar(100)                            NOT NULL COMMENT 'RegisteredClient 고유 식별자 (UUID 등, PK)',
    client_id                     varchar(100)                            NOT NULL COMMENT 'OAuth2 클라이언트 식별자 (client_id)',
    client_id_issued_at           timestamp     DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT 'client_id 발급 시각 (등록 시 자동 생성)',
    client_secret                 varchar(200)  DEFAULT NULL COMMENT '클라이언트 비밀값 (password grant 등에서 사용)',
    client_secret_expires_at      timestamp     DEFAULT NULL COMMENT '클라이언트 비밀 만료 시각 (null이면 무기한)',
    client_name                   varchar(200)                            NOT NULL COMMENT '클라이언트 이름 (서비스 또는 앱 이름)',
    client_authentication_methods varchar(1000)                           NOT NULL COMMENT '클라이언트 인증 방식 목록 (예: client_secret_basic, private_key_jwt)',
    authorization_grant_types     varchar(1000)                           NOT NULL COMMENT '허용된 Grant Type 목록 (예: authorization_code, refresh_token)',
    redirect_uris                 varchar(1000) DEFAULT NULL COMMENT 'Authorization 요청 후 리다이렉트 가능한 URI 목록',
    post_logout_redirect_uris     varchar(1000) DEFAULT NULL COMMENT 'OIDC 로그아웃 이후 리다이렉트 가능한 URI 목록',
    scopes                        varchar(1000)                           NOT NULL COMMENT '클라이언트가 요청 가능한 Scope 목록 (공백 또는 쉼표 구분)',
    client_settings               varchar(2000)                           NOT NULL COMMENT '클라이언트 설정 값 (직렬화된 JSON)',
    token_settings                varchar(2000)                           NOT NULL COMMENT '토큰 설정 값 (직렬화된 JSON, 토큰 유효기간 등)',

    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='OAuth2 클라이언트 등록 테이블';

CREATE TABLE oauth2_authorization_consent
(
    registered_client_id varchar(100)  NOT NULL COMMENT '동의가 부여된 클라이언트의 ID (oauth2_registered_client.client_id)',
    principal_name       varchar(200)  NOT NULL COMMENT '동의한 사용자 식별자 (username 또는 userId)',
    authorities          varchar(1000) NOT NULL COMMENT '사용자가 동의한 권한 목록 (Scope 정보, 공백 또는 쉼표 구분)',

    PRIMARY KEY (registered_client_id, principal_name)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='OAuth2 사용자 동의(consent) 저장 테이블';


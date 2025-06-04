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
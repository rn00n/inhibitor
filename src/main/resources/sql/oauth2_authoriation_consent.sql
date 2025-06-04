CREATE TABLE oauth2_authorization_consent
(
    registered_client_id varchar(100)  NOT NULL COMMENT '동의가 부여된 클라이언트의 ID (oauth2_registered_client.client_id)',
    principal_name       varchar(200)  NOT NULL COMMENT '동의한 사용자 식별자 (username 또는 userId)',
    authorities          varchar(1000) NOT NULL COMMENT '사용자가 동의한 권한 목록 (Scope 정보, 공백 또는 쉼표 구분)',

    PRIMARY KEY (registered_client_id, principal_name)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='OAuth2 사용자 동의(consent) 저장 테이블';

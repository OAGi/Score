CREATE TABLE `oauth2_app`
(
    `oauth2_app_id`                bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'An internal, primary database key of a OAUTH2_APP record.',
    `provider_name`                varchar(100) NOT NULL COMMENT 'The unique name identifying this OAuth 2.0 provider; used as the client registration ID.',
    `issuer_uri`                   varchar(200) DEFAULT NULL COMMENT 'The OpenID Connect issuer URI; when set, the provider''s endpoints are discovered from its metadata.',
    `authorization_uri`            varchar(200) DEFAULT NULL COMMENT 'The authorization endpoint URI of the OAuth 2.0 provider.',
    `token_uri`                    varchar(200) DEFAULT NULL COMMENT 'The token endpoint URI of the OAuth 2.0 provider.',
    `user_info_uri`                varchar(200) DEFAULT NULL COMMENT 'The UserInfo endpoint URI of the OAuth 2.0 provider.',
    `jwk_set_uri`                  varchar(200) DEFAULT NULL COMMENT 'The JSON Web Key (JWK) Set URI of the OAuth 2.0 provider, used to validate tokens.',
    `redirect_uri`                 varchar(200) NOT NULL COMMENT 'The redirect URI to which the OAuth 2.0 provider returns the end user after authorization.',
    `end_session_endpoint`         varchar(200) DEFAULT NULL COMMENT 'The end-session (logout) endpoint URI of the OAuth 2.0 provider.',
    `client_id`                    varchar(200) NOT NULL COMMENT 'The client identifier issued to this application by the OAuth 2.0 provider.',
    `client_secret`                varchar(200) NOT NULL COMMENT 'The client secret issued to this application by the OAuth 2.0 provider.',
    `client_authentication_method` varchar(50)  NOT NULL COMMENT 'The client authentication method used with the provider (e.g. client_secret_basic, client_secret_post).',
    `authorization_grant_type`     varchar(50)  NOT NULL COMMENT 'The OAuth 2.0 authorization grant type used with the provider (e.g. authorization_code).',
    `prompt`                       varchar(20)  DEFAULT NULL COMMENT 'The OpenID Connect ''prompt'' parameter sent to the provider''s authorization endpoint.',
    `display_provider_name`        varchar(100) DEFAULT NULL COMMENT 'The provider name displayed on the login button in the user interface.',
    `background_color`             varchar(50)  DEFAULT NULL COMMENT 'The background color of the provider''s login button in the user interface.',
    `font_color`                   varchar(50)  DEFAULT NULL COMMENT 'The font color of the provider''s login button in the user interface.',
    `display_order`                int(11) DEFAULT 0 COMMENT 'The order in which this provider is displayed among the login options.',
    `is_disabled`                  tinyint(1) NOT NULL DEFAULT 0 COMMENT 'A flag indicating whether this OAuth 2.0 provider is disabled (1 = disabled, 0 = enabled).',
    PRIMARY KEY (`oauth2_app_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci COMMENT='This table stores OAuth 2.0 / OpenID Connect provider registrations, including client credentials, endpoint URIs, and login-button display settings, used to authenticate end users.';
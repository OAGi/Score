CREATE TABLE `oauth2_app`
(
    `oauth2_app_id`                bigint(20) unsigned NOT NULL AUTO_INCREMENT,
    `provider_name`                varchar(100) NOT NULL,
    `issuer_uri`                   varchar(200) DEFAULT NULL,
    `authorization_uri`            varchar(200) DEFAULT NULL,
    `token_uri`                    varchar(200) DEFAULT NULL,
    `user_info_uri`                varchar(200) DEFAULT NULL,
    `jwk_set_uri`                  varchar(200) DEFAULT NULL,
    `redirect_uri`                 varchar(200) NOT NULL,
    `end_session_endpoint`         varchar(200) DEFAULT NULL,
    `client_id`                    varchar(200) NOT NULL,
    `client_secret`                varchar(200) NOT NULL,
    `client_authentication_method` varchar(50)  NOT NULL,
    `authorization_grant_type`     varchar(50)  NOT NULL,
    `prompt`                       varchar(20)  DEFAULT NULL,
    `display_provider_name`        varchar(100) DEFAULT NULL,
    `background_color`             varchar(50)  DEFAULT NULL,
    `font_color`                   varchar(50)  DEFAULT NULL,
    `display_order`                int(11) DEFAULT 0,
    `is_disabled`                  tinyint(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (`oauth2_app_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci;
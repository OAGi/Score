CREATE TABLE `text_template`
(
    `text_template_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'An internal, primary database key of a TEXT_TEMPLATE record.',
    `name`             varchar(100) NOT NULL DEFAULT '' COMMENT 'The name of the text template, used as the key to look up the template.',
    `subject`          varchar(200) DEFAULT NULL COMMENT 'A subject of the template (e.g. an email subject line).',
    `content_type`     varchar(100) DEFAULT 'text/plain' COMMENT 'A content type of the template body (e.g. text/plain, text/html).',
    `template`         longtext DEFAULT NULL COMMENT 'The template body text, with placeholders substituted at rendering time.',
    PRIMARY KEY (`text_template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='This table stores named text templates, each with a subject, content type, and body containing placeholders that are substituted at rendering time. Templates are looked up by name to compose output such as email messages.';
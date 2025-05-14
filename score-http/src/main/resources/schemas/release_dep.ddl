CREATE TABLE `release_dep`
(
    `release_dep_id`       bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary, internal database key.',
    `release_id`           bigint(20) unsigned NOT NULL COMMENT 'A foreign key pointing to a release record.',
    `depend_on_release_id` bigint(20) unsigned NOT NULL COMMENT 'A foreign key pointing to dependent release records of the release specified in release_id.',
    PRIMARY KEY (`release_dep_id`),
    KEY                    `release_dep_release_id_fk` (`release_id`),
    KEY                    `release_dep_depend_on_release_id_fk` (`depend_on_release_id`),
    CONSTRAINT `release_dep_depend_on_release_id_fk` FOREIGN KEY (`depend_on_release_id`) REFERENCES `release` (`release_id`),
    CONSTRAINT `release_dep_release_id_fk` FOREIGN KEY (`release_id`) REFERENCES `release` (`release_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci COMMENT='This table stores release dependency information.';
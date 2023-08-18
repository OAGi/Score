CREATE TABLE `ref_spec`
(
    `ref_spec_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
    `spec`        varchar(30) NOT NULL DEFAULT '',
    PRIMARY KEY (`ref_spec_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
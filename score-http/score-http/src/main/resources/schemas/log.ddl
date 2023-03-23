CREATE TABLE `log`
(
    `log_id`                bigint(20) unsigned              NOT NULL AUTO_INCREMENT,
    `hash`                  char(40) CHARACTER SET ascii     NOT NULL COMMENT 'The unique hash to identify the log.',
    `revision_num`          int(10) unsigned                 NOT NULL DEFAULT '1' COMMENT 'This is an incremental integer. It tracks changes in each component. If a change is made to a component after it has been published, the component receives a new revision number. Revision number can be 1, 2, and so on.',
    `revision_tracking_num` int(10) unsigned                 NOT NULL DEFAULT '1' COMMENT 'This supports the ability to undo changes during a revision (life cycle of a revision is from the component''s WIP state to PUBLISHED state). REVISION_TRACKING_NUM can be 1, 2, and so on.',
    `log_action`            varchar(20)                               DEFAULT NULL COMMENT 'This indicates the action associated with the record.',
    `reference`             varchar(100) CHARACTER SET ascii NOT NULL DEFAULT '',
    `snapshot`              json                                      DEFAULT NULL,
    `prev_log_id`           bigint(20) unsigned                       DEFAULT NULL,
    `next_log_id`           bigint(20) unsigned                       DEFAULT NULL,
    `created_by`            bigint(20) unsigned              NOT NULL,
    `creation_timestamp`    datetime(6)                      NOT NULL,
    PRIMARY KEY (`log_id`),
    KEY `reference` (`reference`),
    KEY `log_created_by_fk` (`created_by`),
    KEY `log_prev_log_id_fk` (`prev_log_id`),
    KEY `log_next_log_id_fk` (`next_log_id`),
    CONSTRAINT `log_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `log_next_log_id_fk` FOREIGN KEY (`next_log_id`) REFERENCES `log` (`log_id`),
    CONSTRAINT `log_prev_log_id_fk` FOREIGN KEY (`prev_log_id`) REFERENCES `log` (`log_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;
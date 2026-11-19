-- ---------------------------------------------------------------------------------------------------------------------
-- 3.6.2: the loads of release packages (the swap of the schema of the application for one that a package was loaded into).
-- ---------------------------------------------------------------------------------------------------------------------

-- This table stays where it is when the schema is swapped (the swap moves every other table), so that a load can be followed
-- from its start to its end, and after it. For the same reason it has no foreign key: a foreign key to a table that is moved
-- would go with that table.
CREATE TABLE `release_package_load_job`
(
    `release_package_load_job_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary key.',
    `state`                 varchar(20)         NOT NULL COMMENT 'Where the load is: PENDING, PREFLIGHT, LOADING, MIGRATING, COPYING, VERIFYING, SWAPPING or ROLLING_BACK while it goes on; SUCCEEDED, FAILED or ROLLED_BACK when it is over.',
    `message`               text                         DEFAULT NULL COMMENT 'What the load is doing, or why it failed.',
    `package_file_name`     varchar(255)        NOT NULL COMMENT 'The name of the file of the release package that is loaded.',
    `release_num`           varchar(100)                 DEFAULT NULL COMMENT 'The release the package adds.',
    `staging_schema`        varchar(64)                  DEFAULT NULL COMMENT 'The schema the package is loaded into, which is dropped when the load is over.',
    `backup_schema`         varchar(64)                  DEFAULT NULL COMMENT 'The schema that holds the tables the swap took out, until the swap is undone or the schema is dropped.',
    `discarded_schema`      varchar(64)                  DEFAULT NULL COMMENT 'The schema that holds the tables of the load when the swap has been undone.',
    `node_id`               varchar(100)                 DEFAULT NULL COMMENT 'The server that does the load, which is the one that was given the package.',
    `owner_user_id`         bigint(20) unsigned NOT NULL COMMENT 'The user who started the load. Not a foreign key, see the table.',
    `creation_timestamp`    datetime(6)         NOT NULL COMMENT 'Timestamp when the load was started.',
    `last_update_timestamp` datetime(6)         NOT NULL COMMENT 'Timestamp when the state or the message of the load last changed; a load that goes on and has not changed for long is taken to have been abandoned.',
    PRIMARY KEY (`release_package_load_job_id`),
    KEY `release_package_load_job_state_idx` (`state`, `last_update_timestamp`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci
  ROW_FORMAT = DYNAMIC COMMENT ='The loads of release packages, each into a schema of its own, to be swapped for the schema the application serves.';

-- ---------------------------------------------------------------------------------------------------------------------
-- A lock that every process that uses this database can take by name, in the database itself, kept until the end of a transaction.
-- ---------------------------------------------------------------------------------------------------------------------

-- A row is a lock: a process takes it with SELECT ... LOCK IN SHARE MODE (to go on together with the others that do) or FOR UPDATE (to be
-- alone), and holds it until its transaction ends or its connection is lost. The first use is the gate that stops every writer, of any
-- process, while the schema is swapped for one that a release package was loaded into: the writers read 'write_gate' with a shared lock
-- before they write, and a load takes it with an exclusive one. Like release_package_load_job this table stays where it is when the schema is
-- swapped (so the rows are the same before and after it), and has no foreign key and no trigger.
CREATE TABLE `named_lock`
(
    `lock_name` varchar(100) NOT NULL COMMENT 'Primary key. What the lock is for.',
    PRIMARY KEY (`lock_name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci
  ROW_FORMAT = DYNAMIC COMMENT ='The rows that are locked, by name, to coordinate processes through the database. A lock whose row is missing does not lock anything.';

INSERT INTO `named_lock` (`lock_name`) VALUES ('write_gate');

CREATE TABLE `named_lock`
(
    `lock_name` varchar(100) NOT NULL COMMENT 'Primary key. What the lock is for.',
    PRIMARY KEY (`lock_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='The rows that are locked, by name, to coordinate processes through the database. A lock whose row is missing does not lock anything.';
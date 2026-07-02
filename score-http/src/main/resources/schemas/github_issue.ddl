CREATE TABLE `github_issue`
(
    `github_issue_id`         bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary key column.',
    `repo_owner`              varchar(100) CHARACTER SET ascii COLLATE ascii_general_ci NOT NULL COMMENT 'GitHub repository owner/org, e.g. OAGi.',
    `repo_name`               varchar(100) CHARACTER SET ascii COLLATE ascii_general_ci NOT NULL COMMENT 'GitHub repository name, e.g. Score.',
    `issue_number`            int(11) NOT NULL COMMENT 'GitHub issue number within the repository.',
    `cached_metadata`         longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT 'Cached GitHub issue metadata as JSON: title, state, type, milestone {title,state}, labels [{name,color,description}], assignees [{login,html_url,avatar_url}], html_url, node_id, etc. NULL until first synced. Project (Projects v2) is excluded (not available via the REST issue endpoint).' CHECK (json_valid(`cached_metadata`)),
    `cached_synced_timestamp` datetime(6) DEFAULT NULL COMMENT 'When cached_metadata was last refreshed from GitHub.',
    `creation_timestamp`      datetime(6) NOT NULL COMMENT 'Timestamp when this record was created.',
    `last_update_timestamp`   datetime(6) NOT NULL COMMENT 'Timestamp when this record was last updated.',
    PRIMARY KEY (`github_issue_id`),
    UNIQUE KEY `github_issue_uk1` (`repo_owner`,`repo_name`,`issue_number`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='Independent registry of referenced GitHub issues (one row per owner/repo/number) with cached metadata JSON. Issue #1533.';
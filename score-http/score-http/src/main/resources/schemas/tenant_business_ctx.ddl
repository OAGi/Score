CREATE TABLE `tenant_business_ctx`
(
    `tenant_business_ctx_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary key column.',
    `tenant_id`              bigint(20) unsigned NOT NULL COMMENT 'Tenant role.',
    `biz_ctx_id`             bigint(20) unsigned NOT NULL COMMENT 'Concrete business context for the company.',
    PRIMARY KEY (`tenant_business_ctx_id`),
    UNIQUE KEY `tenant_business_ctx_pair` (`tenant_id`, `biz_ctx_id`),
    KEY `tenant_business_ctx_tenant_id_fk` (`tenant_id`),
    KEY `organization_business_ctx_biz_ctx_id_fk` (`biz_ctx_id`),
    CONSTRAINT `organization_business_ctx_biz_ctx_id_fk` FOREIGN KEY (`biz_ctx_id`) REFERENCES `biz_ctx` (`biz_ctx_id`),
    CONSTRAINT `tenant_business_ctx_tenant_id_fk` FOREIGN KEY (`tenant_id`) REFERENCES `tenant` (`tenant_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8 COMMENT ='This table captures the tenant role and theirs business contexts.';
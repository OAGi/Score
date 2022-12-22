-- oagi.tenant definition
CREATE TABLE `tenant` (
  `tenant_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary key column.',
  `name` varchar(100) DEFAULT NULL COMMENT 'The name of the tenant.',
  PRIMARY KEY (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 
COMMENT='This table about the user tenant role.';

-- oagi.user to tenant role
CREATE TABLE `user_tenant` (
  `user_tenant_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary key column.',
  `tenant_id`  bigint(20) unsigned NOT NULL COMMENT 'Assigned tenant to the user.',
  `app_user_id` bigint(20) unsigned NOT NULL  COMMENT 'Application user.',
  PRIMARY KEY (`user_tenant_id`),
  UNIQUE KEY `user_tenant_pair` (`tenant_id`,`app_user_id`),
  KEY `user_tenant_tenant_id_fk` (`tenant_id`),
  KEY `user_tenant_tenant_id_app_user_id_fk` (`app_user_id`),
  CONSTRAINT `user_tenant_tenant_id_fk` FOREIGN KEY (`tenant_id`) REFERENCES `tenant` (`tenant_id`),
  CONSTRAINT `user_tenant_tenant_id_app_user_id_fk` FOREIGN KEY (`app_user_id`) REFERENCES `app_user` (`app_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 
COMMENT='This table captures the tenant roles of the user';


-- oagi.tenant to business context definition
CREATE TABLE `tenant_business_ctx` (
  `tenant_business_ctx_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary key column.',
  `tenant_id`  bigint(20) unsigned NOT NULL COMMENT 'Tenant role.',
  `biz_ctx_id` bigint(20) unsigned NOT NULL  COMMENT 'Concrete business context for the company.',
  PRIMARY KEY (`tenant_business_ctx_id`),
  UNIQUE KEY `tenant_business_ctx_pair` (`tenant_id`,`biz_ctx_id`),
  KEY `tenant_business_ctx_tenant_id_fk` (`tenant_id`),
  KEY `organization_business_ctx_biz_ctx_id_fk` (`biz_ctx_id`),
  CONSTRAINT `tenant_business_ctx_tenant_id_fk` FOREIGN KEY (`tenant_id`) REFERENCES `tenant` (`tenant_id`),
  CONSTRAINT `organization_business_ctx_biz_ctx_id_fk` FOREIGN KEY (`biz_ctx_id`) REFERENCES `biz_ctx` (`biz_ctx_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 
COMMENT='This table captures the tenant role and theirs business contexts.';



-- config table 
-- oagi.configuration definition
CREATE TABLE `configuration` (
  `configuration_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary key column.',
  `name` varchar(100) DEFAULT NULL COMMENT 'The name of configuration property.',
  `type` varchar(100) DEFAULT NULL COMMENT 'The type of configuration property.',
  `value` varchar(100) DEFAULT NULL COMMENT 'The value of configuration property.',
  PRIMARY KEY (`configuration_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 
COMMENT='The table stores configuration properties of the application. ';

INSERT INTO configuration(name, `type`, value)
values('isTenant', 'Boolean', 'false');
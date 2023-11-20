package org.oagi.score.gateway.http.configuration.intializer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jooq.DSLContext;
import org.jooq.Meta;
import org.jooq.RowCountQuery;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.oagi.score.repo.api.ScoreRepositoryFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.gateway.http.api.application_management.service.ApplicationConfigurationService.NAVBAR_BRAND_CONFIG_PARAM_NAME;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;
import static org.oagi.score.repo.api.user.model.ScoreUser.SYSTEM_USER_ID;
import static org.oagi.score.repo.api.user.model.ScoreUser.SYSTEM_USER_LOGIN_ID;

@Component
public class FixDatabaseRecordInitializer implements InitializingBean {

    private final Log logger = LogFactory.getLog(getClass());

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private ScoreRepositoryFactory scoreRepositoryFactory;

    @Override
    public void afterPropertiesSet() throws Exception {
        upsertSystemUser();
        issue1476();
        changeValueColumnInConfigurationTable(); // this is a temporal execution.
        insertDefaultBrandSVG();
        addDeprecationColumnsInTopLevelAsbiep();
    }

    private void upsertSystemUser() {
        boolean sysadmExists = dslContext.selectCount()
                .from(APP_USER)
                .where(APP_USER.LOGIN_ID.eq(SYSTEM_USER_LOGIN_ID))
                .fetchOptionalInto(Integer.class).orElse(0) > 0;
        if (!sysadmExists) {
            dslContext.insertInto(APP_USER)
                    .set(APP_USER.APP_USER_ID, ULong.valueOf(SYSTEM_USER_ID))
                    .set(APP_USER.LOGIN_ID, SYSTEM_USER_LOGIN_ID)
                    .set(APP_USER.NAME, "System")
                    .set(APP_USER.ORGANIZATION, "System")
                    .set(APP_USER.IS_DEVELOPER, (byte) 1)
                    .set(APP_USER.IS_ADMIN, (byte) 1)
                    .set(APP_USER.IS_ENABLED, (byte) 1)
                    .execute();
        }

        dslContext.update(APP_USER)
                .set(APP_USER.APP_USER_ID, ULong.valueOf(SYSTEM_USER_ID))
                .where(APP_USER.LOGIN_ID.eq(SYSTEM_USER_LOGIN_ID))
                .execute();
    }

    private void issue1476() {
        boolean nonVerbConfirmExists = dslContext.selectCount()
                .from(ASCCP_MANIFEST_TAG)
                .join(TAG).on(ASCCP_MANIFEST_TAG.TAG_ID.eq(TAG.TAG_ID))
                .join(ASCCP_MANIFEST).on(ASCCP_MANIFEST_TAG.ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.ASCCP_MANIFEST_ID))
                .join(ASCCP).on(ASCCP_MANIFEST.ASCCP_ID.eq(ASCCP.ASCCP_ID))
                .join(NAMESPACE).on(ASCCP.NAMESPACE_ID.eq(NAMESPACE.NAMESPACE_ID))
                .where(and(
                        TAG.NAME.notEqual("Verb"),
                        ASCCP.PROPERTY_TERM.eq("Confirm"),
                        NAMESPACE.URI.eq("http://www.openapplications.org/oagis/10")
                ))
                .fetchOneInto(Integer.class) > 0;
        if (!nonVerbConfirmExists) {
            return;
        }

        logger.info("Issue #1476 - Tagging 'Confirm' ASCCP as a 'Verb'.");

        // 'Confirm' ASCCP should be tagged as a 'Verb'.
        List<ULong> confirmAsccpManifestIdList = dslContext.select(ASCCP_MANIFEST.ASCCP_MANIFEST_ID)
                .from(ASCCP_MANIFEST)
                .join(ASCCP).on(ASCCP_MANIFEST.ASCCP_ID.eq(ASCCP.ASCCP_ID))
                .join(NAMESPACE).on(ASCCP.NAMESPACE_ID.eq(NAMESPACE.NAMESPACE_ID))
                .where(and(
                        ASCCP.PROPERTY_TERM.eq("Confirm"),
                        NAMESPACE.URI.eq("http://www.openapplications.org/oagis/10")
                ))
                .fetchInto(ULong.class);

        ULong verbTagId = dslContext.select(TAG.TAG_ID)
                .from(TAG)
                .where(TAG.NAME.eq("Verb"))
                .fetchOneInto(ULong.class);

        dslContext.update(ASCCP_MANIFEST_TAG)
                .set(ASCCP_MANIFEST_TAG.TAG_ID, verbTagId)
                .where(ASCCP_MANIFEST_TAG.ASCCP_MANIFEST_ID.in(confirmAsccpManifestIdList))
                .execute();
    }

    private void changeValueColumnInConfigurationTable() {
        if ("varchar".equals(CONFIGURATION.VALUE.getDataType().getTypeName().toLowerCase())) {
            dslContext.execute("ALTER TABLE `configuration` MODIFY `value` LONGTEXT DEFAULT NULL COMMENT '" + CONFIGURATION.VALUE.getComment() + "'");
        }
    }

    private void insertDefaultBrandSVG() {
        scoreRepositoryFactory.createConfigurationWriteRepository()
                .upsertConfiguration(sessionService.getScoreSystemUser(), NAVBAR_BRAND_CONFIG_PARAM_NAME, "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"126px\" height=\"26px\" viewBox=\"0 0 126 26\"\n" +
                        "           enable-background=\"new 0 0 126 26\" xml:space=\"preserve\">\n" +
                        "        <image id=\"logo\" width=\"126\" height=\"26\" x=\"0\" y=\"0\" href=\"data:image/png;base64,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 MDowMGYFYlUAAAAASUVORK5CYII=\"/>\n" +
                        "      </svg>");
    }

    private void addDeprecationColumnsInTopLevelAsbiep() {
        boolean hasIsDeprecated = hasColumnExistInTopLevelAsbiep("is_deprecated");
        if (!hasIsDeprecated) {
            dslContext.execute("ALTER TABLE `top_level_asbiep` ADD COLUMN `is_deprecated` tinyint(1) NOT NULL DEFAULT 0 COMMENT 'Indicates whether the TOP_LEVEL_ASBIEP is deprecated.' AFTER `inverse_mode`");
        }

        boolean hasDeprecatedReason = hasColumnExistInTopLevelAsbiep("deprecated_reason");
        if (!hasDeprecatedReason) {
            dslContext.execute("ALTER TABLE `top_level_asbiep` ADD COLUMN `deprecated_reason` text DEFAULT NULL COMMENT 'The reason for the deprecation of the TOP_LEVEL_ASBIEP.' AFTER `is_deprecated`");
        }

        boolean hasDeprecatedRemark = hasColumnExistInTopLevelAsbiep("deprecated_remark");
        if (!hasDeprecatedRemark) {
            dslContext.execute("ALTER TABLE `top_level_asbiep` ADD COLUMN `deprecated_remark` text DEFAULT NULL COMMENT 'The remark for the deprecation of the TOP_LEVEL_ASBIEP.' AFTER `deprecated_reason`");
        }
    }

    private boolean hasColumnExistInTopLevelAsbiep(String columnName) {
        return !dslContext.fetch("SHOW COLUMNS FROM `top_level_asbiep` LIKE '" + columnName + "'").isEmpty();
    }

}

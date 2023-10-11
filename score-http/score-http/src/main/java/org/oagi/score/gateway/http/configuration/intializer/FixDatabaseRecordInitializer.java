package org.oagi.score.gateway.http.configuration.intializer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.jooq.impl.DSL.and;
import static org.jooq.impl.SQLDataType.CLOB;
import static org.jooq.impl.SQLDataType.VARCHAR;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;
import static org.oagi.score.repo.api.user.model.ScoreUser.SYSTEM_USER_ID;
import static org.oagi.score.repo.api.user.model.ScoreUser.SYSTEM_USER_LOGIN_ID;

@Component
public class FixDatabaseRecordInitializer implements InitializingBean {

    private final Log logger = LogFactory.getLog(getClass());

    @Autowired
    private DSLContext dslContext;

    @Override
    public void afterPropertiesSet() throws Exception {
        upsertSystemUser();
        issue1476();
        increaseLengthOfValueColumnInConfigurationTable(); // this is a temporal execution.
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

    private void increaseLengthOfValueColumnInConfigurationTable() {
        if ("varchar".equals(CONFIGURATION.VALUE.getDataType().getTypeName().toLowerCase())) {
            dslContext.execute("ALTER TABLE `configuration` MODIFY `value` TEXT DEFAULT NULL COMMENT '" + CONFIGURATION.VALUE.getComment() + "'");
        }
    }

}

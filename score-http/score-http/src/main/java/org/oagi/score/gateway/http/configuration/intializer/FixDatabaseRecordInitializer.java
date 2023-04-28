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
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;

@Component
public class FixDatabaseRecordInitializer implements InitializingBean {

    private final Log logger = LogFactory.getLog(getClass());

    @Autowired
    private DSLContext dslContext;

    @Override
    public void afterPropertiesSet() throws Exception {
        issue1476();
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

}

package org.oagi.score.gateway.http.configuration.intializer;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.data.OagisComponentType;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.entity.jooq.Tables.*;

@Component
public class ExtensionRevisionCleanUpInitializer implements InitializingBean {

    @Autowired
    private DSLContext dslContext;

    @Override
    public void afterPropertiesSet() throws Exception {
        cleanUpUEGAsccWithRevisionNumIsGreaterThan1();
        cleanUpUEGAsccpWithRevisionNumIsGreaterThan1();
    }

    private void cleanUpUEGAsccWithRevisionNumIsGreaterThan1() {
        List<ULong> asccIdsWithRevisionNumGe1 =
                dslContext.select(ASCC.ASCC_ID)
                        .from(ASCC)
                        .join(ASCCP).on(ASCC.TO_ASCCP_ID.eq(ASCCP.ASCCP_ID))
                        .join(ACC).on(ASCCP.ROLE_OF_ACC_ID.eq(ACC.ACC_ID))
                        .where(and(
                                ACC.OAGIS_COMPONENT_TYPE.eq(OagisComponentType.UserExtensionGroup.getValue()),
                                ASCCP.REVISION_NUM.greaterThan(1),
                                ASCC.REVISION_NUM.greaterThan(1)
                        ))
                        .fetchInto(ULong.class);

        if (!asccIdsWithRevisionNumGe1.isEmpty()) {
            dslContext.deleteFrom(ASCC)
                    .where(ASCC.ASCC_ID.in(asccIdsWithRevisionNumGe1))
                    .execute();
        }
    }

    private void cleanUpUEGAsccpWithRevisionNumIsGreaterThan1() {
        List<ULong> asccpIdsWithRevisionNumGe1 =
                dslContext.select(ASCCP.ASCCP_ID)
                        .from(ASCCP)
                        .join(ACC).on(ASCCP.ROLE_OF_ACC_ID.eq(ACC.ACC_ID))
                        .where(and(
                                ACC.OAGIS_COMPONENT_TYPE.eq(OagisComponentType.UserExtensionGroup.getValue()),
                                ASCCP.REVISION_NUM.greaterThan(1)
                        ))
                        .fetchInto(ULong.class);
        if (!asccpIdsWithRevisionNumGe1.isEmpty()) {
            dslContext.deleteFrom(ASCCP)
                    .where(ASCCP.ASCCP_ID.in(asccpIdsWithRevisionNumGe1))
                    .execute();
        }
    }
}

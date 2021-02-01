package org.oagi.score.gateway.http.configuration.intializer;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.bie_management.data.bie_edit.PurgeBieEvent;
import org.oagi.score.gateway.http.api.bie_management.service.BieEditService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.oagi.score.repo.api.impl.jooq.entity.Tables.ABIE;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.ASBIEP;

@Component
public class OrphanBIEAfterReusedCleanUpInitializer implements InitializingBean {

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private BieEditService service;

    @Override
    public void afterPropertiesSet() {
        // delete orphan nodes of BIE
        List<ULong> topLevelAsbiepIds = dslContext.select(ABIE.OWNER_TOP_LEVEL_ASBIEP_ID)
                .from(ABIE)
                .leftJoin(ASBIEP).on(ABIE.ABIE_ID.eq(ASBIEP.ROLE_OF_ABIE_ID))
                .where(ASBIEP.ASBIEP_ID.isNull())
                .groupBy(ABIE.OWNER_TOP_LEVEL_ASBIEP_ID)
                .fetchInto(ULong.class);

        topLevelAsbiepIds.forEach(topLevelAsbiepId -> service.onPurgeBieEventReceived(
                new PurgeBieEvent(topLevelAsbiepId.toBigInteger())));
    }
}
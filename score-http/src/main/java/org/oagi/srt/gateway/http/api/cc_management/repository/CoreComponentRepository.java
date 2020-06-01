package org.oagi.srt.gateway.http.api.cc_management.repository;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.srt.cache.impl.*;
import org.oagi.srt.data.*;
import org.oagi.srt.entity.jooq.Tables;
import org.oagi.srt.gateway.http.api.cc_management.data.CcState;
import org.oagi.srt.gateway.http.api.info.data.SummaryCcExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.and;
import static org.jooq.impl.DSL.max;

@Repository
public class CoreComponentRepository {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ACCCachingRepository accRepository;
    @Autowired
    private ASCCCachingRepository asccRepository;
    @Autowired
    private BCCCachingRepository bccRepository;
    @Autowired
    private ASCCPCachingRepository asccpRepository;
    @Autowired
    private BCCPCachingRepository bccpRepository;
    @Autowired
    private BDTCachingRepository bdtRepository;

    @Autowired
    private DSLContext dslContext;

    public List<ACC> getAccList() {
        return accRepository.findAll();
    }

    public List<ASCC> getAsccList() {
        return asccRepository.findAll();
    }

    public List<BCC> getBccList() {
        return bccRepository.findAll();
    }

    public List<ASCCP> getAsccpList() {
        return asccpRepository.findAll();
    }

    public List<BCCP> getBccpList() {
        return bccpRepository.findAll();
    }

    public List<DT> getBdtList() {
        return bdtRepository.findAll();
    }

    public List<SummaryCcExt> getSummaryCcExtList() {
        List<ULong> uegAccIds =
                dslContext.select(max(Tables.ACC.CURRENT_ACC_ID).as("id"))
                        .from(Tables.ACC)
                        .where(and(
                                Tables.ACC.OAGIS_COMPONENT_TYPE.eq(OagisComponentType.UserExtensionGroup.getValue()),
                                Tables.ACC.RELEASE_ID.greaterThan(ULong.valueOf(0))
                        ))
                        .groupBy(Tables.ACC.GUID)
                        .fetchInto(ULong.class);

        return dslContext.select(Tables.ACC.ACC_ID,
                Tables.ACC.OBJECT_CLASS_TERM,
                Tables.ACC.STATE,
                Tables.ACC.OWNER_USER_ID,
                Tables.APP_USER.LOGIN_ID)
                .from(Tables.ACC)
                .join(Tables.APP_USER).on(Tables.ACC.OWNER_USER_ID.eq(Tables.APP_USER.APP_USER_ID))
                .where(Tables.ACC.ACC_ID.in(uegAccIds))
                .fetchStream().map(e -> {
                    SummaryCcExt item = new SummaryCcExt();
                    item.setAccId(e.get(Tables.ACC.ACC_ID).longValue());
                    item.setObjectClassTerm(e.get(Tables.ACC.OBJECT_CLASS_TERM));
                    item.setState(CcState.valueOf(e.get(Tables.ACC.STATE)));
                    item.setOwnerUsername(e.get(Tables.APP_USER.LOGIN_ID));
                    item.setOwnerUserId(e.get(Tables.ACC.OWNER_USER_ID).longValue());
                    return item;
                }).collect(Collectors.toList());

    }
}

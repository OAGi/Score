package org.oagi.score.gateway.http.api.cc_management.service;

import com.google.common.collect.Lists;
import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.data.ACC;
import org.oagi.score.data.ASCC;
import org.oagi.score.data.ASCCP;
import org.oagi.score.data.BCCP;
import org.oagi.score.data.*;
import org.oagi.score.entity.jooq.Tables;
import org.oagi.score.gateway.http.api.cc_management.data.*;
import org.oagi.score.gateway.http.api.cc_management.helper.CcUtility;
import org.oagi.score.gateway.http.api.cc_management.repository.CcListRepository;
import org.oagi.score.gateway.http.api.common.data.PageRequest;
import org.oagi.score.gateway.http.api.common.data.PageResponse;
import org.oagi.score.gateway.http.api.info.data.SummaryCcExt;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.oagi.score.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static org.jooq.impl.DSL.and;
import static org.jooq.impl.DSL.max;
import static org.oagi.score.entity.jooq.Tables.*;

@Service
@Transactional(readOnly = true)
public class CcListService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private CcListRepository repository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private DSLContext dslContext;

    public PageResponse<CcList> getCcList(CcListRequest request) {
        List<CcList> ccLists = getCoreComponents(request);
        Stream<CcList> ccListStream = ccLists.stream();
        Comparator<CcList> comparator = getComparator(request.getPageRequest());
        if (comparator != null) {
            ccListStream = ccListStream.sorted(comparator);
        }

        PageResponse<CcList> pageResponse = getPageResponse(
                ccListStream.collect(Collectors.toList()), request.getPageRequest());
        return pageResponse;
    }

    private List<CcList> getCoreComponents(CcListRequest request) {
        request.setUsernameMap(userRepository.getUsernameMap());

        List<CcList> coreComponents = new ArrayList();
        coreComponents.addAll(repository.getAccList(request));
        coreComponents.addAll(repository.getAsccList(request));
        coreComponents.addAll(repository.getBccList(request));
        coreComponents.addAll(repository.getAsccpList(request));
        coreComponents.addAll(repository.getBccpList(request));
        coreComponents.addAll(repository.getBdtList(request));

        return coreComponents;
    }

    private Comparator<CcList> getComparator(PageRequest pageRequest) {
        Comparator<CcList> comparator = null;
        switch (pageRequest.getSortActive()) {
            case "state":
                comparator = Comparator.comparing(CcList::getState);
                break;

            case "den":
                comparator = Comparator.comparing(CcList::getDen);
                break;

            case "lastUpdateTimestamp":
                comparator = Comparator.comparing(CcList::getLastUpdateTimestamp);
                break;
        }

        if (comparator != null) {
            switch (pageRequest.getSortDirection()) {
                case "desc":
                    comparator = comparator.reversed();
                    break;
            }
        }

        return comparator;
    }

    private PageResponse<CcList> getPageResponse(List<CcList> list, PageRequest page) {
        PageResponse pageResponse = new PageResponse();

        int pageIndex = page.getPageIndex();
        pageResponse.setPage(pageIndex);

        int pageSize = page.getPageSize();
        pageResponse.setSize(pageSize);

        pageResponse.setLength(list.size());

        if (pageIndex < 0 || pageSize <= 0) {
            pageResponse.setList(Collections.emptyList());
        } else {
            if (list.size() > pageSize) {
                list = Lists.partition(list, pageSize).get(pageIndex);
            }
            pageResponse.setList(list);
        }

        return pageResponse;
    }

    public List<AsccpForAppendAsccp> getAsccpForAppendAsccpList(AuthenticatedPrincipal user, long releaseId, long extensionId) {
        return dslContext.select(
                ASCCP.ASCCP_ID,
                ASCCP.CURRENT_ASCCP_ID,
                ASCCP.PROPERTY_TERM,
                ASCCP.GUID,
                MODULE.MODULE_.as("module"),
                ASCCP.DEFINITION,
                ASCCP.IS_DEPRECATED.as("deprecated"),
                ASCCP.STATE,
                ASCCP.RELEASE_ID,
                ASCCP.REVISION_NUM,
                ASCCP.REVISION_TRACKING_NUM
        ).from(ASCCP.join(MODULE).on(ASCCP.MODULE_ID.eq(MODULE.MODULE_ID)))
                .where(
                        and(ASCCP.RELEASE_ID.lessOrEqual(ULong.valueOf(releaseId)),
                                ASCCP.STATE.eq(CcState.Published.getValue()))
                ).fetchInto(AsccpForAppendAsccp.class).stream().collect(groupingBy(AsccpForAppendAsccp::getGuid))
                .values().stream().map(e -> CcUtility.getLatestEntity(releaseId, e)).collect(Collectors.toList());
    }

    public List<BccpForAppendBccp> getBccpForAppendBccpList(AuthenticatedPrincipal user, long releaseId, long extensionId) {
        return dslContext.select(
                BCCP.BCCP_ID,
                BCCP.CURRENT_BCCP_ID,
                BCCP.PROPERTY_TERM,
                BCCP.GUID,
                MODULE.MODULE_.as("module"),
                BCCP.DEFINITION,
                BCCP.IS_DEPRECATED.as("deprecated"),
                BCCP.STATE,
                BCCP.RELEASE_ID,
                BCCP.REVISION_NUM,
                BCCP.REVISION_TRACKING_NUM
        ).from(BCCP.join(MODULE).on(BCCP.MODULE_ID.eq(MODULE.MODULE_ID)))
                .where(
                        and(BCCP.RELEASE_ID.lessOrEqual(ULong.valueOf(releaseId)),
                                BCCP.STATE.eq(CcState.Published.getValue()))
                ).fetchInto(BccpForAppendBccp.class).stream().collect(groupingBy(BccpForAppendBccp::getGuid))
                .values().stream().map(e -> CcUtility.getLatestEntity(releaseId, e)).collect(Collectors.toList());
    }

    public ASCCP getAsccp(long id) {
        return dslContext.selectFrom(ASCCP)
                .where(ASCCP.ASCCP_ID.eq(ULong.valueOf(id)))
                .fetchOneInto(ASCCP.class);
    }

    public ACC getAcc(long id) {
        return dslContext.selectFrom(ACC)
                .where(ACC.ACC_ID.eq(ULong.valueOf(id)))
                .fetchOneInto(ACC.class);
    }

    public ACC getAccByCurrentAccId(long currentAccId, long releaseId) {
        List<ACC> accList = dslContext.selectFrom(ACC)
                .where(and(
                        ACC.REVISION_NUM.greaterThan(0),
                        ACC.CURRENT_ACC_ID.eq(ULong.valueOf(currentAccId)),
                        ACC.RELEASE_ID.lessOrEqual(ULong.valueOf(releaseId))
                ))
                .fetchInto(ACC.class);
        return CcUtility.getLatestEntity(releaseId, accList);
    }

    public BCCP getBccp(long id) {
        return dslContext.selectFrom(BCCP)
                .where(BCCP.BCCP_ID.eq(ULong.valueOf(id)))
                .fetchOneInto(BCCP.class);
    }

    public ASCC getAscc(long id) {
        return dslContext.selectFrom(ASCC)
                .where(ASCC.ASCC_ID.eq(ULong.valueOf(id)))
                .fetchOneInto(ASCC.class);
    }

    public org.oagi.score.data.BCC getBcc(long id) {
        return dslContext.selectFrom(BCC)
                .where(BCC.BCC_ID.eq(ULong.valueOf(id)))
                .fetchOneInto(org.oagi.score.data.BCC.class);
    }

    public List<SummaryCcExt> getMyExtensionsUnusedInBIEs(AuthenticatedPrincipal user) {
        long requesterId = sessionService.userId(user);

        List<ULong> uegIds = dslContext.select(max(Tables.ACC.CURRENT_ACC_ID).as("id"))
                .from(Tables.ACC)
                .where(and(
                        Tables.ACC.OAGIS_COMPONENT_TYPE.eq(OagisComponentType.UserExtensionGroup.getValue()),
                        Tables.ACC.RELEASE_ID.greaterThan(ULong.valueOf(0L)),
                        Tables.ACC.OWNER_USER_ID.eq(ULong.valueOf(requesterId))
                ))
                .groupBy(Tables.ACC.GUID)
                .fetchInto(ULong.class);

        byte isUsed = (byte) 0;

        List<SummaryCcExt> summaryCcExtListForAscc = dslContext.select(
                Tables.ACC.ACC_ID,
                Tables.ACC.GUID,
                Tables.ACC.OBJECT_CLASS_TERM,
                Tables.ACC.STATE,
                Tables.ACC.LAST_UPDATE_TIMESTAMP,
                APP_USER.LOGIN_ID,
                APP_USER.APP_USER_ID,
                APP_USER.as("updater").LOGIN_ID,
                TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID,
                TOP_LEVEL_ASBIEP.STATE,
                ASCCP.as("bie").PROPERTY_TERM,
                ASCCP.PROPERTY_TERM,
                ASBIE.SEQ_KEY)
                .from(ASCC)
                .join(ACC).on(ASCC.FROM_ACC_ID.eq(ACC.ACC_ID))
                .join(ASBIE).on(and(ASCC.ASCC_ID.eq(ASBIE.BASED_ASCC_ID), ASBIE.IS_USED.eq(isUsed)))
                .join(ASCCP).on(ASCC.TO_ASCCP_ID.eq(ASCCP.ASCCP_ID))
                .join(APP_USER).on(ACC.OWNER_USER_ID.eq(APP_USER.APP_USER_ID))
                .join(APP_USER.as("updater")).on(ACC.LAST_UPDATED_BY.eq(APP_USER.as("updater").APP_USER_ID))
                .join(TOP_LEVEL_ASBIEP).on(ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID))
                .join(ASBIEP).on(TOP_LEVEL_ASBIEP.ASBIEP_ID.eq(ASBIEP.ASBIEP_ID))
                .join(ASCCP.as("bie")).on(ASBIEP.BASED_ASCCP_ID.eq(ASCCP.as("bie").ASCCP_ID))
                .where(ACC.ACC_ID.in(uegIds))
                .fetchStream().map(e -> {
                    SummaryCcExt item = new SummaryCcExt();
                    item.setAccId(e.get(Tables.ACC.ACC_ID).longValue());
                    item.setGuid(e.get(Tables.ACC.GUID));
                    item.setObjectClassTerm(e.get(Tables.ACC.OBJECT_CLASS_TERM));
                    item.setState(CcState.valueOf(e.get(Tables.ACC.STATE)));
                    item.setLastUpdateTimestamp(e.get(Tables.ACC.LAST_UPDATE_TIMESTAMP));
                    item.setLastUpdateUser(e.get(APP_USER.as("updater").LOGIN_ID));
                    item.setOwnerUsername(e.get(APP_USER.LOGIN_ID));
                    item.setOwnerUserId(e.get(APP_USER.APP_USER_ID).longValue());
                    item.setTopLevelAsbiepId(e.get(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID).longValue());
                    item.setBieState(BieState.valueOf(e.get(TOP_LEVEL_ASBIEP.STATE).intValue()));
                    item.setPropertyTerm(e.get(ASCCP.as("bie").PROPERTY_TERM));
                    item.setAssociationPropertyTerm(e.get(ASCCP.PROPERTY_TERM));
                    item.setSeqKey(e.get(ASBIE.SEQ_KEY).intValue());
                    return item;
                }).collect(Collectors.toList());

        List<SummaryCcExt> summaryCcExtListForBcc = dslContext.select(
                Tables.ACC.ACC_ID,
                Tables.ACC.GUID,
                Tables.ACC.OBJECT_CLASS_TERM,
                Tables.ACC.STATE,
                Tables.ACC.LAST_UPDATE_TIMESTAMP,
                APP_USER.LOGIN_ID,
                APP_USER.APP_USER_ID,
                APP_USER.as("updater").LOGIN_ID,
                TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID,
                TOP_LEVEL_ASBIEP.STATE,
                ASCCP.as("bie").PROPERTY_TERM,
                BCCP.PROPERTY_TERM,
                BBIE.SEQ_KEY)
                .from(BCC)
                .join(ACC).on(BCC.FROM_ACC_ID.eq(ACC.ACC_ID))
                .join(BBIE).on(and(BCC.BCC_ID.eq(BBIE.BASED_BCC_ID), BBIE.IS_USED.eq(isUsed)))
                .join(BCCP).on(BCC.TO_BCCP_ID.eq(BCCP.BCCP_ID))
                .join(APP_USER).on(ACC.OWNER_USER_ID.eq(APP_USER.APP_USER_ID))
                .join(APP_USER.as("updater")).on(ACC.LAST_UPDATED_BY.eq(APP_USER.as("updater").APP_USER_ID))
                .join(TOP_LEVEL_ASBIEP).on(BBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID))
                .join(ASBIEP).on(TOP_LEVEL_ASBIEP.ASBIEP_ID.eq(ASBIEP.ASBIEP_ID))
                .join(ASCCP.as("bie")).on(ASBIEP.BASED_ASCCP_ID.eq(ASCCP.as("bie").ASCCP_ID))
                .where(ACC.ACC_ID.in(uegIds))
                .fetchStream().map(e -> {
                    SummaryCcExt item = new SummaryCcExt();
                    item.setAccId(e.get(Tables.ACC.ACC_ID).longValue());
                    item.setGuid(e.get(Tables.ACC.GUID));
                    item.setObjectClassTerm(e.get(Tables.ACC.OBJECT_CLASS_TERM));
                    item.setState(CcState.valueOf(e.get(Tables.ACC.STATE)));
                    item.setLastUpdateTimestamp(e.get(Tables.ACC.LAST_UPDATE_TIMESTAMP));
                    item.setLastUpdateUser(e.get(APP_USER.as("updater").LOGIN_ID));
                    item.setOwnerUsername(e.get(APP_USER.LOGIN_ID));
                    item.setOwnerUserId(e.get(APP_USER.APP_USER_ID).longValue());
                    item.setTopLevelAsbiepId(e.get(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID).longValue());
                    item.setBieState(BieState.valueOf(e.get(TOP_LEVEL_ASBIEP.STATE).intValue()));
                    item.setPropertyTerm(e.get(ASCCP.as("bie").PROPERTY_TERM));
                    item.setAssociationPropertyTerm(e.get(BCCP.PROPERTY_TERM));
                    item.setSeqKey(e.get(BBIE.SEQ_KEY).intValue());
                    return item;
                }).collect(Collectors.toList());

        Set<SummaryCcExt> set = new HashSet();
        set.addAll(summaryCcExtListForAscc);
        set.addAll(summaryCcExtListForBcc);

        List<SummaryCcExt> result = new ArrayList(set);
        result.sort((o1, o2) -> {
            int compFirst = Long.compare(o1.getAccId(), o2.getAccId());
            if (compFirst == 0) {
                return Integer.compare(o1.getSeqKey(), o2.getSeqKey());
            }
            return compFirst;
        });
        return result;
    }
}


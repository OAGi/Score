package org.oagi.score.gateway.http.api.bie_management.service.edit_tree;

import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Record2;
import org.jooq.Record4;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.DataAccessForbiddenException;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.bie_management.model.BieState;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.abie.AbieId;
import org.oagi.score.gateway.http.api.bie_management.model.abie.AbieSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.asbie.AsbieId;
import org.oagi.score.gateway.http.api.bie_management.model.asbie.AsbieSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.AsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.AsbiepSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.bbie.BbieId;
import org.oagi.score.gateway.http.api.bie_management.model.bbie.BbieSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.bbie_sc.BbieScId;
import org.oagi.score.gateway.http.api.bie_management.model.bbie_sc.BbieScSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.bbiep.BbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.bbiep.BbiepSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.bie_edit.BieEditNode;
import org.oagi.score.gateway.http.api.bie_management.model.bie_edit.tree.*;
import org.oagi.score.gateway.http.api.bie_management.repository.BusinessInformationEntityRepository;
import org.oagi.score.gateway.http.api.bie_management.repository.criteria.InsertAbieArguments;
import org.oagi.score.gateway.http.api.bie_management.service.BieRepository;
import org.oagi.score.gateway.http.api.cc_management.model.Cardinality;
import org.oagi.score.gateway.http.api.cc_management.model.CcAssociation;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.EntityType;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScSummaryRecord;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.oas_management.controller.payload.GetBieForOasDocRequest;
import org.oagi.score.gateway.http.api.oas_management.controller.payload.GetBieForOasDocResponse;
import org.oagi.score.gateway.http.api.oas_management.service.OpenAPIDocService;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtManifestId;
import org.oagi.score.gateway.http.common.model.AccessPrivilege;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.*;
import org.oagi.score.gateway.http.common.util.Utility;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;
import static org.jooq.impl.DSL.and;
import static org.jooq.impl.DSL.inline;
import static org.oagi.score.gateway.http.common.model.ScoreRole.ADMINISTRATOR;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
@Transactional
public class DefaultBieEditTreeController implements BieEditTreeController {

    private static final String DEFAULT_TEXT_CONTENT_TYPE = "json";

    @Autowired
    private RepositoryFactory repositoryFactory;

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private BieRepository repository;

    @Autowired
    private BusinessInformationEntityRepository bieRepository;

    @Autowired
    private OpenAPIDocService openAPIDocService;

    @Autowired
    private RedissonClient redissonClient;

    private boolean initialized;
    private ScoreUser requester;
    private TopLevelAsbiepSummaryRecord topLevelAsbiep;
    private AccessPrivilege accessPrivilege;
    private BieState state;
    private boolean forceBieUpdate;

    public void initialize(ScoreUser requester, TopLevelAsbiepSummaryRecord topLevelAsbiep) {
        this.requester = requester;
        this.topLevelAsbiep = topLevelAsbiep;

        this.state = topLevelAsbiep.state();
        this.forceBieUpdate = true;

        UserId userId = requester.userId();
        accessPrivilege = AccessPrivilege.Prohibited;
        switch (this.state) {
        case Initiating:
            accessPrivilege = AccessPrivilege.Unprepared;
            break;

            case WIP:
                if (topLevelAsbiep.owner().userId().equals(userId)) {
                    accessPrivilege = AccessPrivilege.CanEdit;
                } else {
                    // Issue #1010, #1576, #1635
                    if (hasReuseBie(requester, topLevelAsbiep.topLevelAsbiepId()) ||
                        useAsBaseBie(requester, topLevelAsbiep.topLevelAsbiepId()) ||
                        requester.hasRole(ADMINISTRATOR)) {
                        accessPrivilege = AccessPrivilege.CanView;
                    } else {
                        throw new DataAccessForbiddenException("'" + requester.username() +
                                "' doesn't have an access privilege.");
                    }
                }
                break;

            case QA:
                if (topLevelAsbiep.owner().userId().equals(userId)) {
                    accessPrivilege = AccessPrivilege.CanMove;
                } else {
                    accessPrivilege = AccessPrivilege.CanView;
                }

                break;

            case Production:
                accessPrivilege = AccessPrivilege.CanView;
                break;
        }

        this.initialized = true;
    }

    public boolean hasReuseBie(ScoreUser requester, TopLevelAsbiepId topLevelAsbiepId) {
        var topLevelAsbiepQuery = repositoryFactory.topLevelAsbiepQueryRepository(requester);
        return !topLevelAsbiepQuery.getReusedTopLevelAsbiepSummaryList(topLevelAsbiepId).isEmpty();
    }

    public boolean useAsBaseBie(ScoreUser requester, TopLevelAsbiepId topLevelAsbiepId) {
        var topLevelAsbiepQuery = repositoryFactory.topLevelAsbiepQueryRepository(requester);
        return !topLevelAsbiepQuery.getDerivedTopLevelAsbiepSummaryList(topLevelAsbiepId).isEmpty();
    }

    private boolean isForceBieUpdate() {
        return forceBieUpdate;
    }

    public BieEditAbieNode getRootNode(TopLevelAsbiepId topLevelAsbiepId) {
        BieEditAbieNode rootNode = dslContext.select(
                TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID,
                LIBRARY.LIBRARY_ID,
                TOP_LEVEL_ASBIEP.RELEASE_ID,
                TOP_LEVEL_ASBIEP.STATE.as("top_level_asbiep_state"),
                TOP_LEVEL_ASBIEP.OWNER_USER_ID,
                TOP_LEVEL_ASBIEP.VERSION,
                TOP_LEVEL_ASBIEP.STATUS,
                LIBRARY.NAME.as("library_name"),
                RELEASE.RELEASE_NUM,
                APP_USER.LOGIN_ID,
                ASCCP.GUID,
                ASCCP.PROPERTY_TERM.as("name"),
                ASBIEP.DISPLAY_NAME,
                ASBIEP.ASBIEP_ID,
                ASBIEP.BASED_ASCCP_MANIFEST_ID.as("asccp_manifest_id"),
                ABIE.ABIE_ID,
                ABIE.BASED_ACC_MANIFEST_ID.as("acc_manifest_id"),
                inline("abie").as("type"),
                inline(true).as("used"),
                TOP_LEVEL_ASBIEP.IS_DEPRECATED.as("deprecated"),
                TOP_LEVEL_ASBIEP.DEPRECATED_REASON,
                TOP_LEVEL_ASBIEP.DEPRECATED_REMARK,
                TOP_LEVEL_ASBIEP.INVERSE_MODE,
                TOP_LEVEL_ASBIEP.as("based").TOP_LEVEL_ASBIEP_ID.as("based_top_level_asbiep_id"))
                .from(TOP_LEVEL_ASBIEP)
                .join(ASBIEP).on(and(
                        TOP_LEVEL_ASBIEP.ASBIEP_ID.eq(ASBIEP.ASBIEP_ID),
                        ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.eq(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID)
                ))
                .join(ABIE).on(and(
                        ASBIEP.ROLE_OF_ABIE_ID.eq(ABIE.ABIE_ID),
                        ABIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID)
                ))
                .join(ASCCP_MANIFEST).on(ASBIEP.BASED_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.ASCCP_MANIFEST_ID))
                .join(ASCCP).on(ASCCP_MANIFEST.ASCCP_ID.eq(ASCCP.ASCCP_ID))
                .join(RELEASE).on(TOP_LEVEL_ASBIEP.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(LIBRARY).on(RELEASE.LIBRARY_ID.eq(LIBRARY.LIBRARY_ID))
                .join(APP_USER).on(TOP_LEVEL_ASBIEP.OWNER_USER_ID.eq(APP_USER.APP_USER_ID))
                .leftJoin(TOP_LEVEL_ASBIEP.as("based")).on(TOP_LEVEL_ASBIEP.BASED_TOP_LEVEL_ASBIEP_ID.eq(TOP_LEVEL_ASBIEP.as("based").TOP_LEVEL_ASBIEP_ID))
                .where(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiepId.value())))
                .fetchOneInto(BieEditAbieNode.class);
        rootNode.setHasChild(hasChild(rootNode));
        rootNode.setAccess(accessPrivilege);

        // Issue #1519
        GetBieForOasDocResponse getBieForOasDocResponse = openAPIDocService.getBieForOasDoc(requester,
                new GetBieForOasDocRequest(requester)
                        .withTopLevelAsbiepId(topLevelAsbiepId));
        if (getBieForOasDocResponse.getLength() > 0) {
            rootNode.setBieForOasDoc(getBieForOasDocResponse.getResults().get(0));
        }
        return rootNode;
    }

    // have to check @hakju
    private boolean hasChild(BieEditAbieNode abieNode) {
        TopLevelAsbiepId topLevelAsbiepId = abieNode.getTopLevelAsbiepId();
        ReleaseId releaseId = abieNode.getReleaseId();
        AccSummaryRecord acc = null;
        var accQuery = repositoryFactory.accQueryRepository(requester);
        if (topLevelAsbiepId != null) {
            TopLevelAsbiepSummaryRecord topLevelAsbiep = repositoryFactory.topLevelAsbiepQueryRepository(requester)
                    .getTopLevelAsbiepSummary(topLevelAsbiepId);
            AsbiepSummaryRecord asbiep = repositoryFactory.asbiepQueryRepository(requester)
                    .getAsbiepSummary(topLevelAsbiep.asbiepId());
            AsccpSummaryRecord asccp = repositoryFactory.asccpQueryRepository(requester)
                            .getAsccpSummary(asbiep.basedAsccpManifestId());
            acc = accQuery.getAccSummary(asccp.roleOfAccManifestId());
        } else {
            acc = accQuery.getAccSummary(abieNode.getAccManifestId());
        }

        if (acc == null) {
            return false;
        }

        if (accQuery.getAsccSummaryList(acc.accManifestId()).size() > 0) {
            return true;
        }
        if (accQuery.getAsccSummaryList(acc.accManifestId()).size() > 0) {
            return true;
        }

        if (acc.basedAccManifestId() != null) {
            BieEditAbieNode basedAbieNode = new BieEditAbieNode();
            basedAbieNode.setReleaseId(releaseId);

            acc = accQuery.getAccSummary(acc.basedAccManifestId());
            basedAbieNode.setAccManifestId(acc.accManifestId());
            return hasChild(basedAbieNode);
        }

        return false;
    }

    @Override
    public List<BieEditNode> getDescendants(ScoreUser user, BieEditNode node, boolean hideUnused) {
        /*
         * If this profile BIE is in Editing state, descendants of given node will create during this process,
         * and this must be thread-safe.
         */
        RLock lock = null;
        String lockName = getClass().getSimpleName() + ".getDescendants(" +
                node.getType() + ", " + topLevelAsbiep.topLevelAsbiepId() + ")";
        lock = redissonClient.getLock(lockName);

        try {
            if (lock != null) {
                try {
                    boolean locked = lock.tryLock(10, TimeUnit.SECONDS);
                    if (!locked) {
                        throw new IllegalStateException("Lock is held by another thread/process.");
                    }
                } catch (InterruptedException e) {
                    throw new IllegalStateException("Lock acquisition is cancelled by interrupt.");
                }
            }

            switch (node.getType()) {
                case "abie":
                    return getDescendants((BieEditAbieNode) node, hideUnused);
                case "asbiep":
                    return getDescendants((BieEditAsbiepNode) node, hideUnused);
                case "bbiep":
                    return getDescendants((BieEditBbiepNode) node, hideUnused);
            }
        } finally {
            if (lock != null) {
                lock.unlock();
            }
        }

        return Collections.emptyList();
    }

    private List<BieEditNode> getDescendants(BieEditAbieNode abieNode, boolean hideUnused) {
        Map<AsccManifestId, AsbieSummaryRecord> asbieMap;
        Map<BccManifestId, BbieSummaryRecord> bbieMap;

        AccManifestId accManifestId;
        AsbiepId asbiepId = abieNode.getAsbiepId();

        var asbiepQuery = repositoryFactory.asbiepQueryRepository(requester);
        AsbiepSummaryRecord asbiep = asbiepQuery.getAsbiepSummary(asbiepId);

        var abieQuery = repositoryFactory.abieQueryRepository(requester);
        AbieSummaryRecord abie = abieQuery.getAbieSummary(asbiep.roleOfAbieId());

        var asbieQuery = repositoryFactory.asbieQueryRepository(requester);
        asbieMap = asbieQuery.getAsbieSummaryList(abie.abieId(), abieNode.getTopLevelAsbiepId()).stream()
                .collect(toMap(AsbieSummaryRecord::basedAsccManifestId, Function.identity()));

        var bbieQuery = repositoryFactory.bbieQueryRepository(requester);
        bbieMap = bbieQuery.getBbieSummaryList(abie.abieId(), abieNode.getTopLevelAsbiepId()).stream()
                .collect(toMap(BbieSummaryRecord::basedBccManifestId, Function.identity()));

        var asccpQuery = repositoryFactory.asccpQueryRepository(requester);
        AsccpSummaryRecord asccp = asccpQuery.getAsccpSummary(asbiep.basedAsccpManifestId());
        accManifestId = asccp.roleOfAccManifestId();

        List<BieEditNode> children = getChildren(asbieMap, bbieMap, abie, accManifestId, abieNode, hideUnused);
        return children;
    }

    private List<BieEditNode> getDescendants(BieEditAsbiepNode asbiepNode, boolean hideUnused) {
        Map<AsccManifestId, AsbieSummaryRecord> asbieMap;
        Map<BccManifestId, BbieSummaryRecord> bbieMap;

        AccManifestId accManifestId = asbiepNode.getAccManifestId();
        AbieId abieId = asbiepNode.getAbieId();

        if (abieId == null && isForceBieUpdate()) {
            var accQuery = repositoryFactory.accQueryRepository(requester);
            AccSummaryRecord acc = accQuery.getAccSummary(asbiepNode.getAccManifestId());

            abieId = new InsertAbieArguments(repositoryFactory.abieCommandRepository(requester))
                    .setUserId(requester.userId())
                    .setTopLevelAsbiepId(asbiepNode.getTopLevelAsbiepId())
                    .setAccManifestId(acc.accManifestId())
                    .execute();
        }

        if (abieId != null) {
            var abieQuery = repositoryFactory.abieQueryRepository(requester);
            AbieSummaryRecord abie = abieQuery.getAbieSummary(abieId);

            var asbieQuery = repositoryFactory.asbieQueryRepository(requester);
            asbieMap = asbieQuery.getAsbieSummaryList(abie.abieId(), asbiepNode.getTopLevelAsbiepId()).stream()
                    .collect(toMap(AsbieSummaryRecord::basedAsccManifestId, Function.identity()));

            var bbieQuery = repositoryFactory.bbieQueryRepository(requester);
            bbieMap = bbieQuery.getBbieSummaryList(abie.abieId(), asbiepNode.getTopLevelAsbiepId()).stream()
                    .collect(toMap(BbieSummaryRecord::basedBccManifestId, Function.identity()));

            List<BieEditNode> children = getChildren(asbieMap, bbieMap, abie, accManifestId, asbiepNode, hideUnused);
            return children;
        } else {
            return Collections.emptyList();
        }
    }

    private List<BieEditNode> getChildren(
            Map<AsccManifestId, AsbieSummaryRecord> asbieMap,
            Map<BccManifestId, BbieSummaryRecord> bbieMap,
            AbieSummaryRecord fromAbie, AccManifestId accManifestId,
            BieEditNode node, boolean hideUnused) {
        List<BieEditNode> children = new ArrayList();

        List<CcAssociation> assocList = getAssociationsByAccManifestId(accManifestId);
        int seqKey = 1;
        var accQuery = repositoryFactory.accQueryRepository(requester);
        for (CcAssociation assoc : assocList) {
            if (assoc instanceof AsccSummaryRecord) {
                AsccSummaryRecord ascc = (AsccSummaryRecord) assoc;
                AsbieSummaryRecord asbie = asbieMap.get(ascc.asccManifestId());
                BieEditAsbiepNode asbiepNode = createAsbiepNode(fromAbie, seqKey++, asbie, ascc);
                if (asbiepNode == null) {
                    seqKey--;
                    continue;
                }

                AccSummaryRecord roleOfAcc = accQuery.getAccSummary(asbiepNode.getAccManifestId());
                if (roleOfAcc.isGroup()) {
                    children.addAll(getDescendants(asbiepNode, hideUnused));
                } else {
                    if (hideUnused && (asbie == null || asbie.asbieId() == null || !asbie.used())) {
                        seqKey--;
                        continue;
                    }

                    children.add(asbiepNode);
                }
            } else {
                BccSummaryRecord bcc = (BccSummaryRecord) assoc;
                BbieSummaryRecord bbie = bbieMap.get(bcc.bccManifestId());
                if (hideUnused && (bbie == null || bbie.bbieId() == null || !bbie.used())) {
                    continue;
                }
                BieEditBbiepNode bbiepNode;
                if (bcc.entityType() == EntityType.Attribute) {
                    bbiepNode = createBbiepNode(fromAbie, 0, bbie, bcc, hideUnused);
                } else {
                    bbiepNode = createBbiepNode(fromAbie, seqKey++, bbie, bcc, hideUnused);
                }
                children.add(bbiepNode);
            }
        }

        return children;
    }

    private List<CcAssociation> getAssociationsByAccManifestId(AccManifestId accManifestId) {
        Stack<AccSummaryRecord> accStack = getAccStack(accManifestId);

        List<BccSummaryRecord> attributeBccList = new ArrayList();
        List<CcAssociation> assocList = new ArrayList();

        var accQuery = repositoryFactory.accQueryRepository(requester);
        var asccpQuery = repositoryFactory.asccpQueryRepository(requester);
        while (!accStack.isEmpty()) {
            AccSummaryRecord acc = accStack.pop();

            AccManifestId fromAccManifestId = acc.accManifestId();
            List<AsccSummaryRecord> asccList = accQuery.getAsccSummaryList(fromAccManifestId).stream()
                    .filter(e -> e.state() == CcState.Published).collect(Collectors.toList());
            List<BccSummaryRecord> bccList = accQuery.getBccSummaryList(fromAccManifestId).stream()
                    .filter(e -> e.state() == CcState.Published).collect(Collectors.toList());

            attributeBccList.addAll(
                    bccList.stream().filter(e -> e.entityType() == EntityType.Attribute).collect(Collectors.toList()));

            List<CcAssociation> tmpAssocList = new ArrayList();
            tmpAssocList.addAll(asccList);
            tmpAssocList.addAll(bccList.stream().filter(e -> e.entityType() != EntityType.Attribute).collect(Collectors.toList()));
            tmpAssocList = tmpAssocList.stream()
                    .sorted(Comparator.comparingLong(e -> e.seqKeyId().value().longValue()))
                    .collect(Collectors.toList());

            for (CcAssociation assoc : tmpAssocList) {
                if (assoc instanceof AsccSummaryRecord) {
                    AsccpSummaryRecord toAsccp = asccpQuery.getAsccpSummary(((AsccSummaryRecord) assoc).toAsccpManifestId());
                    AccSummaryRecord roleOfAcc = accQuery.getAccSummary(toAsccp.roleOfAccManifestId());
                    if (roleOfAcc.isGroup()) {
                        assocList.addAll(getAssociationsByAccManifestId(roleOfAcc.accManifestId()));
                    } else {
                        assocList.add(assoc);
                    }
                } else {
                    assocList.add(assoc);
                }
            }
        }

        assocList.addAll(0, attributeBccList);
        return assocList;
    }

    private Stack<AccSummaryRecord> getAccStack(AccManifestId accManifestId) {
        Stack<AccSummaryRecord> accStack = new Stack();
        var accQuery = repositoryFactory.accQueryRepository(requester);
        AccSummaryRecord acc = accQuery.getAccSummary(accManifestId);
        /*
         * Issue #708
         * If the UEG's state is not 'Published', its children couldn't get it by the logic above.
         */
        if (acc == null) {
            return accStack;
        }
        accStack.push(acc);

        while (acc.basedAccManifestId() != null) {
            acc = accQuery.getAccSummary(acc.basedAccManifestId());
            accStack.push(acc);
        }

        return accStack;
    }

    private BieEditAsbiepNode createAsbiepNode(AbieSummaryRecord fromAbie, int seqKey,
                                               AsbieSummaryRecord asbie, AsccSummaryRecord ascc) {
        BieEditAsbiepNode asbiepNode = new BieEditAsbiepNode();

        TopLevelAsbiepId topLevelAsbiepId = topLevelAsbiep.topLevelAsbiepId();
        ReleaseId releaseId = topLevelAsbiep.release().releaseId();

        asbiepNode.setTopLevelAsbiepId(topLevelAsbiepId);
        asbiepNode.setReleaseId(releaseId);
        asbiepNode.setType("asbiep");
        asbiepNode.setGuid(ascc.guid().value());
        asbiepNode.setAsccManifestId(ascc.asccManifestId());

        AsccpSummaryRecord asccp = repositoryFactory.asccpQueryRepository(requester)
                .getAsccpSummary(ascc.toAsccpManifestId());
        asbiepNode.setAsccpManifestId(asccp.asccpManifestId());

        if (!StringUtils.hasLength(asbiepNode.getName())) {
            asbiepNode.setName(asccp.propertyTerm());
        }

        AccSummaryRecord acc;
        var accQuery = repositoryFactory.accQueryRepository(requester);
        if (asbiepNode.getAccManifestId() == null) {
            acc = accQuery.getAccSummary(asccp.roleOfAccManifestId());
            if (acc == null) {
                return null;
            }
            asbiepNode.setAccManifestId(acc.accManifestId());
        } else {
            acc = accQuery.getAccSummary(asbiepNode.getAccManifestId());
        }

        if (asbie == null && isForceBieUpdate()) {
            AbieId abieId = new InsertAbieArguments(repositoryFactory.abieCommandRepository(requester))
                    .setUserId(requester.userId())
                    .setTopLevelAsbiepId(asbiepNode.getTopLevelAsbiepId())
                    .setAccManifestId(acc.accManifestId())
                    .execute();

            AsbiepRecord asbiepRecord = repository.createAsbiep(requester, asccp.asccpManifestId(), abieId, topLevelAsbiepId);
            AsbiepId asbiepId = new AsbiepId(asbiepRecord.getAsbiepId().toBigInteger());
            AsbieRecord asbieRecord = repository.createAsbie(requester, fromAbie.abieId(), asbiepId, ascc.asccManifestId(),
                    seqKey, topLevelAsbiepId);

            asbie = AsbieSummaryRecord.builder(
                            new AsbieId(asbieRecord.getAsbieId().toBigInteger()),
                            ascc.asccManifestId(),
                            fromAbie.abieId(), asbiepId,
                            fromAbie.state(), topLevelAsbiepId,
                            fromAbie.owner(), fromAbie.created(), fromAbie.lastUpdated())
                    .used(asbieRecord.getIsUsed() == 1)
                    .cardinality(new Cardinality(asbieRecord.getCardinalityMin(), asbieRecord.getCardinalityMax()))
                    .build();
        }

        if (asbie != null) {
            asbiepNode.setAsbieId(asbie.asbieId());
            asbiepNode.setAsbiepId(asbie.toAsbiepId());

            var asbiepQuery = repositoryFactory.asbiepQueryRepository(requester);
            AsbiepSummaryRecord asbiep = asbiepQuery.getAsbiepSummary(asbie.toAsbiepId());
            var abieQuery = repositoryFactory.abieQueryRepository(requester);
            AbieSummaryRecord abie = abieQuery.getAbieSummary(asbiep.roleOfAbieId());
            asbiepNode.setAbieId(abie.abieId());
            asbiepNode.setAccManifestId(abie.basedAccManifestId());

            var asccpQuery = repositoryFactory.asccpQueryRepository(requester);
            asccp = asccpQuery.getAsccpSummary(asbiep.basedAsccpManifestId());

            asbiepNode.setName(asccp.propertyTerm());
            asbiepNode.setUsed(asbie.used());
            asbiepNode.setRequired(ascc.cardinality().min() > 0);
        }

        asbiepNode.setHasChild(hasChild(asbiepNode));

        return asbiepNode;
    }

    private BieEditBbiepNode createBbiepNode(AbieSummaryRecord fromAbie, int seqKey,
                                             BbieSummaryRecord bbie, BccSummaryRecord bcc,
                                             boolean hideUnused) {
        BieEditBbiepNode bbiepNode = new BieEditBbiepNode();

        TopLevelAsbiepId topLevelAsbiepId = topLevelAsbiep.topLevelAsbiepId();
        ReleaseId releaseId = topLevelAsbiep.release().releaseId();

        bbiepNode.setTopLevelAsbiepId(topLevelAsbiepId);
        bbiepNode.setReleaseId(releaseId);
        bbiepNode.setType("bbiep");
        bbiepNode.setGuid(bcc.guid().value());
        bbiepNode.setAttribute(bcc.entityType() == EntityType.Attribute);

        bbiepNode.setBccManifestId(bcc.bccManifestId());
        BccpSummaryRecord bccp = repositoryFactory.bccpQueryRepository(requester)
                .getBccpSummary(bcc.toBccpManifestId());
        bbiepNode.setBccpManifestId(bccp.bccpManifestId());
        bbiepNode.setBdtManifestId(bccp.dtManifestId());

        if (!StringUtils.hasLength(bbiepNode.getName())) {
            bbiepNode.setName(bccp.propertyTerm());
        }

        if (bbie == null && isForceBieUpdate()) {
            BbiepRecord bbiepRecord = repository.createBbiep(requester, bcc.toBccpManifestId(), topLevelAsbiepId);
            BbiepId bbiepId = new BbiepId(bbiepRecord.getBbiepId().toBigInteger());
            BbieRecord bbieRecord = repository.createBbie(requester, fromAbie.abieId(), bbiepId,
                    bcc.bccManifestId(), bccp.dtManifestId(), seqKey, topLevelAsbiepId);
            BbieId bbieId = new BbieId(bbieRecord.getBbieId().toBigInteger());

            bbie = BbieSummaryRecord.builder(
                            bbieId, bcc.bccManifestId(),
                            fromAbie.abieId(), bbiepId,
                            fromAbie.state(), fromAbie.ownerTopLevelAsbiepId(),
                            fromAbie.owner(), fromAbie.created(), fromAbie.lastUpdated())
                    .used(bbieRecord.getIsUsed() == 1)
                    .cardinality(new Cardinality(bbieRecord.getCardinalityMin(), bbieRecord.getCardinalityMax()))
                    .build();
        }

        if (bbie != null) {
            bbiepNode.setBbieId(bbie.bbieId());
            bbiepNode.setBbiepId(bbie.toBbiepId());

            var bbiepQuery = repositoryFactory.bbiepQueryRepository(requester);
            BbiepSummaryRecord bbiep = bbiepQuery.getBbiepSummary(bbie.toBbiepId());

            var bccpQuery = repositoryFactory.bccpQueryRepository(requester);
            bccp = bccpQuery.getBccpSummary(bbiep.basedBccpManifestId());

            bbiepNode.setName(bccp.propertyTerm());
            bbiepNode.setUsed(bbie.used());
            bbiepNode.setRequired(bcc.cardinality().min() > 0);
        }

        bbiepNode.setHasChild(hasChild(bbiepNode, hideUnused));

        return bbiepNode;
    }

    public boolean hasChild(BieEditAsbiepNode asbiepNode) {
        BieEditAbieNode abieNode = new BieEditAbieNode();
        abieNode.setReleaseId(asbiepNode.getReleaseId());
        abieNode.setAccManifestId(asbiepNode.getAccManifestId());

        return hasChild(abieNode);
    }

    public boolean hasChild(BieEditBbiepNode bbiepNode, boolean hideUnused) {
        if (hideUnused) {
            var bbieScQuery = repositoryFactory.bbieScQueryRepository(requester);
            return bbieScQuery.getBbieScSummaryList(bbiepNode.getBbieId(), bbiepNode.getTopLevelAsbiepId()).stream()
                    .filter(e -> e.used()).count() > 0;
        } else {
            var bccpQuery = repositoryFactory.bccpQueryRepository(requester);
            BccpSummaryRecord bccp = bccpQuery.getBccpSummary(bbiepNode.getBccpManifestId());
            var dtQuery = repositoryFactory.dtQueryRepository(requester);
            return dtQuery.getDtScSummaryList(bccp.dtManifestId()).stream()
                    .filter(e -> e.cardinality().max() > 0).count() > 0;
        }
    }

    private List<BieEditNode> getDescendants(BieEditBbiepNode bbiepNode, boolean hideUnused) {
        BbiepId bbiepId = bbiepNode.getBbiepId();
        TopLevelAsbiepId topLevelAsbiepId = bbiepNode.getTopLevelAsbiepId();
        BccpSummaryRecord bccp;
        var bccpQuery = repositoryFactory.bccpQueryRepository(requester);
        if (bbiepId != null) {
            var bbiepQuery = repositoryFactory.bbiepQueryRepository(requester);
            BbiepSummaryRecord bbiep = bbiepQuery.getBbiepSummary(bbiepId);
            bccp = bccpQuery.getBccpSummary(bbiep.basedBccpManifestId());
        } else {
            if (hideUnused) {
                return Collections.emptyList();
            }

            bccp = bccpQuery.getBccpSummary(bbiepNode.getBccpManifestId());
        }

        List<BieEditNode> children = new ArrayList();
        List<DtScSummaryRecord> dtScList = repositoryFactory.dtQueryRepository(requester)
                .getDtScSummaryList(bccp.dtManifestId());
        BbieId bbieId = bbiepNode.getBbieId();
        for (DtScSummaryRecord dtSc : dtScList) {
            BieEditBbieScNode bbieScNode = new BieEditBbieScNode();

            bbieScNode.setTopLevelAsbiepId(topLevelAsbiepId);
            bbieScNode.setReleaseId(bbiepNode.getReleaseId());
            bbieScNode.setType("bbie_sc");
            bbieScNode.setGuid(dtSc.guid().value());
            bbieScNode.setName(getName(dtSc));

            DtScManifestId dtScManifestId = dtSc.dtScManifestId();
            if (bbieId != null) {
                var bbieScQuery = repositoryFactory.bbieScQueryRepository(requester);
                BbieScSummaryRecord bbieSc = bbieScQuery.getBbieScSummaryList(bbieId, topLevelAsbiepId).stream()
                        .filter(e -> e.basedDtScManifestId().equals(dtScManifestId))
                        .findFirst().orElse(null);
                if (bbieSc == null) {
                    if (isForceBieUpdate()) {
                        BbieScId bbieScId = repository.createBbieSc(requester, bbieId, dtScManifestId, topLevelAsbiepId);
                        bbieSc = bbieScQuery.getBbieScSummary(bbieScId);
                    }
                }

                if (hideUnused && (bbieSc == null || bbieSc.bbieScId() == null || !bbieSc.used())) {
                    continue;
                }
                bbieScNode.setBbieScId(bbieSc.bbieScId());
                bbieScNode.setUsed(bbieSc.used());
            }

            bbieScNode.setDtScManifestId(dtScManifestId);

            children.add(bbieScNode);
        }

        return children;
    }

    private String getName(DtScSummaryRecord dtSc) {
        String name;
        if (dtSc.representationTerm().equalsIgnoreCase("Text") ||
                dtSc.propertyTerm().contains(dtSc.representationTerm())) {
            name = Utility.spaceSeparator(dtSc.propertyTerm());
        } else {
            name = Utility.spaceSeparator(dtSc.propertyTerm().concat(dtSc.representationTerm()));
        }
        return name;
    }

    @Override
    public BieEditNodeDetail getDetail(BieEditNode node) {
        switch (node.getType()) {
            case "abie":
                return getDetail((BieEditAbieNode) node);
            case "asbiep":
                return getDetail((BieEditAsbiepNode) node);
            case "bbiep":
                return getDetail((BieEditBbiepNode) node);
            case "bbie_sc":
                return getDetail((BieEditBbieScNode) node);
            default:
                throw new IllegalStateException();
        }
    }

    private BieEditNodeDetail getDetail(BieEditAbieNode abieNode) {
        BieEditAbieNodeDetail detail = dslContext.select(
                ABIE.REMARK,
                ABIE.BIZ_TERM,
                ABIE.DEFINITION)
                .from(ABIE)
                .where(ABIE.ABIE_ID.eq(ULong.valueOf(abieNode.getAbieId().value())))
                .fetchOneInto(BieEditAbieNodeDetail.class);

        return detail.append(abieNode);
    }

    private BieEditAsbiepNodeDetail getDetail(BieEditAsbiepNode asbiepNode) {

        BieEditAsbiepNodeDetail detail;
        if (asbiepNode.getAsbieId() != null) {
            detail = dslContext.select(
                    ASBIE.CARDINALITY_MIN.as("bie_cardinality_min"),
                    ASBIE.CARDINALITY_MAX.as("bie_cardinality_max"),
                    ASBIE.IS_USED.as("used"),
                    ASBIE.IS_NILLABLE.as("bie_nillable"),
                    ASBIE.DEFINITION.as("context_definition")
            ).from(ASBIE)
                    .where(ASBIE.ASBIE_ID.eq(ULong.valueOf(asbiepNode.getAsbieId().value())))
                    .fetchOneInto(BieEditAsbiepNodeDetail.class);
        } else {
            detail = dslContext.select(
                    ASCC.CARDINALITY_MIN.as("bie_cardinality_min"),
                    ASCC.CARDINALITY_MAX.as("bie_cardinality_max"))
                    .from(ASCC)
                    .join(ASCC_MANIFEST).on(ASCC.ASCC_ID.eq(ASCC_MANIFEST.ASCC_ID))
                    .where(ASCC_MANIFEST.ASCC_MANIFEST_ID.eq(ULong.valueOf(asbiepNode.getAsccManifestId().value())))
                    .fetchOneInto(BieEditAsbiepNodeDetail.class);
        }

        if (asbiepNode.getAsbiepId() != null) {
            detail.setAsbiepBizTerm(dslContext.select(
                    ASBIEP.BIZ_TERM).from(ASBIEP)
                    .where(ASBIEP.ASBIEP_ID.eq(ULong.valueOf(asbiepNode.getAsbiepId().value())))
                    .fetchOneInto(String.class));

            detail.setAsbiepRemark(dslContext.select(
                    ASBIEP.REMARK).from(ASBIEP)
                    .where(ASBIEP.ASBIEP_ID.eq(ULong.valueOf(asbiepNode.getAsbiepId().value())))
                    .fetchOneInto(String.class));
        }

        if (asbiepNode.getAsccManifestId() != null) {
            Record2<Integer, Integer> res = dslContext.select(
                    ASCC.CARDINALITY_MIN,
                    ASCC.CARDINALITY_MAX)
                    .from(ASCC)
                    .join(ASCC_MANIFEST).on(ASCC.ASCC_ID.eq(ASCC_MANIFEST.ASCC_ID))
                    .where(ASCC_MANIFEST.ASCC_MANIFEST_ID.eq(ULong.valueOf(asbiepNode.getAsccManifestId().value())))
                    .fetchOne();

            detail.setCcCardinalityMin(res.get(ASCC.CARDINALITY_MIN));
            detail.setCcCardinalityMax(res.get(ASCC.CARDINALITY_MAX));
            detail.setRequired(detail.getCcCardinalityMin() > 0);
        }

        if (asbiepNode.getAsccpManifestId() != null) {
            Record1<Byte> ccNillable = dslContext.select(
                    ASCCP.IS_NILLABLE)
                    .from(ASCCP)
                    .join(ASCCP_MANIFEST).on(ASCCP.ASCCP_ID.eq(ASCCP_MANIFEST.ASCCP_ID))
                    .where(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(ULong.valueOf(asbiepNode.getAsccpManifestId().value())))
                    .fetchOne();

            detail.setCcNillable(ccNillable.get(ASCCP.IS_NILLABLE) == 1);
        }

        detail.setAsccDefinition(dslContext.select(
                        ASCC.DEFINITION).from(ASCC)
                .join(ASCC_MANIFEST).on(ASCC.ASCC_ID.eq(ASCC_MANIFEST.ASCC_ID))
                .where(ASCC_MANIFEST.ASCC_MANIFEST_ID.eq(ULong.valueOf(asbiepNode.getAsccManifestId().value())))
                .fetchOneInto(String.class));

        detail.setAsccpDefinition(dslContext.select(
                        ASCCP.DEFINITION).from(ASCCP)
                .join(ASCCP_MANIFEST).on(ASCCP.ASCCP_ID.eq(ASCCP_MANIFEST.ASCCP_ID))
                .where(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(ULong.valueOf(asbiepNode.getAsccpManifestId().value())))
                .fetchOneInto(String.class));

        detail.setAccDefinition(dslContext.select(
                        ACC.DEFINITION).from(ACC)
                .join(ACC_MANIFEST).on(ACC.ACC_ID.eq(ACC_MANIFEST.ACC_MANIFEST_ID))
                .where(ACC.ACC_ID.eq(ULong.valueOf(asbiepNode.getAccManifestId().value())))
                .fetchOneInto(String.class));

        return detail.append(asbiepNode);
    }

    private BieEditBbiepNodeDetail getDetail(BieEditBbiepNode bbiepNode) {
        BieEditBbiepNodeDetail detail;

        if (bbiepNode.getBbieId() != null) {
            detail = dslContext.select(
                    BBIE.CARDINALITY_MIN.as("bie_cardinality_min"),
                    BBIE.CARDINALITY_MAX.as("bie_cardinality_max"),
                    BBIE.IS_USED.as("used"),
                    BBIE.XBT_MANIFEST_ID,
                    BBIE.CODE_LIST_MANIFEST_ID,
                    BBIE.AGENCY_ID_LIST_MANIFEST_ID,
                    BBIE.DEFAULT_VALUE.as("bie_default_value"),
                    BBIE.FIXED_VALUE.as("bie_fixed_value"),
                    BBIE.IS_NILLABLE.as("bie_nillable"),
                    BBIE.DEFINITION.as("context_definition"),
                    BBIE.EXAMPLE
            ).from(BBIE)
                    .where(BBIE.BBIE_ID.eq(ULong.valueOf(bbiepNode.getBbieId().value())))
                    .fetchOneInto(BieEditBbiepNodeDetail.class);

            /* Issue #762 */
            if (detail.getAgencyIdListManifestId() != null && detail.getAgencyIdListManifestId() != null) {
                detail.setXbtManifestId(null);
                detail.setCodeListManifestId(null);
            } else if (detail.getCodeListManifestId() != null && detail.getCodeListManifestId() != null) {
                detail.setXbtManifestId(null);
                detail.setAgencyIdListManifestId(null);
            } else if (detail.getXbtManifestId() != null && detail.getXbtManifestId() != null) {
                detail.setCodeListManifestId(null);
                detail.setAgencyIdListManifestId(null);
            }
        } else {
            detail = dslContext.select(
                    BCC.CARDINALITY_MIN.as("bie_cardinality_min"),
                    BCC.CARDINALITY_MAX.as("bie_cardinality_max"),
                    BCC.DEFAULT_VALUE.as("bie_default_value"),
                    BCC.FIXED_VALUE.as("bie_fixed_value"),
                    BCC.IS_NILLABLE.as("bie_nillable"))
                    .from(BCC)
                    .join(BCC_MANIFEST).on(BCC.BCC_ID.eq(BCC_MANIFEST.BCC_ID))
                    .where(BCC_MANIFEST.BCC_MANIFEST_ID.eq(ULong.valueOf(bbiepNode.getBccManifestId().value())))
                    .fetchOneInto(BieEditBbiepNodeDetail.class);
        }

        if (bbiepNode.getBbiepId() != null) {
            Record4<String, String, ULong, String> rs =
                    dslContext.select(BBIEP.BIZ_TERM, BBIEP.REMARK, BCCP.BDT_ID, DT_MANIFEST.DEN)
                            .from(BBIEP)
                            .join(BCCP_MANIFEST).on(BBIEP.BASED_BCCP_MANIFEST_ID.eq(BCCP_MANIFEST.BCCP_MANIFEST_ID))
                            .join(BCCP).on(BCCP_MANIFEST.BCCP_ID.eq(BCCP.BCCP_ID))
                            .join(DT_MANIFEST).on(BCCP_MANIFEST.BDT_MANIFEST_ID.eq(DT_MANIFEST.DT_MANIFEST_ID))
                            .join(DT).on(DT_MANIFEST.DT_ID.eq(DT.DT_ID))
                            .where(BBIEP.BBIEP_ID.eq(ULong.valueOf(bbiepNode.getBbiepId().value())))
                            .fetchOne();

            detail.setBizTerm(rs.getValue(BBIEP.BIZ_TERM));
            detail.setRemark(rs.getValue(BBIEP.REMARK));
            detail.setBdtId(rs.getValue(BCCP.BDT_ID).toBigInteger());
            detail.setBdtDen(rs.getValue(DT_MANIFEST.DEN).replaceAll("_ ", " "));
        } else {
            Record2<String, ULong> rs = dslContext.select(
                    DT_MANIFEST.DEN, DT_MANIFEST.DT_ID)
                    .from(BBIEP)
                    .join(BCCP_MANIFEST).on(BBIEP.BASED_BCCP_MANIFEST_ID.eq(BCCP_MANIFEST.BCCP_MANIFEST_ID))
                    .join(DT_MANIFEST).on(BCCP_MANIFEST.BDT_MANIFEST_ID.eq(DT_MANIFEST.DT_MANIFEST_ID))
                    .where(BBIEP.BBIEP_ID.eq(ULong.valueOf(bbiepNode.getBbiepId().value()))).fetchOne();
            detail.setBdtDen(rs.getValue(DT_MANIFEST.DEN));
            detail.setBdtId(rs.getValue(DT_MANIFEST.DT_ID).toBigInteger());
        }

        if (bbiepNode.getBbieId() == null) {
            XbtManifestId defaultXbtManifestId = repository.getDefaultXbtManifestIdByDtManifestId(detail.getBdtManifestId());
            detail.setXbtManifestId(defaultXbtManifestId);
        }

        if (bbiepNode.getBccManifestId() != null) {
            BccRecord bccRecord = dslContext.select(BCC.fields())
                    .from(BCC)
                    .join(BCC_MANIFEST).on(BCC.BCC_ID.eq(BCC_MANIFEST.BCC_ID))
                    .where(BCC_MANIFEST.BCC_MANIFEST_ID.eq(ULong.valueOf(bbiepNode.getBccManifestId().value())))
                    .fetchOneInto(BccRecord.class);
            detail.setCcCardinalityMin(bccRecord.getCardinalityMin());
            detail.setCcCardinalityMax(bccRecord.getCardinalityMax());
            detail.setCcDefaultValue(bccRecord.getDefaultValue());
            detail.setCcFixedValue(bccRecord.getFixedValue());
            detail.setCcNillable(bccRecord.getIsNillable() == 1);
            detail.setRequired(detail.getCcCardinalityMin() > 0);
        }

//        BieEditBdtPriRestri bdtPriRestri = getBdtPriRestri(bbiepNode);
//        detail.setXbtList(bdtPriRestri.getXbtList());
//        detail.setCodeLists(bdtPriRestri.getCodeLists());
//        detail.setAgencyIdLists(bdtPriRestri.getAgencyIdLists());
//        detail.setAssociationDefinition(dslContext.select(
//                BCC.DEFINITION).from(BCC)
//                .join(BCC_MANIFEST).on(BCC.BCC_ID.eq(BCC_MANIFEST.BCC_ID))
//                .where(BCC_MANIFEST.BCC_MANIFEST_ID.eq(ULong.valueOf(bbiepNode.getBccManifestId().value())))
//                .fetchOneInto(String.class)
//        );

        detail.setComponentDefinition(dslContext.select(
                        BCCP.DEFINITION).from(BCCP)
                .join(BCCP_MANIFEST).on(BCCP.BCCP_ID.eq(BCCP_MANIFEST.BCCP_ID))
                .where(BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(ULong.valueOf(bbiepNode.getBccpManifestId().value())))
                .fetchOneInto(String.class)
        );

        return detail.append(bbiepNode);
    }

//    private BieEditBdtPriRestri getBdtPriRestri(BieEditBbiepNode bbiepNode) {
//        DtManifestId bdtManifestId = bbiepNode.getBdtManifestId();
//
//        List<BieEditXbt> bieEditXbtList = dslContext.select(
//                        DT_AWD_PRI.DT_AWD_PRI_ID.as("pri_restri_id"),
//                        DT_AWD_PRI.IS_DEFAULT, XBT_MANIFEST.XBT_MANIFEST_ID, XBT.XBT_ID, XBT.NAME.as("xbt_name"))
//                .from(DT_AWD_PRI)
//                .join(XBT_MANIFEST).on(DT_AWD_PRI.XBT_MANIFEST_ID.eq(XBT_MANIFEST.XBT_MANIFEST_ID))
//                .join(XBT).on(XBT_MANIFEST.XBT_ID.eq(XBT.XBT_ID))
//                .where(DT_AWD_PRI.DT_MANIFEST_ID.eq(ULong.valueOf(bdtManifestId.value())))
//                .fetchInto(BieEditXbt.class);
//
//        List<BieEditCodeList> bieEditCodeLists = dslContext.select(
//                CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID, CODE_LIST_MANIFEST.BASED_CODE_LIST_MANIFEST_ID,
//                        DT_AWD_PRI.IS_DEFAULT, CODE_LIST.NAME.as("code_list_name"))
//                .from(DT_AWD_PRI)
//                .join(CODE_LIST_MANIFEST).on(DT_AWD_PRI.CODE_LIST_MANIFEST_ID.eq(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID))
//                .join(CODE_LIST).on(CODE_LIST_MANIFEST.CODE_LIST_ID.eq(CODE_LIST.CODE_LIST_ID))
//                .where(DT_AWD_PRI.DT_MANIFEST_ID.eq(ULong.valueOf(bdtManifestId.value())))
//                .fetchInto(BieEditCodeList.class);
//
//        List<BieEditAgencyIdList> bieEditAgencyIdLists = dslContext.select(
//                AGENCY_ID_LIST.AGENCY_ID_LIST_ID, DT_AWD_PRI.IS_DEFAULT, AGENCY_ID_LIST.NAME.as("agency_id_list_name"))
//                .from(DT_AWD_PRI)
//                .join(AGENCY_ID_LIST_MANIFEST).on(DT_AWD_PRI.AGENCY_ID_LIST_MANIFEST_ID.eq(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID))
//                .where(DT_AWD_PRI.DT_MANIFEST_ID.eq(ULong.valueOf(bdtManifestId.value())))
//                .fetchInto(BieEditAgencyIdList.class);
//
//        if (bieEditCodeLists.isEmpty() && bieEditAgencyIdLists.isEmpty()) {
//            bieEditCodeLists = getAllCodeLists();
//            bieEditAgencyIdLists = getAllAgencyIdLists();
//        } else {
//            if (!bieEditCodeLists.isEmpty()) {
//                List<BieEditCodeList> basedCodeLists = getBieEditCodeListByBasedCodeListManifestIds(
//                        bieEditCodeLists.stream().filter(e -> e.getBasedCodeListManifestId() != null)
//                                .map(e -> e.getBasedCodeListManifestId()).collect(Collectors.toList())
//                );
//                List<BieEditCodeList> basedCodeLists2 = getCodeListsByBasedCodeList(bieEditCodeLists.get(0).getCodeListManifestId());
//                basedCodeLists2.clear();
//                for (int i = 0; i < bieEditCodeLists.size(); i++) {
//                    basedCodeLists2.addAll(getCodeListsByBasedCodeList(bieEditCodeLists.get(i).getCodeListManifestId()));
//                }
//                bieEditCodeLists.addAll(0, basedCodeLists);
//                bieEditCodeLists.addAll(0, basedCodeLists2);
//                basedCodeLists2.clear();
//                for (int i = 0; i < bieEditCodeLists.size(); i++) {
//                    basedCodeLists2.addAll(getCodeListsByBasedCodeList(bieEditCodeLists.get(i).getCodeListManifestId()));
//                }
//                bieEditCodeLists.addAll(basedCodeLists2);
//                Set<BieEditCodeList> set = new HashSet<BieEditCodeList>(bieEditCodeLists);
//                bieEditCodeLists.clear();
//                bieEditCodeLists.addAll(set); // remove dupplicate elements
//            }
//        }
//
//        BieEditBdtPriRestri bdtPriRestri = new BieEditBdtPriRestri();
//
//        bieEditXbtList.sort(Comparator.comparing(BieEditXbt::getPriRestriId, BigInteger::compareTo));
//        bdtPriRestri.setXbtList(bieEditXbtList);
//
//        bieEditCodeLists.sort(Comparator.comparing(e -> e.getCodeListManifestId().value(), BigInteger::compareTo));
//        bdtPriRestri.setCodeLists(bieEditCodeLists);
//
//        bieEditAgencyIdLists.sort(Comparator.comparing(e -> e.getAgencyIdListId().value(), BigInteger::compareTo));
//        bdtPriRestri.setAgencyIdLists(bieEditAgencyIdLists);
//
//        return bdtPriRestri;
//    }

    private BieEditBbieScNodeDetail getDetail(BieEditBbieScNode bbieScNode) {
        BieEditBbieScNodeDetail detail;
        if (bbieScNode.getBbieScId() != null) {
            detail = dslContext.select(
                    BBIE_SC.CARDINALITY_MIN.as("bie_cardinality_min"),
                    BBIE_SC.CARDINALITY_MAX.as("bie_cardinality_max"),
                    BBIE_SC.IS_USED.as("used"),
                    BBIE_SC.XBT_MANIFEST_ID,
                    BBIE_SC.CODE_LIST_MANIFEST_ID,
                    BBIE_SC.AGENCY_ID_LIST_MANIFEST_ID,
                    BBIE_SC.DEFAULT_VALUE.as("bie_default_value"),
                    BBIE_SC.FIXED_VALUE.as("bie_fixed_value"),
                    BBIE_SC.BIZ_TERM,
                    BBIE_SC.REMARK,
                    BBIE_SC.DEFINITION.as("context_definition"),
                    BBIE_SC.EXAMPLE
            )
                    .from(BBIE_SC)
                    .where(BBIE_SC.BBIE_SC_ID.eq(ULong.valueOf(bbieScNode.getBbieScId().value())))
                    .fetchOneInto(BieEditBbieScNodeDetail.class);

            /* Issue #762 */
            if (detail.getAgencyIdListManifestId() != null && detail.getAgencyIdListManifestId() != null) {
                detail.setXbtManifestId(null);
                detail.setCodeListManifestId(null);
            } else if (detail.getCodeListManifestId() != null && detail.getCodeListManifestId() != null) {
                detail.setXbtManifestId(null);
                detail.setAgencyIdListManifestId(null);
            } else if (detail.getXbtManifestId() != null && detail.getXbtManifestId() != null) {
                detail.setCodeListManifestId(null);
                detail.setAgencyIdListManifestId(null);
            }
        } else {
            detail = dslContext.select(
                    DT_SC.CARDINALITY_MIN.as("bie_cardinality_min"),
                    DT_SC.CARDINALITY_MAX.as("bie_cardinality_max"),
                    DT_SC.DEFAULT_VALUE.as("bie_default_value"),
                    DT_SC.FIXED_VALUE.as("bie_fixed_value")
            )
                    .from(DT_SC)
                    .join(DT_SC_MANIFEST).on(DT_SC.DT_SC_ID.eq(DT_SC_MANIFEST.DT_SC_ID))
                    .where(DT_SC_MANIFEST.DT_SC_MANIFEST_ID.eq(ULong.valueOf(bbieScNode.getDtScManifestId().value())))
                    .fetchOneInto(BieEditBbieScNodeDetail.class);
        }

        if (bbieScNode.getBbieScId() == null) {
            XbtManifestId defaultXbtManifestId = repository.getDefaultXbtManifestIdByDtScManifestId(bbieScNode.getDtScManifestId());
            detail.setXbtManifestId(defaultXbtManifestId);
        }

        if (bbieScNode.getDtScManifestId() != null) {
            DtScRecord dtScRecord = dslContext.select(DT_SC.fields())
                    .from(DT_SC)
                    .join(DT_SC_MANIFEST).on(DT_SC.DT_SC_ID.eq(DT_SC_MANIFEST.DT_SC_ID))
                    .where(DT_SC_MANIFEST.DT_SC_MANIFEST_ID.eq(ULong.valueOf(bbieScNode.getDtScManifestId().value())))
                    .fetchOneInto(DtScRecord.class);

            detail.setCcCardinalityMin(dtScRecord.getCardinalityMin());
            detail.setCcCardinalityMax(dtScRecord.getCardinalityMax());
            detail.setCcDefaultValue(dtScRecord.getDefaultValue());
            detail.setCcFixedValue(dtScRecord.getFixedValue());
        }

//        BieEditBdtScPriRestri bdtScPriRestri = getBdtScPriRestri(bbieScNode);
//        detail.setXbtList(bdtScPriRestri.getXbtList());
//        detail.setCodeLists(bdtScPriRestri.getCodeLists());
//        detail.setAgencyIdLists(bdtScPriRestri.getAgencyIdLists());

        detail.setComponentDefinition(
                dslContext.select(DT_SC.DEFINITION)
                        .from(DT_SC)
                        .join(DT_SC_MANIFEST).on(DT_SC.DT_SC_ID.eq(DT_SC_MANIFEST.DT_SC_ID))
                        .where(DT_SC_MANIFEST.DT_SC_MANIFEST_ID.eq(ULong.valueOf(bbieScNode.getDtScManifestId().value())))
                        .fetchOneInto(String.class)
        );
        return detail.append(bbieScNode);
    }

    @Override
    public void updateState(ScoreUser requester, BieState state) {
        Queue<TopLevelAsbiepSummaryRecord> topLevelAsbiepQueue = new LinkedList<>();
        topLevelAsbiepQueue.offer(topLevelAsbiep);

        var topLevelAsbiepQuery = repositoryFactory.topLevelAsbiepQueryRepository(requester);

        while (!topLevelAsbiepQueue.isEmpty()) {
            TopLevelAsbiepSummaryRecord topLevelAsbiep = topLevelAsbiepQueue.poll();
            repository.updateState(topLevelAsbiep.topLevelAsbiepId(), state);

            // Issue #1604
            // Apply cascade state update to reused BIEs.
            topLevelAsbiepQueue.addAll(
                    topLevelAsbiepQuery.getReusedTopLevelAsbiepSummaryList(topLevelAsbiep.topLevelAsbiepId())
                            .stream().filter(e -> topLevelAsbiep.owner().userId().equals(e.owner().userId()))
                            .collect(Collectors.toList())
            );

            // Issue #1635
            // Apply cascade state update to inherited BIEs.
            topLevelAsbiepQueue.addAll(
                    topLevelAsbiepQuery.getDerivedTopLevelAsbiepSummaryList(topLevelAsbiep.topLevelAsbiepId())
                            .stream().filter(e -> topLevelAsbiep.owner().userId().equals(e.owner().userId()))
                            .collect(Collectors.toList())
            );
        }
    }

    @Override
    public boolean updateDetail(BieEditNodeDetail detail) {
        if (detail instanceof BieEditAbieNodeDetail) {
            updateDetail((BieEditAbieNodeDetail) detail);
            return true;
        } else if (detail instanceof BieEditAsbiepNodeDetail) {
            updateDetail((BieEditAsbiepNodeDetail) detail);
            return true;
        } else if (detail instanceof BieEditBbiepNodeDetail) {
            updateDetail((BieEditBbiepNodeDetail) detail);
            return true;
        } else if (detail instanceof BieEditBbieScNodeDetail) {
            updateDetail((BieEditBbieScNodeDetail) detail);
            return true;
        } else {
            return false;
        }
    }

    private void updateDetail(BieEditAbieNodeDetail abieNodeDetail) {
        LocalDateTime timestamp = LocalDateTime.now();
        dslContext.update(ABIE)
                .set(ABIE.REMARK, emptyToNull(abieNodeDetail.getAsbiepRemark()))
                .set(ABIE.BIZ_TERM, emptyToNull(abieNodeDetail.getAsbiepBizTerm()))
                .set(ABIE.DEFINITION, emptyToNull(abieNodeDetail.getAsccpDefinition()))
                .set(ABIE.LAST_UPDATED_BY, ULong.valueOf(requester.userId().value()))
                .set(ABIE.LAST_UPDATE_TIMESTAMP, timestamp)
                .where(ABIE.ABIE_ID.eq(ULong.valueOf(abieNodeDetail.getAbieId().value())))
                .execute();
    }

    private void updateDetail(BieEditAsbiepNodeDetail asbiepNodeDetail) {
        if (asbiepNodeDetail.getBieCardinalityMin() != null) {
            dslContext.update(ASBIE)
                    .set(ASBIE.CARDINALITY_MIN, asbiepNodeDetail.getBieCardinalityMin())
                    .where(ASBIE.ASBIE_ID.eq(ULong.valueOf(asbiepNodeDetail.getAsbieId().value())))
                    .execute();
        }
        if (asbiepNodeDetail.getBieCardinalityMax() != null) {
            dslContext.update(ASBIE)
                    .set(ASBIE.CARDINALITY_MAX, asbiepNodeDetail.getBieCardinalityMax())
                    .where(ASBIE.ASBIE_ID.eq(ULong.valueOf(asbiepNodeDetail.getAsbieId().value())))
                    .execute();
        }
        Record1<Byte> rs = dslContext.select(ASCCP.IS_NILLABLE).from(ASCCP)
                .where(ASCCP.ASCCP_ID.eq(ULong.valueOf(asbiepNodeDetail.getAsccpManifestId().value()))).fetchOne();

        if (rs.getValue(ASCCP.IS_NILLABLE) != 1 && asbiepNodeDetail.getBieNillable() != null) {
            dslContext.update(ASBIE)
                    .set(ASBIE.IS_NILLABLE, (byte) (asbiepNodeDetail.getBieNillable() ? 1 : 0))
                    .where(ASBIE.ASBIE_ID.eq(ULong.valueOf(asbiepNodeDetail.getAsbieId().value())))
                    .execute();
        }

        UserId userId = requester.userId();
        LocalDateTime timestamp = LocalDateTime.now();

        dslContext.update(ASBIE)
                .set(ASBIE.IS_USED, (byte) (asbiepNodeDetail.isUsed() ? 1 : 0))
                .set(ASBIE.DEFINITION, emptyToNull(asbiepNodeDetail.getAsbiepDefinition()))
                .set(ASBIE.LAST_UPDATED_BY, ULong.valueOf(userId.value()))
                .set(ASBIE.LAST_UPDATE_TIMESTAMP, timestamp)
                .where(ASBIE.ASBIE_ID.eq(ULong.valueOf(asbiepNodeDetail.getAsbieId().value())))
                .execute();

        dslContext.update(ASBIEP)
                .set(ASBIEP.BIZ_TERM, emptyToNull(asbiepNodeDetail.getAsbiepBizTerm()))
                .set(ASBIEP.REMARK, emptyToNull(asbiepNodeDetail.getAsbiepRemark()))
                .set(ASBIEP.LAST_UPDATED_BY, ULong.valueOf(userId.value()))
                .set(ASBIEP.LAST_UPDATE_TIMESTAMP, timestamp)
                .where(ASBIEP.ASBIEP_ID.eq(ULong.valueOf(asbiepNodeDetail.getAsbiepId().value())))
                .execute();
    }

    private void updateDetail(BieEditBbiepNodeDetail bbiepNodeDetail) {
        if (bbiepNodeDetail.getBieCardinalityMin() != null) {
            dslContext.update(BBIE)
                    .set(BBIE.CARDINALITY_MIN, bbiepNodeDetail.getBieCardinalityMin())
                    .where(BBIE.BBIE_ID.eq(ULong.valueOf(bbiepNodeDetail.getBbieId().value()))).execute();
        }
        if (bbiepNodeDetail.getBieCardinalityMax() != null) {
            dslContext.update(BBIE)
                    .set(BBIE.CARDINALITY_MAX, bbiepNodeDetail.getBieCardinalityMax())
                    .where(BBIE.BBIE_ID.eq(ULong.valueOf(bbiepNodeDetail.getBbieId().value()))).execute();
        }

        XbtManifestId xbtManifestId = bbiepNodeDetail.getXbtManifestId();
        CodeListManifestId codeListManifestId = bbiepNodeDetail.getCodeListManifestId();
        AgencyIdListManifestId agencyIdListManifestId = bbiepNodeDetail.getAgencyIdListManifestId();

        if (agencyIdListManifestId != null) {
            dslContext.update(BBIE)
                    .set(BBIE.AGENCY_ID_LIST_MANIFEST_ID, ULong.valueOf(agencyIdListManifestId.value()))
                    .setNull(BBIE.CODE_LIST_MANIFEST_ID)
                    .setNull(BBIE.XBT_MANIFEST_ID)
                    .where(BBIE.BBIE_ID.eq(ULong.valueOf(bbiepNodeDetail.getBbieId().value()))).execute();
        } else if (codeListManifestId != null) {
            dslContext.update(BBIE)
                    .setNull(BBIE.AGENCY_ID_LIST_MANIFEST_ID)
                    .set(BBIE.CODE_LIST_MANIFEST_ID, ULong.valueOf(codeListManifestId.value()))
                    .setNull(BBIE.XBT_MANIFEST_ID)
                    .where(BBIE.BBIE_ID.eq(ULong.valueOf(bbiepNodeDetail.getBbieId().value()))).execute();
        } else if (xbtManifestId != null) {
            dslContext.update(BBIE)
                    .setNull(BBIE.AGENCY_ID_LIST_MANIFEST_ID)
                    .setNull(BBIE.CODE_LIST_MANIFEST_ID)
                    .set(BBIE.XBT_MANIFEST_ID, ULong.valueOf(xbtManifestId.value()))
                    .where(BBIE.BBIE_ID.eq(ULong.valueOf(bbiepNodeDetail.getBbieId().value()))).execute();
        }

        BccRecord bccRecord = dslContext.selectFrom(BCC)
                .where(BCC.BCC_ID.eq(ULong.valueOf(bbiepNodeDetail.getBccManifestId().value()))).fetchOne();

        if (bccRecord.getIsNillable() != 1 && bbiepNodeDetail.getBieNillable() != null) {
            dslContext.update(BBIE)
                    .set(BBIE.IS_NILLABLE, (byte) (bbiepNodeDetail.getBieNillable() ? 1 : 0))
                    .where(BBIE.BBIE_ID.eq(ULong.valueOf(bbiepNodeDetail.getBbieId().value()))).execute();
        }

        Record2<String, String> bccp = dslContext.select(BCCP.DEFAULT_VALUE, BCCP.FIXED_VALUE)
                .from(BCCP).where(BCCP.BCCP_ID.eq(ULong.valueOf(bbiepNodeDetail.getBccpManifestId().value()))).fetchOne();

        if (bccRecord.getDefaultValue() == null &&
                bccRecord.getFixedValue() == null &&
                bccp.getValue(BCCP.DEFAULT_VALUE) == null &&
                bccp.getValue(BCCP.FIXED_VALUE) == null) {

            dslContext.update(BBIE)
                    .set(BBIE.FIXED_VALUE, emptyToNull(bbiepNodeDetail.getBieFixedValue()))
                    .set(BBIE.DEFAULT_VALUE, emptyToNull(bbiepNodeDetail.getBieDefaultValue()))
                    .where(BBIE.BBIE_ID.eq(ULong.valueOf(bbiepNodeDetail.getBbieId().value()))).execute();
        }

        dslContext.update(BBIE)
                .set(BBIE.IS_USED, (byte) (bbiepNodeDetail.isUsed() ? 1 : 0))
                .set(BBIE.DEFINITION, emptyToNull(bbiepNodeDetail.getContextDefinition()))
                .set(BBIE.EXAMPLE, emptyToNull(bbiepNodeDetail.getExample()))
                .where(BBIE.BBIE_ID.eq(ULong.valueOf(bbiepNodeDetail.getBbieId().value()))).execute();

        UserId userId = requester.userId();
        LocalDateTime timestamp = LocalDateTime.now();

        dslContext.update(BBIEP)
                .set(BBIEP.BIZ_TERM, emptyToNull(bbiepNodeDetail.getBizTerm()))
                .set(BBIEP.REMARK, emptyToNull(bbiepNodeDetail.getRemark()))
                .set(BBIEP.LAST_UPDATED_BY, ULong.valueOf(userId.value()))
                .set(BBIEP.LAST_UPDATE_TIMESTAMP, timestamp)
                .where(BBIEP.BBIEP_ID.eq(ULong.valueOf(bbiepNodeDetail.getBbiepId().value())))
                .execute();
    }

    private void updateDetail(BieEditBbieScNodeDetail bbieScNodeDetail) {
        if (bbieScNodeDetail.getBieCardinalityMin() != null) {
            dslContext.update(BBIE_SC)
                    .set(BBIE_SC.CARDINALITY_MIN, bbieScNodeDetail.getBieCardinalityMin())
                    .where(BBIE_SC.BBIE_SC_ID.eq(ULong.valueOf(bbieScNodeDetail.getBbieScId().value()))).execute();
        }

        if (bbieScNodeDetail.getBieCardinalityMax() != null) {
            dslContext.update(BBIE_SC)
                    .set(BBIE_SC.CARDINALITY_MAX, bbieScNodeDetail.getBieCardinalityMax())
                    .where(BBIE_SC.BBIE_SC_ID.eq(ULong.valueOf(bbieScNodeDetail.getBbieScId().value()))).execute();
        }

        XbtManifestId xbtManifestId = bbieScNodeDetail.getXbtManifestId();
        CodeListManifestId codeListManifestId = bbieScNodeDetail.getCodeListManifestId();
        AgencyIdListManifestId agencyIdListManifestId = bbieScNodeDetail.getAgencyIdListManifestId();

        if (agencyIdListManifestId != null) {
            dslContext.update(BBIE_SC)
                    .setNull(BBIE_SC.XBT_MANIFEST_ID)
                    .setNull(BBIE_SC.CODE_LIST_MANIFEST_ID)
                    .set(BBIE_SC.AGENCY_ID_LIST_MANIFEST_ID, ULong.valueOf(agencyIdListManifestId.value()))
                    .where(BBIE_SC.BBIE_SC_ID.eq(ULong.valueOf(bbieScNodeDetail.getBbieScId().value()))).execute();
        } else if (codeListManifestId != null) {
            dslContext.update(BBIE_SC)
                    .setNull(BBIE_SC.XBT_MANIFEST_ID)
                    .set(BBIE_SC.CODE_LIST_MANIFEST_ID, ULong.valueOf(codeListManifestId.value()))
                    .setNull(BBIE_SC.AGENCY_ID_LIST_MANIFEST_ID)
                    .where(BBIE_SC.BBIE_SC_ID.eq(ULong.valueOf(bbieScNodeDetail.getBbieScId().value()))).execute();
        } else if (xbtManifestId != null) {
            dslContext.update(BBIE_SC)
                    .set(BBIE_SC.XBT_MANIFEST_ID, ULong.valueOf(xbtManifestId.value()))
                    .setNull(BBIE_SC.CODE_LIST_MANIFEST_ID)
                    .setNull(BBIE_SC.AGENCY_ID_LIST_MANIFEST_ID)
                    .where(BBIE_SC.BBIE_SC_ID.eq(ULong.valueOf(bbieScNodeDetail.getBbieScId().value()))).execute();
        }

        DtScRecord dtScRecord = dslContext.select(DT_SC.fields())
                .from(DT_SC)
                .join(DT_SC_MANIFEST).on(DT_SC.DT_SC_ID.eq(DT_SC_MANIFEST.DT_SC_ID))
                .where(DT_SC_MANIFEST.DT_SC_MANIFEST_ID.eq(ULong.valueOf(bbieScNodeDetail.getDtScManifestId().value())))
                .fetchOneInto(DtScRecord.class);

        if (dtScRecord.getDefaultValue() == null && dtScRecord.getFixedValue() == null) {
            dslContext.update(BBIE_SC)
                    .set(BBIE_SC.DEFAULT_VALUE, emptyToNull(bbieScNodeDetail.getBieDefaultValue()))
                    .set(BBIE_SC.FIXED_VALUE, emptyToNull(bbieScNodeDetail.getBieFixedValue()))
                    .where(BBIE_SC.BBIE_SC_ID.eq(ULong.valueOf(bbieScNodeDetail.getBbieScId().value())))
                    .execute();
        }

        dslContext.update(BBIE_SC)
                .set(BBIE_SC.IS_USED, (byte) (bbieScNodeDetail.isUsed() ? 1 : 0))
                .set(BBIE_SC.DEFINITION, emptyToNull(bbieScNodeDetail.getContextDefinition()))
                .set(BBIE_SC.BIZ_TERM, emptyToNull(bbieScNodeDetail.getBizTerm()))
                .set(BBIE_SC.REMARK, emptyToNull(bbieScNodeDetail.getRemark()))
                .set(BBIE_SC.EXAMPLE, emptyToNull(bbieScNodeDetail.getExample()))
                .where(BBIE_SC.BBIE_SC_ID.eq(ULong.valueOf(bbieScNodeDetail.getBbieScId().value())))
                .execute();
    }

    private String emptyToNull(String str) {
        if (str != null) {
            str = str.trim();
        }
        if (!StringUtils.hasLength(str)) {
            return null;
        }
        return str;
    }
}

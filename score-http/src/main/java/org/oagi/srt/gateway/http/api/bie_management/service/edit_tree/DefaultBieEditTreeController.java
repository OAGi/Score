package org.oagi.srt.gateway.http.api.bie_management.service.edit_tree;

import org.jooq.DSLContext;
import org.jooq.Record2;
import org.jooq.Record4;
import org.jooq.RecordMapper;
import org.jooq.types.ULong;
import org.oagi.srt.data.BieState;
import org.oagi.srt.data.OagisComponentType;
import org.oagi.srt.data.SeqKeySupportable;
import org.oagi.srt.data.TopLevelAbie;
import org.oagi.srt.entity.jooq.Tables;
import org.oagi.srt.gateway.http.api.DataAccessForbiddenException;
import org.oagi.srt.gateway.http.api.bie_management.data.bie_edit.*;
import org.oagi.srt.gateway.http.api.bie_management.data.bie_edit.tree.*;
import org.oagi.srt.gateway.http.api.bie_management.service.BieRepository;
import org.oagi.srt.gateway.http.api.cc_management.repository.CcNodeRepository;
import org.oagi.srt.gateway.http.configuration.security.SessionService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;
import static org.jooq.impl.DSL.and;
import static org.jooq.impl.DSL.inline;
import static org.oagi.srt.entity.jooq.Tables.*;
import static org.oagi.srt.gateway.http.helper.SrtJdbcTemplate.newSqlParameterSource;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
@Transactional
public class DefaultBieEditTreeController implements BieEditTreeController {

    private static final String DEFAULT_TEXT_CONTENT_TYPE = "json";

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private BieRepository repository;

    @Autowired
    private CcNodeRepository ccNodeRepository;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private RedissonClient redissonClient;

    private boolean initialized;
    private User user;
    private TopLevelAbie topLevelAbie;
    private BieState state;
    private boolean forceBieUpdate;

    public void initialize(User user, TopLevelAbie topLevelAbie) {
        this.user = user;
        this.topLevelAbie = topLevelAbie;

        this.state = BieState.valueOf(topLevelAbie.getState());
        this.forceBieUpdate = true;
        switch (this.state) {
            case Editing:
                if (sessionService.userId(user) != topLevelAbie.getOwnerUserId()) {
                    throw new DataAccessForbiddenException("'" + user.getUsername() +
                            "' doesn't have an access privilege.");
                }
                break;
        }

        this.initialized = true;
    }

    private boolean isForceBieUpdate() {
        return forceBieUpdate;
    }

    public BieEditAbieNode getRootNode(long topLevelAbieId) {
        BieEditAbieNode rootNode = dslContext.select(
                TOP_LEVEL_ABIE.TOP_LEVEL_ABIE_ID,
                TOP_LEVEL_ABIE.RELEASE_ID,
                TOP_LEVEL_ABIE.STATE.as("top_level_abie_state"),
                TOP_LEVEL_ABIE.OWNER_USER_ID,
                ASCCP.GUID,
                ASCCP.PROPERTY_TERM.as("name"),
                ASBIEP.ASBIEP_ID,
                ASBIEP.BASED_ASCCP_ID.as("asccp_id"),
                ABIE.ABIE_ID,
                ABIE.BASED_ACC_ID.as("acc_id"),
                inline("abie").as("type"),
                inline(true).as("used"))
                .from(TOP_LEVEL_ABIE)
                .join(ABIE).on(ABIE.ABIE_ID.eq(TOP_LEVEL_ABIE.ABIE_ID))
                .join(ASBIEP).on(ASBIEP.ROLE_OF_ABIE_ID.eq(ABIE.ABIE_ID))
                .join(ASCCP).on(ASCCP.ASCCP_ID.eq(ASBIEP.BASED_ASCCP_ID))
                .where(TOP_LEVEL_ABIE.TOP_LEVEL_ABIE_ID.eq(ULong.valueOf(topLevelAbieId)))
                .fetchOneInto(BieEditAbieNode.class);
        rootNode.setHasChild(hasChild(rootNode));

        return rootNode;
    }

    private boolean hasChild(BieEditAbieNode abieNode) {
        long fromAccId;

        long topLevelAbieId = abieNode.getTopLevelAbieId();
        long releaseId = abieNode.getReleaseId();
        BieEditAcc acc = null;
        if (topLevelAbieId > 0L) {
            fromAccId = repository.getCurrentAccIdByTopLevelAbieId(topLevelAbieId);
        } else {
            acc = repository.getAcc(abieNode.getAccId());
            fromAccId = acc.getCurrentAccId();
        }

        if (repository.getAsccListByFromAccId(fromAccId, releaseId).size() > 0) {
            return true;
        }
        if (repository.getBccListByFromAccId(fromAccId, releaseId).size() > 0) {
            return true;
        }

        long currentAccId = fromAccId;
        if (acc == null) {
            acc = repository.getAccByCurrentAccId(currentAccId, releaseId);
        }
        if (acc != null && acc.getBasedAccId() != null) {
            BieEditAbieNode basedAbieNode = new BieEditAbieNode();
            basedAbieNode.setReleaseId(releaseId);

            acc = repository.getAccByCurrentAccId(acc.getBasedAccId(), releaseId);
            basedAbieNode.setAccId(acc.getAccId());
            return hasChild(basedAbieNode);
        }

        return false;
    }

    @Override
    public List<BieEditNode> getDescendants(BieEditNode node, boolean hideUnused) {
        /*
         * If this profile BIE is in Editing state, descendants of given node will create during this process,
         * and this must be thread-safe.
         */
        RLock lock = null;
        String lockName = getClass().getSimpleName() + ".getDescendants(" +
                node.getType() + ", " + topLevelAbie.getTopLevelAbieId() + ")";
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
        Map<Long, BieEditAsbie> asbieMap;
        Map<Long, BieEditBbie> bbieMap;

        long currentAccId;
        long asbiepId = abieNode.getAsbiepId();
        long abieId = repository.getAbieByAsbiepId(asbiepId).getAbieId();
        asbieMap = repository.getAsbieListByFromAbieId(abieId, abieNode).stream()
                .collect(toMap(BieEditAsbie::getBasedAsccId, Function.identity()));
        bbieMap = repository.getBbieListByFromAbieId(abieId, abieNode).stream()
                .collect(toMap(BieEditBbie::getBasedBccId, Function.identity()));

        currentAccId = repository.getRoleOfAccIdByAsbiepId(asbiepId);

        List<BieEditNode> children = getChildren(asbieMap, bbieMap, abieId, currentAccId, abieNode, hideUnused);
        return children;
    }

    private List<BieEditNode> getDescendants(BieEditAsbiepNode asbiepNode, boolean hideUnused) {
        Map<Long, BieEditAsbie> asbieMap;
        Map<Long, BieEditBbie> bbieMap;

        long currentAccId = repository.getAcc(asbiepNode.getAccId()).getCurrentAccId();
        long abieId = asbiepNode.getAbieId();

        if (abieId == 0L && isForceBieUpdate()) {
            BieEditAcc acc = repository.getAcc(asbiepNode.getAccId());
            abieId = repository.createAbie(user, acc.getAccId(), asbiepNode.getTopLevelAbieId());
        }

        if (abieId > 0L) {
            asbieMap = repository.getAsbieListByFromAbieId(abieId, asbiepNode).stream()
                    .collect(toMap(BieEditAsbie::getBasedAsccId, Function.identity()));
            bbieMap = repository.getBbieListByFromAbieId(abieId, asbiepNode).stream()
                    .collect(toMap(BieEditBbie::getBasedBccId, Function.identity()));
        } else {
            asbieMap = Collections.emptyMap();
            bbieMap = Collections.emptyMap();
        }

        List<BieEditNode> children = getChildren(asbieMap, bbieMap, abieId, currentAccId, asbiepNode, hideUnused);
        return children;
    }

    private List<BieEditNode> getChildren(
            Map<Long, BieEditAsbie> asbieMap,
            Map<Long, BieEditBbie> bbieMap,
            long fromAbieId, long currentAccId,
            BieEditNode node, boolean hideUnused) {
        List<BieEditNode> children = new ArrayList();

        List<SeqKeySupportable> assocList = getAssociationsByCurrentAccId(currentAccId, node.getReleaseId());
        int seqKey = 1;
        for (SeqKeySupportable assoc : assocList) {
            if (assoc instanceof BieEditAscc) {
                BieEditAscc ascc = (BieEditAscc) assoc;
                BieEditAsbie asbie = asbieMap.get(ascc.getAsccId());
                BieEditAsbiepNode asbiepNode = createAsbiepNode(fromAbieId, seqKey++, asbie, ascc);
                if (asbiepNode == null) {
                    seqKey--;
                    continue;
                }

                OagisComponentType oagisComponentType = ccNodeRepository.getOagisComponentTypeByAccId(asbiepNode.getAccId());
                if (oagisComponentType.isGroup()) {
                    children.addAll(getDescendants(asbiepNode, hideUnused));
                } else {
                    if (hideUnused && (asbie == null || asbie.getAsbieId() == 0L || !asbie.isUsed())) {
                        seqKey--;
                        continue;
                    }

                    children.add(asbiepNode);
                }
            } else {
                BieEditBcc bcc = (BieEditBcc) assoc;
                BieEditBbie bbie = bbieMap.get(bcc.getBccId());
                if (hideUnused && (bbie == null || bbie.getBbieId() == 0L || !bbie.isUsed())) {
                    continue;
                }
                BieEditBbiepNode bbiepNode;
                if (bcc.isAttribute()) {
                    bbiepNode = createBbiepNode(fromAbieId, 0, bbie, bcc, hideUnused);
                } else {
                    bbiepNode = createBbiepNode(fromAbieId, seqKey++, bbie, bcc, hideUnused);
                }
                children.add(bbiepNode);
            }
        }

        return children;
    }

    private List<SeqKeySupportable> getAssociationsByCurrentAccId(long currentAccId, long releaseId) {
        Stack<BieEditAcc> accStack = getAccStack(currentAccId, releaseId);

        List<BieEditBcc> attributeBccList = new ArrayList();
        List<SeqKeySupportable> assocList = new ArrayList();

        while (!accStack.isEmpty()) {
            BieEditAcc acc = accStack.pop();

            long fromAccId = acc.getCurrentAccId();
            List<BieEditAscc> asccList = repository.getAsccListByFromAccId(fromAccId, releaseId, true);
            List<BieEditBcc> bccList = repository.getBccListByFromAccId(fromAccId, releaseId, true);

            attributeBccList.addAll(
                    bccList.stream().filter(e -> e.isAttribute()).collect(Collectors.toList()));

            List<SeqKeySupportable> tmpAssocList = new ArrayList();
            tmpAssocList.addAll(asccList);
            tmpAssocList.addAll(bccList.stream().filter(e -> !e.isAttribute()).collect(Collectors.toList()));
            tmpAssocList = tmpAssocList.stream()
                    .sorted(Comparator.comparingInt(SeqKeySupportable::getSeqKey))
                    .collect(Collectors.toList());

            for (SeqKeySupportable assoc : tmpAssocList) {
                if (assoc instanceof BieEditAscc) {
                    OagisComponentType roleOfAccType =
                            repository.getOagisComponentTypeOfAccByAsccpId(((BieEditAscc) assoc).getToAsccpId());
                    if (roleOfAccType.isGroup()) {
                        long roleOfAccId = repository.getRoleOfAccIdByAsccpId(((BieEditAscc) assoc).getToAsccpId());

                        assocList.addAll(
                                getAssociationsByCurrentAccId(roleOfAccId, releaseId)
                        );
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

    private Stack<BieEditAcc> getAccStack(long currentAccId, long releaseId) {
        Stack<BieEditAcc> accStack = new Stack();
        BieEditAcc acc = repository.getAccByCurrentAccId(currentAccId, releaseId);
        /*
         * Issue #708
         * If the UEG's state is not 'Published', its children couldn't get it by the logic above.
         */
        if (acc == null) {
            return accStack;
        }
        accStack.push(acc);

        while (acc.getBasedAccId() != null) {
            acc = repository.getAccByCurrentAccId(acc.getBasedAccId(), releaseId);
            accStack.push(acc);
        }

        return accStack;
    }

    private BieEditAsbiepNode createAsbiepNode(long fromAbieId, int seqKey,
                                               BieEditAsbie asbie, BieEditAscc ascc) {
        BieEditAsbiepNode asbiepNode = new BieEditAsbiepNode();

        long topLevelAbieId = topLevelAbie.getTopLevelAbieId();
        long releaseId = topLevelAbie.getReleaseId();

        asbiepNode.setTopLevelAbieId(topLevelAbieId);
        asbiepNode.setReleaseId(releaseId);
        asbiepNode.setType("asbiep");
        asbiepNode.setGuid(ascc.getGuid());
        asbiepNode.setAsccId(ascc.getAsccId());

        BieEditAsccp asccp = repository.getAsccpByCurrentAsccpId(ascc.getToAsccpId(), releaseId);
        asbiepNode.setAsccpId(asccp.getAsccpId());

        if (StringUtils.isEmpty(asbiepNode.getName())) {
            asbiepNode.setName(asccp.getPropertyTerm());
        }

        BieEditAcc acc;
        if (asbiepNode.getAccId() == 0L) {
            acc = repository.getAccByCurrentAccId(asccp.getRoleOfAccId(), releaseId);
            if (acc == null) {
                return null;
            }
            asbiepNode.setAccId(acc.getAccId());
        } else {
            acc = repository.getAcc(asbiepNode.getAccId());
        }

        if (asbie == null && isForceBieUpdate()) {
            long abieId = repository.createAbie(user, acc.getAccId(), topLevelAbieId);
            long asbiepId = repository.createAsbiep(user, asccp.getAsccpId(), abieId, topLevelAbieId);
            long asbieId = repository.createAsbie(user, fromAbieId, asbiepId, ascc.getAsccId(),
                    seqKey, topLevelAbieId);

            asbie = new BieEditAsbie();
            asbie.setAsbieId(asbieId);
            asbie.setBasedAsccId(ascc.getAsccId());
            asbie.setFromAbieId(fromAbieId);
            asbie.setToAsbiepId(asbiepId);
        }

        if (asbie != null) {
            asbiepNode.setAsbieId(asbie.getAsbieId());
            asbiepNode.setAsbiepId(asbie.getToAsbiepId());

            BieEditAbie abie = repository.getAbieByAsbiepId(asbie.getToAsbiepId());
            asbiepNode.setAbieId(abie.getAbieId());
            asbiepNode.setAccId(abie.getBasedAccId());

            asbiepNode.setName(repository.getAsccpPropertyTermByAsbiepId(asbie.getToAsbiepId()));
            asbiepNode.setUsed(asbie.isUsed());
        }

        asbiepNode.setHasChild(hasChild(asbiepNode));

        return asbiepNode;
    }

    private BieEditBbiepNode createBbiepNode(long fromAbieId, int seqKey,
                                             BieEditBbie bbie, BieEditBcc bcc,
                                             boolean hideUnused) {
        BieEditBbiepNode bbiepNode = new BieEditBbiepNode();

        long topLevelAbieId = topLevelAbie.getTopLevelAbieId();
        long releaseId = topLevelAbie.getReleaseId();

        bbiepNode.setTopLevelAbieId(topLevelAbieId);
        bbiepNode.setReleaseId(releaseId);
        bbiepNode.setType("bbiep");
        bbiepNode.setGuid(bcc.getGuid());
        bbiepNode.setAttribute(bcc.isAttribute());

        bbiepNode.setBccId(bcc.getBccId());
        BieEditBccp bccp = repository.getBccpByCurrentBccpId(bcc.getToBccpId(), topLevelAbie.getReleaseId());
        bbiepNode.setBccpId(bccp.getBccpId());
        bbiepNode.setBdtId(bccp.getBdtId());

        if (StringUtils.isEmpty(bbiepNode.getName())) {
            bbiepNode.setName(bccp.getPropertyTerm());
        }

        if (bbie == null && isForceBieUpdate()) {
            long bbiepId = repository.createBbiep(user, bccp.getBccpId(), topLevelAbieId);
            long bbieId = repository.createBbie(user, fromAbieId, bbiepId,
                    bcc.getBccId(), bccp.getBdtId(), seqKey, topLevelAbieId);

            bbie = new BieEditBbie();
            bbie.setBasedBccId(bcc.getBccId());
            bbie.setBbieId(bbieId);
            bbie.setFromAbieId(fromAbieId);
            bbie.setToBbiepId(bbiepId);
        }

        if (bbie != null) {
            bbiepNode.setBbieId(bbie.getBbieId());
            bbiepNode.setBbiepId(bbie.getToBbiepId());

            bbiepNode.setName(repository.getBccpPropertyTermByBbiepId(bbie.getToBbiepId()));
            bbiepNode.setUsed(bbie.isUsed());
        }

        bbiepNode.setHasChild(hasChild(bbiepNode, hideUnused));

        return bbiepNode;
    }

    public boolean hasChild(BieEditAsbiepNode asbiepNode) {
        BieEditAbieNode abieNode = new BieEditAbieNode();
        abieNode.setReleaseId(asbiepNode.getReleaseId());
        abieNode.setAccId(asbiepNode.getAccId());

        return hasChild(abieNode);
    }

    public boolean hasChild(BieEditBbiepNode bbiepNode, boolean hideUnused) {
        if (hideUnused) {
            return repository.getCountBbieScByBbieIdAndIsUsedAndOwnerTopLevelAbieId(
                    bbiepNode.getBbieId(), true, bbiepNode.getTopLevelAbieId()) > 0;

        } else {
            BieEditBccp bccp = repository.getBccp(bbiepNode.getBccpId());
            return repository.getCountDtScByOwnerDtId(bccp.getBdtId()) > 0;
        }
    }

    private List<BieEditNode> getDescendants(BieEditBbiepNode bbiepNode, boolean hideUnused) {
        long bbiepId = bbiepNode.getBbiepId();
        long topLevelAbieId = bbiepNode.getTopLevelAbieId();
        BieEditBccp bccp;
        if (bbiepId > 0L) {
            BieEditBbiep bbiep = repository.getBbiep(bbiepId, topLevelAbieId);
            bccp = repository.getBccp(bbiep.getBasedBccpId());
        } else {
            if (hideUnused) {
                return Collections.emptyList();
            }

            bccp = repository.getBccp(bbiepNode.getBccpId());
        }

        List<BieEditNode> children = new ArrayList();
        List<BieEditBdtSc> bdtScList = repository.getBdtScListByOwnerDtId(bccp.getBdtId());
        long bbieId = bbiepNode.getBbieId();
        for (BieEditBdtSc bdtSc : bdtScList) {
            BieEditBbieScNode bbieScNode = new BieEditBbieScNode();

            bbieScNode.setTopLevelAbieId(topLevelAbieId);
            bbieScNode.setReleaseId(bbiepNode.getReleaseId());
            bbieScNode.setType("bbie_sc");
            bbieScNode.setGuid(bdtSc.getGuid());
            bbieScNode.setName(bdtSc.getName());

            long dtScId = bdtSc.getDtScId();
            if (bbieId > 0L) {
                BieEditBbieSc bbieSc = repository.getBbieScIdByBbieIdAndDtScId(bbieId, dtScId, topLevelAbieId);
                if (bbieSc == null) {
                    if (isForceBieUpdate()) {
                        long bbieScId = repository.createBbieSc(user, bbieId, dtScId, topLevelAbieId);
                        bbieSc = new BieEditBbieSc();
                        bbieSc.setBbieScId(bbieScId);
                    }
                }

                if (hideUnused && (bbieSc == null || bbieSc.getBbieScId() == 0L || !bbieSc.isUsed())) {
                    continue;
                }
                bbieScNode.setBbieScId(bbieSc.getBbieScId());
                bbieScNode.setUsed(bbieSc.isUsed());
            }

            bbieScNode.setDtScId(dtScId);

            children.add(bbieScNode);
        }

        return children;
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
                Tables.ABIE.VERSION,
                Tables.ABIE.STATUS,
                Tables.ABIE.REMARK,
                Tables.ABIE.BIZ_TERM,
                Tables.ABIE.DEFINITION)
                .from(Tables.ABIE)
                .where(Tables.ABIE.ABIE_ID.eq(ULong.valueOf(abieNode.getAbieId())))
                .fetchOneInto(BieEditAbieNodeDetail.class);

        return detail.append(abieNode);
    }

    private BieEditAsbiepNodeDetail getDetail(BieEditAsbiepNode asbiepNode) {

        BieEditAsbiepNodeDetail detail;
        if (asbiepNode.getAsbieId() > 0L) {
            detail = dslContext.select(
                    Tables.ASBIE.CARDINALITY_MIN.as("bie_cardinality_min"),
                    Tables.ASBIE.CARDINALITY_MAX.as("bie_cardinality_max"),
                    Tables.ASBIE.IS_USED.as("used"),
                    Tables.ASBIE.IS_NILLABLE.as("nillable"),
                    Tables.ASBIE.DEFINITION.as("context_definition")
            ).from(Tables.ASBIE)
                    .where(Tables.ASBIE.ASBIE_ID.eq(ULong.valueOf(asbiepNode.getAsbieId())))
                    .fetchOneInto(BieEditAsbiepNodeDetail.class);

        } else {
            detail = dslContext.select(
                    Tables.ASCC.CARDINALITY_MIN.as("bie_cardinality_min"),
                    Tables.ASCC.CARDINALITY_MAX.as("bie_cardinality_max"))
                    .from(Tables.ASCC)
                    .where(Tables.ASCC.ASCC_ID.eq(ULong.valueOf(asbiepNode.getAsccId())))
                    .fetchOneInto(BieEditAsbiepNodeDetail.class);

        }
        if (asbiepNode.getAsbiepId() > 0L) {
            detail.setBizTerm(dslContext.select(
                    Tables.ASBIEP.BIZ_TERM).from(Tables.ASBIEP)
                    .where(Tables.ASBIEP.ASBIEP_ID.eq(ULong.valueOf(asbiepNode.getAsbiepId())))
                    .fetchOneInto(String.class));

            detail.setRemark(dslContext.select(
                    Tables.ASBIEP.REMARK).from(Tables.ASBIEP)
                    .where(Tables.ASBIEP.ASBIEP_ID.eq(ULong.valueOf(asbiepNode.getAsbiepId())))
                    .fetchOneInto(String.class));
        }

        if (asbiepNode.getAsccId() > 0L) {
            Record2<Integer, Integer> res = dslContext.select(
                    Tables.ASCC.CARDINALITY_MIN,
                    Tables.ASCC.CARDINALITY_MAX)
                    .from(Tables.ASCC)
                    .where(Tables.ASCC.ASCC_ID.eq(ULong.valueOf(asbiepNode.getAsccId())))
                    .fetchOne();

            detail.setCcCardinalityMin(res.get(Tables.ASCC.CARDINALITY_MIN));
            detail.setCcCardinalityMax(res.get(Tables.ASCC.CARDINALITY_MAX));
        }
        detail.setAssociationDefinition(dslContext.select(
                Tables.ASCC.DEFINITION).from(Tables.ASCC)
                .where(Tables.ASCC.ASCC_ID.eq(ULong.valueOf(asbiepNode.getAsccId())))
                .fetchOneInto(String.class));

        detail.setComponentDefinition(dslContext.select(
                Tables.ASCCP.DEFINITION).from(Tables.ASCCP)
                .where(Tables.ASCCP.ASCCP_ID.eq(ULong.valueOf(asbiepNode.getAsccpId())))
                .fetchOneInto(String.class));

        detail.setTypeDefinition(dslContext.select(
                Tables.ACC.DEFINITION).from(Tables.ACC)
                .where(Tables.ACC.ACC_ID.eq(ULong.valueOf(asbiepNode.getAccId())))
                .fetchOneInto(String.class));

        return detail.append(asbiepNode);
    }

    private BieEditBbiepNodeDetail getDetail(BieEditBbiepNode bbiepNode) {
        MapSqlParameterSource parameterSource = newSqlParameterSource()
                .addValue("bbie_id", bbiepNode.getBbieId())
                .addValue("bbiep_id", bbiepNode.getBbiepId())
                .addValue("bcc_id", bbiepNode.getBccId())
                .addValue("bccp_id", bbiepNode.getBccpId());

        BieEditBbiepNodeDetail detail;
        if (bbiepNode.getBbieId() > 0L) {
            detail = dslContext.select(
                    Tables.BBIE.CARDINALITY_MIN.as("bie_cardinality_min"),
                    Tables.BBIE.CARDINALITY_MAX.as("bie_cardinality_max"),
                    Tables.BBIE.IS_USED.as("used"),
                    Tables.BBIE.BDT_PRI_RESTRI_ID,
                    Tables.BBIE.CODE_LIST_ID,
                    Tables.BBIE.AGENCY_ID_LIST_ID,
                    Tables.BBIE.DEFAULT_VALUE,
                    Tables.BBIE.IS_NILLABLE.as("nillable"),
                    Tables.BBIE.FIXED_VALUE,
                    Tables.BBIE.DEFINITION.as("context_definition"))
                    .from(Tables.BBIE)
                    .where(Tables.BBIE.BBIE_ID.eq(ULong.valueOf(bbiepNode.getBbieId())))
                    .fetchOneInto(BieEditBbiepNodeDetail.class);

        } else {
            detail = dslContext.select(
                    Tables.BCC.CARDINALITY_MIN.as("bie_cardinality_min"),
                    Tables.BCC.CARDINALITY_MAX.as("bie_cardinality_max"))
                    .from(Tables.BCC)
                    .where(Tables.BCC.BCC_ID.eq(ULong.valueOf(bbiepNode.getBccId())))
                    .fetchOneInto(BieEditBbiepNodeDetail.class);
        }

        if (bbiepNode.getBbiepId() > 0L) {
            dslContext.select(BBIEP.BIZ_TERM, BBIEP.REMARK, BCCP.BDT_ID, DT.DEN)
                    .from(BBIEP)
                    .join(BCCP).on(BBIEP.BASED_BCCP_ID.eq(BCCP.BCCP_ID))
                    .join(DT).on(BCCP.BDT_ID.eq(DT.DT_ID))
                    .where(BBIEP.BBIEP_ID.eq(ULong.valueOf(bbiepNode.getBbiepId())))
                    .fetchOne((RecordMapper<Record4<String, String, ULong, String>, Object>) rs -> {
                        detail.setBizTerm(rs.getValue(BBIEP.BIZ_TERM));
                        detail.setRemark(rs.getValue(BBIEP.REMARK));
                        detail.setBdtId(rs.getValue(BCCP.BDT_ID).longValue());
                        detail.setBdtDen(rs.getValue(DT.DEN));
                        return detail;
                    });
        } else {
            detail.setBdtDen(dslContext.select(
                    Tables.DT.DEN.as("bdt_den")).from(Tables.BCCP)
                    .join(Tables.DT).on(Tables.BCCP.BDT_ID.eq(Tables.DT.DT_ID))
                    .where(Tables.BCCP.BCCP_ID.eq(ULong.valueOf(bbiepNode.getBbiepId())))
                    .fetchOneInto(String.class)
            );

            detail.setBdtId(dslContext.select(
                    Tables.BCCP.BDT_ID).from(Tables.BCCP)
                    .join(Tables.DT).on(Tables.BCCP.BDT_ID.eq(Tables.DT.DT_ID))
                    .where(Tables.BCCP.BCCP_ID.eq(ULong.valueOf(bbiepNode.getBbiepId())))
                    .fetchOneInto(Long.class)
            );
        }
        if (bbiepNode.getBbieId() == 0L) {
            long defaultBdtPriRestriId = dslContext.select(
                    Tables.BDT_PRI_RESTRI.BDT_PRI_RESTRI_ID)
                    .from(Tables.BDT_PRI_RESTRI)
                    .where(and(Tables.DT.DT_ID.eq(ULong.valueOf(detail.getBdtId())),
                            Tables.BDT_PRI_RESTRI.IS_DEFAULT.eq((byte) 1)))
                    .fetchOneInto(Long.class);

            detail.setBdtPriRestriId(defaultBdtPriRestriId);
        }

        if (bbiepNode.getBccId() > 0L) {
            Record2<Integer, Integer> res = dslContext.select(
                    Tables.BCC.CARDINALITY_MIN,
                    Tables.BCC.CARDINALITY_MAX)
                    .from(Tables.BCC)
                    .where(Tables.BCC.BCC_ID.eq(ULong.valueOf(bbiepNode.getBccId())))
                    .fetchOne();

            detail.setCcCardinalityMin(res.get(Tables.BCC.CARDINALITY_MIN));
            detail.setCcCardinalityMax(res.get(Tables.BCC.CARDINALITY_MAX));
        }

        BieEditBdtPriRestri bdtPriRestri = getBdtPriRestri(bbiepNode);
        detail.setXbtList(bdtPriRestri.getXbtList());
        detail.setCodeLists(bdtPriRestri.getCodeLists());
        detail.setAgencyIdLists(bdtPriRestri.getAgencyIdLists());
        detail.setAssociationDefinition(dslContext.select(
                Tables.BCC.DEFINITION).from(Tables.BCC)
                .where(Tables.BCC.BCC_ID.eq(ULong.valueOf(bbiepNode.getBccId())))
                .fetchOneInto(String.class)
        );

        detail.setComponentDefinition(dslContext.select(
                Tables.BCCP.DEFINITION).from(Tables.BCCP)
                .where(Tables.BCCP.BCCP_ID.eq(ULong.valueOf(bbiepNode.getBccpId())))
                .fetchOneInto(String.class)
        );

        return detail.append(bbiepNode);
    }

    private BieEditBdtPriRestri getBdtPriRestri(BieEditBbiepNode bbiepNode) {
        long bdtId = bbiepNode.getBdtId();

        MapSqlParameterSource parameterSource = newSqlParameterSource()
                .addValue("bdt_id", bdtId);

        List<BieEditXbt> bieEditXbtList = dslContext.select(
                BDT_PRI_RESTRI.BDT_PRI_RESTRI_ID.as("pri_restri_id"),
                BDT_PRI_RESTRI.IS_DEFAULT, XBT.XBT_ID, XBT.NAME.as("xbt_name"))
                .from(BDT_PRI_RESTRI)
                .join(CDT_AWD_PRI_XPS_TYPE_MAP).on(BDT_PRI_RESTRI.CDT_AWD_PRI_XPS_TYPE_MAP_ID.eq(CDT_AWD_PRI_XPS_TYPE_MAP.CDT_AWD_PRI_XPS_TYPE_MAP_ID))
                .join(XBT).on(CDT_AWD_PRI_XPS_TYPE_MAP.XBT_ID.eq(XBT.XBT_ID))
                .where(BDT_PRI_RESTRI.BDT_ID.eq(ULong.valueOf(bdtId)))
                .fetchInto(BieEditXbt.class);

        List<BieEditCodeList> bieEditCodeLists = dslContext.select(
                CODE_LIST.CODE_LIST_ID, CODE_LIST.BASED_CODE_LIST_ID,
                BDT_PRI_RESTRI.IS_DEFAULT, CODE_LIST.NAME.as("code_list_name"))
                .from(BDT_PRI_RESTRI)
                .join(CODE_LIST).on(BDT_PRI_RESTRI.CODE_LIST_ID.eq(CODE_LIST.CODE_LIST_ID))
                .where(BDT_PRI_RESTRI.BDT_ID.eq(ULong.valueOf(bdtId)))
                .fetchInto(BieEditCodeList.class);

        List<BieEditAgencyIdList> bieEditAgencyIdLists = dslContext.select(
                AGENCY_ID_LIST.AGENCY_ID_LIST_ID, BDT_PRI_RESTRI.IS_DEFAULT, AGENCY_ID_LIST.NAME.as("agency_id_list_name"))
                .from(BDT_PRI_RESTRI)
                .join(AGENCY_ID_LIST).on(BDT_PRI_RESTRI.AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST.AGENCY_ID_LIST_ID))
                .where(BDT_PRI_RESTRI.BDT_ID.eq(ULong.valueOf(bdtId)))
                .fetchInto(BieEditAgencyIdList.class);

        if (bieEditCodeLists.isEmpty() && bieEditAgencyIdLists.isEmpty()) {
            bieEditCodeLists = getAllCodeLists();
            bieEditAgencyIdLists = getAllAgencyIdLists();
        } else {
            if (!bieEditCodeLists.isEmpty()) {
                List<BieEditCodeList> basedCodeLists = getBieEditCodeListByBasedCodeListIds(
                        bieEditCodeLists.stream().filter(e -> e.getBasedCodeListId() != null)
                                .map(e -> e.getBasedCodeListId()).collect(Collectors.toList())
                );
                List<BieEditCodeList> basedCodeLists2 = getCodeListsByBasedCodeList(bieEditCodeLists.get(0).getCodeListId());
                basedCodeLists2.clear();
                for (int i = 0; i < bieEditCodeLists.size(); i++) {
                    basedCodeLists2.addAll(getCodeListsByBasedCodeList(bieEditCodeLists.get(i).getCodeListId()));
                }
                bieEditCodeLists.addAll(0, basedCodeLists);
                bieEditCodeLists.addAll(0, basedCodeLists2);
                basedCodeLists2.clear();
                for (int i = 0; i < bieEditCodeLists.size(); i++) {
                    basedCodeLists2.addAll(getCodeListsByBasedCodeList(bieEditCodeLists.get(i).getCodeListId()));
                }
                bieEditCodeLists.addAll(basedCodeLists2);
                Set<BieEditCodeList> set = new HashSet<BieEditCodeList>(bieEditCodeLists);
                bieEditCodeLists.clear();
                bieEditCodeLists.addAll(set); // remove dupplicate elements
            }
        }

        BieEditBdtPriRestri bdtPriRestri = new BieEditBdtPriRestri();

        bieEditXbtList.sort(Comparator.comparingLong(BieEditXbt::getPriRestriId));
        bdtPriRestri.setXbtList(bieEditXbtList);

        bieEditCodeLists.sort(Comparator.comparingLong(BieEditCodeList::getCodeListId));
        bdtPriRestri.setCodeLists(bieEditCodeLists);

        bieEditAgencyIdLists.sort(Comparator.comparingLong(BieEditAgencyIdList::getAgencyIdListId));
        bdtPriRestri.setAgencyIdLists(bieEditAgencyIdLists);

        return bdtPriRestri;
    }

    private BieEditBbieScNodeDetail getDetail(BieEditBbieScNode bbieScNode) {
        BieEditBbieScNodeDetail detail;
        if (bbieScNode.getBbieScId() > 0L) {
            detail = dslContext.select(
                    Tables.BBIE_SC.CARDINALITY_MIN.as("bie_cardinality_min"),
                    Tables.BBIE_SC.CARDINALITY_MAX.as("bie_cardinality_max"),
                    Tables.BBIE_SC.IS_USED.as("used"),
                    Tables.BBIE_SC.DT_SC_PRI_RESTRI_ID,
                    Tables.BBIE_SC.CODE_LIST_ID,
                    Tables.BBIE_SC.AGENCY_ID_LIST_ID,
                    Tables.BBIE_SC.DEFAULT_VALUE,
                    Tables.BBIE_SC.FIXED_VALUE,
                    Tables.BBIE_SC.BIZ_TERM,
                    Tables.BBIE_SC.REMARK,
                    Tables.BBIE_SC.DEFINITION.as("context_definition"))
                    .from(Tables.BBIE_SC)
                    .where(Tables.BBIE_SC.BBIE_SC_ID.eq(ULong.valueOf(bbieScNode.getBbieScId())))
                    .fetchOneInto(BieEditBbieScNodeDetail.class);
        } else {
            detail = dslContext.select(
                    Tables.DT_SC.CARDINALITY_MIN.as("bie_cardinality_min"),
                    Tables.DT_SC.CARDINALITY_MAX.as("bie_cardinality_max"))
                    .from(Tables.DT_SC)
                    .where(Tables.DT_SC.DT_SC_ID.eq(ULong.valueOf(bbieScNode.getDtScId())))
                    .fetchOneInto(BieEditBbieScNodeDetail.class);
        }

        if (bbieScNode.getBbieScId() == 0L) {
            long defaultDtScPriRestriId = dslContext.select(
                    Tables.BDT_SC_PRI_RESTRI.BDT_SC_PRI_RESTRI_ID)
                    .from(Tables.BDT_SC_PRI_RESTRI)
                    .where(and(
                            Tables.BDT_SC_PRI_RESTRI.BDT_SC_ID.eq(ULong.valueOf(bbieScNode.getDtScId())),
                            Tables.BDT_SC_PRI_RESTRI.IS_DEFAULT.eq((byte) 1)))
                    .fetchOneInto(Long.class);
            detail.setDtScPriRestriId(defaultDtScPriRestriId);
        }

        if (bbieScNode.getDtScId() > 0L) {
            Record2<Integer, Integer> res = dslContext.select(
                    Tables.DT_SC.CARDINALITY_MIN,
                    Tables.DT_SC.CARDINALITY_MAX)
                    .from(Tables.DT_SC)
                    .where(Tables.DT_SC.DT_SC_ID.eq(ULong.valueOf(bbieScNode.getDtScId())))
                    .fetchOne();

            detail.setCcCardinalityMin(res.get(Tables.DT_SC.CARDINALITY_MIN));
            detail.setCcCardinalityMax(res.get(Tables.DT_SC.CARDINALITY_MAX));
        }

        BieEditBdtScPriRestri bdtScPriRestri = getBdtScPriRestri(bbieScNode);
        detail.setXbtList(bdtScPriRestri.getXbtList());
        detail.setCodeLists(bdtScPriRestri.getCodeLists());
        detail.setAgencyIdLists(bdtScPriRestri.getAgencyIdLists());
        detail.setComponentDefinition(
                dslContext.select(Tables.DT_SC.DEFINITION)
                        .from(Tables.DT_SC)
                        .where(Tables.DT_SC.DT_SC_ID.eq(ULong.valueOf(bbieScNode.getDtScId())))
                        .fetchOneInto(String.class)
        );
        return detail.append(bbieScNode);
    }

    private BieEditBdtScPriRestri getBdtScPriRestri(BieEditBbieScNode bbieScNode) {
        long dtScId = bbieScNode.getDtScId();

        MapSqlParameterSource parameterSource = newSqlParameterSource()
                .addValue("bdt_sc_id", dtScId);

        List<BieEditXbt> bieEditXbtList = dslContext.select(
                Tables.BDT_SC_PRI_RESTRI.BDT_SC_PRI_RESTRI_ID.as("pri_restri_id"),
                Tables.BDT_SC_PRI_RESTRI.IS_DEFAULT,
                Tables.XBT.XBT_ID,
                Tables.XBT.NAME.as("xbt_name"))
                .from(Tables.BDT_SC_PRI_RESTRI)
                .join(Tables.CDT_SC_AWD_PRI_XPS_TYPE_MAP)
                .on(Tables.BDT_SC_PRI_RESTRI.CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID
                        .eq(Tables.CDT_SC_AWD_PRI_XPS_TYPE_MAP.CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID))
                .join(Tables.XBT).on(Tables.CDT_SC_AWD_PRI_XPS_TYPE_MAP.XBT_ID.eq(Tables.XBT.XBT_ID))
                .where(Tables.BDT_SC_PRI_RESTRI.BDT_SC_ID.eq(ULong.valueOf(dtScId)))
                .fetchInto(BieEditXbt.class);

        List<BieEditCodeList> bieEditCodeLists = dslContext.select(
                Tables.CODE_LIST.CODE_LIST_ID,
                Tables.CODE_LIST.BASED_CODE_LIST_ID,
                Tables.BDT_SC_PRI_RESTRI.IS_DEFAULT,
                Tables.CODE_LIST.NAME.as("code_list_name")).from(Tables.BDT_SC_PRI_RESTRI)
                .join(Tables.CODE_LIST).on(Tables.BDT_SC_PRI_RESTRI.CODE_LIST_ID.eq(Tables.CODE_LIST.CODE_LIST_ID))
                .where(Tables.BDT_SC_PRI_RESTRI.BDT_SC_ID.eq(ULong.valueOf(dtScId)))
                .fetchInto(BieEditCodeList.class);

        List<BieEditAgencyIdList> bieEditAgencyIdLists = dslContext.select(
                Tables.AGENCY_ID_LIST.AGENCY_ID_LIST_ID,
                Tables.BDT_SC_PRI_RESTRI.IS_DEFAULT,
                Tables.AGENCY_ID_LIST.NAME.as("agency_id_list_name"))
                .from(Tables.BDT_SC_PRI_RESTRI)
                .join(Tables.AGENCY_ID_LIST).on(
                        Tables.BDT_SC_PRI_RESTRI.AGENCY_ID_LIST_ID.eq(Tables.AGENCY_ID_LIST.AGENCY_ID_LIST_ID))
                .where(Tables.BDT_SC_PRI_RESTRI.BDT_SC_ID.eq(ULong.valueOf(dtScId)))
                .fetchInto(BieEditAgencyIdList.class);

        if (bieEditCodeLists.isEmpty() && bieEditAgencyIdLists.isEmpty()) {
            bieEditCodeLists = getAllCodeLists();
            bieEditAgencyIdLists = getAllAgencyIdLists();
        } else {
            if (!bieEditCodeLists.isEmpty()) {
                List<BieEditCodeList> basedCodeLists = getBieEditCodeListByBasedCodeListIds(
                        bieEditCodeLists.stream().filter(e -> e.getBasedCodeListId() != null)
                                .map(e -> e.getBasedCodeListId()).collect(Collectors.toList())
                );
                bieEditCodeLists.addAll(0, basedCodeLists);
            }
        }

        BieEditBdtScPriRestri bdtScPriRestri = new BieEditBdtScPriRestri();

        bdtScPriRestri.setXbtList(bieEditXbtList);
        bdtScPriRestri.setCodeLists(bieEditCodeLists);
        bdtScPriRestri.setAgencyIdLists(bieEditAgencyIdLists);

        return bdtScPriRestri;
    }

    private List<BieEditCodeList> getAllCodeLists() {
        return dslContext.select(
                Tables.CODE_LIST.CODE_LIST_ID,
                Tables.CODE_LIST.NAME.as("code_list_name"),
                Tables.CODE_LIST.STATE).from(Tables.CODE_LIST)
                .where(Tables.CODE_LIST.STATE.eq("Published"))
                .fetchInto(BieEditCodeList.class);
    }

    private List<BieEditAgencyIdList> getAllAgencyIdLists() {
        return dslContext.select(
                Tables.AGENCY_ID_LIST.AGENCY_ID_LIST_ID,
                Tables.AGENCY_ID_LIST.NAME.as("agency_id_list_name"))
                .from(Tables.AGENCY_ID_LIST)
                .fetchInto(BieEditAgencyIdList.class);
    }

    private List<BieEditCodeList> getBieEditCodeListByBasedCodeListIds(List<Long> basedCodeListIds) {
        if (basedCodeListIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<BieEditCodeList> bieEditCodeLists = dslContext.select(
                CODE_LIST.CODE_LIST_ID, CODE_LIST.BASED_CODE_LIST_ID, CODE_LIST.NAME.as("code_list_name"))
                .from(CODE_LIST)
                .where(and(
                        CODE_LIST.CODE_LIST_ID.in(
                                basedCodeListIds.stream()
                                        .map(e -> ULong.valueOf(e))
                                        .collect(Collectors.toList())),
                        CODE_LIST.STATE.eq("Published")))
                .fetchInto(BieEditCodeList.class);

        List<BieEditCodeList> basedCodeLists =
                getBieEditCodeListByBasedCodeListIds(
                        bieEditCodeLists.stream().filter(e -> e.getBasedCodeListId() != null)
                                .map(e -> e.getBasedCodeListId()).collect(Collectors.toList())
                );

        bieEditCodeLists.addAll(0, basedCodeLists);
        return bieEditCodeLists;
    }

    private List<BieEditCodeList> getCodeListsByBasedCodeList(Long basedCodeList) {
        if (basedCodeList == null) {
            return Collections.emptyList();
        }

        List<BieEditCodeList> bieEditCodeLists = new ArrayList();
        List<BieEditCodeList> bieEditCodeListsByBasedCodeListId = dslContext.select(
                CODE_LIST.CODE_LIST_ID, CODE_LIST.BASED_CODE_LIST_ID, CODE_LIST.NAME.as("code_list_name"))
                .from(CODE_LIST)
                .where(and(
                        CODE_LIST.BASED_CODE_LIST_ID.eq(ULong.valueOf(basedCodeList)),
                        CODE_LIST.STATE.eq("Published")))
                .fetchInto(BieEditCodeList.class);

        bieEditCodeLists.addAll(bieEditCodeListsByBasedCodeListId);

        for (BieEditCodeList bieEditCodeList : bieEditCodeListsByBasedCodeListId) {
            bieEditCodeLists.addAll(getCodeListsByBasedCodeList(bieEditCodeList.getCodeListId()));
        }

        return bieEditCodeLists;
    }

    @Override
    public void updateState(BieState state) {
        repository.updateState(topLevelAbie.getTopLevelAbieId(), state);
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
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        dslContext.update(Tables.ABIE)
                .set(Tables.ABIE.VERSION, emptyToNull(abieNodeDetail.getVersion()))
                .set(Tables.ABIE.STATUS, emptyToNull(abieNodeDetail.getStatus()))
                .set(Tables.ABIE.REMARK, emptyToNull(abieNodeDetail.getRemark()))
                .set(Tables.ABIE.BIZ_TERM, emptyToNull(abieNodeDetail.getBizTerm()))
                .set(Tables.ABIE.DEFINITION, emptyToNull(abieNodeDetail.getDefinition()))
                .set(Tables.ABIE.LAST_UPDATED_BY, ULong.valueOf(sessionService.userId(user)))
                .set(Tables.ABIE.LAST_UPDATE_TIMESTAMP, timestamp)
                .where(Tables.ABIE.ABIE_ID.eq(ULong.valueOf(abieNodeDetail.getAbieId())))
                .execute();
    }

    private void updateDetail(BieEditAsbiepNodeDetail asbiepNodeDetail) {
        if (asbiepNodeDetail.getBieCardinalityMin() != null) {
            dslContext.update(Tables.ASBIE)
                    .set(Tables.ASBIE.CARDINALITY_MIN, asbiepNodeDetail.getBieCardinalityMin())
                    .where(Tables.ASBIE.ASBIE_ID.eq(ULong.valueOf(asbiepNodeDetail.getAsbieId())))
                    .execute();
        }
        if (asbiepNodeDetail.getBieCardinalityMax() != null) {
            dslContext.update(Tables.ASBIE)
                    .set(Tables.ASBIE.CARDINALITY_MAX, asbiepNodeDetail.getBieCardinalityMax())
                    .where(Tables.ASBIE.ASBIE_ID.eq(ULong.valueOf(asbiepNodeDetail.getAsbieId())))
                    .execute();
        }
        if (asbiepNodeDetail.getNillable() != null) {
            dslContext.update(Tables.ASBIE)
                    .set(Tables.ASBIE.IS_NILLABLE, (byte) (asbiepNodeDetail.getNillable() ? 1 : 0))
                    .where(Tables.ASBIE.ASBIE_ID.eq(ULong.valueOf(asbiepNodeDetail.getAsbieId())))
                    .execute();
        }

        long userId = sessionService.userId(user);
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        dslContext.update(Tables.ASBIE)
                .set(Tables.ASBIE.IS_USED, (byte) (asbiepNodeDetail.isUsed() ? 1 : 0))
                .set(Tables.ASBIE.DEFINITION, emptyToNull(asbiepNodeDetail.getContextDefinition()))
                .set(Tables.ASBIE.LAST_UPDATED_BY, ULong.valueOf(userId))
                .set(Tables.ASBIE.LAST_UPDATE_TIMESTAMP, timestamp)
                .where(Tables.ASBIE.ASBIE_ID.eq(ULong.valueOf(asbiepNodeDetail.getAsbieId())))
                .execute();

        dslContext.update(Tables.ASBIEP)
                .set(Tables.ASBIEP.BIZ_TERM, emptyToNull(asbiepNodeDetail.getBizTerm()))
                .set(Tables.ASBIEP.REMARK, emptyToNull(asbiepNodeDetail.getRemark()))
                .set(Tables.ASBIEP.LAST_UPDATED_BY, ULong.valueOf(userId))
                .set(Tables.ASBIEP.LAST_UPDATE_TIMESTAMP, timestamp)
                .where(Tables.ASBIEP.ASBIEP_ID.eq(ULong.valueOf(asbiepNodeDetail.getAsbiepId())))
                .execute();
    }

    private void updateDetail(BieEditBbiepNodeDetail bbiepNodeDetail) {
        if (bbiepNodeDetail.getBieCardinalityMin() != null) {
            dslContext.update(Tables.BBIE)
                    .set(Tables.BBIE.CARDINALITY_MIN, bbiepNodeDetail.getBieCardinalityMin())
                    .where(Tables.BBIE.BBIE_ID.eq(ULong.valueOf(bbiepNodeDetail.getBbieId()))).execute();
        }
        if (bbiepNodeDetail.getBieCardinalityMax() != null) {
            dslContext.update(Tables.BBIE)
                    .set(Tables.BBIE.CARDINALITY_MAX, bbiepNodeDetail.getBieCardinalityMax())
                    .where(Tables.BBIE.BBIE_ID.eq(ULong.valueOf(bbiepNodeDetail.getBbieId()))).execute();
        }
        if (bbiepNodeDetail.getNillable() != null) {
            dslContext.update(Tables.BBIE)
                    .set(Tables.BBIE.IS_NILLABLE, (byte) (bbiepNodeDetail.getNillable() ? 1 : 0))
                    .where(Tables.BBIE.BBIE_ID.eq(ULong.valueOf(bbiepNodeDetail.getBbieId()))).execute();
        }

        Long bdtPriRestriId = bbiepNodeDetail.getBdtPriRestriId();
        Long codeListId = bbiepNodeDetail.getCodeListId();
        Long agencyIdListId = bbiepNodeDetail.getAgencyIdListId();

        if (bdtPriRestriId != null) {
            dslContext.update(Tables.BBIE)
                    .set(Tables.BBIE.BDT_PRI_RESTRI_ID, ULong.valueOf(bdtPriRestriId))
                    .where(Tables.BBIE.BBIE_ID.eq(ULong.valueOf(bbiepNodeDetail.getBbieId()))).execute();
        }

        if (codeListId != null) {
            dslContext.update(Tables.BBIE)
                    .set(Tables.BBIE.CODE_LIST_ID, ULong.valueOf(codeListId))
                    .where(Tables.BBIE.BBIE_ID.eq(ULong.valueOf(bbiepNodeDetail.getBbieId()))).execute();
        }

        if (agencyIdListId != null) {
            dslContext.update(Tables.BBIE)
                    .set(Tables.BBIE.AGENCY_ID_LIST_ID, ULong.valueOf(agencyIdListId))
                    .where(Tables.BBIE.BBIE_ID.eq(ULong.valueOf(bbiepNodeDetail.getBbieId()))).execute();
        }

        dslContext.update(Tables.BBIE)
                .set(Tables.BBIE.IS_USED, (byte) (bbiepNodeDetail.isUsed() ? 1 : 0))
                .set(Tables.BBIE.DEFINITION, emptyToNull(bbiepNodeDetail.getContextDefinition()))
                .set(Tables.BBIE.FIXED_VALUE, emptyToNull(bbiepNodeDetail.getFixedValue()))
                .set(Tables.BBIE.DEFAULT_VALUE, emptyToNull(bbiepNodeDetail.getDefaultValue()))
                .where(Tables.BBIE.BBIE_ID.eq(ULong.valueOf(bbiepNodeDetail.getBbieId()))).execute();


        long userId = sessionService.userId(user);
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        dslContext.update(Tables.BBIEP)
                .set(Tables.BBIEP.BIZ_TERM, emptyToNull(bbiepNodeDetail.getBizTerm()))
                .set(Tables.BBIEP.REMARK, emptyToNull(bbiepNodeDetail.getRemark()))
                .set(Tables.BBIEP.LAST_UPDATED_BY, ULong.valueOf(userId))
                .set(Tables.BBIEP.LAST_UPDATE_TIMESTAMP, timestamp)
                .where(Tables.BBIEP.BBIEP_ID.eq(ULong.valueOf(bbiepNodeDetail.getBbiepId())))
                .execute();
    }

    private void updateDetail(BieEditBbieScNodeDetail bbieScNodeDetail) {
        if (bbieScNodeDetail.getBieCardinalityMin() != null) {
            dslContext.update(Tables.BBIE_SC)
                    .set(Tables.BBIE_SC.CARDINALITY_MIN, bbieScNodeDetail.getBieCardinalityMin())
                    .where(Tables.BBIE_SC.BBIE_SC_ID.eq(ULong.valueOf(bbieScNodeDetail.getBbieScId()))).execute();
        }

        if (bbieScNodeDetail.getBieCardinalityMax() != null) {
            dslContext.update(Tables.BBIE_SC)
                    .set(Tables.BBIE_SC.CARDINALITY_MAX, bbieScNodeDetail.getBieCardinalityMax())
                    .where(Tables.BBIE_SC.BBIE_SC_ID.eq(ULong.valueOf(bbieScNodeDetail.getBbieScId()))).execute();
        }

        Long dtScPriRestriId = bbieScNodeDetail.getDtScPriRestriId();
        Long codeListId = bbieScNodeDetail.getCodeListId();
        Long agencyIdListId = bbieScNodeDetail.getAgencyIdListId();

        if (dtScPriRestriId != null) {
            dslContext.update(Tables.BBIE_SC)
                    .set(Tables.BBIE_SC.DT_SC_PRI_RESTRI_ID, ULong.valueOf(dtScPriRestriId))
                    .where(Tables.BBIE_SC.BBIE_SC_ID.eq(ULong.valueOf(bbieScNodeDetail.getBbieScId()))).execute();
        }

        if (codeListId != null) {
            dslContext.update(Tables.BBIE_SC)
                    .set(Tables.BBIE_SC.CODE_LIST_ID, ULong.valueOf(codeListId))
                    .where(Tables.BBIE_SC.BBIE_SC_ID.eq(ULong.valueOf(bbieScNodeDetail.getBbieScId()))).execute();
        }

        if (agencyIdListId != null) {
            dslContext.update(Tables.BBIE_SC)
                    .set(Tables.BBIE_SC.AGENCY_ID_LIST_ID, ULong.valueOf(agencyIdListId))
                    .where(Tables.BBIE_SC.BBIE_SC_ID.eq(ULong.valueOf(bbieScNodeDetail.getBbieScId()))).execute();
        }

        dslContext.update(Tables.BBIE_SC)
                .set(Tables.BBIE_SC.IS_USED, (byte) (bbieScNodeDetail.isUsed() ? 1 : 0))
                .set(Tables.BBIE_SC.DEFAULT_VALUE, emptyToNull(bbieScNodeDetail.getDefaultValue()))
                .set(Tables.BBIE_SC.FIXED_VALUE, emptyToNull(bbieScNodeDetail.getFixedValue()))
                .set(Tables.BBIE_SC.DEFINITION, emptyToNull(bbieScNodeDetail.getContextDefinition()))
                .set(Tables.BBIE_SC.BIZ_TERM, emptyToNull(bbieScNodeDetail.getBizTerm()))
                .set(Tables.BBIE_SC.REMARK, emptyToNull(bbieScNodeDetail.getRemark()))
                .where(Tables.BBIE_SC.BBIE_SC_ID.eq(ULong.valueOf(bbieScNodeDetail.getBbieScId())))
                .execute();
    }

    private String emptyToNull(String str) {
        if (str != null) {
            str = str.trim();
        }
        if (StringUtils.isEmpty(str)) {
            return null;
        }
        return str;
    }
}

package org.oagi.score.gateway.http.api.bie_management.service;

import lombok.Data;
import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.data.TopLevelAsbiep;
import org.oagi.score.gateway.http.api.bie_management.data.BieCreateRequest;
import org.oagi.score.gateway.http.api.bie_management.data.bie_edit.CreateInheritedBieRequest;
import org.oagi.score.gateway.http.api.bie_management.data.bie_edit.CreateBieFromExistingBieRequest;
import org.oagi.score.gateway.http.api.cc_management.data.CcType;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.oagi.score.gateway.http.event.BieCreateFromExistingBieRequestEvent;
import org.oagi.score.gateway.http.helper.ScoreGuid;
import org.oagi.score.gateway.http.helper.Utility;
import org.oagi.score.redis.event.EventListenerContainer;
import org.oagi.score.repo.api.bie.model.BieState;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AppUserRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.TopLevelAsbiepRecord;
import org.oagi.score.repository.TopLevelAsbiepRepository;
import org.oagi.score.service.common.data.AppUser;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static org.jooq.impl.DSL.and;
import static org.oagi.score.repo.api.bie.model.BieState.Initiating;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;

@Service
@Transactional
public class BieCreateFromExistingBieService implements InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private BieRepository repository;

    @Autowired
    private TopLevelAsbiepRepository topLevelAsbiepRepository;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private EventListenerContainer eventListenerContainer;

    @Autowired
    private BieService bieService;

    private final String INTERESTED_EVENT_NAME = "bieCreateFromExistingBieRequestEvent";

    @Override
    public void afterPropertiesSet() throws Exception {
        eventListenerContainer.addMessageListener(this, "onEventReceived",
                new ChannelTopic(INTERESTED_EVENT_NAME));
    }

    @Transactional
    public void createBieFromExistingBie(AuthenticatedPrincipal user, CreateBieFromExistingBieRequest request) {
        AppUser requester = sessionService.getAppUserByUsername(user);

        ULong topLevelAsbiepId = ULong.valueOf(request.getTopLevelAsbiepId());
        ULong asbiepId = dslContext.select(ASBIE.TO_ASBIEP_ID)
                .from(ASBIE)
                .where(
                        and(ASBIE.HASH_PATH.eq(request.getAsbieHashPath()),
                            ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(topLevelAsbiepId)))
                .fetchOneInto(ULong.class);

        TopLevelAsbiepRecord topLevelAsbiepRecord = dslContext.selectFrom(TOP_LEVEL_ASBIEP)
                .where(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(topLevelAsbiepId))
                .fetchOne();

        AppUserRecord bieOwnerRecord = dslContext.selectFrom(APP_USER)
                .where(APP_USER.APP_USER_ID.eq(topLevelAsbiepRecord.getOwnerUserId()))
                .fetchOne();

        if (requester.isDeveloper() && bieOwnerRecord.getIsDeveloper() == 0) {
            throw new IllegalArgumentException("Developer does not allow to create new BIE along with the end user's BIE.");
        }

        TopLevelAsbiep sourceTopLevelAsbiep = topLevelAsbiepRepository.findById(topLevelAsbiepId.toBigInteger());

        if (asbiepId != null) {
            BigInteger copiedTopLevelAsbiepId =
                    repository.createTopLevelAsbiep(requester.getAppUserId(), sourceTopLevelAsbiep.getReleaseId(), Initiating);
            BieCreateFromExistingBieRequestEvent event = new BieCreateFromExistingBieRequestEvent(
                    topLevelAsbiepId.toBigInteger(), copiedTopLevelAsbiepId, asbiepId.toBigInteger(),
                    Collections.emptyList(), requester.getAppUserId()
            );
            /*
             * Message Publishing
             */
            redisTemplate.convertAndSend(INTERESTED_EVENT_NAME, event);
        } else {
            // Create empty BIE.
            if (request.getAsccpManifestId() == null) {
                throw new IllegalArgumentException("Unable to find data to create BIE.");
            }
            BieCreateRequest bieRequest = new BieCreateRequest();
            bieRequest.setAsccpManifestId(request.getAsccpManifestId());
            List<BigInteger> bizCtxIds = dslContext.select(BIZ_CTX_ASSIGNMENT.BIZ_CTX_ID)
                    .from(BIZ_CTX_ASSIGNMENT)
                    .where(BIZ_CTX_ASSIGNMENT.TOP_LEVEL_ASBIEP_ID.eq(topLevelAsbiepRecord.getTopLevelAsbiepId()))
                    .fetchStreamInto(BigInteger.class).collect(Collectors.toList());
            bieRequest.setBizCtxIds(bizCtxIds);
            bieService.createBie(user, bieRequest);
        }
    }

    @Transactional
    public void createInheritedBie(AuthenticatedPrincipal user, CreateInheritedBieRequest request) {
        AppUser requester = sessionService.getAppUserByUsername(user);

        ULong basedTopLevelAsbiepId = ULong.valueOf(request.getBasedTopLevelAsbiepId());

        TopLevelAsbiepRecord topLevelAsbiepRecord = dslContext.selectFrom(TOP_LEVEL_ASBIEP)
                .where(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(basedTopLevelAsbiepId))
                .fetchOne();
        ULong asbiepId = topLevelAsbiepRecord.getAsbiepId();
        BigInteger copiedTopLevelAsbiepId =
                repository.createTopLevelAsbiep(requester.getAppUserId(), topLevelAsbiepRecord.getReleaseId().toBigInteger(),
                        topLevelAsbiepRecord.getTopLevelAsbiepId().toBigInteger(), Initiating,
                        topLevelAsbiepRecord.getVersion(), topLevelAsbiepRecord.getStatus());
        BieCreateFromExistingBieRequestEvent event = new BieCreateFromExistingBieRequestEvent(
                topLevelAsbiepRecord.getTopLevelAsbiepId().toBigInteger(), copiedTopLevelAsbiepId, asbiepId.toBigInteger(),
                Collections.emptyList(), requester.getAppUserId()
        );

        /*
         * Message Publishing
         */
        redisTemplate.convertAndSend(INTERESTED_EVENT_NAME, event);
    }


    /**
     * This method is invoked by 'bieCreateFromExistingBieRequestEvent' channel subscriber.
     *
     * @param event
     */
    @Transactional
    public void onEventReceived(BieCreateFromExistingBieRequestEvent event) {
        RLock lock = redissonClient.getLock("BieCreateFromExistingBieRequestEvent:" + event.hashCode());
        if (!lock.tryLock()) {
            return;
        }
        try {
            logger.debug("Received BieCreateFromExistingBieRequestEvent: " + event);

            BieCreateFromExistingBieContext context = new BieCreateFromExistingBieContext(event);
            context.execute();
        } finally {
            lock.unlock();
        }
    }

    private List<BieCreateFromExistingBieAbie> getAbieByOwnerTopLevelAsbiepId(BigInteger ownerTopLevelAsbiepId) {
        return dslContext.select(
                ABIE.ABIE_ID,
                ABIE.GUID,
                ABIE.PATH,
                ABIE.BASED_ACC_MANIFEST_ID
        ).from(ABIE)
                .where(ABIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(ownerTopLevelAsbiepId)))
                .fetchInto(BieCreateFromExistingBieAbie.class);
    }

    private List<BieCreateFromExistingBieAsbie> getAsbieByOwnerTopLevelAsbiepId(BigInteger ownerTopLevelAsbiepId) {
        return dslContext.select(
                ASBIE.ASBIE_ID,
                ASBIE.GUID,
                ASBIE.PATH,
                ASBIE.FROM_ABIE_ID,
                ASBIE.TO_ASBIEP_ID,
                ASBIE.BASED_ASCC_MANIFEST_ID,
                ASBIE.DEFINITION,
                ASBIE.CARDINALITY_MIN,
                ASBIE.CARDINALITY_MAX,
                ASBIE.IS_NILLABLE.as("nillable"),
                ASBIE.REMARK,
                ASBIE.SEQ_KEY,
                ASBIE.IS_USED.as("used")).from(ASBIE)
                .where(ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(ownerTopLevelAsbiepId)))
                .fetchInto(BieCreateFromExistingBieAsbie.class);
    }

    private List<BieCreateFromExistingBieBbie> getBbieByOwnerTopLevelAsbiepId(BigInteger ownerTopLevelAsbiepId) {
        return dslContext.select(
                BBIE.BBIE_ID,
                BBIE.GUID,
                BBIE.PATH,
                BBIE.BASED_BCC_MANIFEST_ID,
                BBIE.FROM_ABIE_ID,
                BBIE.TO_BBIEP_ID,
                BBIE.BDT_PRI_RESTRI_ID,
                BBIE.CODE_LIST_MANIFEST_ID,
                BBIE.AGENCY_ID_LIST_MANIFEST_ID,
                BBIE.CARDINALITY_MIN,
                BBIE.CARDINALITY_MAX,
                BBIE.DEFAULT_VALUE,
                BBIE.IS_NILLABLE.as("nillable"),
                BBIE.FIXED_VALUE,
                BBIE.IS_NULL.as("nill"),
                BBIE.DEFINITION,
                BBIE.EXAMPLE,
                BBIE.REMARK,
                BBIE.SEQ_KEY,
                BBIE.IS_USED.as("used")).from(BBIE)
                .where(BBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(ownerTopLevelAsbiepId)))
                .fetchInto(BieCreateFromExistingBieBbie.class);
    }

    private List<BieCreateFromExistingBieAsbiep> getAsbiepByOwnerTopLevelAsbiepId(BigInteger ownerTopLevelAsbiepId) {
        return dslContext.select(
                ASBIEP.ASBIEP_ID,
                ASBIEP.GUID,
                ASBIEP.PATH,
                ASBIEP.BASED_ASCCP_MANIFEST_ID,
                ASBIEP.ROLE_OF_ABIE_ID,
                ASBIEP.DEFINITION,
                ASBIEP.REMARK,
                ASBIEP.BIZ_TERM).from(ASBIEP)
                .where(ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(ownerTopLevelAsbiepId)))
                .fetchInto(BieCreateFromExistingBieAsbiep.class);
    }

    private List<BieCreateFromExistingBieBbiep> getBbiepByOwnerTopLevelAsbiepId(BigInteger ownerTopLevelAsbiepId) {
        return dslContext.select(
                BBIEP.BBIEP_ID,
                BBIEP.GUID,
                BBIEP.PATH,
                BBIEP.BASED_BCCP_MANIFEST_ID,
                BBIEP.DEFINITION,
                BBIEP.REMARK,
                BBIEP.BIZ_TERM).from(BBIEP)
                .where(BBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(ownerTopLevelAsbiepId)))
                .fetchInto(BieCreateFromExistingBieBbiep.class);
    }

    private List<BieCreateFromExistingBieBbieSc> getBbieScByOwnerTopLevelAsbiepId(BigInteger ownerTopLevelAsbiepId) {
        return dslContext.select(
                BBIE_SC.BBIE_SC_ID,
                BBIE_SC.GUID,
                BBIE_SC.PATH,
                BBIE_SC.BBIE_ID,
                BBIE_SC.BASED_DT_SC_MANIFEST_ID,
                BBIE_SC.DT_SC_PRI_RESTRI_ID,
                BBIE_SC.CODE_LIST_MANIFEST_ID,
                BBIE_SC.AGENCY_ID_LIST_MANIFEST_ID,
                BBIE_SC.CARDINALITY_MIN,
                BBIE_SC.CARDINALITY_MAX,
                BBIE_SC.DEFAULT_VALUE,
                BBIE_SC.FIXED_VALUE,
                BBIE_SC.DEFINITION,
                BBIE_SC.EXAMPLE,
                BBIE_SC.REMARK,
                BBIE_SC.BIZ_TERM,
                BBIE_SC.IS_USED.as("used")).from(BBIE_SC)
                .where(BBIE_SC.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(ownerTopLevelAsbiepId)))
                .fetchInto(BieCreateFromExistingBieBbieSc.class);
    }

    @Data
    public static class BieCreateFromExistingBieAbie {

        private BigInteger abieId;
        private String guid;
        private String path;
        private BigInteger basedAccManifestId;

    }

    @Data
    public static class BieCreateFromExistingBieAsbie {

        private BigInteger asbieId;
        private String guid;
        private String path;
        private BigInteger fromAbieId;
        private BigInteger toAsbiepId;
        private BigInteger basedAsccManifestId;
        private String definition;
        private int cardinalityMin;
        private int cardinalityMax;
        private boolean nillable;
        private String remark;
        private double seqKey;
        private boolean used;

    }

    @Data
    public static class BieCreateFromExistingBieBbie {

        private BigInteger bbieId;
        private String guid;
        private String path;
        private BigInteger basedBccManifestId;
        private BigInteger fromAbieId;
        private BigInteger toBbiepId;
        private Long bdtPriRestriId;
        private Long codeListManifestId;
        private Long agencyIdListManifestId;
        private int cardinalityMin;
        private int cardinalityMax;
        private String defaultValue;
        private boolean nillable;
        private String fixedValue;
        private boolean nill;
        private String definition;
        private String example;
        private String remark;
        private double seqKey;
        private boolean used;

    }

    @Data
    public static class BieCreateFromExistingBieAsbiep {

        private BigInteger asbiepId;
        private String guid;
        private String path;
        private BigInteger basedAsccpManifestId;
        private BigInteger roleOfAbieId;
        private String definition;
        private String remark;
        private String bizTerm;

    }

    @Data
    public static class BieCreateFromExistingBieBbiep {

        private BigInteger bbiepId;
        private String guid;
        private String path;
        private BigInteger basedBccpManifestId;
        private String definition;
        private String remark;
        private String bizTerm;

    }

    @Data
    public static class BieCreateFromExistingBieBbieSc {

        private BigInteger bbieScId;
        private String guid;
        private String path;
        private BigInteger bbieId;
        private BigInteger basedDtScManifestId;
        private Long dtScPriRestriId;
        private Long codeListManifestId;
        private Long agencyIdListManifestId;
        private int cardinalityMin;
        private int cardinalityMax;
        private String defaultValue;
        private String fixedValue;
        private String definition;
        private String example;
        private String remark;
        private String bizTerm;
        private boolean used;

    }

    private class BieCreateFromExistingBieContext {

        private final TopLevelAsbiep sourceTopLevelAsbiep;
        private final TopLevelAsbiep targetTopLevelAsbiep;
        private List<BigInteger> bizCtxIds;
        private final BigInteger userId;
        private final String sourceAsccpKey;

        private LocalDateTime timestamp;


        private List<BieCreateFromExistingBieAbie> abieList;

        private List<BieCreateFromExistingBieAsbiep> asbiepList;
        private final Map<BigInteger, List<BieCreateFromExistingBieAsbiep>> roleOfAbieToAsbiepMap;

        private List<BieCreateFromExistingBieBbiep> bbiepList;

        private List<BieCreateFromExistingBieAsbie> asbieList;
        private final Map<BigInteger, List<BieCreateFromExistingBieAsbie>> fromAbieToAsbieMap;
        private final Map<BigInteger, List<BieCreateFromExistingBieAsbie>> toAsbiepToAsbieMap;

        private List<BieCreateFromExistingBieBbie> bbieList;
        private final Map<BigInteger, List<BieCreateFromExistingBieBbie>> fromAbieToBbieMap;
        private final Map<BigInteger, List<BieCreateFromExistingBieBbie>> toBbiepToBbieMap;

        private List<BieCreateFromExistingBieBbieSc> bbieScList;
        private final Map<BigInteger, List<BieCreateFromExistingBieBbieSc>> bbieToBbieScMap;

        public BieCreateFromExistingBieContext(BieCreateFromExistingBieRequestEvent event) {
            sourceAsccpKey = CcType.ASCCP.name() + "-" + dslContext.select(ASBIEP.BASED_ASCCP_MANIFEST_ID)
                    .from(ASBIEP)
                    .where(ASBIEP.ASBIEP_ID.eq(ULong.valueOf(event.getAsbiepId())))
                    .fetchOneInto(ULong.class).toBigInteger().toString();
            BigInteger sourceTopLevelAsbiepId = event.getSourceTopLevelAsbiepId();
            sourceTopLevelAsbiep = topLevelAsbiepRepository.findById(sourceTopLevelAsbiepId);

            BigInteger targetTopLevelAsbiepId = event.getTargetTopLevelAsbiepId();
            targetTopLevelAsbiep = topLevelAsbiepRepository.findById(targetTopLevelAsbiepId);

            bizCtxIds = event.getBizCtxIds();
            if (bizCtxIds == null || bizCtxIds.isEmpty()) {
                bizCtxIds = dslContext.select(BIZ_CTX_ASSIGNMENT.BIZ_CTX_ID)
                        .from(BIZ_CTX_ASSIGNMENT)
                        .where(BIZ_CTX_ASSIGNMENT.TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(sourceTopLevelAsbiepId)))
                        .fetchInto(BigInteger.class);
            }
            userId = event.getUserId();

            abieList = getAbieByOwnerTopLevelAsbiepId(sourceTopLevelAsbiepId);

            asbiepList = getAsbiepByOwnerTopLevelAsbiepId(sourceTopLevelAsbiepId);
            roleOfAbieToAsbiepMap = asbiepList.stream().collect(groupingBy(BieCreateFromExistingBieAsbiep::getRoleOfAbieId));

            asbieList = getAsbieByOwnerTopLevelAsbiepId(sourceTopLevelAsbiepId);
            fromAbieToAsbieMap = asbieList.stream().collect(groupingBy(BieCreateFromExistingBieAsbie::getFromAbieId));
            toAsbiepToAsbieMap = asbieList.stream().collect(groupingBy(BieCreateFromExistingBieAsbie::getToAsbiepId));

            bbiepList = getBbiepByOwnerTopLevelAsbiepId(sourceTopLevelAsbiepId);

            bbieList = getBbieByOwnerTopLevelAsbiepId(sourceTopLevelAsbiepId);
            fromAbieToBbieMap = bbieList.stream().collect(groupingBy(BieCreateFromExistingBieBbie::getFromAbieId));
            toBbiepToBbieMap = bbieList.stream().collect(groupingBy(BieCreateFromExistingBieBbie::getToBbiepId));

            bbieScList = getBbieScByOwnerTopLevelAsbiepId(sourceTopLevelAsbiepId);
            bbieToBbieScMap = bbieScList.stream().collect(groupingBy(BieCreateFromExistingBieBbieSc::getBbieId));

            arrange(event);
        }

        public void execute() {
            timestamp = LocalDateTime.now();
            logger.debug("Begin creating BIE from existing BIE with the source " + sourceTopLevelAsbiep.getTopLevelAsbiepId() +
                    " and the target " + targetTopLevelAsbiep.getTopLevelAsbiepId());

            repository.createBizCtxAssignments(
                    targetTopLevelAsbiep.getTopLevelAsbiepId(),
                    bizCtxIds);

            for (BieCreateFromExistingBieAbie abie : abieList) {
                BigInteger previousAbieId = abie.getAbieId();
                BigInteger nextAbieId = insertAbie(abie);

                fireChangeEvent("abie", previousAbieId, nextAbieId);
            }

            for (BieCreateFromExistingBieAsbiep asbiep : asbiepList) {
                BigInteger previousAsbiepId = asbiep.getAsbiepId();
                BigInteger nextAsbiepId = insertAsbiep(asbiep);

                fireChangeEvent("asbiep", previousAsbiepId, nextAsbiepId);
            }

            repository.updateAsbiepIdOnTopLevelAsbiep(
                    targetTopLevelAsbiep.getAsbiepId(),
                    targetTopLevelAsbiep.getTopLevelAsbiepId());

            for (BieCreateFromExistingBieBbiep bbiep : bbiepList) {
                BigInteger previousBbiepId = bbiep.getBbiepId();
                BigInteger nextBbiepId = insertBbiep(bbiep);

                fireChangeEvent("bbiep", previousBbiepId, nextBbiepId);
            }

            for (BieCreateFromExistingBieAsbie asbie : asbieList) {
                BigInteger previousAsbieId = asbie.getAsbieId();
                BigInteger nextAsbieId = insertAsbie(asbie);

                fireChangeEvent("asbie", previousAsbieId, nextAsbieId);
            }

            for (BieCreateFromExistingBieBbie bbie : bbieList) {
                BigInteger previousBbieId = bbie.getBbieId();
                BigInteger nextBbieId = insertBbie(bbie);

                fireChangeEvent("bbie", previousBbieId, nextBbieId);
            }

            for (BieCreateFromExistingBieBbieSc bbieSc : bbieScList) {
                BigInteger previousBbieScId = bbieSc.getBbieId();
                BigInteger nextBbieScId = insertBbieSc(bbieSc);

                fireChangeEvent("bbie_sc", previousBbieScId, nextBbieScId);
            }

            repository.updateState(targetTopLevelAsbiep.getTopLevelAsbiepId(), BieState.WIP);

            logger.debug("End create BIE from " + sourceTopLevelAsbiep.getTopLevelAsbiepId() +
                    " to " + targetTopLevelAsbiep.getTopLevelAsbiepId());
        }

        private void arrange(BieCreateFromExistingBieRequestEvent event) {
            List<BieCreateFromExistingBieAbie> abieList = new ArrayList();
            List<BieCreateFromExistingBieAsbiep> asbiepList = new ArrayList();
            List<BieCreateFromExistingBieBbiep> bbiepList = new ArrayList();
            List<BieCreateFromExistingBieAsbie> asbieList = new ArrayList();
            List<BieCreateFromExistingBieBbie> bbieList = new ArrayList();
            List<BieCreateFromExistingBieBbieSc> bbieScList = new ArrayList();

            Map<BigInteger, BieCreateFromExistingBieAsbiep> asbiepMap = this.asbiepList.stream()
                    .collect(Collectors.toMap(BieCreateFromExistingBieAsbiep::getAsbiepId, Function.identity()));

            Map<BigInteger, BieCreateFromExistingBieAbie> abieMap = this.abieList.stream()
                    .collect(Collectors.toMap(BieCreateFromExistingBieAbie::getAbieId, Function.identity()));

            Map<BigInteger, BieCreateFromExistingBieBbiep> bbiepMap = this.bbiepList.stream()
                    .collect(Collectors.toMap(BieCreateFromExistingBieBbiep::getBbiepId, Function.identity()));

            Queue<BieCreateFromExistingBieAsbiep> asbiepQueue = new LinkedList<>();
            BieCreateFromExistingBieAsbiep topLevelAsbiep = asbiepMap.get(event.getAsbiepId());
            asbiepQueue.add(topLevelAsbiep);

            while (!asbiepQueue.isEmpty()) {
                BieCreateFromExistingBieAsbiep currentAsbiep = asbiepQueue.poll();
                asbiepList.add(currentAsbiep);
                BieCreateFromExistingBieAbie roleOfAbie = abieMap.get(currentAsbiep.getRoleOfAbieId());
                abieList.add(roleOfAbie);

                List<BieCreateFromExistingBieAsbie> asbies = this.asbieList.stream()
                        .filter(e -> e.getFromAbieId().equals(roleOfAbie.getAbieId())).collect(Collectors.toList());

                asbieList.addAll(asbies);
                asbies.forEach(e -> {
                    if (asbiepMap.get(e.getToAsbiepId()) != null) {
                        asbiepQueue.add(asbiepMap.get(e.getToAsbiepId()));
                    }
                });

                List<BieCreateFromExistingBieBbie> bbies = this.bbieList.stream()
                        .filter(e -> e.getFromAbieId().equals(roleOfAbie.getAbieId())).collect(Collectors.toList());
                bbieList.addAll(bbies);
                bbies.forEach(e -> {
                    bbiepList.add(bbiepMap.get(e.getToBbiepId()));
                    bbieScList.addAll(this.bbieScList.stream()
                            .filter(sc -> sc.getBbieId().equals(e.getBbieId()))
                            .collect(Collectors.toList()));
                });
            }

            this.abieList = abieList;
            this.asbieList = asbieList;
            this.bbieList = bbieList;
            this.asbiepList = asbiepList;
            this.bbiepList = bbiepList;
            this.bbieScList = bbieScList;

            targetTopLevelAsbiep.setAsbiepId(topLevelAsbiep.getAsbiepId());
        }


        private void fireChangeEvent(String type, BigInteger previousVal, BigInteger nextVal) {
            switch (type) {
                case "abie":
                    roleOfAbieToAsbiepMap.getOrDefault(previousVal, Collections.emptyList()).stream().forEach(asbiep -> {
                        asbiep.setRoleOfAbieId(nextVal);
                    });
                    fromAbieToAsbieMap.getOrDefault(previousVal, Collections.emptyList()).stream().forEach(asbie -> {
                        asbie.setFromAbieId(nextVal);
                    });
                    fromAbieToBbieMap.getOrDefault(previousVal, Collections.emptyList()).stream().forEach(bbie -> {
                        bbie.setFromAbieId(nextVal);
                    });

                    break;

                case "asbiep":
                    if (previousVal.equals(targetTopLevelAsbiep.getAsbiepId())) {
                        targetTopLevelAsbiep.setAsbiepId(nextVal);
                    }

                    toAsbiepToAsbieMap.getOrDefault(previousVal, Collections.emptyList()).stream().forEach(asbie -> {
                        asbie.setToAsbiepId(nextVal);
                    });

                    break;

                case "bbiep":
                    toBbiepToBbieMap.getOrDefault(previousVal, Collections.emptyList()).stream().forEach(bbie -> {
                        bbie.setToBbiepId(nextVal);
                    });

                    break;

                case "bbie":
                    bbieToBbieScMap.getOrDefault(previousVal, Collections.emptyList()).stream().forEach(bbieSc -> {
                        bbieSc.setBbieId(nextVal);
                    });

                    break;
            }
        }

        private BigInteger insertAbie(BieCreateFromExistingBieAbie abie) {

            return dslContext.insertInto(ABIE)
                    .set(ABIE.GUID, ScoreGuid.randomGuid())
                    .set(ABIE.PATH, getPath(abie.getPath()))
                    .set(ABIE.HASH_PATH, getHashPath(abie.getPath()))
                    .set(ABIE.BASED_ACC_MANIFEST_ID, ULong.valueOf(abie.getBasedAccManifestId()))
                    .set(ABIE.CREATED_BY, ULong.valueOf(userId))
                    .set(ABIE.LAST_UPDATED_BY, ULong.valueOf(userId))
                    .set(ABIE.CREATION_TIMESTAMP, timestamp)
                    .set(ABIE.LAST_UPDATE_TIMESTAMP, timestamp)
                    // .set(ABIE.STATE, BieState.Initiating.getValue())
                    .set(ABIE.OWNER_TOP_LEVEL_ASBIEP_ID, ULong.valueOf(targetTopLevelAsbiep.getTopLevelAsbiepId()))
                    .returning(ABIE.ABIE_ID).fetchOne().getValue(ABIE.ABIE_ID).toBigInteger();
        }

        private BigInteger insertAsbiep(BieCreateFromExistingBieAsbiep asbiep) {

            return dslContext.insertInto(ASBIEP)
                    .set(ASBIEP.GUID, ScoreGuid.randomGuid())
                    .set(ASBIEP.PATH, getPath(asbiep.getPath()))
                    .set(ASBIEP.HASH_PATH, getHashPath(asbiep.getPath()))
                    .set(ASBIEP.BASED_ASCCP_MANIFEST_ID, ULong.valueOf(asbiep.getBasedAsccpManifestId()))
                    .set(ASBIEP.ROLE_OF_ABIE_ID, ULong.valueOf(asbiep.getRoleOfAbieId()))
                    .set(ASBIEP.DEFINITION, asbiep.getDefinition())
                    .set(ASBIEP.REMARK, asbiep.getRemark())
                    .set(ASBIEP.BIZ_TERM, asbiep.getBizTerm())
                    .set(ASBIEP.CREATED_BY, ULong.valueOf(userId))
                    .set(ASBIEP.LAST_UPDATED_BY, ULong.valueOf(userId))
                    .set(ASBIEP.CREATION_TIMESTAMP, timestamp)
                    .set(ASBIEP.LAST_UPDATE_TIMESTAMP, timestamp)
                    .set(ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID, ULong.valueOf(targetTopLevelAsbiep.getTopLevelAsbiepId()))
                    .returning(ASBIEP.ASBIEP_ID).fetchOne().getValue(ASBIEP.ASBIEP_ID).toBigInteger();
        }

        private BigInteger insertBbiep(BieCreateFromExistingBieBbiep bbiep) {

            return dslContext.insertInto(BBIEP)
                    .set(BBIEP.GUID, ScoreGuid.randomGuid())
                    .set(BBIEP.PATH, getPath(bbiep.getPath()))
                    .set(BBIEP.HASH_PATH, getHashPath(bbiep.getPath()))
                    .set(BBIEP.BASED_BCCP_MANIFEST_ID, ULong.valueOf(bbiep.getBasedBccpManifestId()))
                    .set(BBIEP.DEFINITION, bbiep.getDefinition())
                    .set(BBIEP.REMARK, bbiep.getRemark())
                    .set(BBIEP.BIZ_TERM, bbiep.getBizTerm())
                    .set(BBIEP.CREATED_BY, ULong.valueOf(userId))
                    .set(BBIEP.LAST_UPDATED_BY, ULong.valueOf(userId))
                    .set(BBIEP.CREATION_TIMESTAMP, timestamp)
                    .set(BBIEP.LAST_UPDATE_TIMESTAMP, timestamp)
                    .set(BBIEP.OWNER_TOP_LEVEL_ASBIEP_ID, ULong.valueOf(targetTopLevelAsbiep.getTopLevelAsbiepId()))
                    .returning(BBIEP.BBIEP_ID).fetchOne().getValue(BBIEP.BBIEP_ID).toBigInteger();
        }

        private BigInteger insertAsbie(BieCreateFromExistingBieAsbie asbie) {

            return dslContext.insertInto(ASBIE)
                    .set(ASBIE.GUID, ScoreGuid.randomGuid())
                    .set(ASBIE.PATH, getPath(asbie.getPath()))
                    .set(ASBIE.HASH_PATH, getHashPath(asbie.getPath()))
                    .set(ASBIE.FROM_ABIE_ID, ULong.valueOf(asbie.getFromAbieId()))
                    .set(ASBIE.TO_ASBIEP_ID, ULong.valueOf(asbie.getToAsbiepId()))
                    .set(ASBIE.BASED_ASCC_MANIFEST_ID, ULong.valueOf(asbie.getBasedAsccManifestId()))
                    .set(ASBIE.DEFINITION, asbie.getDefinition())
                    .set(ASBIE.REMARK, asbie.getRemark())
                    .set(ASBIE.CARDINALITY_MIN, asbie.getCardinalityMin())
                    .set(ASBIE.CARDINALITY_MAX, asbie.getCardinalityMax())
                    .set(ASBIE.IS_NILLABLE, (byte) ((asbie.isNillable()) ? 1 : 0))
                    .set(ASBIE.IS_USED, (byte) ((asbie.isUsed()) ? 1 : 0))
                    .set(ASBIE.SEQ_KEY, BigDecimal.valueOf(asbie.getSeqKey()))
                    .set(ASBIE.CREATED_BY, ULong.valueOf(userId))
                    .set(ASBIE.LAST_UPDATED_BY, ULong.valueOf(userId))
                    .set(ASBIE.CREATION_TIMESTAMP, timestamp)
                    .set(ASBIE.LAST_UPDATE_TIMESTAMP, timestamp)
                    .set(ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID, ULong.valueOf(targetTopLevelAsbiep.getTopLevelAsbiepId()))
                    .returning(ASBIE.ASBIE_ID).fetchOne().getValue(ASBIE.ASBIE_ID).toBigInteger();
        }

        private BigInteger insertBbie(BieCreateFromExistingBieBbie bbie) {

            return dslContext.insertInto(BBIE)
                    .set(BBIE.GUID, ScoreGuid.randomGuid())
                    .set(BBIE.PATH, getPath(bbie.getPath()))
                    .set(BBIE.HASH_PATH, getHashPath(bbie.getPath()))
                    .set(BBIE.FROM_ABIE_ID, ULong.valueOf(bbie.getFromAbieId()))
                    .set(BBIE.TO_BBIEP_ID, ULong.valueOf(bbie.getToBbiepId()))
                    .set(BBIE.BASED_BCC_MANIFEST_ID, ULong.valueOf(bbie.getBasedBccManifestId()))
                    .set(BBIE.BDT_PRI_RESTRI_ID, (bbie.getBdtPriRestriId() != null) ? ULong.valueOf(bbie.getBdtPriRestriId()) : null)
                    .set(BBIE.CODE_LIST_MANIFEST_ID, (bbie.getCodeListManifestId() != null) ? ULong.valueOf(bbie.getCodeListManifestId()) : null)
                    .set(BBIE.AGENCY_ID_LIST_MANIFEST_ID, (bbie.getAgencyIdListManifestId() != null) ? ULong.valueOf(bbie.getAgencyIdListManifestId()) : null)
                    .set(BBIE.DEFAULT_VALUE, bbie.getDefaultValue())
                    .set(BBIE.FIXED_VALUE, bbie.getFixedValue())
                    .set(BBIE.DEFINITION, bbie.getDefinition())
                    .set(BBIE.EXAMPLE, bbie.getExample())
                    .set(BBIE.REMARK, bbie.getRemark())
                    .set(BBIE.CARDINALITY_MIN, bbie.getCardinalityMin())
                    .set(BBIE.CARDINALITY_MAX, bbie.getCardinalityMax())
                    .set(BBIE.IS_NILLABLE, (byte) ((bbie.isNillable()) ? 1 : 0))
                    .set(BBIE.IS_NULL, (byte) ((bbie.isNill()) ? 1 : 0))
                    .set(BBIE.SEQ_KEY, BigDecimal.valueOf(bbie.getSeqKey()))
                    .set(BBIE.IS_USED, (byte) ((bbie.isUsed()) ? 1 : 0))
                    .set(BBIE.CREATED_BY, ULong.valueOf(userId))
                    .set(BBIE.LAST_UPDATED_BY, ULong.valueOf(userId))
                    .set(BBIE.CREATION_TIMESTAMP, timestamp)
                    .set(BBIE.LAST_UPDATE_TIMESTAMP, timestamp)
                    .set(BBIE.OWNER_TOP_LEVEL_ASBIEP_ID, ULong.valueOf(targetTopLevelAsbiep.getTopLevelAsbiepId()))
                    .returning(BBIE.BBIE_ID).fetchOne().getValue(BBIE.BBIE_ID).toBigInteger();
        }

        private BigInteger insertBbieSc(BieCreateFromExistingBieBbieSc bbieSc) {

            return dslContext.insertInto(BBIE_SC)
                    .set(BBIE_SC.GUID, ScoreGuid.randomGuid())
                    .set(BBIE_SC.PATH, getPath(bbieSc.getPath()))
                    .set(BBIE_SC.HASH_PATH, getHashPath(bbieSc.getPath()))
                    .set(BBIE_SC.BBIE_ID, ULong.valueOf(bbieSc.getBbieId()))
                    .set(BBIE_SC.BASED_DT_SC_MANIFEST_ID, ULong.valueOf(bbieSc.getBasedDtScManifestId()))
                    .set(BBIE_SC.DT_SC_PRI_RESTRI_ID, (bbieSc.getDtScPriRestriId() != null) ? ULong.valueOf(bbieSc.getDtScPriRestriId()) : null)
                    .set(BBIE_SC.CODE_LIST_MANIFEST_ID, (bbieSc.getCodeListManifestId() != null) ? ULong.valueOf(bbieSc.getCodeListManifestId()) : null)
                    .set(BBIE_SC.AGENCY_ID_LIST_MANIFEST_ID, (bbieSc.getAgencyIdListManifestId() != null) ? ULong.valueOf(bbieSc.getAgencyIdListManifestId()) : null)
                    .set(BBIE_SC.DEFAULT_VALUE, bbieSc.getDefaultValue())
                    .set(BBIE_SC.FIXED_VALUE, bbieSc.getFixedValue())
                    .set(BBIE_SC.DEFINITION, bbieSc.getDefinition())
                    .set(BBIE_SC.EXAMPLE, bbieSc.getExample())
                    .set(BBIE_SC.REMARK, bbieSc.getRemark())
                    .set(BBIE_SC.BIZ_TERM, bbieSc.getBizTerm())
                    .set(BBIE_SC.CARDINALITY_MIN, bbieSc.getCardinalityMin())
                    .set(BBIE_SC.CARDINALITY_MAX, bbieSc.getCardinalityMax())
                    .set(BBIE_SC.IS_USED, (byte) ((bbieSc.isUsed()) ? 1 : 0))
                    .set(BBIE_SC.OWNER_TOP_LEVEL_ASBIEP_ID, ULong.valueOf(targetTopLevelAsbiep.getTopLevelAsbiepId()))
                    .set(BBIE.CREATED_BY, ULong.valueOf(userId))
                    .set(BBIE.LAST_UPDATED_BY, ULong.valueOf(userId))
                    .set(BBIE.CREATION_TIMESTAMP, timestamp)
                    .set(BBIE.LAST_UPDATE_TIMESTAMP, timestamp)
                    .returning(BBIE_SC.BBIE_SC_ID).fetchOne().getValue(BBIE_SC.BBIE_SC_ID).toBigInteger();
        }

        private String getPath(String path) {
            String seperator = sourceAsccpKey + ">";
            String[] tokens = path.split(sourceAsccpKey);
            if (tokens.length < 2) {
                return sourceAsccpKey;
            } else {
                if(path.endsWith(sourceAsccpKey)) {
                    return sourceAsccpKey;
                }
                return seperator + path.split(seperator)[1];
            }
        }

        private String getHashPath(String path) {
            return Utility.sha256(getPath(path));
        }
    }

}
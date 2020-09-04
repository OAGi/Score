package org.oagi.score.gateway.http.api.bie_management.service;

import lombok.Data;
import org.jooq.DSLContext;
import org.jooq.Record2;
import org.jooq.types.ULong;
import org.oagi.score.data.AppUser;
import org.oagi.score.data.BieState;
import org.oagi.score.data.TopLevelAsbiep;
import org.oagi.score.entity.jooq.tables.records.AppUserRecord;
import org.oagi.score.entity.jooq.tables.records.TopLevelAsbiepRecord;
import org.oagi.score.gateway.http.api.bie_management.data.bie_edit.MakeReusableBieRequest;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.oagi.score.gateway.http.event.BieMakeReusableBieRequestEvent;
import org.oagi.score.gateway.http.helper.SrtGuid;
import org.oagi.score.redis.event.EventListenerContainer;
import org.oagi.score.repository.TopLevelAsbiepRepository;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static org.oagi.score.data.BieState.Initiating;
import static org.oagi.score.entity.jooq.Tables.*;

@Service
@Transactional
public class BieMakeReusableBieService implements InitializingBean {

    private Logger logger = LoggerFactory.getLogger(getClass());

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
    private RedisMessageListenerContainer messageListenerContainer;

    @Autowired
    private EventListenerContainer eventListenerContainer;

    private String INTERESTED_EVENT_NAME = "bieMakeReusableBieRequestEvent";

    @Override
    public void afterPropertiesSet() throws Exception {
        eventListenerContainer.addMessageListener(this, "onEventReceived",
                new ChannelTopic(INTERESTED_EVENT_NAME));
    }

    @Transactional
    public void makeReusableBie(AuthenticatedPrincipal user, MakeReusableBieRequest request) {
        AppUser requester = sessionService.getAppUser(user);
        ULong asbieId = ULong.valueOf(request.getAsbieId());

        Record2<ULong, ULong> res =
                dslContext.select(ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID, ASBIE.TO_ASBIEP_ID)
                        .from(ASBIE)
                        .where(ASBIE.ASBIE_ID.eq(asbieId))
                        .fetchOne();

        ULong topLevelAsbiepId = res.get(ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID);
        ULong asbiepId = res.get(ASBIE.TO_ASBIEP_ID);

        TopLevelAsbiepRecord topLevelAsbiepRecord = dslContext.selectFrom(TOP_LEVEL_ASBIEP)
                .where(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(topLevelAsbiepId))
                .fetchOne();

        AppUserRecord bieOwnerRecord = dslContext.selectFrom(APP_USER)
                .where(APP_USER.APP_USER_ID.eq(topLevelAsbiepRecord.getOwnerUserId()))
                .fetchOne();

        if (requester.isDeveloper() && bieOwnerRecord.getIsDeveloper() == 0) {
            throw new IllegalArgumentException("Developer does not allow to create new BIE along with the end user's BIE.");
        }

        TopLevelAsbiep sourceTopLevelAsbiep = topLevelAsbiepRepository.findById(topLevelAsbiepId.longValue());
        long copiedTopLevelAsbiepId =
                repository.createTopLevelAsbiep(requester.getAppUserId(), sourceTopLevelAsbiep.getReleaseId(), Initiating);

        BieMakeReusableBieRequestEvent event = new BieMakeReusableBieRequestEvent(
                topLevelAsbiepId.longValue(), copiedTopLevelAsbiepId, asbiepId.longValue(),
                Collections.emptyList(), requester.getAppUserId()
        );

        /*
         * Message Publishing
         */
        redisTemplate.convertAndSend(INTERESTED_EVENT_NAME, event);
    }


    /**
     * This method is invoked by 'bieMakeReusableBieRequestEvent' channel subscriber.
     *
     * @param event
     */
    @Transactional
    public void onEventReceived(BieMakeReusableBieRequestEvent event) {
        RLock lock = redissonClient.getLock("BieMakeReusableBieRequestEvent:" + event.hashCode());
        if (!lock.tryLock()) {
            return;
        }
        try {
            logger.debug("Received BieMakeReusableBieRequestEvent: " + event);

            BieMakeReusableBieContext context = new BieMakeReusableBieContext(event);
            context.execute();
        } finally {
            lock.unlock();
        }
    }

    private List<BieMakeReusableBieAbie> getAbieByOwnerTopLevelAsbiepId(long ownerTopLevelAsbiepId) {
        return dslContext.select(
                ABIE.ABIE_ID,
                ABIE.GUID,
                ABIE.BASED_ACC_ID
        ).from(ABIE)
                .where(ABIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(ownerTopLevelAsbiepId)))
                .fetchInto(BieMakeReusableBieAbie.class);
    }

    private List<BieMakeReusableBieAsbie> getAsbieByOwnerTopLevelAsbiepId(long ownerTopLevelAsbiepId) {
        return dslContext.select(
                ASBIE.ASBIE_ID,
                ASBIE.GUID,
                ASBIE.FROM_ABIE_ID,
                ASBIE.TO_ASBIEP_ID,
                ASBIE.BASED_ASCC_ID,
                ASBIE.DEFINITION,
                ASBIE.CARDINALITY_MIN,
                ASBIE.CARDINALITY_MAX,
                ASBIE.IS_NILLABLE.as("nillable"),
                ASBIE.REMARK,
                ASBIE.SEQ_KEY,
                ASBIE.IS_USED.as("used")).from(ASBIE)
                .where(ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(ownerTopLevelAsbiepId)))
                .fetchInto(BieMakeReusableBieAsbie.class);
    }

    private List<BieMakeReusableBieBbie> getBbieByOwnerTopLevelAsbiepId(long ownerTopLevelAsbiepId) {
        return dslContext.select(
                BBIE.BBIE_ID,
                BBIE.GUID,
                BBIE.BASED_BCC_ID,
                BBIE.FROM_ABIE_ID,
                BBIE.TO_BBIEP_ID,
                BBIE.BDT_PRI_RESTRI_ID,
                BBIE.CODE_LIST_ID,
                BBIE.AGENCY_ID_LIST_ID,
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
                .fetchInto(BieMakeReusableBieBbie.class);
    }

    private List<BieMakeReusableBieAsbiep> getAsbiepByOwnerTopLevelAsbiepId(long ownerTopLevelAsbiepId) {
        return dslContext.select(
                ASBIEP.ASBIEP_ID,
                ASBIEP.GUID,
                ASBIEP.BASED_ASCCP_ID,
                ASBIEP.ROLE_OF_ABIE_ID,
                ASBIEP.DEFINITION,
                ASBIEP.REMARK,
                ASBIEP.BIZ_TERM).from(ASBIEP)
                .where(ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(ownerTopLevelAsbiepId)))
                .fetchInto(BieMakeReusableBieAsbiep.class);
    }

    private List<BieMakeReusableBieBbiep> getBbiepByOwnerTopLevelAsbiepId(long ownerTopLevelAsbiepId) {
        return dslContext.select(
                BBIEP.BBIEP_ID,
                BBIEP.GUID,
                BBIEP.BASED_BCCP_ID,
                BBIEP.DEFINITION,
                BBIEP.REMARK,
                BBIEP.BIZ_TERM).from(BBIEP)
                .where(BBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(ownerTopLevelAsbiepId)))
                .fetchInto(BieMakeReusableBieBbiep.class);
    }

    private List<BieMakeReusableBieBbieSc> getBbieScByOwnerTopLevelAsbiepId(long ownerTopLevelAsbiepId) {
        return dslContext.select(
                BBIE_SC.BBIE_SC_ID,
                BBIE_SC.GUID,
                BBIE_SC.BBIE_ID,
                BBIE_SC.DT_SC_ID,
                BBIE_SC.DT_SC_PRI_RESTRI_ID,
                BBIE_SC.CODE_LIST_ID,
                BBIE_SC.AGENCY_ID_LIST_ID,
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
                .fetchInto(BieMakeReusableBieBbieSc.class);
    }

    @Data
    public static class BieMakeReusableBieAbie {

        private long abieId;
        private String guid;
        private long basedAccId;

    }

    @Data
    public static class BieMakeReusableBieAsbie {

        private long asbieId;
        private String guid;
        private long fromAbieId;
        private long toAsbiepId;
        private long basedAsccId;
        private String definition;
        private int cardinalityMin;
        private int cardinalityMax;
        private boolean nillable;
        private String remark;
        private double seqKey;
        private boolean used;

    }

    @Data
    public static class BieMakeReusableBieBbie {

        private long bbieId;
        private String guid;
        private long basedBccId;
        private long fromAbieId;
        private long toBbiepId;
        private Long bdtPriRestriId;
        private Long codeListId;
        private Long agencyIdListId;
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
    public static class BieMakeReusableBieAsbiep {

        private long asbiepId;
        private String guid;
        private long basedAsccpId;
        private long roleOfAbieId;
        private String definition;
        private String remark;
        private String bizTerm;

    }

    @Data
    public static class BieMakeReusableBieBbiep {

        private long bbiepId;
        private String guid;
        private long basedBccpId;
        private String definition;
        private String remark;
        private String bizTerm;

    }

    @Data
    public static class BieMakeReusableBieBbieSc {

        private long bbieScId;
        private String guid;
        private long bbieId;
        private long dtScId;
        private Long dtScPriRestriId;
        private Long codeListId;
        private Long agencyIdListId;
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

    private class BieMakeReusableBieContext {

        private TopLevelAsbiep sourceTopLevelAsbiep;
        private TopLevelAsbiep targetTopLevelAsbiep;
        private List<Long> bizCtxIds;
        private long userId;

        private Timestamp timestamp;


        private List<BieMakeReusableBieAbie> abieList;

        private List<BieMakeReusableBieAsbiep> asbiepList;
        private Map<Long, List<BieMakeReusableBieAsbiep>> roleOfAbieToAsbiepMap;

        private List<BieMakeReusableBieBbiep> bbiepList;

        private List<BieMakeReusableBieAsbie> asbieList;
        private Map<Long, List<BieMakeReusableBieAsbie>> fromAbieToAsbieMap;
        private Map<Long, List<BieMakeReusableBieAsbie>> toAsbiepToAsbieMap;

        private List<BieMakeReusableBieBbie> bbieList;
        private Map<Long, List<BieMakeReusableBieBbie>> fromAbieToBbieMap;
        private Map<Long, List<BieMakeReusableBieBbie>> toBbiepToBbieMap;

        private List<BieMakeReusableBieBbieSc> bbieScList;
        private Map<Long, List<BieMakeReusableBieBbieSc>> bbieToBbieScMap;

        public BieMakeReusableBieContext(BieMakeReusableBieRequestEvent event) {
            long sourceTopLevelAsbiepId = event.getSourceTopLevelAsbiepId();
            sourceTopLevelAsbiep = topLevelAsbiepRepository.findById(sourceTopLevelAsbiepId);

            long targetTopLevelAsbiepId = event.getTargetTopLevelAsbiepId();
            targetTopLevelAsbiep = topLevelAsbiepRepository.findById(targetTopLevelAsbiepId);

            bizCtxIds = event.getBizCtxIds();
            if (bizCtxIds == null || bizCtxIds.isEmpty()) {
                bizCtxIds = dslContext.select(BIZ_CTX_ASSIGNMENT.BIZ_CTX_ID)
                        .from(BIZ_CTX_ASSIGNMENT)
                        .where(BIZ_CTX_ASSIGNMENT.TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(sourceTopLevelAsbiepId)))
                        .fetchInto(Long.class);
            }
            userId = event.getUserId();

            abieList = getAbieByOwnerTopLevelAsbiepId(sourceTopLevelAsbiepId);

            asbiepList = getAsbiepByOwnerTopLevelAsbiepId(sourceTopLevelAsbiepId);
            roleOfAbieToAsbiepMap = asbiepList.stream().collect(groupingBy(BieMakeReusableBieAsbiep::getRoleOfAbieId));

            asbieList = getAsbieByOwnerTopLevelAsbiepId(sourceTopLevelAsbiepId);
            fromAbieToAsbieMap = asbieList.stream().collect(groupingBy(BieMakeReusableBieAsbie::getFromAbieId));
            toAsbiepToAsbieMap = asbieList.stream().collect(groupingBy(BieMakeReusableBieAsbie::getToAsbiepId));

            bbiepList = getBbiepByOwnerTopLevelAsbiepId(sourceTopLevelAsbiepId);

            bbieList = getBbieByOwnerTopLevelAsbiepId(sourceTopLevelAsbiepId);
            fromAbieToBbieMap = bbieList.stream().collect(groupingBy(BieMakeReusableBieBbie::getFromAbieId));
            toBbiepToBbieMap = bbieList.stream().collect(groupingBy(BieMakeReusableBieBbie::getToBbiepId));

            bbieScList = getBbieScByOwnerTopLevelAsbiepId(sourceTopLevelAsbiepId);
            bbieToBbieScMap = bbieScList.stream().collect(groupingBy(BieMakeReusableBieBbieSc::getBbieId));

            arrange(event);
        }

        public void execute() {
            timestamp = new Timestamp(System.currentTimeMillis());
            logger.debug("Begin creating BIE from existing BIE with the source " + sourceTopLevelAsbiep.getTopLevelAsbiepId() +
                    " and the target " + targetTopLevelAsbiep.getTopLevelAsbiepId());

            repository.createBizCtxAssignments(
                    targetTopLevelAsbiep.getTopLevelAsbiepId(),
                    bizCtxIds);

            for (BieMakeReusableBieAbie abie : abieList) {
                long previousAbieId = abie.getAbieId();
                long nextAbieId = insertAbie(abie);

                fireChangeEvent("abie", previousAbieId, nextAbieId);
            }

            for (BieMakeReusableBieAsbiep asbiep : asbiepList) {
                long previousAsbiepId = asbiep.getAsbiepId();
                long nextAsbiepId = insertAsbiep(asbiep);

                fireChangeEvent("asbiep", previousAsbiepId, nextAsbiepId);
            }

            repository.updateAsbiepIdOnTopLevelAsbiep(
                    targetTopLevelAsbiep.getAsbiepId(),
                    targetTopLevelAsbiep.getTopLevelAsbiepId());

            for (BieMakeReusableBieBbiep bbiep : bbiepList) {
                long previousBbiepId = bbiep.getBbiepId();
                long nextBbiepId = insertBbiep(bbiep);

                fireChangeEvent("bbiep", previousBbiepId, nextBbiepId);
            }

            for (BieMakeReusableBieAsbie asbie : asbieList) {
                long previousAsbieId = asbie.getAsbieId();
                long nextAsbieId = insertAsbie(asbie);

                fireChangeEvent("asbie", previousAsbieId, nextAsbieId);
            }

            for (BieMakeReusableBieBbie bbie : bbieList) {
                long previousBbieId = bbie.getBbieId();
                long nextBbieId = insertBbie(bbie);

                fireChangeEvent("bbie", previousBbieId, nextBbieId);
            }

            for (BieMakeReusableBieBbieSc bbieSc : bbieScList) {
                long previousBbieScId = bbieSc.getBbieId();
                long nextBbieScId = insertBbieSc(bbieSc);

                fireChangeEvent("bbie_sc", previousBbieScId, nextBbieScId);
            }

            repository.updateState(targetTopLevelAsbiep.getTopLevelAsbiepId(), BieState.Editing);

            logger.debug("End create BIE from " + sourceTopLevelAsbiep.getTopLevelAsbiepId() +
                    " to " + targetTopLevelAsbiep.getTopLevelAsbiepId());
        }

        private void arrange(BieMakeReusableBieRequestEvent event) {
            List<BieMakeReusableBieAbie> abieList = new ArrayList();
            List<BieMakeReusableBieAsbiep> asbiepList = new ArrayList();
            List<BieMakeReusableBieBbiep> bbiepList = new ArrayList();
            List<BieMakeReusableBieAsbie> asbieList = new ArrayList();
            List<BieMakeReusableBieBbie> bbieList = new ArrayList();
            List<BieMakeReusableBieBbieSc> bbieScList = new ArrayList();

            Map<Long, BieMakeReusableBieAsbiep> asbiepMap = this.asbiepList.stream()
                    .collect(Collectors.toMap(BieMakeReusableBieAsbiep::getAsbiepId, Function.identity()));

            Map<Long, BieMakeReusableBieAbie> abieMap = this.abieList.stream()
                    .collect(Collectors.toMap(BieMakeReusableBieAbie::getAbieId, Function.identity()));

            Map<Long, BieMakeReusableBieBbiep> bbiepMap = this.bbiepList.stream()
                    .collect(Collectors.toMap(BieMakeReusableBieBbiep::getBbiepId, Function.identity()));

            Queue<BieMakeReusableBieAsbiep> asbiepQueue = new LinkedList<>();
            BieMakeReusableBieAsbiep topLevelAsbiep = asbiepMap.get(event.getAsbiepId());
            asbiepQueue.add(topLevelAsbiep);

            while (!asbiepQueue.isEmpty()) {
                BieMakeReusableBieAsbiep currentAsbiep = asbiepQueue.poll();
                asbiepList.add(currentAsbiep);
                BieMakeReusableBieAbie roleOfAbie = abieMap.get(currentAsbiep.getRoleOfAbieId());
                abieList.add(roleOfAbie);

                List<BieMakeReusableBieAsbie> asbies = this.asbieList.stream()
                        .filter(e -> e.getFromAbieId() == roleOfAbie.getAbieId()).collect(Collectors.toList());

                asbieList.addAll(asbies);
                asbies.forEach(e -> {
                    if (asbiepMap.get(e.getToAsbiepId()) != null) {
                        asbiepQueue.add(asbiepMap.get(e.getToAsbiepId()));
                    }
                });

                List<BieMakeReusableBieBbie> bbies = this.bbieList.stream()
                        .filter(e -> e.getFromAbieId() == roleOfAbie.getAbieId()).collect(Collectors.toList());
                bbieList.addAll(bbies);
                bbies.forEach(e -> {
                    bbiepList.add(bbiepMap.get(e.getToBbiepId()));
                    bbieScList.addAll(this.bbieScList.stream()
                        .filter(sc -> sc.getBbieId() == e.getBbieId())
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


        private void fireChangeEvent(String type, long previousVal, long nextVal) {
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
                    if (previousVal == targetTopLevelAsbiep.getAsbiepId()) {
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

        private long insertAbie(BieMakeReusableBieAbie abie) {

            return dslContext.insertInto(ABIE)
                    .set(ABIE.GUID, SrtGuid.randomGuid())
                    .set(ABIE.BASED_ACC_ID, ULong.valueOf(abie.getBasedAccId()))
                    .set(ABIE.CREATED_BY, ULong.valueOf(userId))
                    .set(ABIE.LAST_UPDATED_BY, ULong.valueOf(userId))
                    .set(ABIE.CREATION_TIMESTAMP, timestamp)
                    .set(ABIE.LAST_UPDATE_TIMESTAMP, timestamp)
                    .set(ABIE.STATE, BieState.Initiating.getValue())
                    .set(ABIE.OWNER_TOP_LEVEL_ASBIEP_ID, ULong.valueOf(targetTopLevelAsbiep.getTopLevelAsbiepId()))
                    .returning(ABIE.ABIE_ID).fetchOne().getValue(ABIE.ABIE_ID).longValue();
        }

        private long insertAsbiep(BieMakeReusableBieAsbiep asbiep) {

            return dslContext.insertInto(ASBIEP)
                    .set(ASBIEP.GUID, SrtGuid.randomGuid())
                    .set(ASBIEP.BASED_ASCCP_ID, ULong.valueOf(asbiep.getBasedAsccpId()))
                    .set(ASBIEP.ROLE_OF_ABIE_ID, ULong.valueOf(asbiep.getRoleOfAbieId()))
                    .set(ASBIEP.DEFINITION, asbiep.getDefinition())
                    .set(ASBIEP.REMARK, asbiep.getRemark())
                    .set(ASBIEP.BIZ_TERM, asbiep.getBizTerm())
                    .set(ASBIEP.CREATED_BY, ULong.valueOf(userId))
                    .set(ASBIEP.LAST_UPDATED_BY, ULong.valueOf(userId))
                    .set(ASBIEP.CREATION_TIMESTAMP, timestamp)
                    .set(ASBIEP.LAST_UPDATE_TIMESTAMP, timestamp)
                    .set(ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID, ULong.valueOf(targetTopLevelAsbiep.getTopLevelAsbiepId()))
                    .returning(ASBIEP.ASBIEP_ID).fetchOne().getValue(ASBIEP.ASBIEP_ID).longValue();
        }

        private long insertBbiep(BieMakeReusableBieBbiep bbiep) {

            return dslContext.insertInto(BBIEP)
                    .set(BBIEP.GUID, SrtGuid.randomGuid())
                    .set(BBIEP.BASED_BCCP_ID, ULong.valueOf(bbiep.getBasedBccpId()))
                    .set(BBIEP.DEFINITION, bbiep.getDefinition())
                    .set(BBIEP.REMARK, bbiep.getRemark())
                    .set(BBIEP.BIZ_TERM, bbiep.getBizTerm())
                    .set(BBIEP.CREATED_BY, ULong.valueOf(userId))
                    .set(BBIEP.LAST_UPDATED_BY, ULong.valueOf(userId))
                    .set(BBIEP.CREATION_TIMESTAMP, timestamp)
                    .set(BBIEP.LAST_UPDATE_TIMESTAMP, timestamp)
                    .set(BBIEP.OWNER_TOP_LEVEL_ASBIEP_ID, ULong.valueOf(targetTopLevelAsbiep.getTopLevelAsbiepId()))
                    .returning(BBIEP.BBIEP_ID).fetchOne().getValue(BBIEP.BBIEP_ID).longValue();
        }

        private long insertAsbie(BieMakeReusableBieAsbie asbie) {

            return dslContext.insertInto(ASBIE)
                    .set(ASBIE.GUID, SrtGuid.randomGuid())
                    .set(ASBIE.FROM_ABIE_ID, ULong.valueOf(asbie.getFromAbieId()))
                    .set(ASBIE.TO_ASBIEP_ID, ULong.valueOf(asbie.getToAsbiepId()))
                    .set(ASBIE.BASED_ASCC_ID, ULong.valueOf(asbie.getBasedAsccId()))
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
                    .returning(ASBIE.ASBIE_ID).fetchOne().getValue(ASBIE.ASBIE_ID).longValue();
        }

        private long insertBbie(BieMakeReusableBieBbie bbie) {

            return dslContext.insertInto(BBIE)
                    .set(BBIE.GUID, SrtGuid.randomGuid())
                    .set(BBIE.FROM_ABIE_ID, ULong.valueOf(bbie.getFromAbieId()))
                    .set(BBIE.TO_BBIEP_ID, ULong.valueOf(bbie.getToBbiepId()))
                    .set(BBIE.BASED_BCC_ID, ULong.valueOf(bbie.getBasedBccId()))
                    .set(BBIE.BDT_PRI_RESTRI_ID, (bbie.getBdtPriRestriId() != null) ? ULong.valueOf(bbie.getBdtPriRestriId()) : null)
                    .set(BBIE.CODE_LIST_ID, (bbie.getCodeListId() != null) ? ULong.valueOf(bbie.getCodeListId()) : null)
                    .set(BBIE.AGENCY_ID_LIST_ID, (bbie.getAgencyIdListId() != null) ? ULong.valueOf(bbie.getAgencyIdListId()) : null)
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
                    .returning(BBIE.BBIE_ID).fetchOne().getValue(BBIE.BBIE_ID).longValue();
        }

        private long insertBbieSc(BieMakeReusableBieBbieSc bbieSc) {

            return dslContext.insertInto(BBIE_SC)
                    .set(BBIE_SC.GUID, SrtGuid.randomGuid())
                    .set(BBIE_SC.BBIE_ID, ULong.valueOf(bbieSc.getBbieId()))
                    .set(BBIE_SC.DT_SC_ID, ULong.valueOf(bbieSc.getDtScId()))
                    .set(BBIE_SC.DT_SC_PRI_RESTRI_ID, (bbieSc.getDtScPriRestriId() != null) ? ULong.valueOf(bbieSc.getDtScPriRestriId()) : null)
                    .set(BBIE_SC.CODE_LIST_ID, (bbieSc.getCodeListId() != null) ? ULong.valueOf(bbieSc.getCodeListId()) : null)
                    .set(BBIE_SC.AGENCY_ID_LIST_ID, (bbieSc.getAgencyIdListId() != null) ? ULong.valueOf(bbieSc.getAgencyIdListId()) : null)
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
                    .returning(BBIE_SC.BBIE_SC_ID).fetchOne().getValue(BBIE_SC.BBIE_SC_ID).longValue();
        }
    }

}

package org.oagi.srt.gateway.http.api.bie_management.service;

import lombok.Data;
import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.srt.data.BieState;
import org.oagi.srt.data.TopLevelAbie;
import org.oagi.srt.entity.jooq.Tables;
import org.oagi.srt.gateway.http.api.bie_management.data.BieCopyRequest;
import org.oagi.srt.gateway.http.configuration.security.SessionService;
import org.oagi.srt.gateway.http.event.BieCopyRequestEvent;
import org.oagi.srt.gateway.http.helper.SrtGuid;
import org.oagi.srt.redis.event.EventListenerContainer;
import org.oagi.srt.repository.TopLevelAbieRepository;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;
import static org.oagi.srt.data.BieState.Initiating;

@Service
@Transactional
public class BieCopyService implements InitializingBean {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private BieRepository repository;

    @Autowired
    private TopLevelAbieRepository topLevelAbieRepository;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RedisMessageListenerContainer messageListenerContainer;

    @Autowired
    private EventListenerContainer eventListenerContainer;

    private String INTERESTED_EVENT_NAME = "bieCopyRequestEvent";

    @Override
    public void afterPropertiesSet() throws Exception {
        eventListenerContainer.addMessageListener(this, "onEventReceived",
                new ChannelTopic(INTERESTED_EVENT_NAME));
    }

    @Transactional
    public void copyBie(User user, BieCopyRequest request) {
        long sourceTopLevelAbieId = request.getTopLevelAbieId();
        List<Long> bizCtxIds = request.getBizCtxIds();
        long userId = sessionService.userId(user);

        TopLevelAbie sourceTopLevelAbie = topLevelAbieRepository.findById(sourceTopLevelAbieId);
        long copiedTopLevelAbieId =
                repository.createTopLevelAbie(userId, sourceTopLevelAbie.getReleaseId(), Initiating);

        BieCopyRequestEvent bieCopyRequestEvent = new BieCopyRequestEvent(
                sourceTopLevelAbieId, copiedTopLevelAbieId, bizCtxIds, userId
        );

        /*
         * Message Publishing
         */
        redisTemplate.convertAndSend(INTERESTED_EVENT_NAME, bieCopyRequestEvent);
    }


    /**
     * This method is invoked by 'bieCopyRequestEvent' channel subscriber.
     *
     * @param bieCopyRequestEvent
     */
    @Transactional
    public void onEventReceived(BieCopyRequestEvent bieCopyRequestEvent) {
        RLock lock = redissonClient.getLock("BieCopyRequestEvent:" + bieCopyRequestEvent.hashCode());
        if (!lock.tryLock()) {
            return;
        }
        try {
            logger.debug("Received BieCopyRequestEvent: " + bieCopyRequestEvent);

            BieCopyContext copyContext = new BieCopyContext(bieCopyRequestEvent);
            copyContext.execute();
        } finally {
            lock.unlock();
        }
    }

    private List<BieCopyAbie> getAbieByOwnerTopLevelAbieId(long ownerTopLevelAbieId) {
        return dslContext.select(
                Tables.ABIE.ABIE_ID,
                Tables.ABIE.GUID,
                Tables.ABIE.BASED_ACC_ID,
                Tables.ABIE.DEFINITION,
                Tables.ABIE.CLIENT_ID,
                Tables.ABIE.VERSION,
                Tables.ABIE.STATUS,
                Tables.ABIE.REMARK,
                Tables.ABIE.BIZ_TERM
        ).from(Tables.ABIE)
                .where(Tables.ABIE.OWNER_TOP_LEVEL_ABIE_ID.eq(ULong.valueOf(ownerTopLevelAbieId)))
                .fetchInto(BieCopyAbie.class);
    }

    private List<BieCopyAsbie> getAsbieByOwnerTopLevelAbieId(long ownerTopLevelAbieId) {
        return dslContext.select(
                Tables.ASBIE.ASBIE_ID,
                Tables.ASBIE.GUID,
                Tables.ASBIE.FROM_ABIE_ID,
                Tables.ASBIE.TO_ASBIEP_ID,
                Tables.ASBIE.BASED_ASCC_ID,
                Tables.ASBIE.DEFINITION,
                Tables.ASBIE.CARDINALITY_MIN,
                Tables.ASBIE.CARDINALITY_MAX,
                Tables.ASBIE.IS_NILLABLE.as("nillable"),
                Tables.ASBIE.REMARK,
                Tables.ASBIE.SEQ_KEY,
                Tables.ASBIE.IS_USED.as("used")).from(Tables.ASBIE)
                .where(Tables.ASBIE.OWNER_TOP_LEVEL_ABIE_ID.eq(ULong.valueOf(ownerTopLevelAbieId)))
                .fetchInto(BieCopyAsbie.class);
    }

    private List<BieCopyBbie> getBbieByOwnerTopLevelAbieId(long ownerTopLevelAbieId) {
        return dslContext.select(
                Tables.BBIE.BBIE_ID,
                Tables.BBIE.GUID,
                Tables.BBIE.BASED_BCC_ID,
                Tables.BBIE.FROM_ABIE_ID,
                Tables.BBIE.TO_BBIEP_ID,
                Tables.BBIE.BDT_PRI_RESTRI_ID,
                Tables.BBIE.CODE_LIST_ID,
                Tables.BBIE.AGENCY_ID_LIST_ID,
                Tables.BBIE.CARDINALITY_MIN,
                Tables.BBIE.CARDINALITY_MAX,
                Tables.BBIE.DEFAULT_VALUE,
                Tables.BBIE.IS_NILLABLE.as("nillable"),
                Tables.BBIE.FIXED_VALUE,
                Tables.BBIE.IS_NULL.as("nill"),
                Tables.BBIE.DEFINITION,
                Tables.BBIE.REMARK,
                Tables.BBIE.SEQ_KEY,
                Tables.BBIE.IS_USED.as("used")).from(Tables.BBIE)
                .where(Tables.BBIE.OWNER_TOP_LEVEL_ABIE_ID.eq(ULong.valueOf(ownerTopLevelAbieId)))
                .fetchInto(BieCopyBbie.class);
    }

    private List<BieCopyAsbiep> getAsbiepByOwnerTopLevelAbieId(long ownerTopLevelAbieId) {
        return dslContext.select(
                Tables.ASBIEP.ASBIEP_ID,
                Tables.ASBIEP.GUID,
                Tables.ASBIEP.BASED_ASCCP_ID,
                Tables.ASBIEP.ROLE_OF_ABIE_ID,
                Tables.ASBIEP.DEFINITION,
                Tables.ASBIEP.REMARK,
                Tables.ASBIEP.BIZ_TERM).from(Tables.ASBIEP)
                .where(Tables.ASBIEP.OWNER_TOP_LEVEL_ABIE_ID.eq(ULong.valueOf(ownerTopLevelAbieId)))
                .fetchInto(BieCopyAsbiep.class);
    }

    private List<BieCopyBbiep> getBbiepByOwnerTopLevelAbieId(long ownerTopLevelAbieId) {
        return dslContext.select(
                Tables.BBIEP.BBIEP_ID,
                Tables.BBIEP.GUID,
                Tables.BBIEP.BASED_BCCP_ID,
                Tables.BBIEP.DEFINITION,
                Tables.BBIEP.REMARK,
                Tables.BBIEP.BIZ_TERM).from(Tables.BBIEP)
                .where(Tables.BBIEP.OWNER_TOP_LEVEL_ABIE_ID.eq(ULong.valueOf(ownerTopLevelAbieId)))
                .fetchInto(BieCopyBbiep.class);
    }

    private List<BieCopyBbieSc> getBbieScByOwnerTopLevelAbieId(long ownerTopLevelAbieId) {
        return dslContext.select(
                Tables.BBIE_SC.BBIE_SC_ID,
                Tables.BBIE_SC.GUID,
                Tables.BBIE_SC.BBIE_ID,
                Tables.BBIE_SC.DT_SC_ID,
                Tables.BBIE_SC.DT_SC_PRI_RESTRI_ID,
                Tables.BBIE_SC.CODE_LIST_ID,
                Tables.BBIE_SC.AGENCY_ID_LIST_ID,
                Tables.BBIE_SC.CARDINALITY_MIN,
                Tables.BBIE_SC.CARDINALITY_MAX,
                Tables.BBIE_SC.DEFAULT_VALUE,
                Tables.BBIE_SC.FIXED_VALUE,
                Tables.BBIE_SC.DEFINITION,
                Tables.BBIE_SC.REMARK,
                Tables.BBIE_SC.BIZ_TERM,
                Tables.BBIE_SC.IS_USED.as("used")).from(Tables.BBIE_SC)
                .where(Tables.BBIE_SC.OWNER_TOP_LEVEL_ABIE_ID.eq(ULong.valueOf(ownerTopLevelAbieId)))
                .fetchInto(BieCopyBbieSc.class);
    }

    @Data
    public static class BieCopyAbie {

        private long abieId;
        private String guid;
        private long basedAccId;
        private String definition;
        private Long clientId;
        private String version;
        private String status;
        private String remark;
        private String bizTerm;

    }

    @Data
    public static class BieCopyAsbie {

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
    public static class BieCopyBbie {

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
        private String remark;
        private double seqKey;
        private boolean used;

    }

    @Data
    public static class BieCopyAsbiep {

        private long asbiepId;
        private String guid;
        private long basedAsccpId;
        private long roleOfAbieId;
        private String definition;
        private String remark;
        private String bizTerm;

    }

    @Data
    public static class BieCopyBbiep {

        private long bbiepId;
        private String guid;
        private long basedBccpId;
        private String definition;
        private String remark;
        private String bizTerm;

    }

    @Data
    public static class BieCopyBbieSc {

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
        private String remark;
        private String bizTerm;
        private boolean used;

    }

    private class BieCopyContext {

        private TopLevelAbie sourceTopLevelAbie;
        private TopLevelAbie copiedTopLevelAbie;
        private List<Long> bizCtxIds;
        private long userId;

        private Timestamp timestamp;


        private List<BieCopyAbie> abieList;

        private List<BieCopyAsbiep> asbiepList;
        private Map<Long, List<BieCopyAsbiep>> roleOfAbieToAsbiepMap;

        private List<BieCopyBbiep> bbiepList;

        private List<BieCopyAsbie> asbieList;
        private Map<Long, List<BieCopyAsbie>> fromAbieToAsbieMap;
        private Map<Long, List<BieCopyAsbie>> toAsbiepToAsbieMap;

        private List<BieCopyBbie> bbieList;
        private Map<Long, List<BieCopyBbie>> fromAbieToBbieMap;
        private Map<Long, List<BieCopyBbie>> toBbiepToBbieMap;

        private List<BieCopyBbieSc> bbieScList;
        private Map<Long, List<BieCopyBbieSc>> bbieToBbieScMap;

        public BieCopyContext(BieCopyRequestEvent bieCopyRequestEvent) {
            long sourceTopLevelAbieId = bieCopyRequestEvent.getSourceTopLevelAbieId();
            sourceTopLevelAbie = topLevelAbieRepository.findById(sourceTopLevelAbieId);

            long copiedTopLevelAbieId = bieCopyRequestEvent.getCopiedTopLevelAbieId();
            copiedTopLevelAbie = topLevelAbieRepository.findById(copiedTopLevelAbieId);

            bizCtxIds = bieCopyRequestEvent.getBizCtxIds();
            userId = bieCopyRequestEvent.getUserId();

            abieList = getAbieByOwnerTopLevelAbieId(sourceTopLevelAbieId);

            asbiepList = getAsbiepByOwnerTopLevelAbieId(sourceTopLevelAbieId);
            roleOfAbieToAsbiepMap = asbiepList.stream().collect(groupingBy(BieCopyAsbiep::getRoleOfAbieId));

            bbiepList = getBbiepByOwnerTopLevelAbieId(sourceTopLevelAbieId);

            asbieList = getAsbieByOwnerTopLevelAbieId(sourceTopLevelAbieId);
            fromAbieToAsbieMap = asbieList.stream().collect(groupingBy(BieCopyAsbie::getFromAbieId));
            toAsbiepToAsbieMap = asbieList.stream().collect(groupingBy(BieCopyAsbie::getToAsbiepId));

            bbieList = getBbieByOwnerTopLevelAbieId(sourceTopLevelAbieId);
            fromAbieToBbieMap = bbieList.stream().collect(groupingBy(BieCopyBbie::getFromAbieId));
            toBbiepToBbieMap = bbieList.stream().collect(groupingBy(BieCopyBbie::getToBbiepId));

            bbieScList = getBbieScByOwnerTopLevelAbieId(sourceTopLevelAbieId);
            bbieToBbieScMap = bbieScList.stream().collect(groupingBy(BieCopyBbieSc::getBbieId));
        }

        public void execute() {
            timestamp = new Timestamp(System.currentTimeMillis());
            logger.debug("Begin copying from " + sourceTopLevelAbie.getTopLevelAbieId() +
                    " to " + copiedTopLevelAbie.getTopLevelAbieId());

            repository.createBizCtxAssignments(
                    copiedTopLevelAbie.getTopLevelAbieId(),
                    bizCtxIds);

            for (BieCopyAbie abie : abieList) {
                long previousAbieId = abie.getAbieId();
                long nextAbieId = insertAbie(abie);

                fireChangeEvent("abie", previousAbieId, nextAbieId);
            }

            repository.updateAbieIdOnTopLevelAbie(
                    copiedTopLevelAbie.getAbieId(),
                    copiedTopLevelAbie.getTopLevelAbieId());

            for (BieCopyAsbiep asbiep : asbiepList) {
                long previousAsbiepId = asbiep.getAsbiepId();
                long nextAsbiepId = insertAsbiep(asbiep);

                fireChangeEvent("asbiep", previousAsbiepId, nextAsbiepId);
            }

            for (BieCopyBbiep bbiep : bbiepList) {
                long previousBbiepId = bbiep.getBbiepId();
                long nextBbiepId = insertBbiep(bbiep);

                fireChangeEvent("bbiep", previousBbiepId, nextBbiepId);
            }

            for (BieCopyAsbie asbie : asbieList) {
                long previousAsbieId = asbie.getAsbieId();
                long nextAsbieId = insertAsbie(asbie);

                fireChangeEvent("asbie", previousAsbieId, nextAsbieId);
            }

            for (BieCopyBbie bbie : bbieList) {
                long previousBbieId = bbie.getBbieId();
                long nextBbieId = insertBbie(bbie);

                fireChangeEvent("bbie", previousBbieId, nextBbieId);
            }

            for (BieCopyBbieSc bbieSc : bbieScList) {
                long previousBbieScId = bbieSc.getBbieId();
                long nextBbieScId = insertBbieSc(bbieSc);

                fireChangeEvent("bbie_sc", previousBbieScId, nextBbieScId);
            }

            repository.updateState(copiedTopLevelAbie.getTopLevelAbieId(), BieState.Editing);

            logger.debug("End copying from " + sourceTopLevelAbie.getTopLevelAbieId() +
                    " to " + copiedTopLevelAbie.getTopLevelAbieId());
        }


        private void fireChangeEvent(String type, long previousVal, long nextVal) {
            switch (type) {
                case "abie":
                    if (sourceTopLevelAbie.getAbieId() == previousVal) {
                        copiedTopLevelAbie.setAbieId(nextVal);
                    }

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


        private long insertAbie(BieCopyAbie abie) {

            return dslContext.insertInto(Tables.ABIE)
                    .set(Tables.ABIE.GUID, SrtGuid.randomGuid())
                    .set(Tables.ABIE.BASED_ACC_ID, ULong.valueOf(abie.getBasedAccId()))
                    .set(Tables.ABIE.DEFINITION, abie.getDefinition())
                    .set(Tables.ABIE.CREATED_BY, ULong.valueOf(userId))
                    .set(Tables.ABIE.LAST_UPDATED_BY, ULong.valueOf(userId))
                    .set(Tables.ABIE.CREATION_TIMESTAMP, timestamp)
                    .set(Tables.ABIE.LAST_UPDATE_TIMESTAMP, timestamp)
                    .set(Tables.ABIE.STATE, BieState.Initiating.getValue())
                    .set(Tables.ABIE.CLIENT_ID, (abie.getClientId() != null) ? ULong.valueOf(abie.getClientId()) : null)
                    .set(Tables.ABIE.VERSION, abie.getVersion())
                    .set(Tables.ABIE.STATUS, abie.getStatus())
                    .set(Tables.ABIE.REMARK, abie.getRemark())
                    .set(Tables.ABIE.BIZ_TERM, abie.getBizTerm())
                    .set(Tables.ABIE.OWNER_TOP_LEVEL_ABIE_ID, ULong.valueOf(copiedTopLevelAbie.getTopLevelAbieId()))
                    .returning().fetchOne().getAbieId().longValue();
        }

        private long insertAsbiep(BieCopyAsbiep asbiep) {

            return dslContext.insertInto(Tables.ASBIEP)
                    .set(Tables.ASBIEP.GUID, SrtGuid.randomGuid())
                    .set(Tables.ASBIEP.BASED_ASCCP_ID, ULong.valueOf(asbiep.getBasedAsccpId()))
                    .set(Tables.ASBIEP.ROLE_OF_ABIE_ID, ULong.valueOf(asbiep.getRoleOfAbieId()))
                    .set(Tables.ASBIEP.DEFINITION, asbiep.getDefinition())
                    .set(Tables.ASBIEP.REMARK, asbiep.getRemark())
                    .set(Tables.ASBIEP.BIZ_TERM, asbiep.getBizTerm())
                    .set(Tables.ASBIEP.CREATED_BY, ULong.valueOf(userId))
                    .set(Tables.ASBIEP.LAST_UPDATED_BY, ULong.valueOf(userId))
                    .set(Tables.ASBIEP.CREATION_TIMESTAMP, timestamp)
                    .set(Tables.ASBIEP.LAST_UPDATE_TIMESTAMP, timestamp)
                    .set(Tables.ASBIEP.OWNER_TOP_LEVEL_ABIE_ID, ULong.valueOf(copiedTopLevelAbie.getTopLevelAbieId()))
                    .returning().fetchOne().getAsbiepId().longValue();
        }

        private long insertBbiep(BieCopyBbiep bbiep) {

            return dslContext.insertInto(Tables.BBIEP)
                    .set(Tables.BBIEP.GUID, SrtGuid.randomGuid())
                    .set(Tables.BBIEP.BASED_BCCP_ID, ULong.valueOf(bbiep.getBasedBccpId()))
                    .set(Tables.BBIEP.DEFINITION, bbiep.getDefinition())
                    .set(Tables.BBIEP.REMARK, bbiep.getDefinition())
                    .set(Tables.BBIEP.BIZ_TERM, bbiep.getDefinition())
                    .set(Tables.BBIEP.CREATED_BY, ULong.valueOf(userId))
                    .set(Tables.BBIEP.LAST_UPDATED_BY, ULong.valueOf(userId))
                    .set(Tables.BBIEP.CREATION_TIMESTAMP, timestamp)
                    .set(Tables.BBIEP.LAST_UPDATE_TIMESTAMP, timestamp)
                    .set(Tables.BBIEP.OWNER_TOP_LEVEL_ABIE_ID, ULong.valueOf(copiedTopLevelAbie.getTopLevelAbieId()))
                    .returning().fetchOne().getBbiepId().longValue();
        }

        private long insertAsbie(BieCopyAsbie asbie) {

            return dslContext.insertInto(Tables.ASBIE)
                    .set(Tables.ASBIE.GUID, SrtGuid.randomGuid())
                    .set(Tables.ASBIE.FROM_ABIE_ID, ULong.valueOf(asbie.getFromAbieId()))
                    .set(Tables.ASBIE.TO_ASBIEP_ID, ULong.valueOf(asbie.getToAsbiepId()))
                    .set(Tables.ASBIE.BASED_ASCC_ID, ULong.valueOf(asbie.getBasedAsccId()))
                    .set(Tables.ASBIE.DEFINITION, asbie.getDefinition())
                    .set(Tables.ASBIE.REMARK, asbie.getRemark())
                    .set(Tables.ASBIE.CARDINALITY_MIN, asbie.getCardinalityMin())
                    .set(Tables.ASBIE.CARDINALITY_MAX, asbie.getCardinalityMax())
                    .set(Tables.ASBIE.IS_NILLABLE, (byte) ((asbie.isNillable()) ? 1 : 0))
                    .set(Tables.ASBIE.IS_USED, (byte) ((asbie.isUsed()) ? 1 : 0))
                    .set(Tables.ASBIE.SEQ_KEY, BigDecimal.valueOf(asbie.getSeqKey()))
                    .set(Tables.ASBIE.CREATED_BY, ULong.valueOf(userId))
                    .set(Tables.ASBIE.LAST_UPDATED_BY, ULong.valueOf(userId))
                    .set(Tables.ASBIE.CREATION_TIMESTAMP, timestamp)
                    .set(Tables.ASBIE.LAST_UPDATE_TIMESTAMP, timestamp)
                    .set(Tables.ASBIE.OWNER_TOP_LEVEL_ABIE_ID, ULong.valueOf(copiedTopLevelAbie.getTopLevelAbieId()))
                    .returning().fetchOne().getAsbieId().longValue();
        }

        private long insertBbie(BieCopyBbie bbie) {

            return dslContext.insertInto(Tables.BBIE)
                    .set(Tables.BBIE.GUID, SrtGuid.randomGuid())
                    .set(Tables.BBIE.FROM_ABIE_ID, ULong.valueOf(bbie.getFromAbieId()))
                    .set(Tables.BBIE.TO_BBIEP_ID, ULong.valueOf(bbie.getToBbiepId()))
                    .set(Tables.BBIE.BASED_BCC_ID, ULong.valueOf(bbie.getBasedBccId()))
                    .set(Tables.BBIE.BDT_PRI_RESTRI_ID, (bbie.getBdtPriRestriId() != null) ? ULong.valueOf(bbie.getBdtPriRestriId()) : null)
                    .set(Tables.BBIE.CODE_LIST_ID, (bbie.getCodeListId() != null) ? ULong.valueOf(bbie.getCodeListId()) : null)
                    .set(Tables.BBIE.AGENCY_ID_LIST_ID, (bbie.getAgencyIdListId() != null) ? ULong.valueOf(bbie.getAgencyIdListId()) : null)
                    .set(Tables.BBIE.DEFAULT_VALUE, bbie.getDefaultValue())
                    .set(Tables.BBIE.FIXED_VALUE, bbie.getFixedValue())
                    .set(Tables.BBIE.DEFINITION, bbie.getDefinition())
                    .set(Tables.BBIE.REMARK, bbie.getRemark())
                    .set(Tables.BBIE.CARDINALITY_MIN, bbie.getCardinalityMin())
                    .set(Tables.BBIE.CARDINALITY_MAX, bbie.getCardinalityMax())
                    .set(Tables.BBIE.IS_NILLABLE, (byte) ((bbie.isNillable()) ? 1 : 0))
                    .set(Tables.BBIE.IS_NULL, (byte) ((bbie.isNill()) ? 1 : 0))
                    .set(Tables.BBIE.SEQ_KEY, BigDecimal.valueOf(bbie.getSeqKey()))
                    .set(Tables.BBIE.IS_USED, (byte) ((bbie.isUsed()) ? 1 : 0))
                    .set(Tables.BBIE.CREATED_BY, ULong.valueOf(userId))
                    .set(Tables.BBIE.LAST_UPDATED_BY, ULong.valueOf(userId))
                    .set(Tables.BBIE.CREATION_TIMESTAMP, timestamp)
                    .set(Tables.BBIE.LAST_UPDATE_TIMESTAMP, timestamp)
                    .set(Tables.BBIE.OWNER_TOP_LEVEL_ABIE_ID, ULong.valueOf(copiedTopLevelAbie.getTopLevelAbieId()))
                    .returning().fetchOne().getBbieId().longValue();
        }

        private long insertBbieSc(BieCopyBbieSc bbieSc) {

            return dslContext.insertInto(Tables.BBIE_SC)
                    .set(Tables.BBIE_SC.GUID, SrtGuid.randomGuid())
                    .set(Tables.BBIE_SC.BBIE_ID, ULong.valueOf(bbieSc.getBbieId()))
                    .set(Tables.BBIE_SC.DT_SC_ID, ULong.valueOf(bbieSc.getDtScId()))
                    .set(Tables.BBIE_SC.DT_SC_PRI_RESTRI_ID, (bbieSc.getDtScPriRestriId() != null) ? ULong.valueOf(bbieSc.getDtScPriRestriId()) : null)
                    .set(Tables.BBIE_SC.CODE_LIST_ID, (bbieSc.getCodeListId() != null) ? ULong.valueOf(bbieSc.getCodeListId()) : null)
                    .set(Tables.BBIE_SC.AGENCY_ID_LIST_ID, (bbieSc.getAgencyIdListId() != null) ? ULong.valueOf(bbieSc.getAgencyIdListId()) : null)
                    .set(Tables.BBIE_SC.DEFAULT_VALUE, bbieSc.getDefaultValue())
                    .set(Tables.BBIE_SC.FIXED_VALUE, bbieSc.getFixedValue())
                    .set(Tables.BBIE_SC.DEFINITION, bbieSc.getDefinition())
                    .set(Tables.BBIE_SC.REMARK, bbieSc.getRemark())
                    .set(Tables.BBIE_SC.BIZ_TERM, bbieSc.getBizTerm())
                    .set(Tables.BBIE_SC.CARDINALITY_MIN, bbieSc.getCardinalityMin())
                    .set(Tables.BBIE_SC.CARDINALITY_MAX, bbieSc.getCardinalityMax())
                    .set(Tables.BBIE_SC.IS_USED, (byte) ((bbieSc.isUsed()) ? 1 : 0))
                    .set(Tables.BBIE_SC.OWNER_TOP_LEVEL_ABIE_ID, ULong.valueOf(copiedTopLevelAbie.getTopLevelAbieId()))
                    .returning().fetchOne().getBbieScId().longValue();
        }
    }

}

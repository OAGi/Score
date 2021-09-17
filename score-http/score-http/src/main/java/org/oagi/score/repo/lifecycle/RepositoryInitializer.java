package org.oagi.score.repo.lifecycle;

import org.jooq.DSLContext;
import org.jooq.JSON;
import org.jooq.types.UInteger;
import org.jooq.types.ULong;
import org.oagi.score.service.log.model.LogAction;
import org.oagi.score.gateway.http.helper.ScoreGuid;
import org.oagi.score.service.log.LogRepository;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.*;
import org.oagi.score.service.log.model.LogSerializer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.and;
import static org.jooq.impl.DSL.or;
import static org.oagi.score.gateway.http.helper.Utility.MODULE_SEPARATOR;
import static org.oagi.score.service.log.model.LogUtils.generateHash;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;

@Component
public class RepositoryInitializer implements InitializingBean {

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private LogRepository logRepository;

    @Autowired
    private LogSerializer serializer;

    @Override
    public void afterPropertiesSet() throws Exception {
        initCodeListValueGuid();
        initAgencyIdListValueGuid();
        initSeqKey();

        initAccLog();
        initAsccpLog();
        initBccpLog();

        initCodeListLog();
        initAgencyIdListLog();
        initDtLog();

        initXbtLog();
    }

    private void initCodeListValueGuid() {
        List<CodeListValueRecord> codeListValueRecords = dslContext.selectFrom(CODE_LIST_VALUE)
                .where(or(
                        CODE_LIST_VALUE.GUID.isNull(),
                        CODE_LIST_VALUE.GUID.eq("")
                ))
                .fetch();

        codeListValueRecords.stream()
                .filter(e -> e.getPrevCodeListValueId() == null)
                .forEach(e -> {
                    e.setGuid(ScoreGuid.randomGuid());
                    e.update(CODE_LIST_VALUE.GUID);
                });

        Map<ULong, CodeListValueRecord> codeListValueRecordMapById = codeListValueRecords.stream()
                .collect(Collectors.toMap(CodeListValueRecord::getCodeListValueId, Function.identity()));

        codeListValueRecords.stream()
                .filter(e -> e.getPrevCodeListValueId() != null)
                .forEach(e -> {
                    CodeListValueRecord prevCodeListValueRecord =
                            codeListValueRecordMapById.get(e.getPrevCodeListValueId());
                    e.setGuid(prevCodeListValueRecord.getGuid());
                    e.update(CODE_LIST_VALUE.GUID);
                });
    }

    private void initAgencyIdListValueGuid() {
        List<AgencyIdListValueRecord> agencyIdListValueRecords = dslContext.selectFrom(AGENCY_ID_LIST_VALUE)
                .where(or(
                        AGENCY_ID_LIST_VALUE.GUID.isNull(),
                        AGENCY_ID_LIST_VALUE.GUID.eq("")
                ))
                .fetch();

        agencyIdListValueRecords.stream()
                .filter(e -> e.getPrevAgencyIdListValueId() == null)
                .forEach(e -> {
                    e.setGuid(ScoreGuid.randomGuid());
                    e.update(AGENCY_ID_LIST_VALUE.GUID);
                });

        Map<ULong, AgencyIdListValueRecord> agencyIdListValueRecordMapById = agencyIdListValueRecords.stream()
                .collect(Collectors.toMap(AgencyIdListValueRecord::getAgencyIdListValueId, Function.identity()));

        agencyIdListValueRecords.stream()
                .filter(e -> e.getPrevAgencyIdListValueId() != null)
                .forEach(e -> {
                    AgencyIdListValueRecord prevAgencyIdListValueRecord =
                            agencyIdListValueRecordMapById.get(e.getPrevAgencyIdListValueId());
                    e.setGuid(prevAgencyIdListValueRecord.getGuid());
                    e.update(AGENCY_ID_LIST_VALUE.GUID);
                });
    }

    private void initSeqKey() {
        Set<ULong> distinctFromAccManifestIds = new HashSet();
        distinctFromAccManifestIds.addAll(dslContext.selectDistinct(ASCC_MANIFEST.FROM_ACC_MANIFEST_ID)
                .from(ASCC_MANIFEST)
                .where(ASCC_MANIFEST.SEQ_KEY_ID.isNull())
                .fetch(ASCC_MANIFEST.FROM_ACC_MANIFEST_ID));
        distinctFromAccManifestIds.addAll(dslContext.selectDistinct(BCC_MANIFEST.FROM_ACC_MANIFEST_ID)
                .from(BCC_MANIFEST)
                .where(BCC_MANIFEST.SEQ_KEY_ID.isNull())
                .fetch(BCC_MANIFEST.FROM_ACC_MANIFEST_ID));

        List<ULong> fromAccManifestIds = new ArrayList(distinctFromAccManifestIds);
        Collections.sort(fromAccManifestIds);

        for (ULong fromAccManifestId : fromAccManifestIds) {
            upsertSeqKey(fromAccManifestId);
        }
    }

    private void upsertSeqKey(ULong fromAccManifestId) {
        // cleaning data up at first.
        dslContext.deleteFrom(SEQ_KEY)
                .where(SEQ_KEY.FROM_ACC_MANIFEST_ID.eq(fromAccManifestId))
                .execute();

        List<SeqKeyWrapper> seqKeyWrappers = new ArrayList();

        seqKeyWrappers.addAll(new ArrayList<>(dslContext.select(ASCC_MANIFEST.ASCC_MANIFEST_ID, ASCC.ASCC_ID, ASCC.SEQ_KEY, ASCC.CREATION_TIMESTAMP)
                .from(ASCC)
                .join(ASCC_MANIFEST).on(ASCC.ASCC_ID.eq(ASCC_MANIFEST.ASCC_ID))
                .where(ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(fromAccManifestId))
                .fetch(e -> new SeqKeyWrapper(
                        e.get(ASCC_MANIFEST.ASCC_MANIFEST_ID),
                        null,
                        e.get(ASCC.SEQ_KEY),
                        e.get(ASCC.CREATION_TIMESTAMP)))));
        seqKeyWrappers.addAll(new ArrayList<>(dslContext.select(BCC_MANIFEST.BCC_MANIFEST_ID, BCC.BCC_ID, BCC.SEQ_KEY, BCC.CREATION_TIMESTAMP)
                .from(BCC)
                .join(BCC_MANIFEST).on(BCC.BCC_ID.eq(BCC_MANIFEST.BCC_ID))
                .where(BCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(fromAccManifestId))
                .fetch(e -> new SeqKeyWrapper(
                        null,
                        e.get(BCC_MANIFEST.BCC_MANIFEST_ID),
                        e.get(BCC.SEQ_KEY),
                        e.get(BCC.CREATION_TIMESTAMP)))));

        Collections.sort(seqKeyWrappers, (o1, o2) -> {
            if (o1.getSeqKey() == o2.getSeqKey()) {
                return o1.getTimestamp().compareTo(o2.getTimestamp());
            }
            return Integer.compare(o1.getSeqKey(), o2.getSeqKey());
        });

        for (int i = 0, len = seqKeyWrappers.size(); i < len; ++i) {
            SeqKeyWrapper seqKeyWrapper = seqKeyWrappers.get(i);

            SeqKeyRecord seqKeyRecord = new SeqKeyRecord();
            seqKeyRecord.setFromAccManifestId(fromAccManifestId);
            seqKeyRecord.setAsccManifestId(seqKeyWrapper.getAsccManifestId());
            seqKeyRecord.setBccManifestId(seqKeyWrapper.getBccManifestId());
            seqKeyWrapper.setSeqKeyRecord(seqKeyRecord);
            seqKeyWrapper.setPrev(
                    (i == 0) ? null : seqKeyWrappers.get(i - 1)
            );
            seqKeyWrapper.setNext(
                    (i + 1 == len) ? null : seqKeyWrappers.get(i + 1)
            );

            seqKeyRecord.setSeqKeyId(
                    dslContext.insertInto(SEQ_KEY)
                            .set(seqKeyRecord)
                            .returning(SEQ_KEY.SEQ_KEY_ID)
                            .fetchOne().getSeqKeyId()
            );
        }

        for (SeqKeyWrapper seqKeyWrapper : seqKeyWrappers) {
            seqKeyWrapper.updatePrevNext();
        }
    }

    private class SeqKeyWrapper {
        private final int seqKey;
        private final LocalDateTime timestamp;
        private final ULong asccManifestId;
        private final ULong bccManifestId;

        private SeqKeyRecord seqKeyRecord;

        private SeqKeyWrapper prev;
        private SeqKeyWrapper next;

        SeqKeyWrapper(ULong asccManifestId, ULong bccManifestId, int seqKey, LocalDateTime timestamp) {
            this.seqKey = seqKey;
            this.timestamp = timestamp;
            this.asccManifestId = asccManifestId;
            this.bccManifestId = bccManifestId;
        }

        public int getSeqKey() {
            return seqKey;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public ULong getAsccManifestId() {
            return asccManifestId;
        }

        public ULong getBccManifestId() {
            return bccManifestId;
        }

        public void setSeqKeyRecord(SeqKeyRecord seqKeyRecord) {
            this.seqKeyRecord = seqKeyRecord;
        }

        public void setPrev(SeqKeyWrapper prev) {
            this.prev = prev;
        }

        public void setNext(SeqKeyWrapper next) {
            this.next = next;
        }

        public void updatePrevNext() {
            dslContext.update(SEQ_KEY)
                    .set(SEQ_KEY.PREV_SEQ_KEY_ID, (prev != null) ? prev.seqKeyRecord.getSeqKeyId() : null)
                    .set(SEQ_KEY.NEXT_SEQ_KEY_ID, (next != null) ? next.seqKeyRecord.getSeqKeyId() : null)
                    .where(SEQ_KEY.SEQ_KEY_ID.eq(seqKeyRecord.getSeqKeyId()))
                    .execute();

            if (this.asccManifestId != null) {
                dslContext.update(ASCC_MANIFEST)
                        .set(ASCC_MANIFEST.SEQ_KEY_ID, this.seqKeyRecord.getSeqKeyId())
                        .where(ASCC_MANIFEST.ASCC_MANIFEST_ID.eq(this.asccManifestId))
                        .execute();
            } else {
                dslContext.update(BCC_MANIFEST)
                        .set(BCC_MANIFEST.SEQ_KEY_ID, this.seqKeyRecord.getSeqKeyId())
                        .where(BCC_MANIFEST.BCC_MANIFEST_ID.eq(this.bccManifestId))
                        .execute();
            }
        }
    }

    private void initAccLog() {
        // For 'Non-Working' releases.
        List<AccManifestRecord> accManifestRecordList = dslContext.select(ACC_MANIFEST.fields())
                .from(ACC_MANIFEST).join(RELEASE).on(ACC_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .where(and(
                        RELEASE.RELEASE_NUM.notEqual("Working"),
                        ACC_MANIFEST.LOG_ID.isNull()
                ))
                .fetchInto(AccManifestRecord.class);

        for (AccManifestRecord accManifestRecord : accManifestRecordList) {
            AccRecord accRecord = dslContext.selectFrom(ACC)
                    .where(ACC.ACC_ID.eq(accManifestRecord.getAccId()))
                    .fetchOne();

            List<AsccManifestRecord> asccManifestRecords = dslContext.selectFrom(ASCC_MANIFEST)
                    .where(ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(accManifestRecord.getAccManifestId()))
                    .fetch();

            List<BccManifestRecord> bccManifestRecords = dslContext.selectFrom(BCC_MANIFEST)
                    .where(BCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(accManifestRecord.getAccManifestId()))
                    .fetch();

            List<AsccRecord> asccRecords = dslContext.selectFrom(ASCC)
                    .where(ASCC.FROM_ACC_ID.eq(accRecord.getAccId()))
                    .fetch();

            List<BccRecord> bccRecords = dslContext.selectFrom(BCC)
                    .where(BCC.FROM_ACC_ID.eq(accRecord.getAccId()))
                    .fetch();

            List<SeqKeyRecord> seqKeyRecords = dslContext.selectFrom(SEQ_KEY)
                    .where(SEQ_KEY.FROM_ACC_MANIFEST_ID.eq(accManifestRecord.getAccManifestId()))
                    .fetch();

            LogRecord logRecord = new LogRecord();
            logRecord.setHash(generateHash());
            logRecord.setReference(accRecord.getGuid());
            logRecord.setRevisionNum(UInteger.valueOf(1));
            logRecord.setRevisionTrackingNum(UInteger.valueOf(1));
            logRecord.setLogAction(LogAction.Added.name());
            logRecord.setSnapshot(JSON.valueOf(
                    serializer.serialize(accManifestRecord, accRecord,
                            asccManifestRecords, bccManifestRecords, asccRecords, bccRecords, seqKeyRecords)
            ));
            logRecord.setCreatedBy(accRecord.getCreatedBy());
            logRecord.setCreationTimestamp(accRecord.getCreationTimestamp());

            ULong logId = dslContext.insertInto(LOG)
                    .set(logRecord)
                    .returning(LOG.LOG_ID).fetchOne().getLogId();

            accManifestRecord.setLogId(logId);
            accManifestRecord.update(ACC_MANIFEST.LOG_ID);
        }

        // For 'Working' release.
        accManifestRecordList = dslContext.select(ACC_MANIFEST.fields())
                .from(ACC_MANIFEST).join(RELEASE).on(ACC_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .where(and(
                        RELEASE.RELEASE_NUM.equal("Working"),
                        ACC_MANIFEST.LOG_ID.isNull()
                ))
                .fetchInto(AccManifestRecord.class);

        for (AccManifestRecord nextAccManifestRecord : accManifestRecordList) {
            AccManifestRecord prevAccManifestRecord = dslContext.selectFrom(ACC_MANIFEST)
                    .where(and(
                            ACC_MANIFEST.ACC_ID.equal(nextAccManifestRecord.getAccId()),
                            ACC_MANIFEST.RELEASE_ID.notEqual(nextAccManifestRecord.getReleaseId())
                    ))
                    .fetchOne();

            prevAccManifestRecord.setNextAccManifestId(nextAccManifestRecord.getAccManifestId());
            prevAccManifestRecord.update(ACC_MANIFEST.NEXT_ACC_MANIFEST_ID);

            nextAccManifestRecord.setPrevAccManifestId(prevAccManifestRecord.getAccManifestId());
            nextAccManifestRecord.setLogId(prevAccManifestRecord.getLogId());
            nextAccManifestRecord.update(ACC_MANIFEST.PREV_ACC_MANIFEST_ID, ACC_MANIFEST.LOG_ID);

            // update prev/next ascc_manifest_id
            for (AsccManifestRecord prevAsccManifestRecord :
                    dslContext.select(ASCC_MANIFEST.fields())
                            .from(ASCC_MANIFEST)
                            .where(ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(prevAccManifestRecord.getAccManifestId()))
                            .fetchInto(AsccManifestRecord.class)) {

                AsccManifestRecord nextAsccManifestRecord = dslContext.selectFrom(ASCC_MANIFEST)
                        .where(and(
                                ASCC_MANIFEST.RELEASE_ID.notEqual(
                                        prevAsccManifestRecord.getReleaseId()),
                                ASCC_MANIFEST.ASCC_ID.equal(
                                        prevAsccManifestRecord.getAsccId())
                        ))
                        .fetchOne();

                prevAsccManifestRecord.setNextAsccManifestId(
                        nextAsccManifestRecord.getAsccManifestId());
                prevAsccManifestRecord.update(ASCC_MANIFEST.NEXT_ASCC_MANIFEST_ID);

                nextAsccManifestRecord.setPrevAsccManifestId(
                        prevAsccManifestRecord.getAsccManifestId());
                nextAsccManifestRecord.update(ASCC_MANIFEST.PREV_ASCC_MANIFEST_ID);
            }

            // update prev/next bcc_manifest_id
            for (BccManifestRecord prevBccManifestRecord :
                    dslContext.select(BCC_MANIFEST.fields())
                            .from(BCC_MANIFEST)
                            .where(BCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(prevAccManifestRecord.getAccManifestId()))
                            .fetchInto(BccManifestRecord.class)) {

                BccManifestRecord nextBccManifestRecord = dslContext.selectFrom(BCC_MANIFEST)
                        .where(and(
                                BCC_MANIFEST.RELEASE_ID.notEqual(
                                        prevBccManifestRecord.getReleaseId()),
                                BCC_MANIFEST.BCC_ID.equal(
                                        prevBccManifestRecord.getBccId())
                        ))
                        .fetchOne();

                prevBccManifestRecord.setNextBccManifestId(
                        nextBccManifestRecord.getBccManifestId());
                prevBccManifestRecord.update(BCC_MANIFEST.NEXT_BCC_MANIFEST_ID);

                nextBccManifestRecord.setPrevBccManifestId(
                        prevBccManifestRecord.getBccManifestId());
                nextBccManifestRecord.update(BCC_MANIFEST.PREV_BCC_MANIFEST_ID);
            }
        }
    }

    private void initAsccpLog() {
        // For 'Non-Working' releases.
        List<AsccpManifestRecord> asccpManifestRecordList = dslContext.select(ASCCP_MANIFEST.fields())
                .from(ASCCP_MANIFEST).join(RELEASE).on(ASCCP_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .where(and(
                        RELEASE.RELEASE_NUM.notEqual("Working"),
                        ASCCP_MANIFEST.LOG_ID.isNull()
                ))
                .fetchInto(AsccpManifestRecord.class);

        for (AsccpManifestRecord asccpManifestRecord : asccpManifestRecordList) {
            AsccpRecord asccpRecord = dslContext.selectFrom(ASCCP)
                    .where(ASCCP.ASCCP_ID.eq(asccpManifestRecord.getAsccpId()))
                    .fetchOne();

            LogRecord logRecord = new LogRecord();
            logRecord.setHash(generateHash());
            logRecord.setReference(asccpRecord.getGuid());
            logRecord.setRevisionNum(UInteger.valueOf(1));
            logRecord.setRevisionTrackingNum(UInteger.valueOf(1));
            logRecord.setLogAction(LogAction.Added.name());
            logRecord.setSnapshot(JSON.valueOf(serializer.serialize(asccpManifestRecord, asccpRecord)));
            logRecord.setCreatedBy(asccpRecord.getCreatedBy());
            logRecord.setCreationTimestamp(asccpRecord.getCreationTimestamp());

            ULong logId = dslContext.insertInto(LOG)
                    .set(logRecord)
                    .returning(LOG.LOG_ID).fetchOne().getLogId();

            asccpManifestRecord.setLogId(logId);
            asccpManifestRecord.update(ASCCP_MANIFEST.LOG_ID);
        }

        // For 'Working' release.
        asccpManifestRecordList = dslContext.select(ASCCP_MANIFEST.fields())
                .from(ASCCP_MANIFEST).join(RELEASE).on(ASCCP_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .where(and(
                        RELEASE.RELEASE_NUM.equal("Working"),
                        ASCCP_MANIFEST.LOG_ID.isNull()
                ))
                .fetchInto(AsccpManifestRecord.class);

        for (AsccpManifestRecord nextAsccpManifestRecord : asccpManifestRecordList) {
            AsccpManifestRecord prevAsccpManifestRecord = dslContext.selectFrom(ASCCP_MANIFEST)
                    .where(and(
                            ASCCP_MANIFEST.ASCCP_ID.equal(nextAsccpManifestRecord.getAsccpId()),
                            ASCCP_MANIFEST.RELEASE_ID.notEqual(nextAsccpManifestRecord.getReleaseId())
                    ))
                    .fetchOne();

            prevAsccpManifestRecord.setNextAsccpManifestId(nextAsccpManifestRecord.getAsccpManifestId());
            prevAsccpManifestRecord.update(ASCCP_MANIFEST.NEXT_ASCCP_MANIFEST_ID);

            nextAsccpManifestRecord.setPrevAsccpManifestId(prevAsccpManifestRecord.getAsccpManifestId());
            nextAsccpManifestRecord.setLogId(prevAsccpManifestRecord.getLogId());
            nextAsccpManifestRecord.update(ASCCP_MANIFEST.PREV_ASCCP_MANIFEST_ID, ASCCP_MANIFEST.LOG_ID);
        }
    }

    private void initBccpLog() {
        // For 'Non-Working' releases.
        List<BccpManifestRecord> bccpManifestRecordList = dslContext.select(BCCP_MANIFEST.fields())
                .from(BCCP_MANIFEST).join(RELEASE).on(BCCP_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .where(and(
                        RELEASE.RELEASE_NUM.notEqual("Working"),
                        BCCP_MANIFEST.LOG_ID.isNull()
                ))
                .fetchInto(BccpManifestRecord.class);

        for (BccpManifestRecord bccpManifestRecord : bccpManifestRecordList) {
            BccpRecord bccpRecord = dslContext.selectFrom(BCCP)
                    .where(BCCP.BCCP_ID.eq(bccpManifestRecord.getBccpId()))
                    .fetchOne();

            LogRecord logRecord = new LogRecord();
            logRecord.setHash(generateHash());
            logRecord.setReference(bccpRecord.getGuid());
            logRecord.setRevisionNum(UInteger.valueOf(1));
            logRecord.setRevisionTrackingNum(UInteger.valueOf(1));
            logRecord.setLogAction(LogAction.Added.name());
            logRecord.setSnapshot(JSON.valueOf(serializer.serialize(bccpManifestRecord, bccpRecord)));
            logRecord.setCreatedBy(bccpRecord.getCreatedBy());
            logRecord.setCreationTimestamp(bccpRecord.getCreationTimestamp());

            ULong logId = dslContext.insertInto(LOG)
                    .set(logRecord)
                    .returning(LOG.LOG_ID).fetchOne().getLogId();

            bccpManifestRecord.setLogId(logId);
            bccpManifestRecord.update(BCCP_MANIFEST.LOG_ID);
        }

        // For 'Working' release.
        bccpManifestRecordList = dslContext.select(BCCP_MANIFEST.fields())
                .from(BCCP_MANIFEST).join(RELEASE).on(BCCP_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .where(and(
                        RELEASE.RELEASE_NUM.equal("Working"),
                        BCCP_MANIFEST.LOG_ID.isNull()
                ))
                .fetchInto(BccpManifestRecord.class);

        for (BccpManifestRecord nextBccpManifestRecord : bccpManifestRecordList) {
            BccpManifestRecord prevBccpManifestRecord = dslContext.selectFrom(BCCP_MANIFEST)
                    .where(and(
                            BCCP_MANIFEST.BCCP_ID.equal(nextBccpManifestRecord.getBccpId()),
                            BCCP_MANIFEST.RELEASE_ID.notEqual(nextBccpManifestRecord.getReleaseId())
                    ))
                    .fetchOne();

            prevBccpManifestRecord.setNextBccpManifestId(nextBccpManifestRecord.getBccpManifestId());
            prevBccpManifestRecord.update(BCCP_MANIFEST.NEXT_BCCP_MANIFEST_ID);

            nextBccpManifestRecord.setPrevBccpManifestId(prevBccpManifestRecord.getBccpManifestId());
            nextBccpManifestRecord.setLogId(prevBccpManifestRecord.getLogId());
            nextBccpManifestRecord.update(BCCP_MANIFEST.PREV_BCCP_MANIFEST_ID, BCCP_MANIFEST.LOG_ID);
        }
    }

    private void initCodeListLog() {
        // For 'Non-Working' releases.
        List<CodeListManifestRecord> codeListManifestRecordList = dslContext.select(CODE_LIST_MANIFEST.fields())
                .from(CODE_LIST_MANIFEST).join(RELEASE).on(CODE_LIST_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .where(and(
                        RELEASE.RELEASE_NUM.notEqual("Working"),
                        CODE_LIST_MANIFEST.LOG_ID.isNull()
                ))
                .fetchInto(CodeListManifestRecord.class);

        for (CodeListManifestRecord codeListManifestRecord : codeListManifestRecordList) {
            CodeListRecord codeListRecord = dslContext.selectFrom(CODE_LIST)
                    .where(CODE_LIST.CODE_LIST_ID.eq(codeListManifestRecord.getCodeListId()))
                    .fetchOne();

            List<CodeListValueManifestRecord> codeListValueManifestRecords = dslContext.selectFrom(CODE_LIST_VALUE_MANIFEST)
                    .where(CODE_LIST_VALUE_MANIFEST.CODE_LIST_MANIFEST_ID.eq(codeListManifestRecord.getCodeListManifestId()))
                    .fetch();

            List<CodeListValueRecord> codeListValueRecords = dslContext.selectFrom(CODE_LIST_VALUE)
                    .where(CODE_LIST_VALUE.CODE_LIST_VALUE_ID.in(
                            codeListValueManifestRecords.stream().map(e -> e.getCodeListValueId()).collect(Collectors.toList())
                    ))
                    .fetch();

            LogRecord logRecord = new LogRecord();
            logRecord.setHash(generateHash());
            logRecord.setReference(codeListRecord.getGuid());
            logRecord.setRevisionNum(UInteger.valueOf(1));
            logRecord.setRevisionTrackingNum(UInteger.valueOf(1));
            logRecord.setLogAction(LogAction.Added.name());
            logRecord.setSnapshot(JSON.valueOf(
                    serializer.serialize(codeListManifestRecord, codeListRecord,
                            codeListValueManifestRecords, codeListValueRecords)
            ));
            logRecord.setCreatedBy(codeListRecord.getCreatedBy());
            logRecord.setCreationTimestamp(codeListRecord.getCreationTimestamp());

            ULong logId = dslContext.insertInto(LOG)
                    .set(logRecord)
                    .returning(LOG.LOG_ID).fetchOne().getLogId();

            codeListManifestRecord.setLogId(logId);
            codeListManifestRecord.update(CODE_LIST_MANIFEST.LOG_ID);
        }

        // For 'Working' release.
        codeListManifestRecordList = dslContext.select(CODE_LIST_MANIFEST.fields())
                .from(CODE_LIST_MANIFEST).join(RELEASE).on(CODE_LIST_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .where(and(
                        RELEASE.RELEASE_NUM.equal("Working"),
                        CODE_LIST_MANIFEST.LOG_ID.isNull()
                ))
                .fetchInto(CodeListManifestRecord.class);

        for (CodeListManifestRecord nextCodeListManifestRecord : codeListManifestRecordList) {
            CodeListManifestRecord prevCodeListManifestRecord = dslContext.selectFrom(CODE_LIST_MANIFEST)
                    .where(and(
                            CODE_LIST_MANIFEST.CODE_LIST_ID.equal(nextCodeListManifestRecord.getCodeListId()),
                            CODE_LIST_MANIFEST.RELEASE_ID.notEqual(nextCodeListManifestRecord.getReleaseId())
                    ))
                    .fetchOne();

            prevCodeListManifestRecord.setNextCodeListManifestId(nextCodeListManifestRecord.getCodeListManifestId());
            prevCodeListManifestRecord.update(CODE_LIST_MANIFEST.NEXT_CODE_LIST_MANIFEST_ID);

            nextCodeListManifestRecord.setPrevCodeListManifestId(prevCodeListManifestRecord.getCodeListManifestId());
            nextCodeListManifestRecord.setLogId(prevCodeListManifestRecord.getLogId());
            nextCodeListManifestRecord.update(CODE_LIST_MANIFEST.PREV_CODE_LIST_MANIFEST_ID, CODE_LIST_MANIFEST.LOG_ID);

            // update prev/next code_list_value_manifest_id
            for (CodeListValueManifestRecord prevCodeListValueManifestRecord :
                    dslContext.select(CODE_LIST_VALUE_MANIFEST.fields())
                            .from(CODE_LIST_VALUE_MANIFEST)
                            .join(RELEASE).on(CODE_LIST_VALUE_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                            .where(and(
                                    RELEASE.RELEASE_NUM.notEqual("Working"),
                                    CODE_LIST_VALUE_MANIFEST.CODE_LIST_MANIFEST_ID.eq(prevCodeListManifestRecord.getCodeListManifestId())
                            ))
                            .fetchInto(CodeListValueManifestRecord.class)) {

                CodeListValueManifestRecord nextCodeListValueManifestRecord = dslContext.selectFrom(CODE_LIST_VALUE_MANIFEST)
                        .where(and(
                                CODE_LIST_VALUE_MANIFEST.RELEASE_ID.notEqual(
                                        prevCodeListValueManifestRecord.getReleaseId()),
                                CODE_LIST_VALUE_MANIFEST.CODE_LIST_VALUE_ID.equal(
                                        prevCodeListValueManifestRecord.getCodeListValueId())
                        ))
                        .fetchOne();

                prevCodeListValueManifestRecord.setNextCodeListValueManifestId(
                        nextCodeListValueManifestRecord.getCodeListValueManifestId());
                prevCodeListValueManifestRecord.update(CODE_LIST_VALUE_MANIFEST.NEXT_CODE_LIST_VALUE_MANIFEST_ID);

                nextCodeListValueManifestRecord.setPrevCodeListValueManifestId(
                        prevCodeListValueManifestRecord.getCodeListValueManifestId());
                nextCodeListValueManifestRecord.update(CODE_LIST_VALUE_MANIFEST.PREV_CODE_LIST_VALUE_MANIFEST_ID);
            }
        }
    }

    private void initAgencyIdListLog() {
        // For 'Non-Working' releases.
        List<AgencyIdListManifestRecord> agencyIdListManifestRecordList = dslContext.select(AGENCY_ID_LIST_MANIFEST.fields())
                .from(AGENCY_ID_LIST_MANIFEST).join(RELEASE).on(AGENCY_ID_LIST_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .where(and(
                        RELEASE.RELEASE_NUM.notEqual("Working"),
                        AGENCY_ID_LIST_MANIFEST.LOG_ID.isNull()
                ))
                .fetchInto(AgencyIdListManifestRecord.class);

        for (AgencyIdListManifestRecord agencyIdListManifestRecord : agencyIdListManifestRecordList) {
            AgencyIdListRecord agencyIdListRecord = dslContext.selectFrom(AGENCY_ID_LIST)
                    .where(AGENCY_ID_LIST.AGENCY_ID_LIST_ID.eq(agencyIdListManifestRecord.getAgencyIdListId()))
                    .fetchOne();

            List<AgencyIdListValueManifestRecord> agencyIdListValueManifestRecords = dslContext.selectFrom(AGENCY_ID_LIST_VALUE_MANIFEST)
                    .where(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID.eq(agencyIdListManifestRecord.getAgencyIdListManifestId()))
                    .fetch();

            List<AgencyIdListValueRecord> agencyIdListValueRecords = dslContext.selectFrom(AGENCY_ID_LIST_VALUE)
                    .where(AGENCY_ID_LIST_VALUE.AGENCY_ID_LIST_VALUE_ID.in(
                            agencyIdListValueManifestRecords.stream().map(e -> e.getAgencyIdListValueId()).collect(Collectors.toList())
                    ))
                    .fetch();

            LogRecord logRecord = new LogRecord();
            logRecord.setHash(generateHash());
            logRecord.setReference(agencyIdListRecord.getGuid());
            logRecord.setRevisionNum(UInteger.valueOf(1));
            logRecord.setRevisionTrackingNum(UInteger.valueOf(1));
            logRecord.setLogAction(LogAction.Added.name());
            logRecord.setSnapshot(JSON.valueOf(
                    serializer.serialize(agencyIdListManifestRecord, agencyIdListRecord,
                            agencyIdListValueManifestRecords, agencyIdListValueRecords)
            ));
            logRecord.setCreatedBy(agencyIdListRecord.getCreatedBy());
            logRecord.setCreationTimestamp(agencyIdListRecord.getCreationTimestamp());

            ULong logId = dslContext.insertInto(LOG)
                    .set(logRecord)
                    .returning(LOG.LOG_ID).fetchOne().getLogId();

            agencyIdListManifestRecord.setLogId(logId);
            agencyIdListManifestRecord.update(AGENCY_ID_LIST_MANIFEST.LOG_ID);
        }

        // For 'Working' release.
        agencyIdListManifestRecordList = dslContext.select(AGENCY_ID_LIST_MANIFEST.fields())
                .from(AGENCY_ID_LIST_MANIFEST).join(RELEASE).on(AGENCY_ID_LIST_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .where(and(
                        RELEASE.RELEASE_NUM.equal("Working"),
                        AGENCY_ID_LIST_MANIFEST.LOG_ID.isNull()
                ))
                .fetchInto(AgencyIdListManifestRecord.class);

        for (AgencyIdListManifestRecord nextAgencyIdListManifestRecord : agencyIdListManifestRecordList) {
            AgencyIdListManifestRecord prevAgencyIdListManifestRecord = dslContext.selectFrom(AGENCY_ID_LIST_MANIFEST)
                    .where(and(
                            AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID.equal(nextAgencyIdListManifestRecord.getAgencyIdListId()),
                            AGENCY_ID_LIST_MANIFEST.RELEASE_ID.notEqual(nextAgencyIdListManifestRecord.getReleaseId())
                    ))
                    .fetchOne();

            prevAgencyIdListManifestRecord.setNextAgencyIdListManifestId(nextAgencyIdListManifestRecord.getAgencyIdListManifestId());
            prevAgencyIdListManifestRecord.update(AGENCY_ID_LIST_MANIFEST.NEXT_AGENCY_ID_LIST_MANIFEST_ID);

            nextAgencyIdListManifestRecord.setPrevAgencyIdListManifestId(prevAgencyIdListManifestRecord.getAgencyIdListManifestId());
            nextAgencyIdListManifestRecord.setLogId(prevAgencyIdListManifestRecord.getLogId());
            nextAgencyIdListManifestRecord.update(AGENCY_ID_LIST_MANIFEST.PREV_AGENCY_ID_LIST_MANIFEST_ID, AGENCY_ID_LIST_MANIFEST.LOG_ID);

            // update prev/next code_list_value_manifest_id
            for (AgencyIdListValueManifestRecord prevAgencyIdListValueManifestRecord :
                    dslContext.select(AGENCY_ID_LIST_VALUE_MANIFEST.fields())
                            .from(AGENCY_ID_LIST_VALUE_MANIFEST)
                            .join(RELEASE).on(AGENCY_ID_LIST_VALUE_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                            .where(and(
                                    RELEASE.RELEASE_NUM.notEqual("Working"),
                                    AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID.eq(prevAgencyIdListManifestRecord.getAgencyIdListManifestId())
                            ))
                            .fetchInto(AgencyIdListValueManifestRecord.class)) {

                AgencyIdListValueManifestRecord nextAgencyIdListValueManifestRecord = dslContext.selectFrom(AGENCY_ID_LIST_VALUE_MANIFEST)
                        .where(and(
                                AGENCY_ID_LIST_VALUE_MANIFEST.RELEASE_ID.notEqual(
                                        prevAgencyIdListValueManifestRecord.getReleaseId()),
                                AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_ID.equal(
                                        prevAgencyIdListValueManifestRecord.getAgencyIdListValueId())
                        ))
                        .fetchOne();

                prevAgencyIdListValueManifestRecord.setNextAgencyIdListValueManifestId(
                        nextAgencyIdListValueManifestRecord.getAgencyIdListValueManifestId());
                prevAgencyIdListValueManifestRecord.update(AGENCY_ID_LIST_VALUE_MANIFEST.NEXT_AGENCY_ID_LIST_VALUE_MANIFEST_ID);

                nextAgencyIdListValueManifestRecord.setPrevAgencyIdListValueManifestId(
                        prevAgencyIdListValueManifestRecord.getAgencyIdListValueManifestId());
                nextAgencyIdListValueManifestRecord.update(AGENCY_ID_LIST_VALUE_MANIFEST.PREV_AGENCY_ID_LIST_VALUE_MANIFEST_ID);
            }
        }
    }

    private void initDtLog() {
        // For 'Non-Working' releases.
        List<DtManifestRecord> dtManifestRecordList = dslContext.select(DT_MANIFEST.fields())
                .from(DT_MANIFEST).join(RELEASE).on(DT_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .where(and(
                        RELEASE.RELEASE_NUM.notEqual("Working"),
                        DT_MANIFEST.LOG_ID.isNull()
                ))
                .fetchInto(DtManifestRecord.class);

        for (DtManifestRecord dtManifestRecord : dtManifestRecordList) {
            DtRecord dtRecord = dslContext.selectFrom(DT)
                    .where(DT.DT_ID.eq(dtManifestRecord.getDtId()))
                    .fetchOne();

            List<DtScManifestRecord> dtScManifestRecords = dslContext.selectFrom(DT_SC_MANIFEST)
                    .where(DT_SC_MANIFEST.OWNER_DT_MANIFEST_ID.eq(dtManifestRecord.getDtManifestId()))
                    .fetch();

            List<DtScRecord> dtScRecords = dslContext.selectFrom(DT_SC)
                    .where(DT_SC.DT_SC_ID.in(
                            dtScManifestRecords.stream().map(e -> e.getDtScId()).collect(Collectors.toList())
                    ))
                    .fetch();

            LogRecord logRecord = new LogRecord();
            logRecord.setHash(generateHash());
            logRecord.setReference(dtRecord.getGuid());
            logRecord.setRevisionNum(UInteger.valueOf(1));
            logRecord.setRevisionTrackingNum(UInteger.valueOf(1));
            logRecord.setLogAction(LogAction.Added.name());
            logRecord.setSnapshot(
                    JSON.valueOf(serializer.serialize(dtManifestRecord, dtRecord,
                            dtScManifestRecords, dtScRecords))
            );
            logRecord.setCreatedBy(dtRecord.getCreatedBy());
            logRecord.setCreationTimestamp(dtRecord.getCreationTimestamp());

            ULong logId = dslContext.insertInto(LOG)
                    .set(logRecord)
                    .returning(LOG.LOG_ID).fetchOne().getLogId();

            dtManifestRecord.setLogId(logId);
            dtManifestRecord.update(DT_MANIFEST.LOG_ID);
        }

        // For 'Working' release.
        dtManifestRecordList = dslContext.select(DT_MANIFEST.fields())
                .from(DT_MANIFEST).join(RELEASE).on(DT_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .where(and(
                        RELEASE.RELEASE_NUM.equal("Working"),
                        DT_MANIFEST.LOG_ID.isNull()
                ))
                .fetchInto(DtManifestRecord.class);

        for (DtManifestRecord nextDtManifestRecord : dtManifestRecordList) {
            DtManifestRecord prevDtManifestRecord = dslContext.selectFrom(DT_MANIFEST)
                    .where(and(
                            DT_MANIFEST.DT_ID.equal(nextDtManifestRecord.getDtId()),
                            DT_MANIFEST.RELEASE_ID.notEqual(nextDtManifestRecord.getReleaseId())
                    ))
                    .fetchOne();

            prevDtManifestRecord.setNextDtManifestId(nextDtManifestRecord.getDtManifestId());
            prevDtManifestRecord.update(DT_MANIFEST.NEXT_DT_MANIFEST_ID);

            nextDtManifestRecord.setPrevDtManifestId(prevDtManifestRecord.getDtManifestId());
            nextDtManifestRecord.setLogId(prevDtManifestRecord.getLogId());
            nextDtManifestRecord.update(DT_MANIFEST.PREV_DT_MANIFEST_ID, DT_MANIFEST.LOG_ID);

            // update prev/next code_list_value_manifest_id
            for (DtScManifestRecord prevDtScManifestRecord :
                    dslContext.select(DT_SC_MANIFEST.fields())
                            .from(DT_SC_MANIFEST)
                            .join(RELEASE).on(DT_SC_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                            .where(and(
                                    RELEASE.RELEASE_NUM.notEqual("Working"),
                                    DT_SC_MANIFEST.OWNER_DT_MANIFEST_ID.eq(prevDtManifestRecord.getDtManifestId())
                            ))
                            .fetchInto(DtScManifestRecord.class)) {

                DtScManifestRecord nextDtScManifestRecord = dslContext.selectFrom(DT_SC_MANIFEST)
                        .where(and(
                                DT_SC_MANIFEST.RELEASE_ID.notEqual(
                                        prevDtScManifestRecord.getReleaseId()),
                                DT_SC_MANIFEST.DT_SC_ID.equal(
                                        prevDtScManifestRecord.getDtScId())
                        ))
                        .fetchOne();

                prevDtScManifestRecord.setNextDtScManifestId(
                        nextDtScManifestRecord.getDtScManifestId());
                prevDtScManifestRecord.update(DT_SC_MANIFEST.NEXT_DT_SC_MANIFEST_ID);

                nextDtScManifestRecord.setPrevDtScManifestId(
                        prevDtScManifestRecord.getDtScManifestId());
                nextDtScManifestRecord.update(DT_SC_MANIFEST.PREV_DT_SC_MANIFEST_ID);
            }
        }
    }

    private void initXbtLog() {
        // For 'Non-Working' releases.
        List<XbtManifestRecord> xbtManifestRecordList = dslContext.select(XBT_MANIFEST.fields())
                .from(XBT_MANIFEST).join(RELEASE).on(XBT_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .where(and(
                        RELEASE.RELEASE_NUM.notEqual("Working"),
                        XBT_MANIFEST.LOG_ID.isNull()
                ))
                .fetchInto(XbtManifestRecord.class);

        for (XbtManifestRecord xbtManifestRecord : xbtManifestRecordList) {
            XbtRecord xbtRecord = dslContext.selectFrom(XBT)
                    .where(XBT.XBT_ID.eq(xbtManifestRecord.getXbtId()))
                    .fetchOne();

            LogRecord logRecord = new LogRecord();
            logRecord.setHash(generateHash());
            logRecord.setReference(xbtRecord.getGuid());
            logRecord.setRevisionNum(UInteger.valueOf(1));
            logRecord.setRevisionTrackingNum(UInteger.valueOf(1));
            logRecord.setLogAction(LogAction.Added.name());
            logRecord.setSnapshot(JSON.valueOf(serializer.serialize(xbtManifestRecord, xbtRecord)));
            logRecord.setCreatedBy(xbtRecord.getCreatedBy());
            logRecord.setCreationTimestamp(xbtRecord.getCreationTimestamp());

            ULong logId = dslContext.insertInto(LOG)
                    .set(logRecord)
                    .returning(LOG.LOG_ID).fetchOne().getLogId();

            xbtManifestRecord.setLogId(logId);
            xbtManifestRecord.update(XBT_MANIFEST.LOG_ID);
        }

        // For 'Working' release.
        xbtManifestRecordList = dslContext.select(XBT_MANIFEST.fields())
                .from(XBT_MANIFEST).join(RELEASE).on(XBT_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .where(and(
                        RELEASE.RELEASE_NUM.equal("Working"),
                        XBT_MANIFEST.LOG_ID.isNull()
                ))
                .fetchInto(XbtManifestRecord.class);

        for (XbtManifestRecord nextXbtManifestRecord : xbtManifestRecordList) {
            XbtManifestRecord prevXbtManifestRecord = dslContext.selectFrom(XBT_MANIFEST)
                    .where(and(
                            XBT_MANIFEST.XBT_ID.equal(nextXbtManifestRecord.getXbtId()),
                            XBT_MANIFEST.RELEASE_ID.notEqual(nextXbtManifestRecord.getReleaseId())
                    ))
                    .fetchOne();

            prevXbtManifestRecord.setNextXbtManifestId(nextXbtManifestRecord.getXbtManifestId());
            prevXbtManifestRecord.update(XBT_MANIFEST.NEXT_XBT_MANIFEST_ID);

            nextXbtManifestRecord.setPrevXbtManifestId(prevXbtManifestRecord.getXbtManifestId());
            nextXbtManifestRecord.setLogId(prevXbtManifestRecord.getLogId());
            nextXbtManifestRecord.update(XBT_MANIFEST.PREV_XBT_MANIFEST_ID, XBT_MANIFEST.LOG_ID);
        }
    }
}
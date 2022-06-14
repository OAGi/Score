package org.oagi.score.service.log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.JSON;
import org.jooq.types.UInteger;
import org.jooq.types.ULong;
import org.oagi.score.repo.api.corecomponent.model.CcType;
import org.oagi.score.service.common.data.CcAction;
import org.oagi.score.service.common.data.PageRequest;
import org.oagi.score.service.common.data.PageResponse;
import org.oagi.score.repo.api.base.SortDirection;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.*;
import org.oagi.score.service.log.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.and;
import static org.jooq.impl.DSL.jsonValue;
import static org.oagi.score.repo.api.base.SortDirection.ASC;
import static org.oagi.score.repo.api.base.SortDirection.DESC;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;

@Repository
public class LogRepository {

    private final DSLContext dslContext;

    private final LogSerializer serializer;

    public LogRepository(@Autowired DSLContext dslContext,
                         @Autowired LogSerializer serializer) {
        this.dslContext = dslContext;
        this.serializer = serializer;
    }

    public PageResponse<Log> getLogByReference(LogListRequest request) {
        if (request.getReference().isEmpty()) {
            return null;
        }

        PageRequest pageRequest = request.getPageRequest();
        PageResponse response = new PageResponse<Log>();
        List<Condition> conditions = new ArrayList();
        conditions.add(LOG.REFERENCE.eq(request.getReference()));

        int length = dslContext.selectCount()
                .from(LOG)
                .where(conditions)
                .fetchOptionalInto(Integer.class).orElse(0);

        List<Log> list = dslContext.select(
                LOG.LOG_ID,
                LOG.HASH,
                LOG.REVISION_NUM,
                LOG.REVISION_TRACKING_NUM,
                LOG.LOG_ACTION,
                LOG.PREV_LOG_ID,
                LOG.CREATION_TIMESTAMP.as("timestamp"),
                APP_USER.LOGIN_ID.as("loginId"),
                APP_USER.IS_DEVELOPER
        )
                .from(LOG)
                .join(APP_USER)
                .on(LOG.CREATED_BY.eq(APP_USER.APP_USER_ID))
                .where(conditions)
                .orderBy(LOG.LOG_ID.desc())
                .limit(pageRequest.getOffset(), pageRequest.getPageSize())
                .fetchInto(Log.class);

        response.setList(list);
        response.setPage(pageRequest.getPageIndex());
        response.setSize(pageRequest.getPageSize());
        response.setLength(length);

        return response;
    }

    public String getSnapshotById(AuthenticatedPrincipal user, BigInteger logId) {
        if (logId == null || logId.longValue() <= 0L) {
            return "{}";
        }

        return serializer.deserialize(
                dslContext.select(LOG.SNAPSHOT)
                        .from(LOG)
                        .where(LOG.LOG_ID.eq(ULong.valueOf(logId)))
                        .fetchOptionalInto(String.class).orElse(null)
        ).toString();
    }

    public List<LogRecord> getSortedLogListByReference(String reference, SortDirection sortDirection, CcType ccType) {
        List<LogRecord> logRecordList;

        if (ccType.equals(CcType.CODE_LIST) || ccType.equals(CcType.AGENCY_ID_LIST)) {
            logRecordList =
                    dslContext.select(LOG.LOG_ID, LOG.HASH, LOG.REVISION_NUM, LOG.REVISION_TRACKING_NUM,
                            LOG.LOG_ACTION, LOG.REFERENCE, LOG.PREV_LOG_ID, LOG.NEXT_LOG_ID,
                            LOG.CREATED_BY, LOG.CREATION_TIMESTAMP)
                            .from(LOG)
                            .where(LOG.REFERENCE.eq(reference))
                            .fetchInto(LogRecord.class);
        } else {
            logRecordList =
                    dslContext.select(LOG.LOG_ID, LOG.HASH, LOG.REVISION_NUM, LOG.REVISION_TRACKING_NUM,
                            LOG.LOG_ACTION, LOG.REFERENCE, LOG.PREV_LOG_ID, LOG.NEXT_LOG_ID,
                            LOG.CREATED_BY, LOG.CREATION_TIMESTAMP)
                            .from(LOG)
                            .where(and(LOG.REFERENCE.eq(reference),
                                    jsonValue(LOG.SNAPSHOT, "$.component").eq(JSON.valueOf(ccType.name().toLowerCase()))))
                            .fetchInto(LogRecord.class);
        }

        List<LogRecord> sortedLogRecordList = new ArrayList(logRecordList.size());
        if (logRecordList.size() > 0) {
            Map<ULong, LogRecord> logRecordMap = logRecordList.stream()
                    .collect(Collectors.toMap(LogRecord::getLogId, Function.identity()));

            if (sortDirection == ASC) {
                LogRecord log =
                        logRecordList.stream().filter(e -> e.getPrevLogId() == null).findFirst().get();
                while (log != null) {
                    sortedLogRecordList.add(log);
                    if (log.getNextLogId() != null) {
                        log = logRecordMap.get(log.getNextLogId());
                    } else {
                        log = null;
                    }
                }
            } else if (sortDirection == DESC) {
                LogRecord log =
                        logRecordList.stream().filter(e -> e.getNextLogId() == null).findFirst().get();
                while (log != null) {
                    sortedLogRecordList.add(log);
                    if (log.getPrevLogId() != null) {
                        log = logRecordMap.get(log.getPrevLogId());
                    } else {
                        log = null;
                    }
                }
            }
        }
        return sortedLogRecordList;
    }

    public LogRecord revertToStableStateByReference(String reference, CcType ccType) {
        List<LogRecord> logRecordList = getSortedLogListByReference(reference, SortDirection.DESC, ccType);

        LogRecord logRecordInStableState = null;
        List<ULong> deleteTargetLogIdList = new ArrayList();
        int revisionNum = -1;
        for (int i = 0, len = logRecordList.size(); i < len; ++i) {
            LogRecord logRecord = logRecordList.get(i);
            if (revisionNum < 0) {
                revisionNum = logRecord.getRevisionNum().intValue();
            } else {
                if (logRecord.getRevisionNum().intValue() < revisionNum) {
                    logRecordInStableState = logRecord;
                    break;
                }
            }
            deleteTargetLogIdList.add(logRecord.getLogId());
        }

        if (logRecordInStableState == null) {
            throw new IllegalStateException();
        }

        // To avoid a foreign key constraint
        dslContext.update(LOG)
                .setNull(LOG.NEXT_LOG_ID)
                .where(LOG.LOG_ID.eq(logRecordInStableState.getLogId()))
                .execute();
        logRecordInStableState.setNextLogId(null);

        dslContext.update(LOG)
                .setNull(LOG.PREV_LOG_ID)
                .setNull(LOG.NEXT_LOG_ID)
                .where(
                        deleteTargetLogIdList.size() == 1 ?
                                LOG.LOG_ID.eq(deleteTargetLogIdList.get(0)) :
                                LOG.LOG_ID.in(deleteTargetLogIdList)
                )
                .execute();

        dslContext.deleteFrom(LOG)
                .where(
                        deleteTargetLogIdList.size() == 1 ?
                        LOG.LOG_ID.eq(deleteTargetLogIdList.get(0)) :
                        LOG.LOG_ID.in(deleteTargetLogIdList)
                )
                .execute();

        return logRecordInStableState;
    }

    public LogRecord revertToStableState(AccManifestRecord accManifestRecord) {
        String reference = dslContext.select(LOG.REFERENCE)
                .from(LOG)
                .where(LOG.LOG_ID.eq(accManifestRecord.getLogId()))
                .fetchOneInto(String.class);

        dslContext.update(ACC_MANIFEST)
                .setNull(ACC_MANIFEST.LOG_ID)
                .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(accManifestRecord.getAccManifestId()))
                .execute();

        LogRecord logRecord = revertToStableStateByReference(reference, CcType.ACC);
        accManifestRecord.setLogId(logRecord.getLogId());
        dslContext.update(ACC_MANIFEST)
                .set(ACC_MANIFEST.LOG_ID, logRecord.getLogId())
                .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(accManifestRecord.getAccManifestId()))
                .execute();

        return logRecord;
    }

    public LogRecord revertToStableState(AsccpManifestRecord asccpManifestRecord) {
        String reference = dslContext.select(LOG.REFERENCE)
                .from(LOG)
                .where(LOG.LOG_ID.eq(asccpManifestRecord.getLogId()))
                .fetchOneInto(String.class);

        dslContext.update(ASCCP_MANIFEST)
                .setNull(ASCCP_MANIFEST.LOG_ID)
                .where(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(asccpManifestRecord.getAsccpManifestId()))
                .execute();

        LogRecord logRecord = revertToStableStateByReference(reference, CcType.ASCCP);
        asccpManifestRecord.setLogId(logRecord.getLogId());
        dslContext.update(ASCCP_MANIFEST)
                .set(ASCCP_MANIFEST.LOG_ID, logRecord.getLogId())
                .where(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(asccpManifestRecord.getAsccpManifestId()))
                .execute();

        return logRecord;
    }

    public LogRecord revertToStableState(BccpManifestRecord bccpManifestRecord) {
        String reference = dslContext.select(LOG.REFERENCE)
                .from(LOG)
                .where(LOG.LOG_ID.eq(bccpManifestRecord.getLogId()))
                .fetchOneInto(String.class);

        dslContext.update(BCCP_MANIFEST)
                .setNull(BCCP_MANIFEST.LOG_ID)
                .where(BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(bccpManifestRecord.getBccpManifestId()))
                .execute();

        LogRecord logRecord = revertToStableStateByReference(reference, CcType.BCCP);
        bccpManifestRecord.setLogId(logRecord.getLogId());
        dslContext.update(BCCP_MANIFEST)
                .set(BCCP_MANIFEST.LOG_ID, logRecord.getLogId())
                .where(BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(bccpManifestRecord.getBccpManifestId()))
                .execute();

        return logRecord;
    }

    public LogRecord revertToStableState(DtManifestRecord dtManifestRecord) {
        String reference = dslContext.select(LOG.REFERENCE)
                .from(LOG)
                .where(LOG.LOG_ID.eq(dtManifestRecord.getLogId()))
                .fetchOneInto(String.class);

        dslContext.update(DT_MANIFEST)
                .setNull(DT_MANIFEST.LOG_ID)
                .where(DT_MANIFEST.DT_MANIFEST_ID.eq(dtManifestRecord.getDtManifestId()))
                .execute();

        LogRecord logRecord = revertToStableStateByReference(reference, CcType.DT);
        dtManifestRecord.setLogId(logRecord.getLogId());
        dslContext.update(DT_MANIFEST)
                .set(DT_MANIFEST.LOG_ID, logRecord.getLogId())
                .where(DT_MANIFEST.DT_MANIFEST_ID.eq(dtManifestRecord.getDtManifestId()))
                .execute();

        return logRecord;
    }

    public LogRecord revertToStableState(CodeListManifestRecord codeListManifestRecord) {
        String reference = dslContext.select(LOG.REFERENCE)
                .from(LOG)
                .where(LOG.LOG_ID.eq(codeListManifestRecord.getLogId()))
                .fetchOneInto(String.class);

        dslContext.update(CODE_LIST_MANIFEST)
                .setNull(CODE_LIST_MANIFEST.LOG_ID)
                .where(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID.eq(codeListManifestRecord.getCodeListManifestId()))
                .execute();

        LogRecord logRecord = revertToStableStateByReference(reference, CcType.CODE_LIST);
        codeListManifestRecord.setLogId(logRecord.getLogId());
        dslContext.update(CODE_LIST_MANIFEST)
                .set(CODE_LIST_MANIFEST.LOG_ID, logRecord.getLogId())
                .where(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID.eq(codeListManifestRecord.getCodeListManifestId()))
                .execute();

        return logRecord;
    }

    public class InsertLogArguments {
        final JsonNodeFactory nodeFactory = JsonNodeFactory.instance;

        private final ObjectNode content = nodeFactory.objectNode();
        private ULong logId;
        private UInteger logNum;
        private UInteger logTrackingNum;
        private LogAction logAction;
        private String reference;
        private ULong prevLogId;
        private ULong createdBy;
        private LocalDateTime creationTimestamp;

        public ULong getLogId() {
            return logId;
        }

        public InsertLogArguments setLogId(ULong logId) {
            this.logId = logId;
            return this;
        }

        public UInteger getRevisionNum() {
            return logNum;
        }

        public InsertLogArguments setRevisionNum(UInteger logNum) {
            this.logNum = logNum;
            return this;
        }

        public UInteger getRevisionTrackingNum() {
            return logTrackingNum;
        }

        public InsertLogArguments setRevisionTrackingNum(UInteger logTrackingNum) {
            this.logTrackingNum = logTrackingNum;
            return this;
        }

        public LogAction getLogAction() {
            return logAction;
        }

        public InsertLogArguments setLogAction(LogAction logAction) {
            this.logAction = logAction;
            return this;
        }

        public String getReference() {
            return reference;
        }

        public InsertLogArguments setReference(String reference) {
            this.reference = reference;
            return this;
        }

        public ULong getPrevLogId() {
            return prevLogId;
        }

        public InsertLogArguments setPrevLogId(ULong prevLogId) {
            this.prevLogId = prevLogId;
            return this;
        }

        public ULong getCreatedBy() {
            return createdBy;
        }

        public InsertLogArguments setCreatedBy(ULong createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public LocalDateTime getCreationTimestamp() {
            return creationTimestamp;
        }

        public InsertLogArguments setCreationTimestamp(LocalDateTime creationTimestamp) {
            this.creationTimestamp = creationTimestamp;
            return this;
        }

        public void addContent(String key, Object before, Object after) {
            final ObjectNode entry = nodeFactory.objectNode();
            if (!String.valueOf(before).equals(String.valueOf(after))) {
                entry.put("before", String.valueOf(before));
                entry.put("after", String.valueOf(after));
                content.set(key, entry);
            }
        }

        public void setAction(CcAction action) {
            content.put("ActionDescription", action.toString());
        }

        public ULong execute() {
            if (getPrevLogId() == null) {
                setRevisionNum(UInteger.valueOf(1));
                setRevisionTrackingNum(UInteger.valueOf(1));
            } else {
                LogRecord logRecord = getLogById(getPrevLogId());
                setRevisionNum(logRecord.getRevisionNum());
                setRevisionTrackingNum(logRecord.getRevisionTrackingNum().add(1));
            }

            return dslContext.insertInto(LOG)
                    .set(LOG.SNAPSHOT, content != null ? JSON.valueOf(content.toString()) : null)
                    .set(LOG.PREV_LOG_ID, getPrevLogId())
                    .set(LOG.CREATED_BY, getCreatedBy())
                    .set(LOG.CREATION_TIMESTAMP, getCreationTimestamp())
                    .set(LOG.LOG_ACTION, getLogAction().name())
                    .set(LOG.REVISION_NUM, getRevisionNum())
                    .set(LOG.REVISION_TRACKING_NUM, getRevisionTrackingNum())
                    .returning().fetchOne().getLogId();
        }
    }

    public class UpdateLogArguments {
        final JsonNodeFactory nodeFactory = JsonNodeFactory.instance;

        private ObjectNode content;
        private final ULong logId;
        private String reference;

        UpdateLogArguments(ULong logId) {
            LogRecord logRecord = dslContext
                    .selectFrom(LOG)
                    .where(LOG.LOG_ID.eq(logId))
                    .fetchOne();
            this.logId = logRecord.getLogId();
            try {
                this.content = (ObjectNode) new ObjectMapper().readTree(logRecord.getSnapshot().toString());
            } catch (JsonProcessingException e) {
                this.content = nodeFactory.objectNode();
            }
        }

        public UpdateLogArguments addContent(String key, Object before, Object after) {
            final ObjectNode entry = nodeFactory.objectNode();
            if (String.valueOf(before) != String.valueOf(after)) {
                entry.put("before", String.valueOf(before));
                entry.put("after", String.valueOf(after));
                content.set(key, entry);
            }
            return this;
        }

        public UpdateLogArguments setAction(CcAction action) {
            content.put("ActionDescription", action.toString());
            return this;
        }

        public UpdateLogArguments setReference(String reference) {
            this.reference = reference;
            return this;
        }

        public void execute() {
            if (this.logId == null) {
                return;
            }

            dslContext.update(LOG)
                    .set(LOG.SNAPSHOT, content != null ? JSON.valueOf(content.toString()) : null)
                    .where(LOG.LOG_ID.eq(this.logId))
                    .returning().fetchOne();
        }
    }

    public InsertLogArguments insertLogArguments() {
        return new InsertLogArguments();
    }

    public UpdateLogArguments updateLogArguments(ULong logId) {
        return new UpdateLogArguments(logId);
    }

    public LogRecord getLogById(ULong logId) {
        return dslContext.selectFrom(LOG).where(LOG.LOG_ID.eq(logId)).fetchOne();
    }

    /*
     * Begins ACC
     */
    public LogRecord insertAccLog(AccManifestRecord accManifestRecord,
                                  AccRecord accRecord,
                                  LogAction logAction,
                                  ULong requesterId,
                                  LocalDateTime timestamp) {
        return insertAccLog(accManifestRecord, accRecord, null, logAction, requesterId, timestamp);
    }

    private String serialize(AccManifestRecord accManifestRecord, AccRecord accRecord) {
        List<AsccManifestRecord> asccManifestRecords = dslContext.selectFrom(ASCC_MANIFEST)
                .where(ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(accManifestRecord.getAccManifestId()))
                .fetch();

        List<AsccRecord> asccRecords = dslContext.selectFrom(ASCC)
                .where(ASCC.FROM_ACC_ID.eq(accRecord.getAccId()))
                .fetch();

        List<BccManifestRecord> bccManifestRecords = dslContext.selectFrom(BCC_MANIFEST)
                .where(BCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(accManifestRecord.getAccManifestId()))
                .fetch();

        List<BccRecord> bccRecords = dslContext.selectFrom(BCC)
                .where(BCC.FROM_ACC_ID.eq(accRecord.getAccId()))
                .fetch();

        List<SeqKeyRecord> seqKeyRecords = Collections.emptyList();
        if (accManifestRecord.getAccManifestId() != null) {
            seqKeyRecords = dslContext.selectFrom(SEQ_KEY)
                    .where(SEQ_KEY.FROM_ACC_MANIFEST_ID.eq(accManifestRecord.getAccManifestId()))
                    .fetch();
        }

        return serializer.serialize(accManifestRecord, accRecord,
                asccManifestRecords, bccManifestRecords,
                asccRecords, bccRecords,
                seqKeyRecords);
    }

    public LogRecord insertAccLog(AccManifestRecord accManifestRecord,
                                  AccRecord accRecord,
                                  ULong prevLogId,
                                  LogAction logAction,
                                  ULong requesterId,
                                  LocalDateTime timestamp) {
        return insertAccLog(accManifestRecord, accRecord, prevLogId, logAction, requesterId, timestamp, LogUtils.generateHash());
    }

    public LogRecord insertAccLog(AccManifestRecord accManifestRecord,
                                  AccRecord accRecord,
                                  ULong prevLogId,
                                  LogAction logAction,
                                  ULong requesterId,
                                  LocalDateTime timestamp,
                                  String hash) {

        LogRecord prevLogRecord = null;
        if (prevLogId != null) {
            prevLogRecord = dslContext.selectFrom(LOG)
                    .where(LOG.LOG_ID.eq(prevLogId))
                    .fetchOne();
        }

        LogRecord logRecord = new LogRecord();
        logRecord.setHash(hash);
        if (LogAction.Revised.equals(logAction)) {
            assert (prevLogRecord != null);
            logRecord.setRevisionNum(prevLogRecord.getRevisionNum().add(1));
            logRecord.setRevisionTrackingNum(UInteger.valueOf(1));
        } else if (LogAction.Canceled.equals(logAction)) {
            assert (prevLogRecord != null);
            logRecord.setRevisionNum(prevLogRecord.getRevisionNum().subtract(1));
            logRecord.setRevisionTrackingNum(UInteger.valueOf(1));
        } else {
            if (prevLogRecord != null) {
                logRecord.setRevisionNum(prevLogRecord.getRevisionNum());
                logRecord.setRevisionTrackingNum(prevLogRecord.getRevisionTrackingNum().add(1));
            } else {
                logRecord.setRevisionNum(UInteger.valueOf(1));
                logRecord.setRevisionTrackingNum(UInteger.valueOf(1));
            }
        }
        logRecord.setLogAction(logAction.name());

        logRecord.setSnapshot(JSON.valueOf(serialize(accManifestRecord, accRecord)));
        logRecord.setReference(accRecord.getGuid());
        logRecord.setCreatedBy(requesterId);
        logRecord.setCreationTimestamp(timestamp);
        if (prevLogRecord != null) {
            logRecord.setPrevLogId(prevLogRecord.getLogId());
        }

        logRecord.setLogId(dslContext.insertInto(LOG)
                .set(logRecord)
                .returning(LOG.LOG_ID).fetchOne().getLogId());
        if (prevLogRecord != null) {
            prevLogRecord.setNextLogId(logRecord.getLogId());
            prevLogRecord.update(LOG.NEXT_LOG_ID);
        }

        return logRecord;
    }

    /*
     * Begins ASCCP
     */
    public LogRecord insertAsccpLog(AsccpManifestRecord asccpManifestRecord,
                                    AsccpRecord asccpRecord,
                                    LogAction logAction,
                                    ULong requesterId,
                                    LocalDateTime timestamp) {
        return insertAsccpLog(asccpManifestRecord, asccpRecord, null, logAction, requesterId, timestamp);
    }

    public LogRecord insertAsccpLog(AsccpManifestRecord asccpManifestRecord,
                                    AsccpRecord asccpRecord,
                                    ULong prevLogId,
                                    LogAction logAction,
                                    ULong requesterId,
                                    LocalDateTime timestamp) {

        LogRecord prevLogRecord = null;
        if (prevLogId != null) {
            prevLogRecord = dslContext.selectFrom(LOG)
                    .where(LOG.LOG_ID.eq(prevLogId))
                    .fetchOne();
        }

        LogRecord logRecord = new LogRecord();
        logRecord.setHash(LogUtils.generateHash());
        if (LogAction.Revised.equals(logAction)) {
            assert (prevLogRecord != null);
            logRecord.setRevisionNum(prevLogRecord.getRevisionNum().add(1));
            logRecord.setRevisionTrackingNum(UInteger.valueOf(1));
        } else if (LogAction.Canceled.equals(logAction)) {
            assert (prevLogRecord != null);
            logRecord.setRevisionNum(prevLogRecord.getRevisionNum().subtract(1));
            logRecord.setRevisionTrackingNum(UInteger.valueOf(1));
        } else {
            if (prevLogRecord != null) {
                logRecord.setRevisionNum(prevLogRecord.getRevisionNum());
                logRecord.setRevisionTrackingNum(prevLogRecord.getRevisionTrackingNum().add(1));
            } else {
                logRecord.setRevisionNum(UInteger.valueOf(1));
                logRecord.setRevisionTrackingNum(UInteger.valueOf(1));
            }
        }
        logRecord.setLogAction(logAction.name());
        logRecord.setSnapshot(JSON.valueOf(serializer.serialize(asccpManifestRecord, asccpRecord)));
        logRecord.setReference(asccpRecord.getGuid());
        logRecord.setCreatedBy(requesterId);
        logRecord.setCreationTimestamp(timestamp);
        if (prevLogRecord != null) {
            logRecord.setPrevLogId(prevLogRecord.getLogId());
        }

        logRecord.setLogId(dslContext.insertInto(LOG)
                .set(logRecord)
                .returning(LOG.LOG_ID).fetchOne().getLogId());
        if (prevLogRecord != null) {
            prevLogRecord.setNextLogId(logRecord.getLogId());
            prevLogRecord.update(LOG.NEXT_LOG_ID);
        }

        return logRecord;
    }

    /*
     * Begins BCCP
     */
    public LogRecord insertBccpLog(BccpManifestRecord bccpManifestRecord,
                                   BccpRecord bccpRecord,
                                   LogAction logAction,
                                   ULong requesterId,
                                   LocalDateTime timestamp) {
        return insertBccpLog(bccpManifestRecord, bccpRecord, null, logAction, requesterId, timestamp);
    }

    public LogRecord insertBccpLog(BccpManifestRecord bccpManifestRecord,
                                   BccpRecord bccpRecord,
                                   ULong prevLogId,
                                   LogAction logAction,
                                   ULong requesterId,
                                   LocalDateTime timestamp) {

        LogRecord prevLogRecord = null;
        if (prevLogId != null) {
            prevLogRecord = dslContext.selectFrom(LOG)
                    .where(LOG.LOG_ID.eq(prevLogId))
                    .fetchOne();
        }

        LogRecord logRecord = new LogRecord();
        logRecord.setHash(LogUtils.generateHash());
        if (LogAction.Revised.equals(logAction)) {
            assert (prevLogRecord != null);
            logRecord.setRevisionNum(prevLogRecord.getRevisionNum().add(1));
            logRecord.setRevisionTrackingNum(UInteger.valueOf(1));
        } else if (LogAction.Canceled.equals(logAction)) {
            assert (prevLogRecord != null);
            logRecord.setRevisionNum(prevLogRecord.getRevisionNum().subtract(1));
            logRecord.setRevisionTrackingNum(UInteger.valueOf(1));
        } else {
            if (prevLogRecord != null) {
                logRecord.setRevisionNum(prevLogRecord.getRevisionNum());
                logRecord.setRevisionTrackingNum(prevLogRecord.getRevisionTrackingNum().add(1));
            } else {
                logRecord.setRevisionNum(UInteger.valueOf(1));
                logRecord.setRevisionTrackingNum(UInteger.valueOf(1));
            }
        }
        logRecord.setLogAction(logAction.name());
        logRecord.setSnapshot(JSON.valueOf(serializer.serialize(bccpManifestRecord, bccpRecord)));
        logRecord.setReference(bccpRecord.getGuid());
        logRecord.setCreatedBy(requesterId);
        logRecord.setCreationTimestamp(timestamp);
        if (prevLogRecord != null) {
            logRecord.setPrevLogId(prevLogRecord.getLogId());
        }

        logRecord.setLogId(dslContext.insertInto(LOG)
                .set(logRecord)
                .returning(LOG.LOG_ID).fetchOne().getLogId());
        if (prevLogRecord != null) {
            prevLogRecord.setNextLogId(logRecord.getLogId());
            prevLogRecord.update(LOG.NEXT_LOG_ID);
        }

        return logRecord;
    }

    /*
     * Begins DT
     */
    public LogRecord insertBdtLog(DtManifestRecord bdtManifestRecord,
                                   DtRecord bdtRecord,
                                   LogAction logAction,
                                   ULong requesterId,
                                   LocalDateTime timestamp) {
        return insertBdtLog(bdtManifestRecord, bdtRecord, null, logAction, requesterId, timestamp);
    }

    public LogRecord insertBdtLog(DtManifestRecord bdtManifestRecord,
                                   DtRecord bdtRecord,
                                   ULong prevLogId,
                                   LogAction logAction,
                                   ULong requesterId,
                                   LocalDateTime timestamp) {

        LogRecord prevLogRecord = null;
        if (prevLogId != null) {
            prevLogRecord = dslContext.selectFrom(LOG)
                    .where(LOG.LOG_ID.eq(prevLogId))
                    .fetchOne();
        }

        LogRecord logRecord = new LogRecord();
        logRecord.setHash(LogUtils.generateHash());
        if (LogAction.Revised.equals(logAction)) {
            assert (prevLogRecord != null);
            logRecord.setRevisionNum(prevLogRecord.getRevisionNum().add(1));
            logRecord.setRevisionTrackingNum(UInteger.valueOf(1));
        } else if (LogAction.Canceled.equals(logAction)) {
            assert (prevLogRecord != null);
            logRecord.setRevisionNum(prevLogRecord.getRevisionNum().subtract(1));
            logRecord.setRevisionTrackingNum(UInteger.valueOf(1));
        } else {
            if (prevLogRecord != null) {
                logRecord.setRevisionNum(prevLogRecord.getRevisionNum());
                logRecord.setRevisionTrackingNum(prevLogRecord.getRevisionTrackingNum().add(1));
            } else {
                logRecord.setRevisionNum(UInteger.valueOf(1));
                logRecord.setRevisionTrackingNum(UInteger.valueOf(1));
            }
        }

        List<DtScManifestRecord> dtScManifestRecords = dslContext.selectFrom(DT_SC_MANIFEST)
                .where(DT_SC_MANIFEST.OWNER_DT_MANIFEST_ID.eq(bdtManifestRecord.getDtManifestId()))
                .fetch();

        List<DtScRecord> dtScRecords = dslContext.selectFrom(DT_SC)
                .where(DT_SC.DT_SC_ID.in(
                        dtScManifestRecords.stream().map(e -> e.getDtScId()).collect(Collectors.toList())
                ))
                .fetch();
        
        logRecord.setLogAction(logAction.name());
        logRecord.setSnapshot(JSON.valueOf(serializer.serialize(bdtManifestRecord, bdtRecord, dtScManifestRecords, dtScRecords)));
        logRecord.setReference(bdtRecord.getGuid());
        logRecord.setCreatedBy(requesterId);
        logRecord.setCreationTimestamp(timestamp);
        if (prevLogRecord != null) {
            logRecord.setPrevLogId(prevLogRecord.getLogId());
        }

        logRecord.setLogId(dslContext.insertInto(LOG)
                .set(logRecord)
                .returning(LOG.LOG_ID).fetchOne().getLogId());
        if (prevLogRecord != null) {
            prevLogRecord.setNextLogId(logRecord.getLogId());
            prevLogRecord.update(LOG.NEXT_LOG_ID);
        }

        return logRecord;
    }

    /*
     * Begins Code List
     */
    public LogRecord insertCodeListLog(CodeListManifestRecord codeListManifestRecord,
                                       CodeListRecord codeListRecord,
                                       LogAction logAction,
                                       ULong requesterId,
                                       LocalDateTime timestamp) {
        return insertCodeListLog(codeListManifestRecord, codeListRecord, null, logAction, requesterId, timestamp);
    }

    public LogRecord insertCodeListLog(CodeListManifestRecord codeListManifestRecord,
                                       CodeListRecord codeListRecord,
                                       ULong prevLogId,
                                       LogAction logAction,
                                       ULong requesterId,
                                       LocalDateTime timestamp) {

        LogRecord prevLogRecord = null;
        if (prevLogId != null) {
            prevLogRecord = dslContext.selectFrom(LOG)
                    .where(LOG.LOG_ID.eq(prevLogId))
                    .fetchOne();
        }

        LogRecord logRecord = new LogRecord();
        logRecord.setHash(LogUtils.generateHash());
        if (LogAction.Revised.equals(logAction)) {
            assert (prevLogRecord != null);
            logRecord.setRevisionNum(prevLogRecord.getRevisionNum().add(1));
            logRecord.setRevisionTrackingNum(UInteger.valueOf(1));
        } else if (LogAction.Canceled.equals(logAction)) {
            assert (prevLogRecord != null);
            logRecord.setRevisionNum(prevLogRecord.getRevisionNum().subtract(1));
            logRecord.setRevisionTrackingNum(UInteger.valueOf(1));
        } else {
            if (prevLogRecord != null) {
                logRecord.setRevisionNum(prevLogRecord.getRevisionNum());
                logRecord.setRevisionTrackingNum(prevLogRecord.getRevisionTrackingNum().add(1));
            } else {
                logRecord.setRevisionNum(UInteger.valueOf(1));
                logRecord.setRevisionTrackingNum(UInteger.valueOf(1));
            }
        }
        logRecord.setLogAction(logAction.name());

        List<CodeListValueManifestRecord> codeListValueManifestRecords = dslContext.selectFrom(CODE_LIST_VALUE_MANIFEST)
                .where(CODE_LIST_VALUE_MANIFEST.CODE_LIST_MANIFEST_ID.eq(codeListManifestRecord.getCodeListManifestId()))
                .fetch();

        List<CodeListValueRecord> codeListValueRecords = dslContext.selectFrom(CODE_LIST_VALUE)
                .where(CODE_LIST_VALUE.CODE_LIST_VALUE_ID.in(
                        codeListValueManifestRecords.stream().map(e -> e.getCodeListValueId()).collect(Collectors.toList())
                ))
                .fetch();

        logRecord.setSnapshot(JSON.valueOf(serializer.serialize(codeListManifestRecord, codeListRecord,
                codeListValueManifestRecords, codeListValueRecords)));
        logRecord.setReference(codeListRecord.getGuid());
        logRecord.setCreatedBy(requesterId);
        logRecord.setCreationTimestamp(timestamp);
        if (prevLogRecord != null) {
            logRecord.setPrevLogId(prevLogRecord.getLogId());
        }

        logRecord.setLogId(dslContext.insertInto(LOG)
                .set(logRecord)
                .returning(LOG.LOG_ID).fetchOne().getLogId());
        if (prevLogRecord != null) {
            prevLogRecord.setNextLogId(logRecord.getLogId());
            prevLogRecord.update(LOG.NEXT_LOG_ID);
        }

        return logRecord;
    }
}

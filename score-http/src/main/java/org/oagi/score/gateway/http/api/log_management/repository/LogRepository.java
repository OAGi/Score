package org.oagi.score.gateway.http.api.log_management.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.RecordMapper;
import org.jooq.impl.DSL;
import org.jooq.types.UInteger;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.cc_management.model.CcType;
import org.oagi.score.gateway.http.api.log_management.controller.payload.LogListRequest;
import org.oagi.score.gateway.http.api.log_management.model.Log;
import org.oagi.score.gateway.http.api.log_management.model.LogAction;
import org.oagi.score.gateway.http.api.log_management.model.LogId;
import org.oagi.score.gateway.http.api.log_management.service.LogSerializer;
import org.oagi.score.gateway.http.common.model.*;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.gateway.http.common.model.SortDirection.ASC;
import static org.oagi.score.gateway.http.common.model.SortDirection.DESC;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;

@Repository
public class LogRepository {

    private final DSLContext dslContext;

    private LogSerializer serializer;

    public LogRepository(@Autowired DSLContext dslContext, @Autowired RepositoryFactory repositoryFactory) {
        this.dslContext = dslContext;
        this.serializer = repositoryFactory.logSerializer();
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
                .limit(pageRequest.pageOffset(), pageRequest.pageSize())
                .fetch(mapper());

        response.setList(list);
        response.setPage(pageRequest.pageIndex());
        response.setSize(pageRequest.pageSize());
        response.setLength(length);

        return response;
    }

    private RecordMapper<org.jooq.Record, Log> mapper() {
        return record -> {
            return new Log(
                    new LogId(record.get(LOG.LOG_ID).toBigInteger()),
                    record.get(LOG.HASH),
                    record.get(LOG.REVISION_NUM).intValue(),
                    record.get(LOG.REVISION_TRACKING_NUM).intValue(),
                    LogAction.valueOf(record.get(LOG.LOG_ACTION)),
                    record.get(APP_USER.LOGIN_ID.as("loginId")),
                    Date.from(record.get(LOG.CREATION_TIMESTAMP.as("timestamp")).atZone(ZoneId.systemDefault()).toInstant()),
                    (record.get(LOG.PREV_LOG_ID) != null) ?
                            new LogId(record.get(LOG.PREV_LOG_ID).toBigInteger()) : null,
                    record.get(APP_USER.IS_DEVELOPER) == (byte) 1);
        };
    }

    public String getSnapshotById(ScoreUser requester, BigInteger logId) {
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

    private static Field<String> jsonExtract(Field<?> field, String jsonPath) {
        return DSL.field("json_extract({0}, {1})", String.class, field, DSL.inline(jsonPath));
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
                            .where(and(
                                    LOG.REFERENCE.eq(reference),
                                    jsonExtract(LOG.SNAPSHOT, "$.component").eq(ccType.name().toLowerCase())
                            ))
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
        List<LogRecord> logRecordList = getSortedLogListByReference(reference, DESC, ccType);

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
                    .set(LOG.SNAPSHOT, content != null ? content.toString() : null)
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
                    .set(LOG.SNAPSHOT, content != null ? content.toString() : null)
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

}

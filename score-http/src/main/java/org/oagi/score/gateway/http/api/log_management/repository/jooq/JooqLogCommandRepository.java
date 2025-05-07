package org.oagi.score.gateway.http.api.log_management.repository.jooq;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.jooq.types.UInteger;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListDetailsRecord;
import org.oagi.score.gateway.http.api.cc_management.model.CcType;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccDetailsRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpDetailsRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpDetailsRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtDetailsRecord;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListDetailsRecord;
import org.oagi.score.gateway.http.api.log_management.model.LogAction;
import org.oagi.score.gateway.http.api.log_management.model.LogId;
import org.oagi.score.gateway.http.api.log_management.model.LogSummaryRecord;
import org.oagi.score.gateway.http.api.log_management.model.LogUtils;
import org.oagi.score.gateway.http.api.log_management.repository.LogCommandRepository;
import org.oagi.score.gateway.http.api.log_management.service.LogSerializer;
import org.oagi.score.gateway.http.common.model.Guid;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.model.SortDirection;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.LogRecord;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.gateway.http.common.model.SortDirection.ASC;
import static org.oagi.score.gateway.http.common.model.SortDirection.DESC;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.LOG;

public class JooqLogCommandRepository extends JooqBaseRepository implements LogCommandRepository {

    private final LogSerializer serializer;

    public JooqLogCommandRepository(DSLContext dslContext, ScoreUser requester,
                                    RepositoryFactory repositoryFactory,
                                    LogSerializer serializer) {
        super(dslContext, requester, repositoryFactory);

        this.serializer = serializer;
    }

    @Override
    public LogId create(AccDetailsRecord accDetails, LogAction logAction, String logHash) {

        return create(accDetails.log(), logAction, logHash,
                serializer.serialize(requester(), accDetails),
                accDetails.guid());
    }

    @Override
    public LogId create(AsccpDetailsRecord asccpDetails, LogAction logAction) {

        return create(asccpDetails.log(), logAction, LogUtils.generateHash(),
                serializer.serialize(requester(), asccpDetails),
                asccpDetails.guid());
    }

    @Override
    public LogId create(BccpDetailsRecord bccpDetails, LogAction logAction) {

        return create(bccpDetails.log(), logAction, LogUtils.generateHash(),
                serializer.serialize(requester(), bccpDetails),
                bccpDetails.guid());
    }

    @Override
    public LogId create(DtDetailsRecord dtDetails, LogAction logAction) {

        return create(dtDetails.log(), logAction, LogUtils.generateHash(),
                serializer.serialize(requester(), dtDetails),
                dtDetails.guid());
    }

    public LogId create(CodeListDetailsRecord codeListDetails, LogAction logAction) {

        return create(codeListDetails.log(), logAction, LogUtils.generateHash(),
                serializer.serialize(requester(), codeListDetails),
                codeListDetails.guid());
    }

    public LogId create(AgencyIdListDetailsRecord agencyIdListDetails, LogAction logAction) {

        return create(agencyIdListDetails.log(), logAction, LogUtils.generateHash(),
                serializer.serialize(requester(), agencyIdListDetails),
                agencyIdListDetails.guid());
    }

    private LogId create(LogSummaryRecord prevLog, LogAction logAction, String logHash,
                         String serializedString, Guid reference) {

        LogRecord prevLogRecord = null;
        if (prevLog != null) {
            prevLogRecord = dslContext().selectFrom(LOG)
                    .where(LOG.LOG_ID.eq(valueOf(prevLog.logId())))
                    .fetchOne();
        }

        LogRecord logRecord = new LogRecord();
        logRecord.setHash(logHash);
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

        logRecord.setSnapshot(serializedString);
        logRecord.setReference(reference.value());
        logRecord.setCreatedBy(valueOf(requester().userId()));
        logRecord.setCreationTimestamp(LocalDateTime.now());
        if (prevLogRecord != null) {
            logRecord.setPrevLogId(prevLogRecord.getLogId());
        }

        logRecord.setLogId(dslContext().insertInto(LOG)
                .set(logRecord)
                .returning(LOG.LOG_ID).fetchOne().getLogId());
        if (prevLogRecord != null) {
            prevLogRecord.setNextLogId(logRecord.getLogId());
            prevLogRecord.update(LOG.NEXT_LOG_ID);
        }

        return new LogId(logRecord.getLogId().toBigInteger());
    }

    @Override
    public void deleteByReference(Guid guid) {
        dslContext().update(LOG)
                .setNull(LOG.PREV_LOG_ID)
                .setNull(LOG.NEXT_LOG_ID)
                .where(LOG.REFERENCE.eq(guid.value()))
                .execute();

        dslContext().deleteFrom(LOG)
                .where(LOG.REFERENCE.eq(guid.value()))
                .execute();
    }

    @Override
    public LogId revertToStableStateByReference(Guid reference, CcType ccType) {
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
        dslContext().update(LOG)
                .setNull(LOG.NEXT_LOG_ID)
                .where(LOG.LOG_ID.eq(logRecordInStableState.getLogId()))
                .execute();
        logRecordInStableState.setNextLogId(null);

        dslContext().update(LOG)
                .setNull(LOG.PREV_LOG_ID)
                .setNull(LOG.NEXT_LOG_ID)
                .where(
                        deleteTargetLogIdList.size() == 1 ?
                                LOG.LOG_ID.eq(deleteTargetLogIdList.get(0)) :
                                LOG.LOG_ID.in(deleteTargetLogIdList)
                )
                .execute();

        dslContext().deleteFrom(LOG)
                .where(
                        deleteTargetLogIdList.size() == 1 ?
                                LOG.LOG_ID.eq(deleteTargetLogIdList.get(0)) :
                                LOG.LOG_ID.in(deleteTargetLogIdList)
                )
                .execute();

        return new LogId(logRecordInStableState.getLogId().toBigInteger());
    }

    public List<LogRecord> getSortedLogListByReference(Guid reference, SortDirection sortDirection, CcType ccType) {
        List<LogRecord> logRecordList;

        if (ccType.equals(CcType.CODE_LIST) || ccType.equals(CcType.AGENCY_ID_LIST)) {
            logRecordList =
                    dslContext().select(LOG.LOG_ID, LOG.HASH, LOG.REVISION_NUM, LOG.REVISION_TRACKING_NUM,
                                    LOG.LOG_ACTION, LOG.REFERENCE, LOG.PREV_LOG_ID, LOG.NEXT_LOG_ID,
                                    LOG.CREATED_BY, LOG.CREATION_TIMESTAMP)
                            .from(LOG)
                            .where(LOG.REFERENCE.eq(reference.value()))
                            .orderBy((sortDirection == ASC) ? LOG.LOG_ID.asc() : LOG.LOG_ID.desc())
                            .fetchInto(LogRecord.class);
        } else {
            logRecordList =
                    dslContext().select(LOG.LOG_ID, LOG.HASH, LOG.REVISION_NUM, LOG.REVISION_TRACKING_NUM,
                                    LOG.LOG_ACTION, LOG.REFERENCE, LOG.PREV_LOG_ID, LOG.NEXT_LOG_ID,
                                    LOG.CREATED_BY, LOG.CREATION_TIMESTAMP)
                            .from(LOG)
                            .where(and(
                                    LOG.REFERENCE.eq(reference.value()),
                                    jsonExtract(LOG.SNAPSHOT, "$.component").eq(ccType.name().toLowerCase())
                            ))
                            .orderBy((sortDirection == ASC) ? LOG.LOG_ID.asc() : LOG.LOG_ID.desc())
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

    private Field<String> jsonExtract(Field<?> field, String jsonPath) {
        return DSL.field("json_extract({0}, {1})", String.class, field, DSL.inline(jsonPath));
    }

}

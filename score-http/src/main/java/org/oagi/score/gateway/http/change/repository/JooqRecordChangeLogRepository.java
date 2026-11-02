package org.oagi.score.gateway.http.change.repository;

import org.oagi.score.gateway.http.change.ChangeLogEntry;
import org.oagi.score.gateway.http.change.RecordType;

import org.jooq.DSLContext;
import org.jooq.DatePart;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.types.ULong;

import java.math.BigInteger;
import java.time.Duration;
import java.util.List;
import java.util.Set;

import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;

public class JooqRecordChangeLogRepository implements RecordChangeLogRepository {

    /** The same connection and settings as everywhere, without the log of every statement: the log is read often. */
    private final DSLContext dslContext;

    public JooqRecordChangeLogRepository(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    @Override
    public long maxSeq() {
        ULong max = dslContext.select(DSL.max(RECORD_CHANGE_LOG.RECORD_CHANGE_LOG_ID)).from(RECORD_CHANGE_LOG).fetchOne(0, ULong.class);
        return (max != null) ? max.longValue() : 0L;
    }

    @Override
    public List<ChangeLogEntry> readAfter(long seq, int limit) {
        return dslContext.select(RECORD_CHANGE_LOG.RECORD_CHANGE_LOG_ID, RECORD_CHANGE_LOG.RECORD_TYPE, RECORD_CHANGE_LOG.RECORD_ID, RECORD_CHANGE_LOG.PARENT_ID,
                        RECORD_CHANGE_LOG.RELEASE_ID, CHANGED)
                .from(RECORD_CHANGE_LOG)
                .where(RECORD_CHANGE_LOG.RECORD_CHANGE_LOG_ID.gt(ULong.valueOf(seq)))
                .orderBy(RECORD_CHANGE_LOG.RECORD_CHANGE_LOG_ID)
                .limit(limit)
                .fetch(JooqRecordChangeLogRepository::toChange);
    }

    @Override
    public List<ChangeLogEntry> readRange(long from, long to) {
        return dslContext.select(RECORD_CHANGE_LOG.RECORD_CHANGE_LOG_ID, RECORD_CHANGE_LOG.RECORD_TYPE, RECORD_CHANGE_LOG.RECORD_ID, RECORD_CHANGE_LOG.PARENT_ID,
                        RECORD_CHANGE_LOG.RELEASE_ID, CHANGED)
                .from(RECORD_CHANGE_LOG)
                .where(RECORD_CHANGE_LOG.RECORD_CHANGE_LOG_ID.between(ULong.valueOf(from), ULong.valueOf(to)))
                .orderBy(RECORD_CHANGE_LOG.RECORD_CHANGE_LOG_ID)
                .fetch(JooqRecordChangeLogRepository::toChange);
    }

    @Override
    public int purgeOlderThan(Duration age, int limit) {
        return dslContext.deleteFrom(RECORD_CHANGE_LOG)
                .where(RECORD_CHANGE_LOG.CREATION_TIMESTAMP.lt(
                        DSL.localDateTimeSub(DSL.currentLocalDateTime(6), age.toSeconds(), DatePart.SECOND)))
                .limit(limit)
                .execute();
    }

    /** The column {@code changed}: by name, so that it is read before the model has been generated again. */
    private static final Field<String> CHANGED = DSL.field(DSL.name("record_change_log", "changed"), SQLDataType.VARCHAR);

    /** The names of the columns in the text of the log, or null when the row does not say. */
    static Set<String> columns(String text) {
        if (text == null) {
            return null;
        }
        return text.isEmpty() ? Set.of() : Set.of(text.split(","));
    }

    /** The id in column {@code index}: every id of the database is a {@code bigint unsigned}. */
    private static BigInteger big(Record r, int index) {
        ULong value = r.get(index, ULong.class);
        return (value != null) ? value.toBigInteger() : null;
    }

    private static ChangeLogEntry toChange(Record r) {
        return new ChangeLogEntry(
                r.get(0, Long.class),
                RecordType.of(r.get(1, String.class)),
                big(r, 2),
                big(r, 3),
                big(r, 4),
                columns(r.get(5, String.class)));
    }

    @Override
    public boolean restartNumbering() {
        try {
            // Raw SQL: jOOQ has no NOWAIT or AUTO_INCREMENT option for ALTER TABLE.
            // NOWAIT: waiting for a lock that an open transaction holds would also hold back everything that writes behind it
            dslContext.execute("ALTER TABLE " + RECORD_CHANGE_LOG.getName() + " NOWAIT AUTO_INCREMENT = 1");
            return true;
        } catch (DataAccessException e) {
            return false;
        }
    }
}

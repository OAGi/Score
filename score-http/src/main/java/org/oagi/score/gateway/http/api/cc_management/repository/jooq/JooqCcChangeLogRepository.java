package org.oagi.score.gateway.http.api.cc_management.repository.jooq;

import org.jooq.DSLContext;
import org.jooq.DatePart;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Select;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.cc_management.cache.CcChange;
import org.oagi.score.gateway.http.api.cc_management.cache.CcChangeKind;
import org.oagi.score.gateway.http.api.cc_management.repository.CcChangeLogRepository;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;

public class JooqCcChangeLogRepository implements CcChangeLogRepository {

    /** The parent column of a kind that has none, typed so that the driver does not report a column of the type NULL. */
    private static final Field<ULong> NO_PARENT = DSL.inline((ULong) null, SQLDataType.BIGINTUNSIGNED);

    /**
     * A kind of record: its manifest table, the component table behind it and, for the kinds that are tagged, the tag table.
     * The lookups ask the kinds in the order of {@link #SPECS}.
     */
    private record Spec(CcChangeKind kind,
                        Table<?> manifest,
                        TableField<?, ULong> manifestId,
                        TableField<?, ULong> manifestReleaseId,
                        TableField<?, ULong> manifestComponentId,
                        Field<ULong> parent,
                        TableField<?, ULong> componentId,
                        TableField<?, ULong> ownerUserId,
                        TableField<?, ULong> createdBy,
                        TableField<?, ULong> lastUpdatedBy,
                        Table<?> tagTable,
                        TableField<?, ULong> tagManifestId,
                        TableField<?, ULong> tagId) {
    }

    private static final List<Spec> SPECS = List.of(
            new Spec(CcChangeKind.ACC, ACC_MANIFEST, ACC_MANIFEST.ACC_MANIFEST_ID, ACC_MANIFEST.RELEASE_ID, ACC_MANIFEST.ACC_ID,
                    NO_PARENT, ACC.ACC_ID, ACC.OWNER_USER_ID, ACC.CREATED_BY, ACC.LAST_UPDATED_BY,
                    ACC_MANIFEST_TAG, ACC_MANIFEST_TAG.ACC_MANIFEST_ID, ACC_MANIFEST_TAG.TAG_ID),
            new Spec(CcChangeKind.ASCC, ASCC_MANIFEST, ASCC_MANIFEST.ASCC_MANIFEST_ID, ASCC_MANIFEST.RELEASE_ID, ASCC_MANIFEST.ASCC_ID,
                    ASCC_MANIFEST.FROM_ACC_MANIFEST_ID, ASCC.ASCC_ID, ASCC.OWNER_USER_ID, ASCC.CREATED_BY, ASCC.LAST_UPDATED_BY,
                    null, null, null),
            new Spec(CcChangeKind.BCC, BCC_MANIFEST, BCC_MANIFEST.BCC_MANIFEST_ID, BCC_MANIFEST.RELEASE_ID, BCC_MANIFEST.BCC_ID,
                    BCC_MANIFEST.FROM_ACC_MANIFEST_ID, BCC.BCC_ID, BCC.OWNER_USER_ID, BCC.CREATED_BY, BCC.LAST_UPDATED_BY,
                    null, null, null),
            new Spec(CcChangeKind.ASCCP, ASCCP_MANIFEST, ASCCP_MANIFEST.ASCCP_MANIFEST_ID, ASCCP_MANIFEST.RELEASE_ID, ASCCP_MANIFEST.ASCCP_ID,
                    NO_PARENT, ASCCP.ASCCP_ID, ASCCP.OWNER_USER_ID, ASCCP.CREATED_BY, ASCCP.LAST_UPDATED_BY,
                    ASCCP_MANIFEST_TAG, ASCCP_MANIFEST_TAG.ASCCP_MANIFEST_ID, ASCCP_MANIFEST_TAG.TAG_ID),
            new Spec(CcChangeKind.BCCP, BCCP_MANIFEST, BCCP_MANIFEST.BCCP_MANIFEST_ID, BCCP_MANIFEST.RELEASE_ID, BCCP_MANIFEST.BCCP_ID,
                    NO_PARENT, BCCP.BCCP_ID, BCCP.OWNER_USER_ID, BCCP.CREATED_BY, BCCP.LAST_UPDATED_BY,
                    BCCP_MANIFEST_TAG, BCCP_MANIFEST_TAG.BCCP_MANIFEST_ID, BCCP_MANIFEST_TAG.TAG_ID),
            new Spec(CcChangeKind.DT, DT_MANIFEST, DT_MANIFEST.DT_MANIFEST_ID, DT_MANIFEST.RELEASE_ID, DT_MANIFEST.DT_ID,
                    NO_PARENT, DT.DT_ID, DT.OWNER_USER_ID, DT.CREATED_BY, DT.LAST_UPDATED_BY,
                    DT_MANIFEST_TAG, DT_MANIFEST_TAG.DT_MANIFEST_ID, DT_MANIFEST_TAG.TAG_ID),
            new Spec(CcChangeKind.DT_SC, DT_SC_MANIFEST, DT_SC_MANIFEST.DT_SC_MANIFEST_ID, DT_SC_MANIFEST.RELEASE_ID, DT_SC_MANIFEST.DT_SC_ID,
                    DT_SC_MANIFEST.OWNER_DT_MANIFEST_ID, DT_SC.DT_SC_ID, DT_SC.OWNER_USER_ID, DT_SC.CREATED_BY, DT_SC.LAST_UPDATED_BY,
                    null, null, null));

    /** The same connection and settings as everywhere, without the log of every statement: the feed asks twice a second. */
    private final DSLContext dslContext;

    public JooqCcChangeLogRepository(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    @Override
    public long maxSeq() {
        ULong max = dslContext.select(DSL.max(CC_CHANGE_LOG.CC_CHANGE_LOG_ID)).from(CC_CHANGE_LOG).fetchOne(0, ULong.class);
        return (max != null) ? max.longValue() : 0L;
    }

    @Override
    public List<CcChange> readAfter(long seq, int limit) {
        return dslContext.select(CC_CHANGE_LOG.CC_CHANGE_LOG_ID, CC_CHANGE_LOG.KIND, CC_CHANGE_LOG.TARGET_ID, CC_CHANGE_LOG.PARENT_MANIFEST_ID)
                .from(CC_CHANGE_LOG)
                .where(CC_CHANGE_LOG.CC_CHANGE_LOG_ID.gt(ULong.valueOf(seq)))
                .orderBy(CC_CHANGE_LOG.CC_CHANGE_LOG_ID)
                .limit(limit)
                .fetch(JooqCcChangeLogRepository::toChange);
    }

    @Override
    public List<CcChange> readRange(long from, long to) {
        return dslContext.select(CC_CHANGE_LOG.CC_CHANGE_LOG_ID, CC_CHANGE_LOG.KIND, CC_CHANGE_LOG.TARGET_ID, CC_CHANGE_LOG.PARENT_MANIFEST_ID)
                .from(CC_CHANGE_LOG)
                .where(CC_CHANGE_LOG.CC_CHANGE_LOG_ID.between(ULong.valueOf(from), ULong.valueOf(to)))
                .orderBy(CC_CHANGE_LOG.CC_CHANGE_LOG_ID)
                .fetch(JooqCcChangeLogRepository::toChange);
    }

    @Override
    public int purgeOlderThan(Duration age, int limit) {
        return dslContext.deleteFrom(CC_CHANGE_LOG)
                .where(CC_CHANGE_LOG.CREATION_TIMESTAMP.lt(
                        DSL.localDateTimeSub(DSL.currentLocalDateTime(6), age.toSeconds(), DatePart.SECOND)))
                .limit(limit)
                .execute();
    }

    private static CcChange toChange(Record r) {
        return new CcChange(
                r.get(0, Long.class),
                CcChangeKind.valueOf(r.get(1, String.class)),
                r.get(2, Long.class),
                r.get(3, Long.class));
    }

    @Override
    public boolean restartNumbering() {
        try {
            // Raw SQL: jOOQ has no NOWAIT or AUTO_INCREMENT option for ALTER TABLE.
            // NOWAIT: waiting for a lock that an open transaction holds would also hold back everything that writes behind it
            dslContext.execute("ALTER TABLE " + CC_CHANGE_LOG.getName() + " NOWAIT AUTO_INCREMENT = 1");
            return true;
        } catch (DataAccessException e) {
            return false;
        }
    }

    // ---- what a change that reaches many records reaches ----------------------------------------------------------

    @Override
    public List<RecordRef> recordsOfRelease(long releaseId) {
        List<RecordRef> refs = new ArrayList<>();
        for (Spec s : SPECS) {
            collect(refs, s, dslContext.select(s.manifestId(), s.parent())
                    .from(s.manifest())
                    .where(s.manifestReleaseId().eq(ULong.valueOf(releaseId))));
        }
        return refs;
    }

    @Override
    public List<RecordRef> recordsOfLibrary(long libraryId) {
        List<RecordRef> refs = new ArrayList<>();
        for (Spec s : SPECS) {
            collect(refs, s, dslContext.select(s.manifestId(), s.parent())
                    .from(s.manifest())
                    .join(RELEASE).on(RELEASE.RELEASE_ID.eq(s.manifestReleaseId()))
                    .where(RELEASE.LIBRARY_ID.eq(ULong.valueOf(libraryId))));
        }
        return refs;
    }

    @Override
    public List<RecordRef> recordsOfUser(long userId) {
        ULong user = ULong.valueOf(userId);
        List<RecordRef> refs = new ArrayList<>();
        for (Spec s : SPECS) {
            collect(refs, s, dslContext.select(s.manifestId(), s.parent())
                    .from(s.manifest())
                    .join(s.componentId().getTable()).on(s.componentId().eq(s.manifestComponentId()))
                    .where(s.ownerUserId().eq(user).or(s.createdBy().eq(user)).or(s.lastUpdatedBy().eq(user))));
        }
        return refs;
    }

    @Override
    public List<RecordRef> recordsTaggedWith(long tagId) {
        List<RecordRef> refs = new ArrayList<>();
        for (Spec s : SPECS) {
            if (s.tagTable() == null) {
                continue; // only ACC, ASCCP, BCCP and DT are tagged
            }
            dslContext.select(s.tagManifestId())
                    .from(s.tagTable())
                    .where(s.tagId().eq(ULong.valueOf(tagId)))
                    .forEach(r -> refs.add(new RecordRef(s.kind(), r.get(0, Long.class), null)));
        }
        return refs;
    }

    @Override
    public List<Long> allReleaseIds() {
        return dslContext.select(RELEASE.RELEASE_ID).from(RELEASE).fetch(r -> r.get(0, Long.class));
    }

    private void collect(List<RecordRef> refs, Spec spec, Select<? extends Record> query) {
        query.fetch().forEach(r -> refs.add(new RecordRef(spec.kind(), r.get(0, Long.class), r.get(1, Long.class))));
    }
}

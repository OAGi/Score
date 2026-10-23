package org.oagi.score.gateway.http.api.cc_management.repository.jooq;

import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListRecordTypes;
import org.oagi.score.gateway.http.api.cc_management.model.CcRecordTypes;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListRecordTypes;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtRecordTypes;

import org.oagi.score.gateway.http.change.RecordRef;

import org.oagi.score.gateway.http.api.cc_management.repository.AffectedRecordsRepository;
import org.oagi.score.gateway.http.change.RecordType;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Select;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.types.ULong;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;

public class JooqAffectedRecordsRepository implements AffectedRecordsRepository {

    /** The parent column of a kind that has none, typed so that the driver does not report a column of the type NULL. */
    private static final Field<ULong> NO_PARENT = DSL.inline((ULong) null, SQLDataType.BIGINTUNSIGNED);

    /**
     * A kind of record: its manifest table, the component table behind it and, for the kinds that are tagged, the tag table.
     * The lookups ask the kinds in the order of {@link #SPECS}.
     */
    private record Spec(RecordType type,
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
            new Spec(CcRecordTypes.ACC, ACC_MANIFEST, ACC_MANIFEST.ACC_MANIFEST_ID, ACC_MANIFEST.RELEASE_ID, ACC_MANIFEST.ACC_ID,
                    NO_PARENT, ACC.ACC_ID, ACC.OWNER_USER_ID, ACC.CREATED_BY, ACC.LAST_UPDATED_BY,
                    ACC_MANIFEST_TAG, ACC_MANIFEST_TAG.ACC_MANIFEST_ID, ACC_MANIFEST_TAG.TAG_ID),
            new Spec(CcRecordTypes.ASCC, ASCC_MANIFEST, ASCC_MANIFEST.ASCC_MANIFEST_ID, ASCC_MANIFEST.RELEASE_ID, ASCC_MANIFEST.ASCC_ID,
                    ASCC_MANIFEST.FROM_ACC_MANIFEST_ID, ASCC.ASCC_ID, ASCC.OWNER_USER_ID, ASCC.CREATED_BY, ASCC.LAST_UPDATED_BY,
                    null, null, null),
            new Spec(CcRecordTypes.BCC, BCC_MANIFEST, BCC_MANIFEST.BCC_MANIFEST_ID, BCC_MANIFEST.RELEASE_ID, BCC_MANIFEST.BCC_ID,
                    BCC_MANIFEST.FROM_ACC_MANIFEST_ID, BCC.BCC_ID, BCC.OWNER_USER_ID, BCC.CREATED_BY, BCC.LAST_UPDATED_BY,
                    null, null, null),
            new Spec(CcRecordTypes.ASCCP, ASCCP_MANIFEST, ASCCP_MANIFEST.ASCCP_MANIFEST_ID, ASCCP_MANIFEST.RELEASE_ID, ASCCP_MANIFEST.ASCCP_ID,
                    NO_PARENT, ASCCP.ASCCP_ID, ASCCP.OWNER_USER_ID, ASCCP.CREATED_BY, ASCCP.LAST_UPDATED_BY,
                    ASCCP_MANIFEST_TAG, ASCCP_MANIFEST_TAG.ASCCP_MANIFEST_ID, ASCCP_MANIFEST_TAG.TAG_ID),
            new Spec(CcRecordTypes.BCCP, BCCP_MANIFEST, BCCP_MANIFEST.BCCP_MANIFEST_ID, BCCP_MANIFEST.RELEASE_ID, BCCP_MANIFEST.BCCP_ID,
                    NO_PARENT, BCCP.BCCP_ID, BCCP.OWNER_USER_ID, BCCP.CREATED_BY, BCCP.LAST_UPDATED_BY,
                    BCCP_MANIFEST_TAG, BCCP_MANIFEST_TAG.BCCP_MANIFEST_ID, BCCP_MANIFEST_TAG.TAG_ID),
            new Spec(CcRecordTypes.DT, DT_MANIFEST, DT_MANIFEST.DT_MANIFEST_ID, DT_MANIFEST.RELEASE_ID, DT_MANIFEST.DT_ID,
                    NO_PARENT, DT.DT_ID, DT.OWNER_USER_ID, DT.CREATED_BY, DT.LAST_UPDATED_BY,
                    DT_MANIFEST_TAG, DT_MANIFEST_TAG.DT_MANIFEST_ID, DT_MANIFEST_TAG.TAG_ID),
            new Spec(CcRecordTypes.DT_SC, DT_SC_MANIFEST, DT_SC_MANIFEST.DT_SC_MANIFEST_ID, DT_SC_MANIFEST.RELEASE_ID, DT_SC_MANIFEST.DT_SC_ID,
                    DT_SC_MANIFEST.OWNER_DT_MANIFEST_ID, DT_SC.DT_SC_ID, DT_SC.OWNER_USER_ID, DT_SC.CREATED_BY, DT_SC.LAST_UPDATED_BY,
                    null, null, null),
            new Spec(XbtRecordTypes.XBT, XBT_MANIFEST, XBT_MANIFEST.XBT_MANIFEST_ID, XBT_MANIFEST.RELEASE_ID, XBT_MANIFEST.XBT_ID,
                    NO_PARENT, XBT.XBT_ID, XBT.OWNER_USER_ID, XBT.CREATED_BY, XBT.LAST_UPDATED_BY,
                    null, null, null),
            new Spec(CodeListRecordTypes.CODE_LIST, CODE_LIST_MANIFEST, CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID, CODE_LIST_MANIFEST.RELEASE_ID,
                    CODE_LIST_MANIFEST.CODE_LIST_ID, NO_PARENT, CODE_LIST.CODE_LIST_ID, CODE_LIST.OWNER_USER_ID, CODE_LIST.CREATED_BY,
                    CODE_LIST.LAST_UPDATED_BY, null, null, null),
            new Spec(AgencyIdListRecordTypes.AGENCY_ID_LIST, AGENCY_ID_LIST_MANIFEST, AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID,
                    AGENCY_ID_LIST_MANIFEST.RELEASE_ID, AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID, NO_PARENT,
                    AGENCY_ID_LIST.AGENCY_ID_LIST_ID, AGENCY_ID_LIST.OWNER_USER_ID, AGENCY_ID_LIST.CREATED_BY,
                    AGENCY_ID_LIST.LAST_UPDATED_BY, null, null, null));

    /** The same connection and settings as everywhere, without the log of every statement. */
    private final DSLContext dslContext;

    public JooqAffectedRecordsRepository(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    /** The id in column {@code index}: every id of the database is a {@code bigint unsigned}. */
    private static BigInteger big(Record r, int index) {
        ULong value = r.get(index, ULong.class);
        return (value != null) ? value.toBigInteger() : null;
    }

    // ---- what a change that reaches many records reaches ----------------------------------------------------------

    @Override
    public List<RecordRef> recordsOfRelease(BigInteger releaseId) {
        List<RecordRef> refs = new ArrayList<>();
        for (Spec s : SPECS) {
            collect(refs, s, dslContext.select(s.manifestId(), s.parent(), s.manifestReleaseId())
                    .from(s.manifest())
                    .where(s.manifestReleaseId().eq(ULong.valueOf(releaseId))));
        }
        return refs;
    }

    @Override
    public List<RecordRef> recordsOfLibrary(BigInteger libraryId) {
        List<RecordRef> refs = new ArrayList<>();
        for (Spec s : SPECS) {
            collect(refs, s, dslContext.select(s.manifestId(), s.parent(), s.manifestReleaseId())
                    .from(s.manifest())
                    .join(RELEASE).on(RELEASE.RELEASE_ID.eq(s.manifestReleaseId()))
                    .where(RELEASE.LIBRARY_ID.eq(ULong.valueOf(libraryId))));
        }
        return refs;
    }

    @Override
    public List<RecordRef> recordsOfUser(BigInteger userId) {
        ULong user = ULong.valueOf(userId);
        List<RecordRef> refs = new ArrayList<>();
        for (Spec s : SPECS) {
            collect(refs, s, dslContext.select(s.manifestId(), s.parent(), s.manifestReleaseId())
                    .from(s.manifest())
                    .join(s.componentId().getTable()).on(s.componentId().eq(s.manifestComponentId()))
                    .where(s.ownerUserId().eq(user).or(s.createdBy().eq(user)).or(s.lastUpdatedBy().eq(user))));
        }
        return refs;
    }

    @Override
    public List<RecordRef> recordsTaggedWith(BigInteger tagId) {
        List<RecordRef> refs = new ArrayList<>();
        for (Spec s : SPECS) {
            if (s.tagTable() == null) {
                continue; // only ACC, ASCCP, BCCP and DT are tagged
            }
            dslContext.select(s.tagManifestId())
                    .from(s.tagTable())
                    .where(s.tagId().eq(ULong.valueOf(tagId)))
                    .forEach(r -> refs.add(new RecordRef(s.type(), big(r, 0), null)));
        }
        return refs;
    }

    @Override
    public List<RecordRef> recordsShowing(RecordType type, Collection<BigInteger> manifestIds) {
        List<RecordRef> refs = new ArrayList<>();
        TableField<?, ULong> inDt;
        TableField<?, ULong> inDtSc;
        if (type.equals(XbtRecordTypes.XBT)) {
            inDt = DT_AWD_PRI.XBT_MANIFEST_ID;
            inDtSc = DT_SC_AWD_PRI.XBT_MANIFEST_ID;
        } else if (type.equals(CodeListRecordTypes.CODE_LIST)) {
            inDt = DT_AWD_PRI.CODE_LIST_MANIFEST_ID;
            inDtSc = DT_SC_AWD_PRI.CODE_LIST_MANIFEST_ID;
        } else if (type.equals(AgencyIdListRecordTypes.AGENCY_ID_LIST)) {
            inDt = DT_AWD_PRI.AGENCY_ID_LIST_MANIFEST_ID;
            inDtSc = DT_SC_AWD_PRI.AGENCY_ID_LIST_MANIFEST_ID;
        } else {
            return refs;
        }
        List<BigInteger> all = new ArrayList<>(manifestIds);
        for (int from = 0; from < all.size(); from += 1000) {
            List<ULong> chunk = all.subList(from, Math.min(from + 1000, all.size())).stream().map(ULong::valueOf).toList();
            dslContext.select(DT_MANIFEST.DT_MANIFEST_ID)
                    .from(DT_AWD_PRI)
                    .join(DT_MANIFEST).on(DT_MANIFEST.DT_ID.eq(DT_AWD_PRI.DT_ID).and(DT_MANIFEST.RELEASE_ID.eq(DT_AWD_PRI.RELEASE_ID)))
                    .where(inDt.in(chunk))
                    .fetch().forEach(r -> refs.add(new RecordRef(CcRecordTypes.DT, big(r, 0), null)));
            dslContext.select(DT_SC_MANIFEST.DT_SC_MANIFEST_ID, DT_SC_MANIFEST.OWNER_DT_MANIFEST_ID)
                    .from(DT_SC_AWD_PRI)
                    .join(DT_SC_MANIFEST).on(DT_SC_MANIFEST.DT_SC_ID.eq(DT_SC_AWD_PRI.DT_SC_ID)
                            .and(DT_SC_MANIFEST.RELEASE_ID.eq(DT_SC_AWD_PRI.RELEASE_ID)))
                    .where(inDtSc.in(chunk))
                    .fetch().forEach(r -> refs.add(new RecordRef(CcRecordTypes.DT_SC, big(r, 0), big(r, 1))));
        }
        return refs;
    }

    @Override
    public List<BigInteger> allReleaseIds() {
        return dslContext.select(RELEASE.RELEASE_ID).from(RELEASE).fetch(r -> big(r, 0));
    }

    private void collect(List<RecordRef> refs, Spec spec, Select<? extends Record> query) {
        query.fetch().forEach(r -> refs.add(new RecordRef(spec.type(), big(r, 0), big(r, 1), big(r, 2))));
    }
}

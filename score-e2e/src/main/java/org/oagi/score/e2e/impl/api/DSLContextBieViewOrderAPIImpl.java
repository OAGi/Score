package org.oagi.score.e2e.impl.api;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.e2e.api.BieViewOrderAPI;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.records.BieViewOrderRecord;
import org.oagi.score.e2e.obj.ACCObject;
import org.oagi.score.e2e.obj.ASCCObject;
import org.oagi.score.e2e.obj.BCCObject;
import org.oagi.score.e2e.obj.BieViewOrderObject;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.e2e.impl.api.jooq.entity.Tables.*;

public class DSLContextBieViewOrderAPIImpl implements BieViewOrderAPI {

    private final DSLContext dslContext;

    public DSLContextBieViewOrderAPIImpl(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    @Override
    public void setAsccViewOrder(ACCObject fromAcc, ASCCObject ascc, int weight) {
        ULong asccManifestId = dslContext.select(ASCC_MANIFEST.ASCC_MANIFEST_ID)
                .from(ASCC_MANIFEST)
                .where(and(
                        ASCC_MANIFEST.ASCC_ID.eq(ULong.valueOf(ascc.getAsccId())),
                        ASCC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(fromAcc.getReleaseId()))))
                .fetchOneInto(ULong.class);
        if (asccManifestId == null) {
            throw new IllegalStateException("Cannot find the ASCC manifest for the given association.");
        }
        insert(ULong.valueOf(fromAcc.getAccManifestId()), asccManifestId, null,
                weight, ULong.valueOf(fromAcc.getCreatedBy()));
    }

    @Override
    public void setBccViewOrder(ACCObject fromAcc, BCCObject bcc, int weight) {
        ULong bccManifestId = dslContext.select(BCC_MANIFEST.BCC_MANIFEST_ID)
                .from(BCC_MANIFEST)
                .where(and(
                        BCC_MANIFEST.BCC_ID.eq(ULong.valueOf(bcc.getBccId())),
                        BCC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(fromAcc.getReleaseId()))))
                .fetchOneInto(ULong.class);
        if (bccManifestId == null) {
            throw new IllegalStateException("Cannot find the BCC manifest for the given association.");
        }
        insert(ULong.valueOf(fromAcc.getAccManifestId()), null, bccManifestId,
                weight, ULong.valueOf(fromAcc.getCreatedBy()));
    }

    private void insert(ULong fromAccManifestId, ULong asccManifestId, ULong bccManifestId,
                        int weight, ULong requesterId) {
        LocalDateTime timestamp = LocalDateTime.now();
        BieViewOrderRecord record = new BieViewOrderRecord();
        record.setFromAccManifestId(fromAccManifestId);
        record.setAsccManifestId(asccManifestId);
        record.setBccManifestId(bccManifestId);
        record.setWeight(weight);
        record.setCreatedBy(requesterId);
        record.setLastUpdatedBy(requesterId);
        record.setCreationTimestamp(timestamp);
        record.setLastUpdateTimestamp(timestamp);
        dslContext.insertInto(BIE_VIEW_ORDER).set(record).execute();
    }

    @Override
    public List<BieViewOrderObject> getViewOrdersByFromAccManifestId(BigInteger fromAccManifestId) {
        return dslContext.selectFrom(BIE_VIEW_ORDER)
                .where(BIE_VIEW_ORDER.FROM_ACC_MANIFEST_ID.eq(ULong.valueOf(fromAccManifestId)))
                .fetch(this::mapper);
    }

    @Override
    public void deleteViewOrdersByFromAccManifestId(BigInteger fromAccManifestId) {
        dslContext.deleteFrom(BIE_VIEW_ORDER)
                .where(BIE_VIEW_ORDER.FROM_ACC_MANIFEST_ID.eq(ULong.valueOf(fromAccManifestId)))
                .execute();
    }

    @Override
    public void deleteViewOrdersByRelease(BigInteger releaseId) {
        dslContext.deleteFrom(BIE_VIEW_ORDER)
                .where(BIE_VIEW_ORDER.FROM_ACC_MANIFEST_ID.in(
                        dslContext.select(ACC_MANIFEST.ACC_MANIFEST_ID)
                                .from(ACC_MANIFEST)
                                .where(ACC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)))))
                .execute();
    }

    @Override
    public List<BieViewOrderObject> getViewOrdersByRelease(BigInteger releaseId) {
        return dslContext.select(BIE_VIEW_ORDER.fields())
                .from(BIE_VIEW_ORDER)
                .join(ACC_MANIFEST)
                .on(BIE_VIEW_ORDER.FROM_ACC_MANIFEST_ID.eq(ACC_MANIFEST.ACC_MANIFEST_ID))
                .where(ACC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)))
                .fetch(record -> mapper(record.into(BIE_VIEW_ORDER)));
    }

    @Override
    public BigInteger findForwardedAccManifestId(BigInteger workingAccManifestId, BigInteger releaseId) {
        ULong forwarded = dslContext.select(ACC_MANIFEST.ACC_MANIFEST_ID)
                .from(ACC_MANIFEST)
                .where(and(
                        ACC_MANIFEST.NEXT_ACC_MANIFEST_ID.eq(ULong.valueOf(workingAccManifestId)),
                        ACC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId))))
                .fetchOneInto(ULong.class);
        return (forwarded != null) ? forwarded.toBigInteger() : null;
    }

    @Override
    public BigInteger findForwardedAsccManifestId(BigInteger workingAsccManifestId, BigInteger releaseId) {
        ULong forwarded = dslContext.select(ASCC_MANIFEST.ASCC_MANIFEST_ID)
                .from(ASCC_MANIFEST)
                .where(and(
                        ASCC_MANIFEST.NEXT_ASCC_MANIFEST_ID.eq(ULong.valueOf(workingAsccManifestId)),
                        ASCC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId))))
                .fetchOneInto(ULong.class);
        return (forwarded != null) ? forwarded.toBigInteger() : null;
    }

    @Override
    public BigInteger findForwardedBccManifestId(BigInteger workingBccManifestId, BigInteger releaseId) {
        ULong forwarded = dslContext.select(BCC_MANIFEST.BCC_MANIFEST_ID)
                .from(BCC_MANIFEST)
                .where(and(
                        BCC_MANIFEST.NEXT_BCC_MANIFEST_ID.eq(ULong.valueOf(workingBccManifestId)),
                        BCC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId))))
                .fetchOneInto(ULong.class);
        return (forwarded != null) ? forwarded.toBigInteger() : null;
    }

    private BieViewOrderObject mapper(BieViewOrderRecord record) {
        BieViewOrderObject object = new BieViewOrderObject();
        object.setBieViewOrderId(record.getBieViewOrderId().toBigInteger());
        object.setFromAccManifestId(record.getFromAccManifestId().toBigInteger());
        if (record.getAsccManifestId() != null) {
            object.setAsccManifestId(record.getAsccManifestId().toBigInteger());
        }
        if (record.getBccManifestId() != null) {
            object.setBccManifestId(record.getBccManifestId().toBigInteger());
        }
        object.setWeight(record.getWeight());
        return object;
    }
}

package org.oagi.score.repo.api.impl.jooq.corecomponent;

import org.jooq.DSLContext;
import org.jooq.UpdateSetStep;
import org.jooq.UpdateWhereStep;
import org.jooq.types.ULong;
import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.corecomponent.seqkey.SeqKeyWriteRepository;
import org.oagi.score.repo.api.corecomponent.seqkey.model.*;
import org.oagi.score.repo.api.impl.jooq.JooqScoreRepository;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.SeqKeyRecord;
import org.oagi.score.repo.api.security.AccessControl;

import java.math.BigInteger;

import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;
import static org.oagi.score.repo.api.user.model.ScoreRole.DEVELOPER;
import static org.oagi.score.repo.api.user.model.ScoreRole.END_USER;

public class JooqSeqKeyWriteRepository
        extends JooqScoreRepository
        implements SeqKeyWriteRepository {

    public JooqSeqKeyWriteRepository(DSLContext dslContext) {
        super(dslContext);
    }

    @Override
    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public CreateSeqKeyResponse createSeqKey(CreateSeqKeyRequest request) throws ScoreDataAccessException {

        SeqKeyRecord record = new SeqKeyRecord();
        record.setFromAccManifestId(ULong.valueOf(request.getFromAccManifestId()));
        if (SeqKeyType.ASCC == request.getType()) {
            record.setAsccManifestId(ULong.valueOf(request.getManifestId()));
        } else {
            record.setBccManifestId(ULong.valueOf(request.getManifestId()));
        }

        record.setSeqKeyId(
                dslContext().insertInto(SEQ_KEY)
                        .set(record)
                        .returning(SEQ_KEY.SEQ_KEY_ID)
                        .fetchOne().getSeqKeyId()
        );

        switch (request.getType()) {
            case ASCC:
                dslContext().update(ASCC_MANIFEST)
                        .set(ASCC_MANIFEST.SEQ_KEY_ID, record.getSeqKeyId())
                        .where(ASCC_MANIFEST.ASCC_MANIFEST_ID.eq(record.getAsccManifestId()))
                        .execute();
                break;

            case BCC:
                dslContext().update(BCC_MANIFEST)
                        .set(BCC_MANIFEST.SEQ_KEY_ID, record.getSeqKeyId())
                        .where(BCC_MANIFEST.BCC_MANIFEST_ID.eq(record.getBccManifestId()))
                        .execute();
                break;
        }

        SeqKey seqKey = new SeqKey();
        seqKey.setSeqKeyId(record.getSeqKeyId().toBigInteger());
        seqKey.setFromAccManifestId(record.getFromAccManifestId().toBigInteger());
        if (record.getAsccManifestId() != null) {
            seqKey.setAsccManifestId(record.getAsccManifestId().toBigInteger());
        }
        if (record.getBccManifestId() != null) {
            seqKey.setBccManifestId(record.getBccManifestId().toBigInteger());
        }

        return new CreateSeqKeyResponse(seqKey);
    }

    private void setPrev(BigInteger key, BigInteger prev) {
        if (key == null) {
            return;
        }
        setPrev(ULong.valueOf(key), (prev != null) ? ULong.valueOf(prev) : null);
    }

    private void setPrev(ULong key, ULong prev) {
        if (key != null) {
            if (prev != null) {
                dslContext().update(SEQ_KEY)
                        .set(SEQ_KEY.PREV_SEQ_KEY_ID, prev)
                        .where(SEQ_KEY.SEQ_KEY_ID.eq(key))
                        .execute();
            } else {
                dslContext().update(SEQ_KEY)
                        .setNull(SEQ_KEY.PREV_SEQ_KEY_ID)
                        .where(SEQ_KEY.SEQ_KEY_ID.eq(key))
                        .execute();
            }
        }
    }

    private void setNext(BigInteger key, BigInteger next) {
        if (key == null) {
            return;
        }
        setNext(ULong.valueOf(key), (next != null) ? ULong.valueOf(next) : null);
    }

    private void setNext(ULong key, ULong next) {
        if (key != null) {
            if (next != null) {
                dslContext().update(SEQ_KEY)
                        .set(SEQ_KEY.NEXT_SEQ_KEY_ID, next)
                        .where(SEQ_KEY.SEQ_KEY_ID.eq(key))
                        .execute();
            } else {
                dslContext().update(SEQ_KEY)
                        .setNull(SEQ_KEY.NEXT_SEQ_KEY_ID)
                        .where(SEQ_KEY.SEQ_KEY_ID.eq(key))
                        .execute();
            }
        }
    }

    private void brokeLinks(SeqKey seqKey) {
        SeqKeyRecord record = get(seqKey.getSeqKeyId());

        ULong prev = record.getPrevSeqKeyId();
        ULong next = record.getNextSeqKeyId();

        setNext(prev, next);
        setPrev(next, prev);

        setPrev(record.getSeqKeyId(), null);
        setNext(record.getSeqKeyId(), null);
    }

    private SeqKeyRecord get(BigInteger id) {
        return dslContext().selectFrom(SEQ_KEY)
                .where(SEQ_KEY.SEQ_KEY_ID.eq(ULong.valueOf(id)))
                .fetchOne();
    }

    @Override
    public MoveAfterResponse moveAfter(MoveAfterRequest request) throws ScoreDataAccessException {
        if (request.getAfter() == null) {
            throw new ScoreDataAccessException(new IllegalArgumentException());
        }

        brokeLinks(request.getItem());

        // DO NOT change orders of executions.

        BigInteger current = request.getItem().getSeqKeyId();
        BigInteger after = request.getAfter().getSeqKeyId();
        BigInteger prev = request.getAfter().getSeqKeyId();
        BigInteger next = (request.getAfter().getNextSeqKey() != null) ?
                request.getAfter().getNextSeqKey().getSeqKeyId() : null;

        setPrev(current, prev);
        if (next != null) {
            setNext(current, next);
        }

        setNext(after, current);
        if (next != null) {
            setPrev(next, current);
        }

        return new MoveAfterResponse();
    }

    @Override
    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public UpdateSeqKeyResponse updateSeqKey(UpdateSeqKeyRequest request) throws ScoreDataAccessException {
        SeqKey seqKey = request.getSeqKey();

        UpdateSetStep step = dslContext().update(SEQ_KEY);
        SeqKey prev = seqKey.getPrevSeqKey();
        if (prev == null) {
            step = step.setNull(SEQ_KEY.PREV_SEQ_KEY_ID);
        } else {
            step = step.set(SEQ_KEY.PREV_SEQ_KEY_ID, ULong.valueOf(prev.getSeqKeyId()));
        }

        SeqKey next = seqKey.getNextSeqKey();
        if (next == null) {
            step = step.setNull(SEQ_KEY.NEXT_SEQ_KEY_ID);
        } else {
            step = step.set(SEQ_KEY.NEXT_SEQ_KEY_ID, ULong.valueOf(next.getSeqKeyId()));
        }

        int affectedRows = 0;
        if (step instanceof UpdateWhereStep) {
            affectedRows = ((UpdateWhereStep) step)
                    .where(SEQ_KEY.SEQ_KEY_ID.eq(ULong.valueOf(seqKey.getSeqKeyId())))
                    .execute();
        }

        return new UpdateSeqKeyResponse((affectedRows == 0) ? null : seqKey.getSeqKeyId());
    }

    @Override
    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public DeleteSeqKeyResponse deleteSeqKey(DeleteSeqKeyRequest request) throws ScoreDataAccessException {
        BigInteger seqKeyId = request.getSeqKeyId();
        SeqKeyRecord seqKeyRecord = dslContext().selectFrom(SEQ_KEY)
                .where(SEQ_KEY.SEQ_KEY_ID.eq(ULong.valueOf(seqKeyId)))
                .fetchOne();

        // disconnect links between prev and next
        {
            SeqKeyRecord prev = dslContext().selectFrom(SEQ_KEY)
                    .where(SEQ_KEY.PREV_SEQ_KEY_ID.eq(ULong.valueOf(seqKeyId)))
                    .fetchOptional().orElse(null);

            SeqKeyRecord next = dslContext().selectFrom(SEQ_KEY)
                    .where(SEQ_KEY.NEXT_SEQ_KEY_ID.eq(ULong.valueOf(seqKeyId)))
                    .fetchOptional().orElse(null);

            SeqKeyRecord current = dslContext().selectFrom(SEQ_KEY)
                    .where(SEQ_KEY.SEQ_KEY_ID.eq(ULong.valueOf(seqKeyId)))
                    .fetchOptional().orElse(null);

            if (prev != null) {
                dslContext().update(SEQ_KEY)
                        .set(SEQ_KEY.PREV_SEQ_KEY_ID, current.getPrevSeqKeyId())
                        .where(SEQ_KEY.SEQ_KEY_ID.eq(prev.getSeqKeyId()))
                        .execute();
            }

            if (next != null) {
                dslContext().update(SEQ_KEY)
                        .set(SEQ_KEY.NEXT_SEQ_KEY_ID, current.getNextSeqKeyId())
                        .where(SEQ_KEY.SEQ_KEY_ID.eq(next.getSeqKeyId()))
                        .execute();
            }
        }

        if (seqKeyRecord.getAsccManifestId() != null) {
            dslContext().update(ASCC_MANIFEST)
                    .setNull(ASCC_MANIFEST.SEQ_KEY_ID)
                    .where(ASCC_MANIFEST.ASCC_MANIFEST_ID.eq(seqKeyRecord.getAsccManifestId()))
                    .execute();
        } else if (seqKeyRecord.getBccManifestId() != null) {
            dslContext().update(BCC_MANIFEST)
                    .setNull(BCC_MANIFEST.SEQ_KEY_ID)
                    .where(BCC_MANIFEST.BCC_MANIFEST_ID.eq(seqKeyRecord.getBccManifestId()))
                    .execute();
        }

        int affectedRows = dslContext().deleteFrom(SEQ_KEY)
                .where(SEQ_KEY.SEQ_KEY_ID.eq(ULong.valueOf(seqKeyId)))
                .execute();

        return new DeleteSeqKeyResponse((affectedRows == 0) ? null : seqKeyId);
    }

}

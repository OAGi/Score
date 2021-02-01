package org.oagi.score.repo.api.impl.jooq.bie;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.bie.BieWriteRepository;
import org.oagi.score.repo.api.bie.model.*;
import org.oagi.score.repo.api.impl.jooq.JooqScoreRepository;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.*;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;

import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;

public class JooqBieWriteRepository
        extends JooqScoreRepository
        implements BieWriteRepository {

    public JooqBieWriteRepository(DSLContext dslContext) {
        super(dslContext);
    }

    @Override
    public CreateBieResponse createBie(CreateBieRequest request) throws ScoreDataAccessException {
        ScoreUser requester = request.getRequester();
        WrappedAsbiep topLevelAsbiep = request.getTopLevelAsbiep();

        TopLevelAsbiepRecord topLevelAsbiepRecord = insertTopLevelAsbiep(request);
        request.getBizCtxIds().forEach(bizCtxId -> {
            insertBizCtxAssignment(topLevelAsbiepRecord, bizCtxId);
        });
        BigInteger topLevelAsbiepId = topLevelAsbiepRecord.getTopLevelAsbiepId().toBigInteger();

        insertAbie(topLevelAsbiep.getRoleOfAbie(), requester, topLevelAsbiepId);
        topLevelAsbiep.getAsbiep().setRoleOfAbieId(topLevelAsbiep.getRoleOfAbie().getAbieId());
        insertAsbiep(topLevelAsbiep.getAsbiep(), requester, topLevelAsbiepId);
        dslContext().update(TOP_LEVEL_ASBIEP)
                .set(TOP_LEVEL_ASBIEP.ASBIEP_ID, ULong.valueOf(topLevelAsbiep.getAsbiep().getAsbiepId()))
                .where(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(topLevelAsbiepRecord.getTopLevelAsbiepId()))
                .execute();

        request.getAsbieList().forEach(asbie -> {
            if (asbie.getToAsbiep() == null && asbie.getRefTopLevelAsbiepId() == null) {
                return;
            }

            insertAbie(asbie.getFromAbie(), requester, topLevelAsbiepId);
            asbie.getAsbie().setFromAbieId(asbie.getFromAbie().getAbieId());
            if (asbie.getToAsbiep() == null) {
                BigInteger toAsbiepId = dslContext().select(TOP_LEVEL_ASBIEP.ASBIEP_ID)
                        .from(TOP_LEVEL_ASBIEP)
                        .where(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(asbie.getRefTopLevelAsbiepId())))
                        .fetchOneInto(BigInteger.class);
                asbie.getAsbie().setToAsbiepId(toAsbiepId);
            } else {
                insertAbie(asbie.getToAsbiep().getRoleOfAbie(), requester, topLevelAsbiepId);
                asbie.getToAsbiep().getAsbiep().setRoleOfAbieId(asbie.getToAsbiep().getRoleOfAbie().getAbieId());
                insertAsbiep(asbie.getToAsbiep().getAsbiep(), requester, topLevelAsbiepId);
                asbie.getAsbie().setToAsbiepId(asbie.getToAsbiep().getAsbiep().getAsbiepId());
            }
            insertAsbie(asbie.getAsbie(), requester, topLevelAsbiepId);
        });
        request.getBbieList().forEach(bbie -> {
            insertAbie(bbie.getFromAbie(), requester, topLevelAsbiepId);
            bbie.getBbie().setFromAbieId(bbie.getFromAbie().getAbieId());
            insertBbiep(bbie.getToBbiep(), requester, topLevelAsbiepId);
            bbie.getBbie().setToBbiepId(bbie.getToBbiep().getBbiepId());
            insertBbie(bbie.getBbie(), requester, topLevelAsbiepId);
        });
        request.getBbieScList().forEach(bbieSc -> {
            insertBbie(bbieSc.getBbie(), requester, topLevelAsbiepId);
            bbieSc.getBbieSc().setBbieId(bbieSc.getBbie().getBbieId());
            insertBbieSc(bbieSc.getBbieSc(), requester, topLevelAsbiepId);
        });

        return new CreateBieResponse(topLevelAsbiepId);
    }

    private TopLevelAsbiepRecord insertTopLevelAsbiep(CreateBieRequest request) {
        ULong userId = ULong.valueOf(request.getRequester().getUserId());

        TopLevelAsbiepRecord topLevelAsbiepRecord = new TopLevelAsbiepRecord();
        topLevelAsbiepRecord.setReleaseId(dslContext().select(ASCCP_MANIFEST.RELEASE_ID)
                .from(ASCCP_MANIFEST)
                .where(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(
                        ULong.valueOf(request.getTopLevelAsbiep().getAsbiep().getBasedAsccpManifestId())))
                .fetchOneInto(ULong.class)
        );
        topLevelAsbiepRecord.setState(BieState.WIP.name());
        topLevelAsbiepRecord.setStatus(request.getStatus());
        topLevelAsbiepRecord.setVersion(request.getVersion());
        topLevelAsbiepRecord.setOwnerUserId(userId);
        topLevelAsbiepRecord.setLastUpdatedBy(userId);
        topLevelAsbiepRecord.setLastUpdateTimestamp(LocalDateTime.now());

        topLevelAsbiepRecord.setTopLevelAsbiepId(
                dslContext().insertInto(TOP_LEVEL_ASBIEP)
                        .set(topLevelAsbiepRecord)
                        .returning(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID)
                        .fetchOne().getTopLevelAsbiepId());
        return topLevelAsbiepRecord;
    }

    private void insertBizCtxAssignment(TopLevelAsbiepRecord topLevelAsbiepRecord,
                                        BigInteger bizCtxId) {

        BizCtxAssignmentRecord bizCtxAssignmentRecord = new BizCtxAssignmentRecord();
        bizCtxAssignmentRecord.setTopLevelAsbiepId(topLevelAsbiepRecord.getTopLevelAsbiepId());
        bizCtxAssignmentRecord.setBizCtxId(ULong.valueOf(bizCtxId));

        dslContext().insertInto(BIZ_CTX_ASSIGNMENT)
                .set(bizCtxAssignmentRecord)
                .execute();
    }

    private void insertAbie(Abie abie, ScoreUser user, BigInteger topLevelAsbiepId) {
        if (abie == null) {
            throw new IllegalArgumentException();
        }
        if (abie.getAbieId() != null) {
            return;
        }

        ULong userId = ULong.valueOf(user.getUserId());

        AbieRecord abieRecord = new AbieRecord();
        abieRecord.setGuid(abie.getGuid());
        abieRecord.setBasedAccManifestId(ULong.valueOf(abie.getBasedAccManifestId()));
        abieRecord.setPath(abie.getPath());
        abieRecord.setHashPath(abie.getHashPath());
        abieRecord.setDefinition(abie.getDefinition());
        abieRecord.setCreatedBy(userId);
        abieRecord.setLastUpdatedBy(userId);
        abieRecord.setCreationTimestamp(LocalDateTime.now());
        abieRecord.setLastUpdateTimestamp(LocalDateTime.now());
        abieRecord.setRemark(abie.getRemark());
        abieRecord.setBizTerm(abie.getBizTerm());
        abieRecord.setOwnerTopLevelAsbiepId(ULong.valueOf(topLevelAsbiepId));

        abie.setAbieId(dslContext().insertInto(ABIE)
                .set(abieRecord)
                .returning(ABIE.ABIE_ID).fetchOne()
                .getAbieId().toBigInteger());
    }

    private void insertAsbiep(Asbiep asbiep, ScoreUser user, BigInteger topLevelAsbiepId) {
        if (asbiep == null) {
            throw new IllegalArgumentException();
        }
        if (asbiep.getAsbiepId() != null) {
            return;
        }

        ULong userId = ULong.valueOf(user.getUserId());

        AsbiepRecord asbiepRecord = new AsbiepRecord();
        asbiepRecord.setGuid(asbiep.getGuid());
        asbiepRecord.setBasedAsccpManifestId(ULong.valueOf(asbiep.getBasedAsccpManifestId()));
        asbiepRecord.setPath(asbiep.getPath());
        asbiepRecord.setHashPath(asbiep.getHashPath());
        asbiepRecord.setRoleOfAbieId(ULong.valueOf(asbiep.getRoleOfAbieId()));
        asbiepRecord.setDefinition(asbiep.getDefinition());
        asbiepRecord.setRemark(asbiep.getRemark());
        asbiepRecord.setBizTerm(asbiep.getBizTerm());
        asbiepRecord.setCreatedBy(userId);
        asbiepRecord.setLastUpdatedBy(userId);
        asbiepRecord.setCreationTimestamp(LocalDateTime.now());
        asbiepRecord.setLastUpdateTimestamp(LocalDateTime.now());
        asbiepRecord.setOwnerTopLevelAsbiepId(ULong.valueOf(topLevelAsbiepId));

        asbiep.setAsbiepId(dslContext().insertInto(ASBIEP)
                .set(asbiepRecord)
                .returning(ASBIEP.ASBIEP_ID).fetchOne()
                .getAsbiepId().toBigInteger());
    }

    private void insertBbiep(Bbiep bbiep, ScoreUser user, BigInteger topLevelAsbiepId) {
        if (bbiep == null) {
            throw new IllegalArgumentException();
        }
        if (bbiep.getBbiepId() != null) {
            return;
        }

        ULong userId = ULong.valueOf(user.getUserId());

        BbiepRecord bbiepRecord = new BbiepRecord();
        bbiepRecord.setGuid(bbiep.getGuid());
        bbiepRecord.setBasedBccpManifestId(ULong.valueOf(bbiep.getBasedBccpManifestId()));
        bbiepRecord.setPath(bbiep.getPath());
        bbiepRecord.setHashPath(bbiep.getHashPath());
        bbiepRecord.setDefinition(bbiep.getDefinition());
        bbiepRecord.setRemark(bbiep.getRemark());
        bbiepRecord.setBizTerm(bbiep.getBizTerm());
        bbiepRecord.setCreatedBy(userId);
        bbiepRecord.setLastUpdatedBy(userId);
        bbiepRecord.setCreationTimestamp(LocalDateTime.now());
        bbiepRecord.setLastUpdateTimestamp(LocalDateTime.now());
        bbiepRecord.setOwnerTopLevelAsbiepId(ULong.valueOf(topLevelAsbiepId));

        bbiep.setBbiepId(dslContext().insertInto(BBIEP)
                .set(bbiepRecord)
                .returning(BBIEP.BBIEP_ID).fetchOne()
                .getBbiepId().toBigInteger());
    }

    private void insertAsbie(Asbie asbie, ScoreUser user, BigInteger topLevelAsbiepId) {
        if (asbie == null) {
            throw new IllegalArgumentException();
        }
        if (asbie.getAsbieId() != null) {
            return;
        }

        ULong userId = ULong.valueOf(user.getUserId());

        AsbieRecord asbieRecord = new AsbieRecord();
        asbieRecord.setGuid(asbie.getGuid());
        asbieRecord.setBasedAsccManifestId(ULong.valueOf(asbie.getBasedAsccManifestId()));
        asbieRecord.setPath(asbie.getPath());
        asbieRecord.setHashPath(asbie.getHashPath());
        asbieRecord.setFromAbieId(ULong.valueOf(asbie.getFromAbieId()));
        asbieRecord.setToAsbiepId(ULong.valueOf(asbie.getToAsbiepId()));
        asbieRecord.setDefinition(asbie.getDefinition());
        asbieRecord.setCardinalityMin(asbie.getCardinalityMin());
        asbieRecord.setCardinalityMax(asbie.getCardinalityMax());
        asbieRecord.setIsNillable((byte) (asbie.isNillable() ? 1 : 0));
        asbieRecord.setRemark(asbie.getRemark());
        asbieRecord.setIsUsed((byte) (asbie.isUsed() ? 1 : 0));
        asbieRecord.setSeqKey(BigDecimal.ZERO);
        asbieRecord.setCreatedBy(userId);
        asbieRecord.setLastUpdatedBy(userId);
        asbieRecord.setCreationTimestamp(LocalDateTime.now());
        asbieRecord.setLastUpdateTimestamp(LocalDateTime.now());
        asbieRecord.setOwnerTopLevelAsbiepId(ULong.valueOf(topLevelAsbiepId));

        asbie.setAsbieId(dslContext().insertInto(ASBIE)
                .set(asbieRecord)
                .returning(ASBIE.ASBIE_ID).fetchOne()
                .getAsbieId().toBigInteger());
    }

    private void insertBbie(Bbie bbie, ScoreUser user, BigInteger topLevelAsbiepId) {
        if (bbie == null) {
            throw new IllegalArgumentException();
        }
        if (bbie.getBbieId() != null) {
            return;
        }

        ULong userId = ULong.valueOf(user.getUserId());

        BbieRecord bbieRecord = new BbieRecord();
        bbieRecord.setGuid(bbie.getGuid());
        bbieRecord.setBasedBccManifestId(ULong.valueOf(bbie.getBasedBccManifestId()));
        bbieRecord.setPath(bbie.getPath());
        bbieRecord.setHashPath(bbie.getHashPath());
        bbieRecord.setFromAbieId(ULong.valueOf(bbie.getFromAbieId()));
        bbieRecord.setToBbiepId(ULong.valueOf(bbie.getToBbiepId()));
        if (bbie.getBdtPriRestriId() != null) {
            bbieRecord.setBdtPriRestriId(ULong.valueOf(bbie.getBdtPriRestriId()));
        }
        if (bbie.getCodeListId() != null) {
            bbieRecord.setCodeListId(ULong.valueOf(bbie.getCodeListId()));
        }
        if (bbie.getAgencyIdListId() != null) {
            bbieRecord.setAgencyIdListId(ULong.valueOf(bbie.getAgencyIdListId()));
        }
        bbieRecord.setDefaultValue(bbie.getDefaultValue());
        bbieRecord.setFixedValue(bbie.getFixedValue());
        bbieRecord.setDefinition(bbie.getDefinition());
        bbieRecord.setCardinalityMin(bbie.getCardinalityMin());
        bbieRecord.setCardinalityMax(bbie.getCardinalityMax());
        bbieRecord.setIsNillable((byte) (bbie.isNillable() ? 1 : 0));
        bbieRecord.setIsNull((byte) 0);
        bbieRecord.setRemark(bbie.getRemark());
        bbieRecord.setExample(bbie.getExample());
        bbieRecord.setIsUsed((byte) (bbie.isUsed() ? 1 : 0));
        bbieRecord.setSeqKey(BigDecimal.ZERO);
        bbieRecord.setCreatedBy(userId);
        bbieRecord.setLastUpdatedBy(userId);
        bbieRecord.setCreationTimestamp(LocalDateTime.now());
        bbieRecord.setLastUpdateTimestamp(LocalDateTime.now());
        bbieRecord.setOwnerTopLevelAsbiepId(ULong.valueOf(topLevelAsbiepId));

        bbie.setBbieId(dslContext().insertInto(BBIE)
                .set(bbieRecord)
                .returning(BBIE.BBIE_ID).fetchOne()
                .getBbieId().toBigInteger());
    }

    private void insertBbieSc(BbieSc bbieSc, ScoreUser user, BigInteger topLevelAsbiepId) {
        if (bbieSc == null) {
            throw new IllegalArgumentException();
        }
        if (bbieSc.getBbieScId() != null) {
            return;
        }

        ULong userId = ULong.valueOf(user.getUserId());

        BbieScRecord bbieScRecord = new BbieScRecord();
        bbieScRecord.setGuid(bbieSc.getGuid());
        bbieScRecord.setBasedDtScManifestId(ULong.valueOf(bbieSc.getBasedDtScManifestId()));
        bbieScRecord.setPath(bbieSc.getPath());
        bbieScRecord.setHashPath(bbieSc.getHashPath());
        bbieScRecord.setBbieId(ULong.valueOf(bbieSc.getBbieId()));
        if (bbieSc.getDtScPriRestriId() != null) {
            bbieScRecord.setDtScPriRestriId(ULong.valueOf(bbieSc.getDtScPriRestriId()));
        }
        if (bbieSc.getCodeListId() != null) {
            bbieScRecord.setCodeListId(ULong.valueOf(bbieSc.getCodeListId()));
        }
        if (bbieSc.getAgencyIdListId() != null) {
            bbieScRecord.setAgencyIdListId(ULong.valueOf(bbieSc.getAgencyIdListId()));
        }
        bbieScRecord.setDefaultValue(bbieSc.getDefaultValue());
        bbieScRecord.setFixedValue(bbieSc.getFixedValue());
        bbieScRecord.setDefinition(bbieSc.getDefinition());
        bbieScRecord.setCardinalityMin(bbieSc.getCardinalityMin());
        bbieScRecord.setCardinalityMax(bbieSc.getCardinalityMax());
        bbieScRecord.setBizTerm(bbieSc.getBizTerm());
        bbieScRecord.setRemark(bbieSc.getRemark());
        bbieScRecord.setExample(bbieSc.getExample());
        bbieScRecord.setIsUsed((byte) (bbieSc.isUsed() ? 1 : 0));
        bbieScRecord.setCreatedBy(userId);
        bbieScRecord.setLastUpdatedBy(userId);
        bbieScRecord.setCreationTimestamp(LocalDateTime.now());
        bbieScRecord.setLastUpdateTimestamp(LocalDateTime.now());
        bbieScRecord.setOwnerTopLevelAsbiepId(ULong.valueOf(topLevelAsbiepId));

        bbieSc.setBbieId(dslContext().insertInto(BBIE_SC)
                .set(bbieScRecord)
                .returning(BBIE_SC.BBIE_SC_ID).fetchOne()
                .getBbieScId().toBigInteger());
    }

}

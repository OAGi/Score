package org.oagi.score.gateway.http.api.bie_management.repository.jooq;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.bie_management.controller.payload.CreateBieRequest;
import org.oagi.score.gateway.http.api.bie_management.controller.payload.CreateBieResponse;
import org.oagi.score.gateway.http.api.bie_management.model.BieState;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.abie.Abie;
import org.oagi.score.gateway.http.api.bie_management.model.abie.AbieId;
import org.oagi.score.gateway.http.api.bie_management.model.asbie.Asbie;
import org.oagi.score.gateway.http.api.bie_management.model.asbie.AsbieId;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.Asbiep;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.AsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.WrappedAsbiep;
import org.oagi.score.gateway.http.api.bie_management.model.bbie.Bbie;
import org.oagi.score.gateway.http.api.bie_management.model.bbie.BbieId;
import org.oagi.score.gateway.http.api.bie_management.model.bbie_sc.BbieSc;
import org.oagi.score.gateway.http.api.bie_management.model.bbie_sc.BbieScId;
import org.oagi.score.gateway.http.api.bie_management.model.bbiep.Bbiep;
import org.oagi.score.gateway.http.api.bie_management.model.bbiep.BbiepId;
import org.oagi.score.gateway.http.api.bie_management.repository.BieCommandRepository;
import org.oagi.score.gateway.http.api.bie_management.repository.BiePackageCommandRepository;
import org.oagi.score.gateway.http.api.context_management.business_context.model.BusinessContextId;
import org.oagi.score.gateway.http.api.oas_management.model.OasMessageBodyId;
import org.oagi.score.gateway.http.api.oas_management.repository.OpenApiDocumentCommandRepository;
import org.oagi.score.gateway.http.api.oas_management.repository.OpenApiDocumentQueryRepository;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.model.base.ScoreDataAccessException;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;

public class JooqBieCommandRepository extends JooqBaseRepository implements BieCommandRepository {

    private final OpenApiDocumentQueryRepository openApiDocumentQueryRepository;

    private final OpenApiDocumentCommandRepository openApiDocumentCommandRepository;

    private final BiePackageCommandRepository biePackageCommandRepository;

    public JooqBieCommandRepository(DSLContext dslContext,
                                    ScoreUser requester,
                                    RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);

        this.openApiDocumentQueryRepository = repositoryFactory.openApiDocumentQueryRepository(requester);
        this.openApiDocumentCommandRepository = repositoryFactory.openApiDocumentCommandRepository(requester);
        this.biePackageCommandRepository = repositoryFactory.biePackageCommandRepository(requester);
    }

    @Override
    public void deleteByTopLevelAsbiepIdList(Collection<TopLevelAsbiepId> topLevelAsbiepIdList) {
        if (topLevelAsbiepIdList == null || topLevelAsbiepIdList.isEmpty()) {
            return;
        }

        setForeignKeyChecks(false);
        try {
            deleteAbieList(topLevelAsbiepIdList);
            deleteAsbieList(topLevelAsbiepIdList);
            deleteAsbiepList(topLevelAsbiepIdList);
            deleteBbieList(topLevelAsbiepIdList);
            deleteBbiepList(topLevelAsbiepIdList);
            deleteBbieScList(topLevelAsbiepIdList);
            deleteTopLevelAsbiepList(topLevelAsbiepIdList);
            deleteBizCtxAssignmentList(topLevelAsbiepIdList);

            // Issue #1492
            List<OasMessageBodyId> oasMessageBodyIdList = openApiDocumentQueryRepository.getOasMessageBodyIdList(topLevelAsbiepIdList);
            openApiDocumentCommandRepository.deleteMessageBodyList(oasMessageBodyIdList);

            // Issue #1615
            biePackageCommandRepository.deleteAssignedTopLevelAsbiepIdList(topLevelAsbiepIdList);
        } finally {
            setForeignKeyChecks(true);
        }

        resetSourceTopLevelAsbiepList(topLevelAsbiepIdList);

        // Issue #1635
        resetBasedTopLevelAsbiepList(topLevelAsbiepIdList);
    }

    private void deleteAbieList(Collection<TopLevelAsbiepId> topLevelAsbiepIdList) {
        dslContext().deleteFrom(ABIE)
                .where(ABIE.OWNER_TOP_LEVEL_ASBIEP_ID.in(valueOf(topLevelAsbiepIdList)))
                .execute();
    }

    private void deleteAsbieList(Collection<TopLevelAsbiepId> topLevelAsbiepIdList) {
        dslContext().deleteFrom(ASBIE)
                .where(ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID.in(valueOf(topLevelAsbiepIdList)))
                .execute();
    }

    private void deleteAsbiepList(Collection<TopLevelAsbiepId> topLevelAsbiepIdList) {
        dslContext().deleteFrom(ASBIEP)
                .where(ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.in(valueOf(topLevelAsbiepIdList)))
                .execute();
    }

    private void deleteBbieList(Collection<TopLevelAsbiepId> topLevelAsbiepIdList) {
        dslContext().deleteFrom(BBIE)
                .where(BBIE.OWNER_TOP_LEVEL_ASBIEP_ID.in(valueOf(topLevelAsbiepIdList)))
                .execute();
    }

    private void deleteBbiepList(Collection<TopLevelAsbiepId> topLevelAsbiepIdList) {
        dslContext().deleteFrom(BBIEP)
                .where(BBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.in(valueOf(topLevelAsbiepIdList)))
                .execute();
    }

    private void deleteBbieScList(Collection<TopLevelAsbiepId> topLevelAsbiepIdList) {
        dslContext().deleteFrom(BBIE_SC)
                .where(BBIE_SC.OWNER_TOP_LEVEL_ASBIEP_ID.in(valueOf(topLevelAsbiepIdList)))
                .execute();
    }

    private void deleteBizCtxAssignmentList(Collection<TopLevelAsbiepId> topLevelAsbiepIdList) {
        dslContext().deleteFrom(BIZ_CTX_ASSIGNMENT)
                .where(BIZ_CTX_ASSIGNMENT.TOP_LEVEL_ASBIEP_ID.in(valueOf(topLevelAsbiepIdList)))
                .execute();
    }

    private void deleteTopLevelAsbiepList(Collection<TopLevelAsbiepId> topLevelAsbiepIdList) {
        dslContext().deleteFrom(TOP_LEVEL_ASBIEP)
                .where(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.in(valueOf(topLevelAsbiepIdList)))
                .execute();
    }

    private void resetSourceTopLevelAsbiepList(Collection<TopLevelAsbiepId> topLevelAsbiepIdList) {
        dslContext().update(TOP_LEVEL_ASBIEP)
                .setNull(TOP_LEVEL_ASBIEP.SOURCE_TOP_LEVEL_ASBIEP_ID)
                .setNull(TOP_LEVEL_ASBIEP.SOURCE_ACTION)
                .setNull(TOP_LEVEL_ASBIEP.SOURCE_TIMESTAMP)
                .where(TOP_LEVEL_ASBIEP.SOURCE_TOP_LEVEL_ASBIEP_ID.in(valueOf(topLevelAsbiepIdList)))
                .execute();
    }

    private void resetBasedTopLevelAsbiepList(Collection<TopLevelAsbiepId> topLevelAsbiepIdList) {
        dslContext().update(TOP_LEVEL_ASBIEP)
                .setNull(TOP_LEVEL_ASBIEP.BASED_TOP_LEVEL_ASBIEP_ID)
                .where(TOP_LEVEL_ASBIEP.BASED_TOP_LEVEL_ASBIEP_ID.in(valueOf(topLevelAsbiepIdList)))
                .execute();
    }

    @Override
    public CreateBieResponse createBie(CreateBieRequest request) throws ScoreDataAccessException {
        ScoreUser requester = request.getRequester();
        WrappedAsbiep topLevelAsbiep = request.getTopLevelAsbiep();

        TopLevelAsbiepRecord topLevelAsbiepRecord = insertTopLevelAsbiep(request);
        request.getBizCtxIds().forEach(bizCtxId -> {
            insertBizCtxAssignment(topLevelAsbiepRecord, bizCtxId);
        });
        TopLevelAsbiepId topLevelAsbiepId = new TopLevelAsbiepId(topLevelAsbiepRecord.getTopLevelAsbiepId().toBigInteger());

        insertAbie(topLevelAsbiep.getRoleOfAbie(), topLevelAsbiepId);
        topLevelAsbiep.getAsbiep().setRoleOfAbieId(topLevelAsbiep.getRoleOfAbie().getAbieId());
        insertAsbiep(topLevelAsbiep.getAsbiep(), topLevelAsbiepId);
        dslContext().update(TOP_LEVEL_ASBIEP)
                .set(TOP_LEVEL_ASBIEP.ASBIEP_ID, valueOf(topLevelAsbiep.getAsbiep().getAsbiepId()))
                .where(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(topLevelAsbiepRecord.getTopLevelAsbiepId()))
                .execute();

        request.getAsbieList().forEach(asbie -> {
            if (asbie.getToAsbiep() == null && asbie.getRefTopLevelAsbiepId() == null) {
                return;
            }

            insertAbie(asbie.getFromAbie(), topLevelAsbiepId);
            asbie.getAsbie().setFromAbieId(asbie.getFromAbie().getAbieId());
            if (asbie.getToAsbiep() == null) {
                AsbiepId toAsbiepId = new AsbiepId(dslContext().select(TOP_LEVEL_ASBIEP.ASBIEP_ID)
                        .from(TOP_LEVEL_ASBIEP)
                        .where(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(valueOf(asbie.getRefTopLevelAsbiepId())))
                        .fetchOneInto(BigInteger.class));
                asbie.getAsbie().setToAsbiepId(toAsbiepId);
            } else {
                insertAbie(asbie.getToAsbiep().getRoleOfAbie(), topLevelAsbiepId);
                asbie.getToAsbiep().getAsbiep().setRoleOfAbieId(asbie.getToAsbiep().getRoleOfAbie().getAbieId());
                insertAsbiep(asbie.getToAsbiep().getAsbiep(), topLevelAsbiepId);
                asbie.getAsbie().setToAsbiepId(asbie.getToAsbiep().getAsbiep().getAsbiepId());
            }
            insertAsbie(asbie.getAsbie(), topLevelAsbiepId);
        });
        request.getBbieList().forEach(bbie -> {
            insertAbie(bbie.getFromAbie(), topLevelAsbiepId);
            bbie.getBbie().setFromAbieId(bbie.getFromAbie().getAbieId());
            insertBbiep(bbie.getToBbiep(), topLevelAsbiepId);
            bbie.getBbie().setToBbiepId(bbie.getToBbiep().getBbiepId());
            insertBbie(bbie.getBbie(), topLevelAsbiepId);
        });
        request.getBbieScList().forEach(bbieSc -> {
            insertBbie(bbieSc.getBbie(), topLevelAsbiepId);
            bbieSc.getBbieSc().setBbieId(bbieSc.getBbie().getBbieId());
            insertBbieSc(bbieSc.getBbieSc(), topLevelAsbiepId);
        });

        return new CreateBieResponse(topLevelAsbiepId);
    }

    private TopLevelAsbiepRecord insertTopLevelAsbiep(CreateBieRequest request) {
        ULong userId = ULong.valueOf(request.getRequester().userId().value());

        TopLevelAsbiepRecord topLevelAsbiepRecord = new TopLevelAsbiepRecord();
        topLevelAsbiepRecord.setReleaseId(dslContext().select(ASCCP_MANIFEST.RELEASE_ID)
                .from(ASCCP_MANIFEST)
                .where(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(
                        valueOf(request.getTopLevelAsbiep().getAsbiep().getBasedAsccpManifestId())))
                .fetchOneInto(ULong.class)
        );
        topLevelAsbiepRecord.setState(BieState.WIP.name());
        topLevelAsbiepRecord.setStatus(request.getStatus());
        topLevelAsbiepRecord.setVersion(request.getVersion());
        topLevelAsbiepRecord.setOwnerUserId(userId);
        topLevelAsbiepRecord.setLastUpdatedBy(userId);
        LocalDateTime timestamp = LocalDateTime.now();
        topLevelAsbiepRecord.setLastUpdateTimestamp(timestamp);

        if (request.getSourceTopLevelAsbiepId() != null) {
            topLevelAsbiepRecord.setSourceTopLevelAsbiepId(valueOf(request.getSourceTopLevelAsbiepId()));
            topLevelAsbiepRecord.setSourceAction(request.getSourceAction());
            LocalDateTime sourceTimestamp = (request.getSourceTimestamp() != null) ? request.getSourceTimestamp() : timestamp;
            topLevelAsbiepRecord.setSourceTimestamp(sourceTimestamp);
        }

        topLevelAsbiepRecord.setTopLevelAsbiepId(
                dslContext().insertInto(TOP_LEVEL_ASBIEP)
                        .set(topLevelAsbiepRecord)
                        .returning(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID)
                        .fetchOne().getTopLevelAsbiepId());
        return topLevelAsbiepRecord;
    }

    private void insertBizCtxAssignment(TopLevelAsbiepRecord topLevelAsbiepRecord,
                                        BusinessContextId bizCtxId) {

        BizCtxAssignmentRecord bizCtxAssignmentRecord = new BizCtxAssignmentRecord();
        bizCtxAssignmentRecord.setTopLevelAsbiepId(topLevelAsbiepRecord.getTopLevelAsbiepId());
        bizCtxAssignmentRecord.setBizCtxId(ULong.valueOf(bizCtxId.value()));

        dslContext().insertInto(BIZ_CTX_ASSIGNMENT)
                .set(bizCtxAssignmentRecord)
                .execute();
    }

    private void insertAbie(Abie abie, TopLevelAsbiepId topLevelAsbiepId) {
        if (abie == null) {
            throw new IllegalArgumentException();
        }
        if (abie.getAbieId() != null) {
            return;
        }

        ULong userId = ULong.valueOf(requester().userId().value());

        AbieRecord abieRecord = new AbieRecord();
        abieRecord.setGuid(abie.getGuid());
        abieRecord.setBasedAccManifestId(valueOf(abie.getBasedAccManifestId()));
        abieRecord.setPath(abie.getPath());
        abieRecord.setHashPath(abie.getHashPath());
        abieRecord.setDefinition(abie.getDefinition());
        abieRecord.setCreatedBy(userId);
        abieRecord.setLastUpdatedBy(userId);
        abieRecord.setCreationTimestamp(LocalDateTime.now());
        abieRecord.setLastUpdateTimestamp(LocalDateTime.now());
        abieRecord.setRemark(abie.getRemark());
        abieRecord.setBizTerm(abie.getBizTerm());
        abieRecord.setOwnerTopLevelAsbiepId(valueOf(topLevelAsbiepId));

        abie.setAbieId(new AbieId(dslContext().insertInto(ABIE)
                .set(abieRecord)
                .returning(ABIE.ABIE_ID).fetchOne()
                .getAbieId().toBigInteger()));
    }

    private void insertAsbiep(Asbiep asbiep, TopLevelAsbiepId topLevelAsbiepId) {
        if (asbiep == null) {
            throw new IllegalArgumentException();
        }
        if (asbiep.getAsbiepId() != null) {
            return;
        }

        ULong userId = ULong.valueOf(requester().userId().value());

        AsbiepRecord asbiepRecord = new AsbiepRecord();
        asbiepRecord.setGuid(asbiep.getGuid());
        asbiepRecord.setBasedAsccpManifestId(valueOf(asbiep.getBasedAsccpManifestId()));
        asbiepRecord.setPath(asbiep.getPath());
        asbiepRecord.setHashPath(asbiep.getHashPath());
        asbiepRecord.setRoleOfAbieId(valueOf(asbiep.getRoleOfAbieId()));
        asbiepRecord.setDefinition(asbiep.getDefinition());
        asbiepRecord.setRemark(asbiep.getRemark());
        asbiepRecord.setBizTerm(asbiep.getBizTerm());
        asbiepRecord.setDisplayName(asbiep.getDisplayName());
        asbiepRecord.setCreatedBy(userId);
        asbiepRecord.setLastUpdatedBy(userId);
        asbiepRecord.setCreationTimestamp(LocalDateTime.now());
        asbiepRecord.setLastUpdateTimestamp(LocalDateTime.now());
        asbiepRecord.setOwnerTopLevelAsbiepId(valueOf(topLevelAsbiepId));

        asbiep.setAsbiepId(new AsbiepId(dslContext().insertInto(ASBIEP)
                .set(asbiepRecord)
                .returning(ASBIEP.ASBIEP_ID).fetchOne()
                .getAsbiepId().toBigInteger()));
    }

    private void insertBbiep(Bbiep bbiep, TopLevelAsbiepId topLevelAsbiepId) {
        if (bbiep == null) {
            throw new IllegalArgumentException();
        }
        if (bbiep.getBbiepId() != null) {
            return;
        }

        ULong userId = ULong.valueOf(requester().userId().value());

        BbiepRecord bbiepRecord = new BbiepRecord();
        bbiepRecord.setGuid(bbiep.getGuid());
        bbiepRecord.setBasedBccpManifestId(valueOf(bbiep.getBasedBccpManifestId()));
        bbiepRecord.setPath(bbiep.getPath());
        bbiepRecord.setHashPath(bbiep.getHashPath());
        bbiepRecord.setDefinition(bbiep.getDefinition());
        bbiepRecord.setRemark(bbiep.getRemark());
        bbiepRecord.setBizTerm(bbiep.getBizTerm());
        bbiepRecord.setDisplayName(bbiep.getDisplayName());
        bbiepRecord.setCreatedBy(userId);
        bbiepRecord.setLastUpdatedBy(userId);
        bbiepRecord.setCreationTimestamp(LocalDateTime.now());
        bbiepRecord.setLastUpdateTimestamp(LocalDateTime.now());
        bbiepRecord.setOwnerTopLevelAsbiepId(valueOf(topLevelAsbiepId));

        bbiep.setBbiepId(new BbiepId(dslContext().insertInto(BBIEP)
                .set(bbiepRecord)
                .returning(BBIEP.BBIEP_ID).fetchOne()
                .getBbiepId().toBigInteger()));
    }

    private void insertAsbie(Asbie asbie, TopLevelAsbiepId topLevelAsbiepId) {
        if (asbie == null) {
            throw new IllegalArgumentException();
        }
        if (asbie.getAsbieId() != null) {
            return;
        }

        ULong userId = ULong.valueOf(requester().userId().value());

        AsbieRecord asbieRecord = new AsbieRecord();
        asbieRecord.setGuid(asbie.getGuid());
        asbieRecord.setBasedAsccManifestId(valueOf(asbie.getBasedAsccManifestId()));
        asbieRecord.setPath(asbie.getPath());
        asbieRecord.setHashPath(asbie.getHashPath());
        asbieRecord.setFromAbieId(valueOf(asbie.getFromAbieId()));
        asbieRecord.setToAsbiepId(valueOf(asbie.getToAsbiepId()));
        asbieRecord.setDefinition(asbie.getDefinition());
        asbieRecord.setCardinalityMin(asbie.getCardinalityMin());
        asbieRecord.setCardinalityMax(asbie.getCardinalityMax());
        asbieRecord.setIsNillable((byte) (asbie.isNillable() ? 1 : 0));
        asbieRecord.setRemark(asbie.getRemark());
        asbieRecord.setIsDeprecated((byte) (asbie.isDeprecated() ? 1 : 0));
        asbieRecord.setIsUsed((byte) (asbie.isUsed() ? 1 : 0));
        asbieRecord.setSeqKey(BigDecimal.ZERO);
        asbieRecord.setCreatedBy(userId);
        asbieRecord.setLastUpdatedBy(userId);
        asbieRecord.setCreationTimestamp(LocalDateTime.now());
        asbieRecord.setLastUpdateTimestamp(LocalDateTime.now());
        asbieRecord.setOwnerTopLevelAsbiepId(valueOf(topLevelAsbiepId));

        asbie.setAsbieId(new AsbieId(dslContext().insertInto(ASBIE)
                .set(asbieRecord)
                .returning(ASBIE.ASBIE_ID).fetchOne()
                .getAsbieId().toBigInteger()));
    }

    private void insertBbie(Bbie bbie, TopLevelAsbiepId topLevelAsbiepId) {
        if (bbie == null) {
            throw new IllegalArgumentException();
        }
        if (bbie.getBbieId() != null) {
            return;
        }

        ULong userId = ULong.valueOf(requester().userId().value());

        BbieRecord bbieRecord = new BbieRecord();
        bbieRecord.setGuid(bbie.getGuid());
        bbieRecord.setBasedBccManifestId(valueOf(bbie.getBasedBccManifestId()));
        bbieRecord.setPath(bbie.getPath());
        bbieRecord.setHashPath(bbie.getHashPath());
        bbieRecord.setFromAbieId(valueOf(bbie.getFromAbieId()));
        bbieRecord.setToBbiepId(valueOf(bbie.getToBbiepId()));
        if (bbie.getXbtManifestId() != null) {
            bbieRecord.setXbtManifestId(valueOf(bbie.getXbtManifestId()));
        }
        if (bbie.getCodeListManifestId() != null) {
            bbieRecord.setCodeListManifestId(valueOf(bbie.getCodeListManifestId()));
        }
        if (bbie.getAgencyIdListManifestId() != null) {
            bbieRecord.setAgencyIdListManifestId(valueOf(bbie.getAgencyIdListManifestId()));
        }
        bbieRecord.setDefaultValue(bbie.getDefaultValue());
        bbieRecord.setFixedValue(bbie.getFixedValue());
        if (bbie.getFacetMinLength() != null) {
            bbieRecord.setFacetMinLength(ULong.valueOf(bbie.getFacetMinLength()));
        }
        if (bbie.getFacetMaxLength() != null) {
            bbieRecord.setFacetMaxLength(ULong.valueOf(bbie.getFacetMaxLength()));
        }
        bbieRecord.setFacetPattern(bbie.getFacetPattern());
        bbieRecord.setDefinition(bbie.getDefinition());
        bbieRecord.setCardinalityMin(bbie.getCardinalityMin());
        bbieRecord.setCardinalityMax(bbie.getCardinalityMax());
        bbieRecord.setIsNillable((byte) (bbie.isNillable() ? 1 : 0));
        bbieRecord.setIsNull((byte) 0);
        bbieRecord.setRemark(bbie.getRemark());
        bbieRecord.setExample(bbie.getExample());
        bbieRecord.setIsDeprecated((byte) (bbie.isDeprecated() ? 1 : 0));
        bbieRecord.setIsUsed((byte) (bbie.isUsed() ? 1 : 0));
        bbieRecord.setSeqKey(BigDecimal.ZERO);
        bbieRecord.setCreatedBy(userId);
        bbieRecord.setLastUpdatedBy(userId);
        bbieRecord.setCreationTimestamp(LocalDateTime.now());
        bbieRecord.setLastUpdateTimestamp(LocalDateTime.now());
        bbieRecord.setOwnerTopLevelAsbiepId(valueOf(topLevelAsbiepId));

        bbie.setBbieId(new BbieId(dslContext().insertInto(BBIE)
                .set(bbieRecord)
                .returning(BBIE.BBIE_ID).fetchOne()
                .getBbieId().toBigInteger()));
    }

    private void insertBbieSc(BbieSc bbieSc, TopLevelAsbiepId topLevelAsbiepId) {
        if (bbieSc == null) {
            throw new IllegalArgumentException();
        }
        if (bbieSc.getBbieScId() != null) {
            return;
        }

        ULong userId = ULong.valueOf(requester().userId().value());

        BbieScRecord bbieScRecord = new BbieScRecord();
        bbieScRecord.setGuid(bbieSc.getGuid());
        bbieScRecord.setBasedDtScManifestId(valueOf(bbieSc.getBasedDtScManifestId()));
        bbieScRecord.setPath(bbieSc.getPath());
        bbieScRecord.setHashPath(bbieSc.getHashPath());
        bbieScRecord.setBbieId(valueOf(bbieSc.getBbieId()));
        if (bbieSc.getXbtManifestId() != null) {
            bbieScRecord.setXbtManifestId(valueOf(bbieSc.getXbtManifestId()));
        }
        if (bbieSc.getCodeListManifestId() != null) {
            bbieScRecord.setCodeListManifestId(valueOf(bbieSc.getCodeListManifestId()));
        }
        if (bbieSc.getAgencyIdListManifestId() != null) {
            bbieScRecord.setAgencyIdListManifestId(valueOf(bbieSc.getAgencyIdListManifestId()));
        }
        bbieScRecord.setDefaultValue(bbieSc.getDefaultValue());
        bbieScRecord.setFixedValue(bbieSc.getFixedValue());
        if (bbieSc.getFacetMinLength() != null) {
            bbieScRecord.setFacetMinLength(ULong.valueOf(bbieSc.getFacetMinLength()));
        }
        if (bbieSc.getFacetMaxLength() != null) {
            bbieScRecord.setFacetMaxLength(ULong.valueOf(bbieSc.getFacetMaxLength()));
        }
        bbieScRecord.setFacetPattern(bbieSc.getFacetPattern());
        bbieScRecord.setDefinition(bbieSc.getDefinition());
        bbieScRecord.setCardinalityMin(bbieSc.getCardinalityMin());
        bbieScRecord.setCardinalityMax(bbieSc.getCardinalityMax());
        bbieScRecord.setBizTerm(bbieSc.getBizTerm());
        bbieScRecord.setRemark(bbieSc.getRemark());
        bbieScRecord.setDisplayName(bbieSc.getDisplayName());
        bbieScRecord.setExample(bbieSc.getExample());
        bbieScRecord.setIsDeprecated((byte) (bbieSc.isDeprecated() ? 1 : 0));
        bbieScRecord.setIsUsed((byte) (bbieSc.isUsed() ? 1 : 0));
        bbieScRecord.setCreatedBy(userId);
        bbieScRecord.setLastUpdatedBy(userId);
        bbieScRecord.setCreationTimestamp(LocalDateTime.now());
        bbieScRecord.setLastUpdateTimestamp(LocalDateTime.now());
        bbieScRecord.setOwnerTopLevelAsbiepId(valueOf(topLevelAsbiepId));

        bbieSc.setBbieScId(new BbieScId(dslContext().insertInto(BBIE_SC)
                .set(bbieScRecord)
                .returning(BBIE_SC.BBIE_SC_ID).fetchOne()
                .getBbieScId().toBigInteger()));
    }

}

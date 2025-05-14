package org.oagi.score.gateway.http.api.bie_management.repository.jooq;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.bie_management.controller.payload.UpsertBbieRequest;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.bbie.BbieDetailsRecord;
import org.oagi.score.gateway.http.api.bie_management.model.bbie.BbieNode;
import org.oagi.score.gateway.http.api.bie_management.repository.BbieCommandRepository;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtAwdPriSummaryRecord;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.BbieRecord;
import org.oagi.score.gateway.http.common.util.ScoreGuidUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;
import static org.oagi.score.gateway.http.common.util.Utility.emptyToNull;

public class JooqBbieCommandRepository extends JooqBaseRepository implements BbieCommandRepository {

    public JooqBbieCommandRepository(DSLContext dslContext, ScoreUser requester, RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);
    }

    @Override
    public BbieNode.Bbie upsertBbie(UpsertBbieRequest request) {
        BbieNode.Bbie bbie = request.getBbie();
        ULong topLevelAsbiepId = valueOf(request.getTopLevelAsbiepId());
        String hashPath = bbie.getHashPath();
        BbieRecord bbieRecord = dslContext().selectFrom(BBIE)
                .where(and(
                        BBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(topLevelAsbiepId),
                        BBIE.HASH_PATH.eq(hashPath)
                ))
                .fetchOptional().orElse(null);

        ScoreUser requester = requester();
        ULong requesterId = valueOf(requester.userId());
        LocalDateTime timestamp = LocalDateTime.now();

        if (bbieRecord == null) {
            bbieRecord = new BbieRecord();
            bbieRecord.setGuid(ScoreGuidUtils.randomGuid());
            bbieRecord.setBasedBccManifestId(valueOf(bbie.getBasedBccManifestId()));
            bbieRecord.setPath(bbie.getPath());
            bbieRecord.setHashPath(hashPath);
            bbieRecord.setFromAbieId(dslContext().select(ABIE.ABIE_ID)
                    .from(ABIE)
                    .where(and(
                            ABIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(topLevelAsbiepId),
                            ABIE.HASH_PATH.eq(bbie.getFromAbieHashPath())
                    ))
                    .fetchOneInto(ULong.class));
            bbieRecord.setToBbiepId(dslContext().select(BBIEP.BBIEP_ID)
                    .from(BBIEP)
                    .where(and(
                            BBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.eq(topLevelAsbiepId),
                            BBIEP.HASH_PATH.eq(bbie.getToBbiepHashPath())
                    ))
                    .fetchOneInto(ULong.class));
            bbieRecord.setSeqKey(BigDecimal.valueOf(bbie.getSeqKey().longValue()));

            if (bbie.getUsed() != null) {
                bbieRecord.setIsUsed((byte) (bbie.getUsed() ? 1 : 0));
            }

            if (bbie.getNillable() != null) {
                bbieRecord.setIsNillable((byte) (bbie.getNillable() ? 1 : 0));
            }

            if (bbie.getDeprecated() != null) {
                bbieRecord.setIsDeprecated((byte) (bbie.getDeprecated() ? 1 : 0));
            }

            bbieRecord.setDefinition(bbie.getDefinition());

            var accQuery = repositoryFactory().accQueryRepository(requester());
            BccSummaryRecord bccRecord = accQuery.getBccSummary(bbie.getBasedBccManifestId());
            if (bccRecord == null) {
                throw new IllegalArgumentException();
            }

            if (bbie.getCardinalityMin() == null) {
                bbieRecord.setCardinalityMin(bccRecord.cardinality().min());
            } else {
                bbieRecord.setCardinalityMin(bbie.getCardinalityMin());
            }

            if (bbie.getCardinalityMax() == null) {
                bbieRecord.setCardinalityMax(bccRecord.cardinality().max());
            } else {
                bbieRecord.setCardinalityMax(bbie.getCardinalityMax());
            }
            if (bbieRecord.getCardinalityMax() > 0 && bbieRecord.getCardinalityMin() > bbieRecord.getCardinalityMax()) {
                throw new IllegalArgumentException("Cardinality is not valid.");
            }

            if (bbie.getFacetMinLength() != null) {
                bbieRecord.setFacetMinLength(ULong.valueOf(bbie.getFacetMinLength()));
            } else {
                bbieRecord.setFacetMinLength(null);
            }
            if (bbie.getFacetMaxLength() != null) {
                bbieRecord.setFacetMaxLength(ULong.valueOf(bbie.getFacetMaxLength()));
            } else {
                bbieRecord.setFacetMaxLength(null);
            }
            if (bbieRecord.getFacetMinLength() != null && bbieRecord.getFacetMaxLength() != null) {
                if (bbieRecord.getFacetMinLength().intValue() < 0) {
                    throw new IllegalArgumentException("Minimum Length must be greater than or equals to 0.");
                }
                if (bbieRecord.getFacetMinLength().compareTo(bbieRecord.getFacetMaxLength()) > 0) {
                    throw new IllegalArgumentException("Minimum Length must be less than equals to Maximum Length.");
                }
            }
            if (StringUtils.hasLength(bbie.getFacetPattern())) {
                bbieRecord.setFacetPattern(bbie.getFacetPattern());
            } else {
                bbieRecord.setFacetPattern(null);
            }

            bbieRecord.setExample(bbie.getExample());
            bbieRecord.setRemark(bbie.getRemark());

            if (StringUtils.hasLength(bbie.getDefaultValue())) {
                bbieRecord.setDefaultValue(bbie.getDefaultValue());
                bbieRecord.setFixedValue(null);
            } else if (StringUtils.hasLength(bbie.getFixedValue())) {
                bbieRecord.setDefaultValue(null);
                bbieRecord.setFixedValue(bbie.getFixedValue());
            }

            if (bbie.isEmptyPrimitive()) {
                var bccpQuery = repositoryFactory().bccpQueryRepository(requester());
                BccpSummaryRecord bccpRecord = bccpQuery.getBccpSummary(bccRecord.toBccpManifestId());

                var dtQuery = repositoryFactory().dtQueryRepository(requester());
                List<DtAwdPriSummaryRecord> dtAwdPriList = dtQuery.getDtAwdPriSummaryList(bccpRecord.dtManifestId());
                dtAwdPriList = dtAwdPriList.stream().filter(e -> e.isDefault())
                        .collect(Collectors.toList());
                if (dtAwdPriList.size() != 1) {
                    throw new IllegalArgumentException();
                }

                DtAwdPriSummaryRecord defaultBdtPriRestri = dtAwdPriList.get(0);
                if (defaultBdtPriRestri.codeListManifestId() != null) {
                    bbieRecord.setXbtManifestId(null);
                    bbieRecord.setCodeListManifestId(valueOf(defaultBdtPriRestri.codeListManifestId()));
                    bbieRecord.setAgencyIdListManifestId(null);
                } else if (defaultBdtPriRestri.agencyIdListManifestId() != null) {
                    bbieRecord.setXbtManifestId(null);
                    bbieRecord.setCodeListManifestId(null);
                    bbieRecord.setAgencyIdListManifestId(valueOf(defaultBdtPriRestri.agencyIdListManifestId()));
                } else {
                    bbieRecord.setXbtManifestId(valueOf(defaultBdtPriRestri.xbtManifestId()));
                    bbieRecord.setCodeListManifestId(null);
                    bbieRecord.setAgencyIdListManifestId(null);
                }
            } else {
                if (bbie.getXbtManifestId() != null) {
                    bbieRecord.setXbtManifestId(valueOf(bbie.getXbtManifestId()));
                    bbieRecord.setCodeListManifestId(null);
                    bbieRecord.setAgencyIdListManifestId(null);
                } else if (bbie.getCodeListManifestId() != null) {
                    bbieRecord.setXbtManifestId(null);
                    bbieRecord.setCodeListManifestId(valueOf(bbie.getCodeListManifestId()));
                    bbieRecord.setAgencyIdListManifestId(null);
                } else if (bbie.getAgencyIdListManifestId() != null) {
                    bbieRecord.setXbtManifestId(null);
                    bbieRecord.setCodeListManifestId(null);
                    bbieRecord.setAgencyIdListManifestId(valueOf(bbie.getAgencyIdListManifestId()));
                }
            }

            bbieRecord.setOwnerTopLevelAsbiepId(topLevelAsbiepId);

            bbieRecord.setCreatedBy(requesterId);
            bbieRecord.setLastUpdatedBy(requesterId);
            bbieRecord.setCreationTimestamp(timestamp);
            bbieRecord.setLastUpdateTimestamp(timestamp);

            bbieRecord.setBbieId(
                    dslContext().insertInto(BBIE)
                            .set(bbieRecord)
                            .returning(BBIE.BBIE_ID)
                            .fetchOne().getBbieId()
            );
        } else {
            bbieRecord.setSeqKey(BigDecimal.valueOf(bbie.getSeqKey().longValue()));

            if (bbie.getUsed() != null) {
                bbieRecord.setIsUsed((byte) (bbie.getUsed() ? 1 : 0));
            }

            if (bbie.getNillable() != null) {
                bbieRecord.setIsNillable((byte) (bbie.getNillable() ? 1 : 0));
            }

            if (bbie.getDeprecated() != null) {
                bbieRecord.setIsDeprecated((byte) (bbie.getDeprecated() ? 1 : 0));
            }

            if (bbie.getDefinition() != null) {
                bbieRecord.setDefinition(emptyToNull(bbie.getDefinition()));
            }

            if (bbie.getCardinalityMin() != null) {
                bbieRecord.setCardinalityMin(bbie.getCardinalityMin());
            }

            if (bbie.getCardinalityMax() != null) {
                bbieRecord.setCardinalityMax(bbie.getCardinalityMax());
            }

            if (bbieRecord.getCardinalityMax() > 0 && bbieRecord.getCardinalityMin() > bbieRecord.getCardinalityMax()) {
                throw new IllegalArgumentException("Cardinality is not valid.");
            }

            if (bbie.getFacetMinLength() != null) {
                bbieRecord.setFacetMinLength(ULong.valueOf(bbie.getFacetMinLength()));
            } else {
                bbieRecord.setFacetMinLength(null);
            }
            if (bbie.getFacetMaxLength() != null) {
                bbieRecord.setFacetMaxLength(ULong.valueOf(bbie.getFacetMaxLength()));
            } else {
                bbieRecord.setFacetMaxLength(null);
            }
            if (bbieRecord.getFacetMinLength() != null && bbieRecord.getFacetMaxLength() != null) {
                if (bbieRecord.getFacetMinLength().intValue() < 0) {
                    throw new IllegalArgumentException("Minimum Length must be greater than or equals to 0.");
                }
                if (bbieRecord.getFacetMinLength().compareTo(bbieRecord.getFacetMaxLength()) > 0) {
                    throw new IllegalArgumentException("Minimum Length must be less than equals to Maximum Length.");
                }
            }
            if (StringUtils.hasLength(bbie.getFacetPattern())) {
                bbieRecord.setFacetPattern(bbie.getFacetPattern());
            } else {
                bbieRecord.setFacetPattern(null);
            }

            if (bbie.getExample() != null) {
                bbieRecord.setExample(emptyToNull(bbie.getExample()));
            }

            if (bbie.getRemark() != null) {
                bbieRecord.setRemark(emptyToNull(bbie.getRemark()));
            }

            if (StringUtils.hasLength(bbie.getDefaultValue())) {
                bbieRecord.setDefaultValue(bbie.getDefaultValue());
                bbieRecord.setFixedValue(null);
            } else if (StringUtils.hasLength(bbie.getFixedValue())) {
                bbieRecord.setDefaultValue(null);
                bbieRecord.setFixedValue(bbie.getFixedValue());
            }

            if (!bbie.isEmptyPrimitive()) {
                if (bbie.getXbtManifestId() != null) {
                    bbieRecord.setXbtManifestId(valueOf(bbie.getXbtManifestId()));
                    bbieRecord.setCodeListManifestId(null);
                    bbieRecord.setAgencyIdListManifestId(null);
                } else if (bbie.getCodeListManifestId() != null) {
                    bbieRecord.setXbtManifestId(null);
                    bbieRecord.setCodeListManifestId(valueOf(bbie.getCodeListManifestId()));
                    bbieRecord.setAgencyIdListManifestId(null);
                } else if (bbie.getAgencyIdListManifestId() != null) {
                    bbieRecord.setXbtManifestId(null);
                    bbieRecord.setCodeListManifestId(null);
                    bbieRecord.setAgencyIdListManifestId(valueOf(bbie.getAgencyIdListManifestId()));
                }
            }

            if (bbieRecord.changed()) {
                bbieRecord.setLastUpdatedBy(requesterId);
                bbieRecord.setLastUpdateTimestamp(timestamp);
                bbieRecord.update(
                        BBIE.SEQ_KEY,
                        BBIE.IS_USED,
                        BBIE.IS_NILLABLE,
                        BBIE.IS_DEPRECATED,
                        BBIE.DEFINITION,
                        BBIE.CARDINALITY_MIN,
                        BBIE.CARDINALITY_MAX,
                        BBIE.FACET_MIN_LENGTH,
                        BBIE.FACET_MAX_LENGTH,
                        BBIE.FACET_PATTERN,
                        BBIE.EXAMPLE,
                        BBIE.REMARK,
                        BBIE.DEFAULT_VALUE,
                        BBIE.FIXED_VALUE,
                        BBIE.XBT_MANIFEST_ID,
                        BBIE.CODE_LIST_MANIFEST_ID,
                        BBIE.AGENCY_ID_LIST_MANIFEST_ID,
                        BBIE.LAST_UPDATED_BY,
                        BBIE.LAST_UPDATE_TIMESTAMP
                );
            }
        }
        return getBbie(request.getTopLevelAsbiepId(), hashPath);
    }

    private BbieNode.Bbie getBbie(TopLevelAsbiepId topLevelAsbiepId, String hashPath) {
        BbieNode.Bbie bbie = new BbieNode.Bbie();
        bbie.setHashPath(hashPath);

        var query = repositoryFactory().bbieQueryRepository(requester());
        BbieDetailsRecord bbieDetails = query.getBbieDetails(topLevelAsbiepId, hashPath);
        if (bbieDetails != null) {
            bbie.setOwnerTopLevelAsbiepId(bbieDetails.ownerTopLevelAsbiep().topLevelAsbiepId());
            bbie.setBbieId(bbieDetails.bbieId());
            bbie.setToBbiepId(bbieDetails.toBbiepId());
            bbie.setFromAbieHashPath(dslContext().select(ABIE.HASH_PATH)
                    .from(ABIE)
                    .where(and(
                            ABIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(valueOf(topLevelAsbiepId)),
                            ABIE.ABIE_ID.eq(valueOf(bbieDetails.fromAbieId()))
                    ))
                    .fetchOneInto(String.class));
            bbie.setToBbiepHashPath(dslContext().select(BBIEP.HASH_PATH)
                    .from(BBIEP)
                    .where(and(
                            BBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.eq(valueOf(topLevelAsbiepId)),
                            BBIEP.BBIEP_ID.eq(valueOf(bbieDetails.toBbiepId()))
                    ))
                    .fetchOneInto(String.class));
            bbie.setBasedBccManifestId(bbieDetails.basedBcc().bccManifestId());
            bbie.setUsed(bbieDetails.used());
            bbie.setGuid(bbieDetails.guid().value());
            bbie.setCardinalityMin(bbieDetails.cardinality().min());
            bbie.setCardinalityMax(bbieDetails.cardinality().max());
            if (bbieDetails.facet() != null) {
                bbie.setFacetMinLength(bbieDetails.facet().minLength());
                bbie.setFacetMaxLength(bbieDetails.facet().maxLength());
                bbie.setFacetPattern(bbieDetails.facet().pattern());
            }
            bbie.setNillable(bbieDetails.nillable());
            bbie.setRemark(bbieDetails.remark());
            bbie.setDefinition(bbieDetails.definition());
            if (bbieDetails.valueConstraint() != null) {
                bbie.setDefaultValue(bbieDetails.valueConstraint().defaultValue());
                bbie.setFixedValue(bbieDetails.valueConstraint().fixedValue());
            }
            bbie.setExample(bbieDetails.example());
            bbie.setDeprecated(bbieDetails.deprecated());

            bbie.setXbtManifestId((bbieDetails.primitiveRestriction().xbtManifestId() != null) ?
                    bbieDetails.primitiveRestriction().xbtManifestId() : null);
            bbie.setCodeListManifestId((bbieDetails.primitiveRestriction().codeListManifestId() != null) ?
                    bbieDetails.primitiveRestriction().codeListManifestId() : null);
            bbie.setAgencyIdListManifestId((bbieDetails.primitiveRestriction().agencyIdListManifestId() != null) ?
                    bbieDetails.primitiveRestriction().agencyIdListManifestId() : null);
        }

        return bbie;
    }
}

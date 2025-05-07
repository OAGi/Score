package org.oagi.score.gateway.http.api.bie_management.repository.jooq;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.bie_management.controller.payload.UpsertBbieScRequest;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.bbie_sc.BbieScDetailsRecord;
import org.oagi.score.gateway.http.api.bie_management.model.bbie_sc.BbieScNode;
import org.oagi.score.gateway.http.api.bie_management.repository.BbieScCommandRepository;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScAwdPriSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScSummaryRecord;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.BbieScRecord;
import org.oagi.score.gateway.http.common.util.ScoreGuidUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.BBIE;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.BBIE_SC;
import static org.oagi.score.gateway.http.common.util.Utility.emptyToNull;

public class JooqBbieScCommandRepository extends JooqBaseRepository implements BbieScCommandRepository {
    
    public JooqBbieScCommandRepository(DSLContext dslContext, ScoreUser requester, RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);
    }

    @Override
    public BbieScNode.BbieSc upsertBbieSc(UpsertBbieScRequest request) {
        BbieScNode.BbieSc bbieSc = request.getBbieSc();
        ULong topLevelAsbiepId = valueOf(request.getTopLevelAsbiepId());
        String hashPath = bbieSc.getHashPath();
        BbieScRecord bbieScRecord = dslContext().selectFrom(BBIE_SC)
                .where(and(
                        BBIE_SC.OWNER_TOP_LEVEL_ASBIEP_ID.eq(topLevelAsbiepId),
                        BBIE_SC.HASH_PATH.eq(hashPath)
                ))
                .fetchOptional().orElse(null);

        ScoreUser requester = requester();
        ULong userId = valueOf(requester.userId());
        LocalDateTime timestamp = LocalDateTime.now();

        if (bbieScRecord == null) {
            bbieScRecord = new BbieScRecord();
            bbieScRecord.setGuid(ScoreGuidUtils.randomGuid());
            bbieScRecord.setBasedDtScManifestId(valueOf(bbieSc.getBasedDtScManifestId()));
            bbieScRecord.setPath(bbieSc.getPath());
            bbieScRecord.setHashPath(hashPath);
            bbieScRecord.setBbieId(dslContext().select(BBIE.BBIE_ID)
                    .from(BBIE)
                    .where(and(
                            BBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(topLevelAsbiepId),
                            BBIE.HASH_PATH.eq(bbieSc.getBbieHashPath())
                    ))
                    .fetchOneInto(ULong.class));

            if (bbieSc.getUsed() != null) {
                bbieScRecord.setIsUsed((byte) (bbieSc.getUsed() ? 1 : 0));
            }

            if (bbieSc.getDeprecated() != null) {
                bbieScRecord.setIsDeprecated((byte) (bbieSc.getDeprecated() ? 1 : 0));
            }

            bbieScRecord.setDefinition(bbieSc.getDefinition());

            var dtQuery = repositoryFactory().dtQueryRepository(requester());
            DtScSummaryRecord dtScRecord = dtQuery.getDtScSummary(bbieSc.getBasedDtScManifestId());
            if (dtScRecord == null) {
                throw new IllegalArgumentException();
            }
            if (bbieSc.getCardinalityMin() == null) {
                bbieScRecord.setCardinalityMin(dtScRecord.cardinality().min());
            } else {
                bbieScRecord.setCardinalityMin(bbieSc.getCardinalityMin());
            }

            if (bbieSc.getCardinalityMax() == null) {
                bbieScRecord.setCardinalityMax(dtScRecord.cardinality().max());
            } else {
                bbieScRecord.setCardinalityMax(bbieSc.getCardinalityMax());
            }
            if (bbieScRecord.getCardinalityMax() > 0 && bbieScRecord.getCardinalityMin() > bbieScRecord.getCardinalityMax()) {
                throw new IllegalArgumentException("Cardinality is not valid.");
            }

            if (bbieSc.getFacetMinLength() != null) {
                bbieScRecord.setFacetMinLength(ULong.valueOf(bbieSc.getFacetMinLength()));
            } else {
                bbieScRecord.setFacetMinLength(null);
            }
            if (bbieSc.getFacetMaxLength() != null) {
                bbieScRecord.setFacetMaxLength(ULong.valueOf(bbieSc.getFacetMaxLength()));
            } else {
                bbieScRecord.setFacetMaxLength(null);
            }
            if (bbieScRecord.getFacetMinLength() != null && bbieScRecord.getFacetMaxLength() != null) {
                if (bbieScRecord.getFacetMinLength().intValue() < 0) {
                    throw new IllegalArgumentException("Minimum Length must be greater than or equals to 0.");
                }
                if (bbieScRecord.getFacetMinLength().compareTo(bbieScRecord.getFacetMaxLength()) > 0) {
                    throw new IllegalArgumentException("Minimum Length must be less than equals to Maximum Length.");
                }
            }
            if (StringUtils.hasLength(bbieSc.getFacetPattern())) {
                bbieScRecord.setFacetPattern(bbieSc.getFacetPattern());
            } else {
                bbieScRecord.setFacetPattern(null);
            }

            bbieScRecord.setBizTerm(bbieSc.getBizTerm());
            bbieScRecord.setExample(bbieSc.getExample());
            bbieScRecord.setRemark(bbieSc.getRemark());
            bbieScRecord.setDisplayName(bbieSc.getDisplayName());

            if (StringUtils.hasLength(bbieSc.getDefaultValue())) {
                bbieScRecord.setDefaultValue(bbieSc.getDefaultValue());
                bbieScRecord.setFixedValue(null);
            } else if (StringUtils.hasLength(bbieSc.getFixedValue())) {
                bbieScRecord.setDefaultValue(null);
                bbieScRecord.setFixedValue(bbieSc.getFixedValue());
            }

            if (bbieSc.isEmptyPrimitive()) {
                List<DtScAwdPriSummaryRecord> dtScAwdPriList = dtQuery.getDtScAwdPriSummaryList(bbieSc.getBasedDtScManifestId());
                dtScAwdPriList = dtScAwdPriList.stream().filter(e -> e.isDefault())
                        .collect(Collectors.toList());
                if (dtScAwdPriList.size() != 1) {
                    throw new IllegalArgumentException();
                }

                DtScAwdPriSummaryRecord defaultDtScAwdPri = dtScAwdPriList.get(0);
                if (defaultDtScAwdPri.codeListManifestId() != null) {
                    bbieScRecord.setXbtManifestId(null);
                    bbieScRecord.setCodeListManifestId(valueOf(defaultDtScAwdPri.codeListManifestId()));
                    bbieScRecord.setAgencyIdListManifestId(null);
                } else if (defaultDtScAwdPri.agencyIdListManifestId() != null) {
                    bbieScRecord.setXbtManifestId(null);
                    bbieScRecord.setCodeListManifestId(null);
                    bbieScRecord.setAgencyIdListManifestId(valueOf(defaultDtScAwdPri.agencyIdListManifestId()));
                } else {
                    bbieScRecord.setXbtManifestId(valueOf(defaultDtScAwdPri.xbtManifestId()));
                    bbieScRecord.setCodeListManifestId(null);
                    bbieScRecord.setAgencyIdListManifestId(null);
                }
            } else {
                if (bbieSc.getXbtManifestId() != null) {
                    bbieScRecord.setXbtManifestId(valueOf(bbieSc.getXbtManifestId()));
                    bbieScRecord.setCodeListManifestId(null);
                    bbieScRecord.setAgencyIdListManifestId(null);
                } else if (bbieSc.getCodeListManifestId() != null) {
                    bbieScRecord.setXbtManifestId(null);
                    bbieScRecord.setCodeListManifestId(valueOf(bbieSc.getCodeListManifestId()));
                    bbieScRecord.setAgencyIdListManifestId(null);
                } else if (bbieSc.getAgencyIdListManifestId() != null) {
                    bbieScRecord.setXbtManifestId(null);
                    bbieScRecord.setCodeListManifestId(null);
                    bbieScRecord.setAgencyIdListManifestId(valueOf(bbieSc.getAgencyIdListManifestId()));
                }
            }

            bbieScRecord.setOwnerTopLevelAsbiepId(topLevelAsbiepId);

            bbieScRecord.setCreatedBy(userId);
            bbieScRecord.setLastUpdatedBy(userId);
            bbieScRecord.setCreationTimestamp(timestamp);
            bbieScRecord.setLastUpdateTimestamp(timestamp);

            bbieScRecord.setBbieScId(
                    dslContext().insertInto(BBIE_SC)
                            .set(bbieScRecord)
                            .returning(BBIE_SC.BBIE_SC_ID)
                            .fetchOne().getBbieScId()
            );
        } else {
            if (bbieSc.getUsed() != null) {
                bbieScRecord.setIsUsed((byte) (bbieSc.getUsed() ? 1 : 0));
            }

            if (bbieSc.getDeprecated() != null) {
                bbieScRecord.setIsDeprecated((byte) (bbieSc.getDeprecated() ? 1 : 0));
            }

            if (bbieSc.getDefinition() != null) {
                bbieScRecord.setDefinition(emptyToNull(bbieSc.getDefinition()));
            }

            if (bbieSc.getCardinalityMin() != null) {
                bbieScRecord.setCardinalityMin(bbieSc.getCardinalityMin());
            }

            if (bbieSc.getCardinalityMax() != null) {
                bbieScRecord.setCardinalityMax(bbieSc.getCardinalityMax());
            }

            if (bbieScRecord.getCardinalityMax() > 0 && bbieScRecord.getCardinalityMin() > bbieScRecord.getCardinalityMax()) {
                throw new IllegalArgumentException("Cardinality is not valid.");
            }

            if (bbieSc.getFacetMinLength() != null) {
                bbieScRecord.setFacetMinLength(ULong.valueOf(bbieSc.getFacetMinLength()));
            } else {
                bbieScRecord.setFacetMinLength(null);
            }
            if (bbieSc.getFacetMaxLength() != null) {
                bbieScRecord.setFacetMaxLength(ULong.valueOf(bbieSc.getFacetMaxLength()));
            } else {
                bbieScRecord.setFacetMaxLength(null);
            }
            if (bbieScRecord.getFacetMinLength() != null && bbieScRecord.getFacetMaxLength() != null) {
                if (bbieScRecord.getFacetMinLength().intValue() < 0) {
                    throw new IllegalArgumentException("Minimum Length must be greater than or equals to 0.");
                }
                if (bbieScRecord.getFacetMinLength().compareTo(bbieScRecord.getFacetMaxLength()) > 0) {
                    throw new IllegalArgumentException("Minimum Length must be less than equals to Maximum Length.");
                }
            }
            if (StringUtils.hasLength(bbieSc.getFacetPattern())) {
                bbieScRecord.setFacetPattern(bbieSc.getFacetPattern());
            } else {
                bbieScRecord.setFacetPattern(null);
            }

            if (bbieSc.getBizTerm() != null) {
                bbieScRecord.setBizTerm(emptyToNull(bbieSc.getBizTerm()));
            }

            if (bbieSc.getExample() != null) {
                bbieScRecord.setExample(emptyToNull(bbieSc.getExample()));
            }

            if (bbieSc.getRemark() != null) {
                bbieScRecord.setRemark(emptyToNull(bbieSc.getRemark()));
            }

            if (bbieSc.getDisplayName() != null) {
                bbieScRecord.setDisplayName(emptyToNull(bbieSc.getDisplayName()));
            }

            if (StringUtils.hasLength(bbieSc.getDefaultValue())) {
                bbieScRecord.setDefaultValue(bbieSc.getDefaultValue());
                bbieScRecord.setFixedValue(null);
            } else if (StringUtils.hasLength(bbieSc.getFixedValue())) {
                bbieScRecord.setDefaultValue(null);
                bbieScRecord.setFixedValue(bbieSc.getFixedValue());
            }

            if (!bbieSc.isEmptyPrimitive()) {
                if (bbieSc.getXbtManifestId() != null) {
                    bbieScRecord.setXbtManifestId(valueOf(bbieSc.getXbtManifestId()));
                    bbieScRecord.setCodeListManifestId(null);
                    bbieScRecord.setAgencyIdListManifestId(null);
                } else if (bbieSc.getCodeListManifestId() != null) {
                    bbieScRecord.setXbtManifestId(null);
                    bbieScRecord.setCodeListManifestId(valueOf(bbieSc.getCodeListManifestId()));
                    bbieScRecord.setAgencyIdListManifestId(null);
                } else if (bbieSc.getAgencyIdListManifestId() != null) {
                    bbieScRecord.setXbtManifestId(null);
                    bbieScRecord.setCodeListManifestId(null);
                    bbieScRecord.setAgencyIdListManifestId(valueOf(bbieSc.getAgencyIdListManifestId()));
                }
            }

            if (bbieScRecord.changed()) {
                bbieScRecord.setLastUpdatedBy(userId);
                bbieScRecord.setLastUpdateTimestamp(timestamp);

                bbieScRecord.update(
                        BBIE_SC.IS_USED,
                        BBIE_SC.IS_DEPRECATED,
                        BBIE_SC.DEFINITION,
                        BBIE_SC.CARDINALITY_MIN,
                        BBIE_SC.CARDINALITY_MAX,
                        BBIE_SC.FACET_MIN_LENGTH,
                        BBIE_SC.FACET_MAX_LENGTH,
                        BBIE_SC.FACET_PATTERN,
                        BBIE_SC.BIZ_TERM,
                        BBIE_SC.EXAMPLE,
                        BBIE_SC.REMARK,
                        BBIE_SC.DISPLAY_NAME,
                        BBIE_SC.DEFAULT_VALUE,
                        BBIE_SC.FIXED_VALUE,
                        BBIE_SC.XBT_MANIFEST_ID,
                        BBIE_SC.CODE_LIST_MANIFEST_ID,
                        BBIE_SC.AGENCY_ID_LIST_MANIFEST_ID,
                        BBIE_SC.LAST_UPDATED_BY,
                        BBIE_SC.LAST_UPDATE_TIMESTAMP
                );
            }
        }

        return getBbieSc(request.getTopLevelAsbiepId(), hashPath);
    }

    private BbieScNode.BbieSc getBbieSc(TopLevelAsbiepId topLevelAsbiepId, String hashPath) {
        BbieScNode.BbieSc bbieSc = new BbieScNode.BbieSc();
        bbieSc.setHashPath(hashPath);

        var query = repositoryFactory().bbieScQueryRepository(requester());
        BbieScDetailsRecord bbieScDetails = query.getBbieScDetails(topLevelAsbiepId, hashPath);
        if (bbieScDetails != null) {
            bbieSc.setOwnerTopLevelAsbiepId(bbieScDetails.ownerTopLevelAsbiep().topLevelAsbiepId());
            bbieSc.setBbieScId(bbieScDetails.bbieScId());
            bbieSc.setBbieHashPath(dslContext().select(BBIE.HASH_PATH)
                    .from(BBIE)
                    .where(and(
                            BBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(valueOf(topLevelAsbiepId)),
                            BBIE.BBIE_ID.eq(valueOf(bbieScDetails.bbieId()))
                    ))
                    .fetchOneInto(String.class));
            bbieSc.setBasedDtScManifestId(bbieScDetails.basedDtSc().dtScManifestId());
            bbieSc.setUsed(bbieScDetails.used());
            bbieSc.setGuid(bbieScDetails.getGuid().value());
            bbieSc.setCardinalityMin(bbieScDetails.cardinality().min());
            bbieSc.setCardinalityMax(bbieScDetails.cardinality().max());
            if (bbieScDetails.facet() != null) {
                bbieSc.setFacetMinLength(bbieScDetails.facet().minLength());
                bbieSc.setFacetMaxLength(bbieScDetails.facet().maxLength());
                bbieSc.setFacetPattern(bbieScDetails.facet().pattern());
            }
            bbieSc.setRemark(bbieScDetails.remark());
            bbieSc.setBizTerm(bbieScDetails.bizTerm());
            bbieSc.setDefinition(bbieScDetails.definition());
            bbieSc.setDisplayName(bbieScDetails.displayName());
            if (bbieScDetails.valueConstraint() != null) {
                bbieSc.setDefaultValue(bbieScDetails.valueConstraint().defaultValue());
                bbieSc.setFixedValue(bbieScDetails.valueConstraint().fixedValue());
            }
            bbieSc.setExample(bbieScDetails.example());
            bbieSc.setDeprecated(bbieScDetails.deprecated());

            bbieSc.setXbtManifestId((bbieScDetails.primitiveRestriction().xbtManifestId() != null) ?
                    bbieScDetails.primitiveRestriction().xbtManifestId() : null);
            bbieSc.setCodeListManifestId((bbieScDetails.primitiveRestriction().codeListManifestId() != null) ?
                    bbieScDetails.primitiveRestriction().codeListManifestId() : null);
            bbieSc.setAgencyIdListManifestId((bbieScDetails.primitiveRestriction().agencyIdListManifestId() != null) ?
                    bbieScDetails.primitiveRestriction().agencyIdListManifestId() : null);
        }

        return bbieSc;
    }
}

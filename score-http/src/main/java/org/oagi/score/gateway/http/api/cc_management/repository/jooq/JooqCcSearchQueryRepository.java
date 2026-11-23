package org.oagi.score.gateway.http.api.cc_management.repository.jooq;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.cc_search.CcSearchAttribute;
import org.oagi.score.gateway.http.api.cc_management.model.cc_search.CcSearchType;
import org.oagi.score.gateway.http.api.cc_management.model.cc_search.CcSearchNodes;
import org.oagi.score.gateway.http.api.cc_management.model.cc_search.CcSearchPredicate;
import org.oagi.score.gateway.http.api.cc_management.model.cc_search.CcSearchRequest;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtAwdPriId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtAwdPriSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.ValueDomainType;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScAwdPriId;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScAwdPriSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScManifestId;
import org.oagi.score.gateway.http.api.cc_management.repository.CcSearchQueryRepository;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.model.TextSearchOperator;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.repository.jooq.TextSearchConditions;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;

/** Searches the text of the core components of one release for the predicates of a tree search. */
public class JooqCcSearchQueryRepository extends JooqBaseRepository implements CcSearchQueryRepository {

    public JooqCcSearchQueryRepository(DSLContext dslContext, ScoreUser requester, RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);
    }

    @Override
    public ReleaseId getReleaseOfRoot(CcSearchRequest request) {
        ULong release;
        if (request.accManifestId() != null) {
            release = dslContext().select(ACC_MANIFEST.RELEASE_ID).from(ACC_MANIFEST)
                    .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(valueOf(request.accManifestId()))).fetchOne(ACC_MANIFEST.RELEASE_ID);
        } else if (request.asccpManifestId() != null) {
            release = dslContext().select(ASCCP_MANIFEST.RELEASE_ID).from(ASCCP_MANIFEST)
                    .where(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(valueOf(request.asccpManifestId()))).fetchOne(ASCCP_MANIFEST.RELEASE_ID);
        } else if (request.bccpManifestId() != null) {
            release = dslContext().select(BCCP_MANIFEST.RELEASE_ID).from(BCCP_MANIFEST)
                    .where(BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(valueOf(request.bccpManifestId()))).fetchOne(BCCP_MANIFEST.RELEASE_ID);
        } else if (request.dtManifestId() != null) {
            release = dslContext().select(DT_MANIFEST.RELEASE_ID).from(DT_MANIFEST)
                    .where(DT_MANIFEST.DT_MANIFEST_ID.eq(valueOf(request.dtManifestId()))).fetchOne(DT_MANIFEST.RELEASE_ID);
        } else {
            return null;
        }
        return release != null ? new ReleaseId(release.toBigInteger()) : null;
    }

    @Override
    public CcSearchNodes searchNodes(ReleaseId releaseId, CcSearchPredicate predicate) {
        CcSearchAttribute attribute = predicate.attribute();
        if (predicate.value() == null) {
            throw new IllegalArgumentException("A search predicate needs a value.");
        }
        boolean storable = TextSearchConditions.isStorable(predicate.value());
        if (!storable && predicate.operator() != TextSearchOperator.NOT_EQUALS) {
            // see TextSearchConditions.isStorable
            return CcSearchNodes.none();
        }
        if (attribute == CcSearchAttribute.VALUE_DOMAIN || attribute == CcSearchAttribute.VALUE_DOMAIN_RESTRICTION) {
            return searchDefaultValueDomain(releaseId, attribute, predicate, storable);
        }

        return new CcSearchNodes(
                idsOf(CcSearchType.ACC, releaseId, attribute, predicate, storable).stream().map(AccManifestId::from).toList(),
                idsOf(CcSearchType.ASCC, releaseId, attribute, predicate, storable).stream().map(AsccManifestId::from).toList(),
                idsOf(CcSearchType.ASCCP, releaseId, attribute, predicate, storable).stream().map(AsccpManifestId::from).toList(),
                idsOf(CcSearchType.BCC, releaseId, attribute, predicate, storable).stream().map(BccManifestId::from).toList(),
                idsOf(CcSearchType.BCCP, releaseId, attribute, predicate, storable).stream().map(BccpManifestId::from).toList(),
                idsOf(CcSearchType.DT, releaseId, attribute, predicate, storable).stream().map(DtManifestId::from).toList(),
                idsOf(CcSearchType.DT_SC, releaseId, attribute, predicate, storable).stream().map(DtScManifestId::from).toList());
    }

    /**
     * The BCCPs and DT_SCs whose default value domain satisfies the predicate. The default of a data type (or of a
     * supplementary component) is picked by {@code DtQueryRepository.getDefaultDtAwdPriSummaryMap}, which applies the one
     * rule that holds it; the allowed primitives it picked are then compared in the database like any other text.
     */
    private CcSearchNodes searchDefaultValueDomain(ReleaseId releaseId, CcSearchAttribute attribute,
                                                   CcSearchPredicate predicate, boolean storable) {
        DefaultAwdPris defaults = defaultAwdPrisOf(releaseId);
        List<ULong> defaultDtAwdPriIds = valueOf(defaults.dt());
        List<ULong> defaultDtScAwdPriIds = valueOf(defaults.dtSc());

        // the name of the value domain a row of DT_AWD_PRI / DT_SC_AWD_PRI holds, or its type
        Field<String> awdPriText = valueDomainText(attribute, DT_AWD_PRI.XBT_MANIFEST_ID, DT_AWD_PRI.CODE_LIST_MANIFEST_ID);
        Field<String> scAwdPriText = valueDomainText(attribute, DT_SC_AWD_PRI.XBT_MANIFEST_ID, DT_SC_AWD_PRI.CODE_LIST_MANIFEST_ID);
        Condition dtCondition = valueDomainCondition(awdPriText, predicate, storable);
        Condition dtScCondition = valueDomainCondition(scAwdPriText, predicate, storable);

        List<ULong> dtIds = defaultDtAwdPriIds.isEmpty() ? List.of() : dslContext().selectDistinct(DT_AWD_PRI.DT_ID)
                .from(DT_AWD_PRI)
                .leftJoin(XBT_MANIFEST).on(XBT_MANIFEST.XBT_MANIFEST_ID.eq(DT_AWD_PRI.XBT_MANIFEST_ID))
                .leftJoin(XBT).on(XBT.XBT_ID.eq(XBT_MANIFEST.XBT_ID))
                .leftJoin(CODE_LIST_MANIFEST).on(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID.eq(DT_AWD_PRI.CODE_LIST_MANIFEST_ID))
                .leftJoin(CODE_LIST).on(CODE_LIST.CODE_LIST_ID.eq(CODE_LIST_MANIFEST.CODE_LIST_ID))
                .leftJoin(AGENCY_ID_LIST_MANIFEST).on(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID.eq(DT_AWD_PRI.AGENCY_ID_LIST_MANIFEST_ID))
                .leftJoin(AGENCY_ID_LIST).on(AGENCY_ID_LIST.AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID))
                .where(DT_AWD_PRI.DT_AWD_PRI_ID.in(defaultDtAwdPriIds))
                .and(dtCondition)
                .fetch(DT_AWD_PRI.DT_ID);
        List<ULong> dtScIds = defaultDtScAwdPriIds.isEmpty() ? List.of() : dslContext().selectDistinct(DT_SC_AWD_PRI.DT_SC_ID)
                .from(DT_SC_AWD_PRI)
                .leftJoin(XBT_MANIFEST).on(XBT_MANIFEST.XBT_MANIFEST_ID.eq(DT_SC_AWD_PRI.XBT_MANIFEST_ID))
                .leftJoin(XBT).on(XBT.XBT_ID.eq(XBT_MANIFEST.XBT_ID))
                .leftJoin(CODE_LIST_MANIFEST).on(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID.eq(DT_SC_AWD_PRI.CODE_LIST_MANIFEST_ID))
                .leftJoin(CODE_LIST).on(CODE_LIST.CODE_LIST_ID.eq(CODE_LIST_MANIFEST.CODE_LIST_ID))
                .leftJoin(AGENCY_ID_LIST_MANIFEST).on(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID.eq(DT_SC_AWD_PRI.AGENCY_ID_LIST_MANIFEST_ID))
                .leftJoin(AGENCY_ID_LIST).on(AGENCY_ID_LIST.AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID))
                .where(DT_SC_AWD_PRI.DT_SC_AWD_PRI_ID.in(defaultDtScAwdPriIds))
                .and(dtScCondition)
                .fetch(DT_SC_AWD_PRI.DT_SC_ID);

        List<BccpManifestId> bccps = dtIds.isEmpty() ? List.of() : dslContext().select(BCCP_MANIFEST.BCCP_MANIFEST_ID)
                .from(BCCP_MANIFEST)
                .join(DT_MANIFEST).on(DT_MANIFEST.DT_MANIFEST_ID.eq(BCCP_MANIFEST.BDT_MANIFEST_ID))
                .where(BCCP_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .and(DT_MANIFEST.DT_ID.in(dtIds))
                .fetch(r -> BccpManifestId.from(r.value1().toBigInteger()));
        List<DtScManifestId> dtScs = dtScIds.isEmpty() ? List.of() : dslContext().select(DT_SC_MANIFEST.DT_SC_MANIFEST_ID)
                .from(DT_SC_MANIFEST)
                .where(DT_SC_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .and(DT_SC_MANIFEST.DT_SC_ID.in(dtScIds))
                .fetch(r -> DtScManifestId.from(r.value1().toBigInteger()));
        return new CcSearchNodes(List.of(), List.of(), List.of(), List.of(), bccps, List.of(), dtScs);
    }

    /** What the default value domain of a release is made of; it does not depend on the predicate. */
    private record DefaultAwdPris(List<DtAwdPriId> dt, List<DtScAwdPriId> dtSc) {
    }

    /** One request asks several predicates of one release, and the defaults are the same for all of them: worked out once. */
    private final Map<ReleaseId, DefaultAwdPris> defaultAwdPris = new HashMap<>();

    private DefaultAwdPris defaultAwdPrisOf(ReleaseId releaseId) {
        return defaultAwdPris.computeIfAbsent(releaseId, key -> {
            var dtQuery = repositoryFactory().dtQueryRepository(requester());
            return new DefaultAwdPris(
                    dtQuery.getDefaultDtAwdPriSummaryMap(releaseId).values().stream()
                            .map(DtAwdPriSummaryRecord::dtAwdPriId).toList(),
                    dtQuery.getDefaultDtScAwdPriSummaryMap(releaseId).values().stream()
                            .map(DtScAwdPriSummaryRecord::dtScAwdPriId).toList());
        });
    }

    private static Field<String> valueDomainText(CcSearchAttribute attribute, TableField<?, ULong> xbt, TableField<?, ULong> codeList) {
        if (attribute == CcSearchAttribute.VALUE_DOMAIN) {
            return DSL.coalesce(XBT.NAME, CODE_LIST.NAME, AGENCY_ID_LIST.NAME);
        }
        return DSL.when(xbt.isNotNull(), DSL.inline(ValueDomainType.PRIMITIVE.label()))
                .when(codeList.isNotNull(), DSL.inline(ValueDomainType.CODE_LIST.label()))
                .otherwise(DSL.inline(ValueDomainType.AGENCY_ID_LIST.label()));
    }

    private static Condition valueDomainCondition(Field<String> text, CcSearchPredicate predicate, boolean storable) {
        return storable ? TextSearchConditions.matches(text, predicate.operator(), predicate.value(), predicate.caseSensitive())
                : text.isNotNull();
    }

    /** The column that holds the text of the attribute in the table of the type. */
    private static Field<String> text(CcSearchAttribute attribute, CcSearchType type) {
        return switch (attribute) {
            case DEFINITION_SOURCE -> switch (type) {
                case ACC -> ACC.DEFINITION_SOURCE;
                case ASCC -> ASCC.DEFINITION_SOURCE;
                case ASCCP -> ASCCP.DEFINITION_SOURCE;
                case BCC -> BCC.DEFINITION_SOURCE;
                case BCCP -> BCCP.DEFINITION_SOURCE;
                case DT -> DT.DEFINITION_SOURCE;
                case DT_SC -> DT_SC.DEFINITION_SOURCE;
            };
            case DEFAULT_VALUE -> switch (type) {
                case BCC -> BCC.DEFAULT_VALUE;
                case BCCP -> BCCP.DEFAULT_VALUE;
                default -> DT_SC.DEFAULT_VALUE;
            };
            case FIXED_VALUE -> switch (type) {
                case BCC -> BCC.FIXED_VALUE;
                case BCCP -> BCCP.FIXED_VALUE;
                default -> DT_SC.FIXED_VALUE;
            };
            case CONTENT_COMPONENT_DEFINITION -> DT.CONTENT_COMPONENT_DEFINITION;
            default -> switch (type) {
                case ACC -> ACC.DEFINITION;
                case ASCC -> ASCC.DEFINITION;
                case ASCCP -> ASCCP.DEFINITION;
                case BCC -> BCC.DEFINITION;
                case BCCP -> BCCP.DEFINITION;
                case DT -> DT.DEFINITION;
                case DT_SC -> DT_SC.DEFINITION;
            };
        };
    }

    private static boolean has(CcSearchAttribute attribute, CcSearchType type) {
        return attribute.types().contains(type);
    }

    /**
     * Where the text of each type of component is looked for: the manifest that ties the component to a release, and the
     * component that holds the text.
     */
    private record Binding(Table<?> manifest, TableField<?, ULong> manifestId, TableField<?, ULong> releaseId,
                           Condition join, Table<?> component) {
    }

    private static final Map<CcSearchType, Binding> BINDINGS = Map.of(
            CcSearchType.ACC, new Binding(ACC_MANIFEST, ACC_MANIFEST.ACC_MANIFEST_ID, ACC_MANIFEST.RELEASE_ID,
                    ACC_MANIFEST.ACC_ID.eq(ACC.ACC_ID), ACC),
            CcSearchType.ASCC, new Binding(ASCC_MANIFEST, ASCC_MANIFEST.ASCC_MANIFEST_ID, ASCC_MANIFEST.RELEASE_ID,
                    ASCC_MANIFEST.ASCC_ID.eq(ASCC.ASCC_ID), ASCC),
            CcSearchType.ASCCP, new Binding(ASCCP_MANIFEST, ASCCP_MANIFEST.ASCCP_MANIFEST_ID, ASCCP_MANIFEST.RELEASE_ID,
                    ASCCP_MANIFEST.ASCCP_ID.eq(ASCCP.ASCCP_ID), ASCCP),
            CcSearchType.BCC, new Binding(BCC_MANIFEST, BCC_MANIFEST.BCC_MANIFEST_ID, BCC_MANIFEST.RELEASE_ID,
                    BCC_MANIFEST.BCC_ID.eq(BCC.BCC_ID), BCC),
            CcSearchType.BCCP, new Binding(BCCP_MANIFEST, BCCP_MANIFEST.BCCP_MANIFEST_ID, BCCP_MANIFEST.RELEASE_ID,
                    BCCP_MANIFEST.BCCP_ID.eq(BCCP.BCCP_ID), BCCP),
            CcSearchType.DT, new Binding(DT_MANIFEST, DT_MANIFEST.DT_MANIFEST_ID, DT_MANIFEST.RELEASE_ID,
                    DT_MANIFEST.DT_ID.eq(DT.DT_ID), DT),
            CcSearchType.DT_SC, new Binding(DT_SC_MANIFEST, DT_SC_MANIFEST.DT_SC_MANIFEST_ID, DT_SC_MANIFEST.RELEASE_ID,
                    DT_SC_MANIFEST.DT_SC_ID.eq(DT_SC.DT_SC_ID), DT_SC));

    /** The manifests of the type in the release whose component's text satisfies the predicate; none for a type that has no such text. */
    private List<BigInteger> idsOf(CcSearchType type, ReleaseId releaseId, CcSearchAttribute attribute,
                                   CcSearchPredicate predicate, boolean storable) {
        if (!has(attribute, type)) {
            return List.of();
        }
        Binding binding = BINDINGS.get(type);
        Field<String> text = text(attribute, type);
        Condition condition = storable
                ? TextSearchConditions.matches(text, predicate.operator(), predicate.value(), predicate.caseSensitive())
                : text.isNotNull();
        return dslContext().select(binding.manifestId())
                .from(binding.manifest())
                .join(binding.component()).on(binding.join())
                .where(binding.releaseId().eq(valueOf(releaseId)))
                .and(condition)
                .fetch(Record1::value1).stream()
                .map(ULong::toBigInteger)
                .toList();
    }
}

package org.oagi.score.gateway.http.api.cc_management.repository.jooq;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DefaultAwdPriRule;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtAwdPriSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtId;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScAwdPriSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.cc_search.CcSearchAttribute;
import org.oagi.score.gateway.http.api.cc_management.model.cc_search.CcSearchKind;
import org.oagi.score.gateway.http.api.cc_management.model.cc_search.CcSearchNodes;
import org.oagi.score.gateway.http.api.cc_management.model.cc_search.CcSearchPredicate;
import org.oagi.score.gateway.http.api.cc_management.model.cc_search.CcSearchRequest;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScManifestId;
import org.oagi.score.gateway.http.api.cc_management.repository.CcSearchQueryRepository;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.model.TextSearchOperator;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.repository.jooq.TextSearchConditions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        CcSearchAttribute attribute = CcSearchAttribute.of(predicate.attribute())
                .orElseThrow(() -> new IllegalArgumentException("Unknown search attribute: " + predicate.attribute()));
        if (predicate.value() == null) {
            throw new IllegalArgumentException("A search predicate needs a value.");
        }
        boolean storable = TextSearchConditions.isStorable(predicate.value());
        if (!storable && predicate.operator() != TextSearchOperator.NOT_EQUALS) {
            // see TextSearchConditions.isStorable
            return CcSearchNodes.none();
        }
        ULong release = valueOf(releaseId);
        if (attribute == CcSearchAttribute.VALUE_DOMAIN || attribute == CcSearchAttribute.VALUE_DOMAIN_RESTRICTION) {
            return searchDefaultValueDomain(releaseId, release, attribute, predicate, storable);
        }

        List<AccManifestId> accs = !has(attribute, CcSearchKind.ACC) ? List.of() :
                ids(release, predicate, storable, ACC_MANIFEST, ACC_MANIFEST.ACC_MANIFEST_ID, ACC_MANIFEST.RELEASE_ID,
                        ACC_MANIFEST.ACC_ID.eq(ACC.ACC_ID), ACC, text(attribute, CcSearchKind.ACC))
                        .stream().map(AccManifestId::from).toList();
        List<AsccManifestId> asccs = !has(attribute, CcSearchKind.ASCC) ? List.of() :
                ids(release, predicate, storable, ASCC_MANIFEST, ASCC_MANIFEST.ASCC_MANIFEST_ID, ASCC_MANIFEST.RELEASE_ID,
                        ASCC_MANIFEST.ASCC_ID.eq(ASCC.ASCC_ID), ASCC, text(attribute, CcSearchKind.ASCC))
                        .stream().map(AsccManifestId::from).toList();
        List<AsccpManifestId> asccps = !has(attribute, CcSearchKind.ASCCP) ? List.of() :
                ids(release, predicate, storable, ASCCP_MANIFEST, ASCCP_MANIFEST.ASCCP_MANIFEST_ID, ASCCP_MANIFEST.RELEASE_ID,
                        ASCCP_MANIFEST.ASCCP_ID.eq(ASCCP.ASCCP_ID), ASCCP, text(attribute, CcSearchKind.ASCCP))
                        .stream().map(AsccpManifestId::from).toList();
        List<BccManifestId> bccs = !has(attribute, CcSearchKind.BCC) ? List.of() :
                ids(release, predicate, storable, BCC_MANIFEST, BCC_MANIFEST.BCC_MANIFEST_ID, BCC_MANIFEST.RELEASE_ID,
                        BCC_MANIFEST.BCC_ID.eq(BCC.BCC_ID), BCC, text(attribute, CcSearchKind.BCC))
                        .stream().map(BccManifestId::from).toList();
        List<BccpManifestId> bccps = !has(attribute, CcSearchKind.BCCP) ? List.of() :
                ids(release, predicate, storable, BCCP_MANIFEST, BCCP_MANIFEST.BCCP_MANIFEST_ID, BCCP_MANIFEST.RELEASE_ID,
                        BCCP_MANIFEST.BCCP_ID.eq(BCCP.BCCP_ID), BCCP, text(attribute, CcSearchKind.BCCP))
                        .stream().map(BccpManifestId::from).toList();
        List<DtManifestId> dts = !has(attribute, CcSearchKind.DT) ? List.of() :
                ids(release, predicate, storable, DT_MANIFEST, DT_MANIFEST.DT_MANIFEST_ID, DT_MANIFEST.RELEASE_ID,
                        DT_MANIFEST.DT_ID.eq(DT.DT_ID), DT, text(attribute, CcSearchKind.DT))
                        .stream().map(DtManifestId::from).toList();
        List<DtScManifestId> dtScs = !has(attribute, CcSearchKind.DT_SC) ? List.of() :
                ids(release, predicate, storable, DT_SC_MANIFEST, DT_SC_MANIFEST.DT_SC_MANIFEST_ID, DT_SC_MANIFEST.RELEASE_ID,
                        DT_SC_MANIFEST.DT_SC_ID.eq(DT_SC.DT_SC_ID), DT_SC, text(attribute, CcSearchKind.DT_SC))
                        .stream().map(DtScManifestId::from).toList();
        return new CcSearchNodes(accs, asccs, asccps, bccs, bccps, dts, dtScs);
    }

    static final String KIND_PRIMITIVE = "Primitive";
    static final String KIND_CODE_LIST = "Code List";
    static final String KIND_AGENCY_ID_LIST = "Agency ID List";

    /**
     * The BCCPs and DT_SCs whose default value domain satisfies the predicate. The default of a data type (or of a
     * supplementary component) is picked by {@link DefaultAwdPriRule}, the one place that holds that rule; the
     * allowed primitives it picked are then compared in the database like any other text.
     */
    private CcSearchNodes searchDefaultValueDomain(ReleaseId releaseId, ULong release, CcSearchAttribute attribute,
                                                   CcSearchPredicate predicate, boolean storable) {
        DefaultAwdPris defaults = defaultAwdPrisOf(releaseId, release);
        List<ULong> defaultDtAwdPriIds = defaults.dt();
        List<ULong> defaultDtScAwdPriIds = defaults.dtSc();

        // the name of the value domain a row of DT_AWD_PRI / DT_SC_AWD_PRI holds, or its kind
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
                .where(BCCP_MANIFEST.RELEASE_ID.eq(release))
                .and(DT_MANIFEST.DT_ID.in(dtIds))
                .fetch(r -> BccpManifestId.from(r.value1().toBigInteger()));
        List<DtScManifestId> dtScs = dtScIds.isEmpty() ? List.of() : dslContext().select(DT_SC_MANIFEST.DT_SC_MANIFEST_ID)
                .from(DT_SC_MANIFEST)
                .where(DT_SC_MANIFEST.RELEASE_ID.eq(release))
                .and(DT_SC_MANIFEST.DT_SC_ID.in(dtScIds))
                .fetch(r -> DtScManifestId.from(r.value1().toBigInteger()));
        return new CcSearchNodes(List.of(), List.of(), List.of(), List.of(), bccps, List.of(), dtScs);
    }

    /** What the default value domain of a release is made of; it does not depend on the predicate. */
    private record DefaultAwdPris(List<ULong> dt, List<ULong> dtSc) {
    }

    /** One request asks several predicates of one release, and the defaults are the same for all of them: worked out once. */
    private final Map<ULong, DefaultAwdPris> defaultAwdPris = new HashMap<>();

    private DefaultAwdPris defaultAwdPrisOf(ReleaseId releaseId, ULong release) {
        return defaultAwdPris.computeIfAbsent(release, key -> {
            var dtQuery = repositoryFactory().dtQueryRepository(requester());
            Map<DtId, String> termOfDt = new HashMap<>();
            dslContext().select(DT_MANIFEST.DT_ID, DT.DATA_TYPE_TERM).from(DT_MANIFEST).join(DT).on(DT_MANIFEST.DT_ID.eq(DT.DT_ID))
                    .where(DT_MANIFEST.RELEASE_ID.eq(release))
                    .forEach(r -> termOfDt.put(new DtId(r.value1().toBigInteger()), r.value2()));
            Map<DtId, List<DtAwdPriSummaryRecord>> awdPriOfDt = dtQuery.getDtAwdPriSummaryList(releaseId).stream()
                    .collect(Collectors.groupingBy(DtAwdPriSummaryRecord::dtId));
            List<ULong> defaultDtAwdPriIds = new ArrayList<>();
            awdPriOfDt.forEach((dtId, list) -> defaultOf(() -> DefaultAwdPriRule.select(termOfDt.get(dtId), list))
                    .ifPresent(e -> defaultDtAwdPriIds.add(ULong.valueOf(e.dtAwdPriId().value()))));

            Map<DtScId, String> termOfDtSc = new HashMap<>();
            dslContext().select(DT_SC_MANIFEST.DT_SC_ID, DT_SC.REPRESENTATION_TERM).from(DT_SC_MANIFEST).join(DT_SC).on(DT_SC_MANIFEST.DT_SC_ID.eq(DT_SC.DT_SC_ID))
                    .where(DT_SC_MANIFEST.RELEASE_ID.eq(release))
                    .forEach(r -> termOfDtSc.put(new DtScId(r.value1().toBigInteger()), r.value2()));
            Map<DtScId, List<DtScAwdPriSummaryRecord>> awdPriOfDtSc = dtQuery.getDtScAwdPriSummaryList(releaseId).stream()
                    .collect(Collectors.groupingBy(DtScAwdPriSummaryRecord::dtScId));
            List<ULong> defaultDtScAwdPriIds = new ArrayList<>();
            awdPriOfDtSc.forEach((dtScId, list) -> defaultOf(() -> DefaultAwdPriRule.selectForSc(termOfDtSc.get(dtScId), list))
                    .ifPresent(e -> defaultDtScAwdPriIds.add(ULong.valueOf(e.dtScAwdPriId().value()))));
            return new DefaultAwdPris(defaultDtAwdPriIds, defaultDtScAwdPriIds);
        });
    }

    /** A data type without a default (or without an allowed primitive to start with) has no default value domain. */
    private static <T> java.util.Optional<T> defaultOf(java.util.function.Supplier<T> rule) {
        try {
            return java.util.Optional.ofNullable(rule.get());
        } catch (IllegalStateException e) {
            return java.util.Optional.empty();
        }
    }

    private static Field<String> valueDomainText(CcSearchAttribute attribute, TableField<?, ULong> xbt, TableField<?, ULong> codeList) {
        if (attribute == CcSearchAttribute.VALUE_DOMAIN) {
            return DSL.coalesce(XBT.NAME, CODE_LIST.NAME, AGENCY_ID_LIST.NAME);
        }
        return DSL.when(xbt.isNotNull(), DSL.inline(KIND_PRIMITIVE))
                .when(codeList.isNotNull(), DSL.inline(KIND_CODE_LIST))
                .otherwise(DSL.inline(KIND_AGENCY_ID_LIST));
    }

    private static Condition valueDomainCondition(Field<String> text, CcSearchPredicate predicate, boolean storable) {
        return storable ? TextSearchConditions.matches(text, predicate.operator(), predicate.value(), predicate.caseSensitive())
                : text.isNotNull();
    }

    /** The column that holds the text of the attribute in the table of the kind. */
    private static Field<String> text(CcSearchAttribute attribute, CcSearchKind kind) {
        return switch (attribute) {
            case DEFINITION_SOURCE -> switch (kind) {
                case ACC -> ACC.DEFINITION_SOURCE;
                case ASCC -> ASCC.DEFINITION_SOURCE;
                case ASCCP -> ASCCP.DEFINITION_SOURCE;
                case BCC -> BCC.DEFINITION_SOURCE;
                case BCCP -> BCCP.DEFINITION_SOURCE;
                case DT -> DT.DEFINITION_SOURCE;
                case DT_SC -> DT_SC.DEFINITION_SOURCE;
            };
            case DEFAULT_VALUE -> switch (kind) {
                case BCC -> BCC.DEFAULT_VALUE;
                case BCCP -> BCCP.DEFAULT_VALUE;
                default -> DT_SC.DEFAULT_VALUE;
            };
            case FIXED_VALUE -> switch (kind) {
                case BCC -> BCC.FIXED_VALUE;
                case BCCP -> BCCP.FIXED_VALUE;
                default -> DT_SC.FIXED_VALUE;
            };
            case CONTENT_COMPONENT_DEFINITION -> DT.CONTENT_COMPONENT_DEFINITION;
            default -> switch (kind) {
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

    private static boolean has(CcSearchAttribute attribute, CcSearchKind kind) {
        return attribute.kinds().contains(kind);
    }

    /** The manifests of the release whose component's text satisfies the predicate. */
    private List<java.math.BigInteger> ids(ULong release, CcSearchPredicate predicate, boolean storable,
                                           Table<?> manifest, TableField<?, ULong> manifestId, TableField<?, ULong> releaseId,
                                           Condition join, Table<?> component, Field<String> text) {
        Condition condition = storable
                ? TextSearchConditions.matches(text, predicate.operator(), predicate.value(), predicate.caseSensitive())
                : text.isNotNull();
        return dslContext().select(manifestId)
                .from(manifest)
                .join(component).on(join)
                .where(releaseId.eq(release))
                .and(condition)
                .fetch(Record1::value1).stream()
                .map(ULong::toBigInteger)
                .toList();
    }
}

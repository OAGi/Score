package org.oagi.score.gateway.http.api.bie_management.repository.jooq;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record2;
import org.jooq.Select;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.bie_search.BieSearchAttribute;
import org.oagi.score.gateway.http.api.bie_management.model.bie_search.BieSearchNodeKey;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DefaultAwdPriRule;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtAwdPriSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtId;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScAwdPriSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.model.TextSearchOperator;
import org.oagi.score.gateway.http.api.bie_management.model.bie_search.BieSearchPredicate;
import org.oagi.score.gateway.http.api.bie_management.repository.BieSearchQueryRepository;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.repository.jooq.TextSearchConditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;

/**
 * Searches the text of one BIE, row by row, for the predicates of a tree search. The text columns are compared in the
 * database's collation (case and accent insensitive); a case sensitive predicate compares them as binary strings.
 */
public class JooqBieSearchQueryRepository extends JooqBaseRepository implements BieSearchQueryRepository {

    public JooqBieSearchQueryRepository(DSLContext dslContext, ScoreUser requester, RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);
    }

    /** A column that holds searchable text, in the row of one table. */
    private record Target(String table, Table<?> from, Field<String> hashPath, Field<String> path, Field<String> text,
                          Condition scope) {
    }

    @Override
    public List<BieSearchNodeKey> searchNodes(TopLevelAsbiepId topLevelAsbiepId, BieSearchPredicate predicate) {
        BieSearchAttribute attribute = BieSearchAttribute.of(predicate.attribute())
                .orElseThrow(() -> new IllegalArgumentException("Unknown search attribute: " + predicate.attribute()));
        if (predicate.value() == null) {
            throw new IllegalArgumentException("A search predicate needs a value.");
        }
        boolean storable = TextSearchConditions.isStorable(predicate.value());
        if (!storable && predicate.operator() != TextSearchOperator.NOT_EQUALS) {
            // The text columns are utf8mb3: the database cannot hold such a character, and comparing with one is an error.
            return List.of();
        }

        List<BieSearchNodeKey> keys = new ArrayList<>();
        for (Target target : targets(valueOf(topLevelAsbiepId), attribute)) {
            Condition condition = storable ? TextSearchConditions.matches(target.text(), predicate.operator(), predicate.value(), predicate.caseSensitive()) : target.text().isNotNull();
            Select<Record2<String, String>> select = dslContext().selectDistinct(target.hashPath(), target.path())
                    .from(target.from())
                    .where(target.scope())
                    .and(condition);
            for (Record2<String, String> row : select.fetch()) {
                keys.add(new BieSearchNodeKey(target.table(), row.value1(), row.value2()));
            }
        }
        return keys;
    }

    @Override
    public List<String> suggestValues(TopLevelAsbiepId topLevelAsbiepId, String attributeName, String text, int limit) {
        BieSearchAttribute attribute = BieSearchAttribute.of(attributeName)
                .orElseThrow(() -> new IllegalArgumentException("Unknown search attribute: " + attributeName));
        if (!TextSearchConditions.isStorable(text)) {
            return List.of();
        }
        if (attribute == BieSearchAttribute.VALUE_DOMAIN_RESTRICTION) {
            // three fixed kinds, not read from the BIE
            return Stream.of(KIND_PRIMITIVE, KIND_CODE_LIST, KIND_AGENCY_ID_LIST)
                    .filter(kind -> kind.toLowerCase(Locale.ROOT).contains(text.toLowerCase(Locale.ROOT)))
                    .limit(limit).toList();
        }
        // the places the attribute is searched, so that a value offered is a value that finds something
        java.util.Set<String> values = new java.util.TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (Target target : targets(valueOf(topLevelAsbiepId), attribute)) {
            Condition condition = text.isEmpty() ? target.text().isNotNull()
                    : TextSearchConditions.matches(target.text(), TextSearchOperator.CONTAINS, text, false);
            values.addAll(dslContext().selectDistinct(target.text())
                    .from(target.from())
                    .where(target.scope())
                    .and(condition)
                    .orderBy(target.text())
                    .limit(limit)
                    .fetch(target.text()));
        }
        if (attribute == BieSearchAttribute.VALUE_DOMAIN) {
            // a place the BIE stores no value domain for starts with the default of its data type, which a search finds too
            String needle = text.toLowerCase(Locale.ROOT);
            defaultValueDomainNames(valueOf(topLevelAsbiepId)).stream()
                    .filter(name -> needle.isEmpty() || name.toLowerCase(Locale.ROOT).contains(needle))
                    .forEach(values::add);
        }
        return values.stream().limit(limit).toList();
    }

    /**
     * The names of the default value domains of the data types of the release of the BIE. The tree shows the whole
     * component of the BIE, so a place the BIE stores nothing for starts with the default of its data type; the search
     * finds the place by that name.
     */
    private java.util.Set<String> defaultValueDomainNames(org.jooq.types.ULong owner) {
        var dtQuery = repositoryFactory().dtQueryRepository(requester());
        java.util.Set<String> names = new java.util.TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        org.jooq.types.ULong release = dslContext().select(TOP_LEVEL_ASBIEP.RELEASE_ID).from(TOP_LEVEL_ASBIEP)
                .where(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(owner)).fetchOne(TOP_LEVEL_ASBIEP.RELEASE_ID);
        if (release == null) {
            return names;
        }
        ReleaseId releaseId = new ReleaseId(release.toBigInteger());

        // the data types the basic core components of the release are typed by, and their supplementary components
        java.util.Map<DtId, String> termOfDt = new java.util.HashMap<>();
        java.util.List<org.jooq.types.ULong> dtManifests = new java.util.ArrayList<>();
        dslContext().selectDistinct(DT_MANIFEST.DT_MANIFEST_ID, DT_MANIFEST.DT_ID, DT.DATA_TYPE_TERM)
                .from(BCCP_MANIFEST)
                .join(DT_MANIFEST).on(DT_MANIFEST.DT_MANIFEST_ID.eq(BCCP_MANIFEST.BDT_MANIFEST_ID))
                .join(DT).on(DT.DT_ID.eq(DT_MANIFEST.DT_ID))
                .where(BCCP_MANIFEST.RELEASE_ID.eq(release))
                .forEach(r -> {
                    dtManifests.add(r.value1());
                    termOfDt.put(new DtId(r.value2().toBigInteger()), r.value3());
                });
        dtQuery.getDtAwdPriSummaryList(releaseId).stream()
                .filter(e -> termOfDt.containsKey(e.dtId()))
                .collect(java.util.stream.Collectors.groupingBy(DtAwdPriSummaryRecord::dtId))
                .forEach((dtId, list) -> defaultOf(() -> DefaultAwdPriRule.select(termOfDt.get(dtId), list))
                        .ifPresent(e -> addName(names, e.xbtName(), e.codeListName(), e.agencyIdListName())));

        java.util.Map<DtScId, String> termOfDtSc = new java.util.HashMap<>();
        if (!dtManifests.isEmpty()) {
            dslContext().selectDistinct(DT_SC_MANIFEST.DT_SC_ID, DT_SC.REPRESENTATION_TERM)
                    .from(DT_SC_MANIFEST)
                    .join(DT_SC).on(DT_SC.DT_SC_ID.eq(DT_SC_MANIFEST.DT_SC_ID))
                    .where(DT_SC_MANIFEST.RELEASE_ID.eq(release))
                    .and(DT_SC_MANIFEST.OWNER_DT_MANIFEST_ID.in(dtManifests))
                    .forEach(r -> termOfDtSc.put(new DtScId(r.value1().toBigInteger()), r.value2()));
        }
        dtQuery.getDtScAwdPriSummaryList(releaseId).stream()
                .filter(e -> termOfDtSc.containsKey(e.dtScId()))
                .collect(java.util.stream.Collectors.groupingBy(DtScAwdPriSummaryRecord::dtScId))
                .forEach((dtScId, list) -> defaultOf(() -> DefaultAwdPriRule.selectForSc(termOfDtSc.get(dtScId), list))
                        .ifPresent(e -> addName(names, e.xbtName(), e.codeListName(), e.agencyIdListName())));
        return names;
    }

    /** A data type without a default (or without an allowed primitive to start with) has no default value domain. */
    private static <T> java.util.Optional<T> defaultOf(java.util.function.Supplier<T> rule) {
        try {
            return java.util.Optional.ofNullable(rule.get());
        } catch (IllegalStateException e) {
            return java.util.Optional.empty();
        }
    }

    /** The name of the value domain a row holds: the primitive, the code list or the agency ID list, one of the three. */
    private static void addName(java.util.Set<String> names, String... candidates) {
        for (String name : candidates) {
            if (name != null) {
                names.add(name);
                return;
            }
        }
    }

    private List<Target> targets(org.jooq.types.ULong owner, BieSearchAttribute attribute) {
        // The root ASBIEP is the one the top-level ASBIEP points to; the definition of any other ASBIEP is not edited
        // on its own (the ASBIE carries it), so it is not searched.
        Table<?> rootAsbiep = ASBIEP.join(TOP_LEVEL_ASBIEP).on(TOP_LEVEL_ASBIEP.ASBIEP_ID.eq(ASBIEP.ASBIEP_ID));
        Target asbie = new Target("asbie", ASBIE, ASBIE.HASH_PATH, ASBIE.PATH, ASBIE.DEFINITION, ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(owner));
        Target bbie = new Target("bbie", BBIE, BBIE.HASH_PATH, BBIE.PATH, null, BBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(owner));
        Target bbieSc = new Target("bbie_sc", BBIE_SC, BBIE_SC.HASH_PATH, BBIE_SC.PATH, null, BBIE_SC.OWNER_TOP_LEVEL_ASBIEP_ID.eq(owner));
        Target asbiep = new Target("asbiep", ASBIEP, ASBIEP.HASH_PATH, ASBIEP.PATH, null, ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.eq(owner));
        Target bbiep = new Target("bbiep", BBIEP, BBIEP.HASH_PATH, BBIEP.PATH, null, BBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.eq(owner));
        Target abie = new Target("abie", ABIE, ABIE.HASH_PATH, ABIE.PATH, null, ABIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(owner));

        return switch (attribute) {
            case DEFINITION -> List.of(
                    new Target("asbiep", rootAsbiep, ASBIEP.HASH_PATH, ASBIEP.PATH, ASBIEP.DEFINITION, ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.eq(owner)),
                    asbie, withText(bbie, BBIE.DEFINITION), withText(bbieSc, BBIE_SC.DEFINITION));
            case REMARK -> List.of(
                    withText(asbiep, ASBIEP.REMARK), withText(bbiep, BBIEP.REMARK), withText(bbieSc, BBIE_SC.REMARK));
            case EXAMPLE -> List.of(withText(bbie, BBIE.EXAMPLE), withText(bbieSc, BBIE_SC.EXAMPLE));
            case FIXED_VALUE -> List.of(withText(bbie, BBIE.FIXED_VALUE), withText(bbieSc, BBIE_SC.FIXED_VALUE));
            case DEFAULT_VALUE -> List.of(withText(bbie, BBIE.DEFAULT_VALUE), withText(bbieSc, BBIE_SC.DEFAULT_VALUE));
            // Both places a business term lives: the terms assigned to an ASBIE or a BBIE, and the `biz_term` text of the
            // other nodes. Whoever asks, and whatever the Business Term setting is, a node matches by either.
            case BUSINESS_TERM -> List.of(
                    new Target("asbie",
                            ASBIE.join(ASBIE_BIZTERM).on(ASBIE_BIZTERM.ASBIE_ID.eq(ASBIE.ASBIE_ID))
                                    .join(ASCC_BIZTERM).on(ASCC_BIZTERM.ASCC_BIZTERM_ID.eq(ASBIE_BIZTERM.ASCC_BIZTERM_ID))
                                    .join(BUSINESS_TERM).on(BUSINESS_TERM.BUSINESS_TERM_ID.eq(ASCC_BIZTERM.BUSINESS_TERM_ID)),
                            ASBIE.HASH_PATH, ASBIE.PATH, BUSINESS_TERM.BUSINESS_TERM_, ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(owner)),
                    new Target("bbie",
                            BBIE.join(BBIE_BIZTERM).on(BBIE_BIZTERM.BBIE_ID.eq(BBIE.BBIE_ID))
                                    .join(BCC_BIZTERM).on(BCC_BIZTERM.BCC_BIZTERM_ID.eq(BBIE_BIZTERM.BCC_BIZTERM_ID))
                                    .join(BUSINESS_TERM).on(BUSINESS_TERM.BUSINESS_TERM_ID.eq(BCC_BIZTERM.BUSINESS_TERM_ID)),
                            BBIE.HASH_PATH, BBIE.PATH, BUSINESS_TERM.BUSINESS_TERM_, BBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(owner)),
                    withText(abie, ABIE.BIZ_TERM), withText(asbiep, ASBIEP.BIZ_TERM),
                    withText(bbiep, BBIEP.BIZ_TERM), withText(bbieSc, BBIE_SC.BIZ_TERM));
            case VALUE_DOMAIN_RESTRICTION -> valueDomainTargets(owner, true);
            case VALUE_DOMAIN -> valueDomainTargets(owner, false);
        };
    }

    /** The kinds of value domain, as the search names them in `value_domain_restriction`. */
    static final String KIND_PRIMITIVE = "Primitive";
    static final String KIND_CODE_LIST = "Code List";
    static final String KIND_AGENCY_ID_LIST = "Agency ID List";

    /**
     * Where a BBIE or a BBIE_SC holds its value domain: a primitive (XBT), a code list or an agency ID list, one of the
     * three at a time. The restriction is the kind it holds; the value domain is the name of the one it holds, so a
     * search that does not name the kind looks in all three.
     */
    private static List<Target> valueDomainTargets(org.jooq.types.ULong owner, boolean restriction) {
        Table<?> bbieXbt = BBIE.join(XBT_MANIFEST).on(XBT_MANIFEST.XBT_MANIFEST_ID.eq(BBIE.XBT_MANIFEST_ID))
                .join(XBT).on(XBT.XBT_ID.eq(XBT_MANIFEST.XBT_ID));
        Table<?> bbieCode = BBIE.join(CODE_LIST_MANIFEST).on(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID.eq(BBIE.CODE_LIST_MANIFEST_ID))
                .join(CODE_LIST).on(CODE_LIST.CODE_LIST_ID.eq(CODE_LIST_MANIFEST.CODE_LIST_ID));
        Table<?> bbieAgency = BBIE.join(AGENCY_ID_LIST_MANIFEST).on(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID.eq(BBIE.AGENCY_ID_LIST_MANIFEST_ID))
                .join(AGENCY_ID_LIST).on(AGENCY_ID_LIST.AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID));
        Table<?> scXbt = BBIE_SC.join(XBT_MANIFEST).on(XBT_MANIFEST.XBT_MANIFEST_ID.eq(BBIE_SC.XBT_MANIFEST_ID))
                .join(XBT).on(XBT.XBT_ID.eq(XBT_MANIFEST.XBT_ID));
        Table<?> scCode = BBIE_SC.join(CODE_LIST_MANIFEST).on(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID.eq(BBIE_SC.CODE_LIST_MANIFEST_ID))
                .join(CODE_LIST).on(CODE_LIST.CODE_LIST_ID.eq(CODE_LIST_MANIFEST.CODE_LIST_ID));
        Table<?> scAgency = BBIE_SC.join(AGENCY_ID_LIST_MANIFEST).on(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID.eq(BBIE_SC.AGENCY_ID_LIST_MANIFEST_ID))
                .join(AGENCY_ID_LIST).on(AGENCY_ID_LIST.AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID));
        Condition bbieScope = BBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(owner);
        Condition scScope = BBIE_SC.OWNER_TOP_LEVEL_ASBIEP_ID.eq(owner);
        // the joins hold only the rows that have the kind; the restriction is the same constant for every row of its target
        return List.of(
                new Target("bbie", bbieXbt, BBIE.HASH_PATH, BBIE.PATH, restriction ? DSL.inline(KIND_PRIMITIVE) : XBT.NAME, bbieScope),
                new Target("bbie", bbieCode, BBIE.HASH_PATH, BBIE.PATH, restriction ? DSL.inline(KIND_CODE_LIST) : CODE_LIST.NAME, bbieScope),
                new Target("bbie", bbieAgency, BBIE.HASH_PATH, BBIE.PATH, restriction ? DSL.inline(KIND_AGENCY_ID_LIST) : AGENCY_ID_LIST.NAME, bbieScope),
                new Target("bbie_sc", scXbt, BBIE_SC.HASH_PATH, BBIE_SC.PATH, restriction ? DSL.inline(KIND_PRIMITIVE) : XBT.NAME, scScope),
                new Target("bbie_sc", scCode, BBIE_SC.HASH_PATH, BBIE_SC.PATH, restriction ? DSL.inline(KIND_CODE_LIST) : CODE_LIST.NAME, scScope),
                new Target("bbie_sc", scAgency, BBIE_SC.HASH_PATH, BBIE_SC.PATH, restriction ? DSL.inline(KIND_AGENCY_ID_LIST) : AGENCY_ID_LIST.NAME, scScope));
    }

    private static Target withText(Target target, Field<String> text) {
        return new Target(target.table(), target.from(), target.hashPath(), target.path(), text, target.scope());
    }
}

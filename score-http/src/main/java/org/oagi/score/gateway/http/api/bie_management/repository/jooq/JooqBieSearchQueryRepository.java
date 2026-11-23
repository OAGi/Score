package org.oagi.score.gateway.http.api.bie_management.repository.jooq;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record2;
import org.jooq.Select;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.bie_search.BieSearchAttribute;
import org.oagi.score.gateway.http.api.bie_management.model.bie_search.BieSearchNodeKey;
import org.oagi.score.gateway.http.api.bie_management.model.bie_search.BieSearchTable;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtAwdPriSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.ValueDomainType;
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
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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
    private record Target(BieSearchTable table, Table<?> from, Field<String> hashPath, Field<String> path, Field<String> text,
                          Condition scope) {
    }

    @Override
    public List<BieSearchNodeKey> searchNodes(TopLevelAsbiepId topLevelAsbiepId, BieSearchPredicate predicate) {
        if (predicate.value() == null) {
            throw new IllegalArgumentException("A search predicate needs a value.");
        }
        boolean storable = TextSearchConditions.isStorable(predicate.value());
        if (!storable && predicate.operator() != TextSearchOperator.NOT_EQUALS) {
            // The text columns are utf8mb3: the database cannot hold such a character, and comparing with one is an error.
            return List.of();
        }

        List<BieSearchNodeKey> keys = new ArrayList<>();
        for (Target target : targets(topLevelAsbiepId, predicate.attribute())) {
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
    public List<String> suggestValues(TopLevelAsbiepId topLevelAsbiepId, BieSearchAttribute attribute, String text, int limit) {
        if (!TextSearchConditions.isStorable(text)) {
            return List.of();
        }
        if (attribute == BieSearchAttribute.VALUE_DOMAIN_RESTRICTION) {
            // three fixed types, not read from the BIE
            return Arrays.stream(ValueDomainType.values())
                    .map(ValueDomainType::label)
                    .filter(type -> type.toLowerCase(Locale.ROOT).contains(text.toLowerCase(Locale.ROOT)))
                    .limit(limit).toList();
        }
        // the places the attribute is searched, so that a value offered is a value that finds something
        Set<String> values = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (Target target : targets(topLevelAsbiepId, attribute)) {
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
            defaultValueDomainNames(topLevelAsbiepId).stream()
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
    private Set<String> defaultValueDomainNames(TopLevelAsbiepId topLevelAsbiepId) {
        Set<String> names = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        ULong release = dslContext().select(TOP_LEVEL_ASBIEP.RELEASE_ID).from(TOP_LEVEL_ASBIEP)
                .where(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(valueOf(topLevelAsbiepId))).fetchOne(TOP_LEVEL_ASBIEP.RELEASE_ID);
        if (release == null) {
            return names;
        }
        ReleaseId releaseId = new ReleaseId(release.toBigInteger());
        var dtQuery = repositoryFactory().dtQueryRepository(requester());

        // the data types the basic core components of the release are typed by, and their supplementary components
        List<ULong> dtManifests = new ArrayList<>();
        List<DtId> dtIds = new ArrayList<>();
        dslContext().selectDistinct(DT_MANIFEST.DT_MANIFEST_ID, DT_MANIFEST.DT_ID)
                .from(BCCP_MANIFEST)
                .join(DT_MANIFEST).on(DT_MANIFEST.DT_MANIFEST_ID.eq(BCCP_MANIFEST.BDT_MANIFEST_ID))
                .where(BCCP_MANIFEST.RELEASE_ID.eq(release))
                .forEach(r -> {
                    dtManifests.add(r.value1());
                    dtIds.add(new DtId(r.value2().toBigInteger()));
                });
        Map<DtId, DtAwdPriSummaryRecord> defaultOfDt = dtQuery.getDefaultDtAwdPriSummaryMap(releaseId);
        dtIds.stream().map(defaultOfDt::get).filter(e -> e != null)
                .forEach(e -> addName(names, e.xbtName(), e.codeListName(), e.agencyIdListName()));

        if (!dtManifests.isEmpty()) {
            List<DtScId> dtScIds = new ArrayList<>();
            dslContext().selectDistinct(DT_SC_MANIFEST.DT_SC_ID)
                    .from(DT_SC_MANIFEST)
                    .where(DT_SC_MANIFEST.RELEASE_ID.eq(release))
                    .and(DT_SC_MANIFEST.OWNER_DT_MANIFEST_ID.in(dtManifests))
                    .forEach(r -> dtScIds.add(new DtScId(r.value1().toBigInteger())));
            Map<DtScId, DtScAwdPriSummaryRecord> defaultOfDtSc = dtQuery.getDefaultDtScAwdPriSummaryMap(releaseId);
            dtScIds.stream().map(defaultOfDtSc::get).filter(e -> e != null)
                    .forEach(e -> addName(names, e.xbtName(), e.codeListName(), e.agencyIdListName()));
        }
        return names;
    }

    /** The name of the value domain a row holds: the primitive, the code list or the agency ID list, one of the three. */
    private static void addName(Set<String> names, String... candidates) {
        for (String name : candidates) {
            if (name != null) {
                names.add(name);
                return;
            }
        }
    }

    private List<Target> targets(TopLevelAsbiepId topLevelAsbiepId, BieSearchAttribute attribute) {
        ULong owner = valueOf(topLevelAsbiepId);
        // The root ASBIEP is the one the top-level ASBIEP points to; the definition of any other ASBIEP is not edited
        // on its own (the ASBIE carries it), so it is not searched.
        Table<?> rootAsbiep = ASBIEP.join(TOP_LEVEL_ASBIEP).on(TOP_LEVEL_ASBIEP.ASBIEP_ID.eq(ASBIEP.ASBIEP_ID));
        Target asbie = new Target(BieSearchTable.ASBIE, ASBIE, ASBIE.HASH_PATH, ASBIE.PATH, ASBIE.DEFINITION, ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(owner));
        Target bbie = new Target(BieSearchTable.BBIE, BBIE, BBIE.HASH_PATH, BBIE.PATH, null, BBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(owner));
        Target bbieSc = new Target(BieSearchTable.BBIE_SC, BBIE_SC, BBIE_SC.HASH_PATH, BBIE_SC.PATH, null, BBIE_SC.OWNER_TOP_LEVEL_ASBIEP_ID.eq(owner));
        Target asbiep = new Target(BieSearchTable.ASBIEP, ASBIEP, ASBIEP.HASH_PATH, ASBIEP.PATH, null, ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.eq(owner));
        Target bbiep = new Target(BieSearchTable.BBIEP, BBIEP, BBIEP.HASH_PATH, BBIEP.PATH, null, BBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.eq(owner));
        Target abie = new Target(BieSearchTable.ABIE, ABIE, ABIE.HASH_PATH, ABIE.PATH, null, ABIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(owner));

        return switch (attribute) {
            case DEFINITION -> List.of(
                    new Target(BieSearchTable.ASBIEP, rootAsbiep, ASBIEP.HASH_PATH, ASBIEP.PATH, ASBIEP.DEFINITION, ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.eq(owner)),
                    asbie, withText(bbie, BBIE.DEFINITION), withText(bbieSc, BBIE_SC.DEFINITION));
            case REMARK -> List.of(
                    withText(asbiep, ASBIEP.REMARK), withText(bbiep, BBIEP.REMARK), withText(bbieSc, BBIE_SC.REMARK));
            case EXAMPLE -> List.of(withText(bbie, BBIE.EXAMPLE), withText(bbieSc, BBIE_SC.EXAMPLE));
            case FIXED_VALUE -> List.of(withText(bbie, BBIE.FIXED_VALUE), withText(bbieSc, BBIE_SC.FIXED_VALUE));
            case DEFAULT_VALUE -> List.of(withText(bbie, BBIE.DEFAULT_VALUE), withText(bbieSc, BBIE_SC.DEFAULT_VALUE));
            // Both places a business term lives: the terms assigned to an ASBIE or a BBIE, and the `biz_term` text of the
            // other nodes. Whoever asks, and whatever the Business Term setting is, a node matches by either.
            case BUSINESS_TERM -> List.of(
                    new Target(BieSearchTable.ASBIE,
                            ASBIE.join(ASBIE_BIZTERM).on(ASBIE_BIZTERM.ASBIE_ID.eq(ASBIE.ASBIE_ID))
                                    .join(ASCC_BIZTERM).on(ASCC_BIZTERM.ASCC_BIZTERM_ID.eq(ASBIE_BIZTERM.ASCC_BIZTERM_ID))
                                    .join(BUSINESS_TERM).on(BUSINESS_TERM.BUSINESS_TERM_ID.eq(ASCC_BIZTERM.BUSINESS_TERM_ID)),
                            ASBIE.HASH_PATH, ASBIE.PATH, BUSINESS_TERM.BUSINESS_TERM_, ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(owner)),
                    new Target(BieSearchTable.BBIE,
                            BBIE.join(BBIE_BIZTERM).on(BBIE_BIZTERM.BBIE_ID.eq(BBIE.BBIE_ID))
                                    .join(BCC_BIZTERM).on(BCC_BIZTERM.BCC_BIZTERM_ID.eq(BBIE_BIZTERM.BCC_BIZTERM_ID))
                                    .join(BUSINESS_TERM).on(BUSINESS_TERM.BUSINESS_TERM_ID.eq(BCC_BIZTERM.BUSINESS_TERM_ID)),
                            BBIE.HASH_PATH, BBIE.PATH, BUSINESS_TERM.BUSINESS_TERM_, BBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(owner)),
                    withText(abie, ABIE.BIZ_TERM), withText(asbiep, ASBIEP.BIZ_TERM),
                    withText(bbiep, BBIEP.BIZ_TERM), withText(bbieSc, BBIE_SC.BIZ_TERM));
            case VALUE_DOMAIN_RESTRICTION -> valueDomainTargets(topLevelAsbiepId, true);
            case VALUE_DOMAIN -> valueDomainTargets(topLevelAsbiepId, false);
        };
    }

    /**
     * Where a BBIE or a BBIE_SC holds its value domain: a primitive (XBT), a code list or an agency ID list, one of the
     * three at a time. The restriction is the type it holds; the value domain is the name of the one it holds, so a
     * search that does not name the type looks in all three.
     */
    private List<Target> valueDomainTargets(TopLevelAsbiepId topLevelAsbiepId, boolean restriction) {
        ULong owner = valueOf(topLevelAsbiepId);
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
        // the joins hold only the rows that have the type; the restriction is the same constant for every row of its target
        return List.of(
                new Target(BieSearchTable.BBIE, bbieXbt, BBIE.HASH_PATH, BBIE.PATH, restriction ? DSL.inline(ValueDomainType.PRIMITIVE.label()) : XBT.NAME, bbieScope),
                new Target(BieSearchTable.BBIE, bbieCode, BBIE.HASH_PATH, BBIE.PATH, restriction ? DSL.inline(ValueDomainType.CODE_LIST.label()) : CODE_LIST.NAME, bbieScope),
                new Target(BieSearchTable.BBIE, bbieAgency, BBIE.HASH_PATH, BBIE.PATH, restriction ? DSL.inline(ValueDomainType.AGENCY_ID_LIST.label()) : AGENCY_ID_LIST.NAME, bbieScope),
                new Target(BieSearchTable.BBIE_SC, scXbt, BBIE_SC.HASH_PATH, BBIE_SC.PATH, restriction ? DSL.inline(ValueDomainType.PRIMITIVE.label()) : XBT.NAME, scScope),
                new Target(BieSearchTable.BBIE_SC, scCode, BBIE_SC.HASH_PATH, BBIE_SC.PATH, restriction ? DSL.inline(ValueDomainType.CODE_LIST.label()) : CODE_LIST.NAME, scScope),
                new Target(BieSearchTable.BBIE_SC, scAgency, BBIE_SC.HASH_PATH, BBIE_SC.PATH, restriction ? DSL.inline(ValueDomainType.AGENCY_ID_LIST.label()) : AGENCY_ID_LIST.NAME, scScope));
    }

    private static Target withText(Target target, Field<String> text) {
        return new Target(target.table(), target.from(), target.hashPath(), target.path(), text, target.scope());
    }
}

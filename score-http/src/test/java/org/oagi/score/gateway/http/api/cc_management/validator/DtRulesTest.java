package org.oagi.score.gateway.http.api.cc_management.validator;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.Cardinality;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.Definition;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.validator.DtValidationInput.SupplementaryComponent;
import org.oagi.score.gateway.http.api.cc_management.validator.DtValidationInput.ValueDomain;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceSummaryRecord;
import org.oagi.score.gateway.http.api.validation.PathSegment;
import org.oagi.score.gateway.http.api.validation.Severity;
import org.oagi.score.gateway.http.api.validation.ValidationRule;
import org.oagi.score.gateway.http.api.validation.Violation;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtManifestId;
import org.oagi.score.gateway.http.common.model.ScoreRole;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The rules of a DT, run on an input written by hand. */
class DtRulesTest {

    private static final NamespaceId NAMESPACE = new NamespaceId(BigInteger.ONE);
    private static final NamespaceSummaryRecord STANDARD = new NamespaceSummaryRecord(NAMESPACE, "u", "p", true);
    private static final Definition DEFINITION = new Definition("d", null);
    private static final UserSummaryRecord DEVELOPER =
            new UserSummaryRecord(new UserId(BigInteger.ONE), "dev", "Dev", List.of(ScoreRole.DEVELOPER));

    private static final ValueDomain TOKEN = new ValueDomain("Token", new XbtManifestId(BigInteger.ONE), null, null, true);
    private static final ValueDomain STRING = new ValueDomain("String", new XbtManifestId(BigInteger.TWO), null, null, false);
    private static final ValueDomain INTEGER = new ValueDomain("Integer", new XbtManifestId(BigInteger.TEN), null, null, false);
    private static final ValueDomain CODE_LIST = new ValueDomain("Token", null, new CodeListManifestId(BigInteger.ONE), null, false);

    private static final List<ValidationRule<DtValidationInput>> RULES = new DtSubjectValidator().rules();

    private static DtSummaryRecord dt(String term, NamespaceId namespace, Definition definition, boolean deprecated, int revision,
                                      UserSummaryRecord owner) {
        return new DtSummaryRecord(null, null, new DtManifestId(BigInteger.ONE), null, null, null, term + ". Type", term, null, "Text",
                null, false, deprecated, CcState.WIP, namespace, null, definition, revision, owner, null, null);
    }

    private static DtSummaryRecord dt() {
        return dt("Text", NAMESPACE, DEFINITION, false, 1, null);
    }

    private static SupplementaryComponent sc(long id, String property, String representation, Integer min, Integer max,
                                             Definition definition, List<ValueDomain> valueDomains) {
        DtScSummaryRecord sc = new DtScSummaryRecord(null, null, new DtScManifestId(BigInteger.valueOf(id)), null, null, null, null,
                "Text. " + property, "Text", property, representation, new Cardinality(min, max), false, CcState.WIP, null,
                definition, 1, null, null, null);
        return new SupplementaryComponent(sc, valueDomains, List.of(new PathSegment("DT", "1"), new PathSegment("DT_SC", String.valueOf(id))));
    }

    private static SupplementaryComponent sc(long id, String property, String representation) {
        return sc(id, property, representation, 0, 1, DEFINITION, List.of(TOKEN));
    }

    private static DtValidationInput input(DtSummaryRecord root, DtSummaryRecord previous, NamespaceSummaryRecord namespace,
                                           List<ValueDomain> valueDomains, List<SupplementaryComponent> scs) {
        return new DtValidationInput(root, previous, namespace, valueDomains, scs);
    }

    private static List<String> ids(DtValidationInput input) {
        return RULES.stream().flatMap(r -> r.evaluate(input)).map(Violation::ruleId).sorted().collect(Collectors.toList());
    }

    @Test
    void a_plain_dt_has_nothing_to_report() {
        assertTrue(ids(input(dt(), null, STANDARD, List.of(TOKEN, STRING.isDefault() ? STRING : INTEGER), List.of(sc(2, "List", "Name")))).isEmpty());
    }

    @Test
    void the_ids_have_no_gaps_inside_a_category() {
        assertEquals(List.of("DT-ATTR-01", "DT-ATTR-02", "DT-ATTR-03", "DT-ATTR-04", "DT-ATTR-05", "DT-ATTR-06", "DT-ATTR-07",
                "DT-VD-01", "DT-VD-02", "DT-VD-03", "DT-VD-04", "DT-SC-01", "DT-SC-02", "DT-SC-03"),
                RULES.stream().map(ValidationRule::id).toList());
    }

    @Test
    void the_namespace_is_required_and_standard_for_a_developer() {
        assertEquals(List.of("DT-ATTR-01"), ids(input(dt("Text", null, DEFINITION, false, 1, null), null, null, List.of(TOKEN), List.of())));
        DtSummaryRecord developers = dt("Text", NAMESPACE, DEFINITION, false, 1, DEVELOPER);
        assertEquals(List.of("DT-ATTR-02"), ids(input(developers, null, new NamespaceSummaryRecord(NAMESPACE, "u", "p", false), List.of(TOKEN), List.of())));
        assertTrue(ids(input(developers, null, STANDARD, List.of(TOKEN), List.of())).isEmpty());
    }

    @Test
    void a_first_revision_is_not_deprecated_and_a_revision_keeps_what_it_has() {
        assertEquals(List.of("DT-ATTR-03"), ids(input(dt("Text", NAMESPACE, DEFINITION, true, 1, null), null, STANDARD, List.of(TOKEN), List.of())));
        DtSummaryRecord previous = dt();
        DtSummaryRecord changed = dt("Date", new NamespaceId(BigInteger.TEN), DEFINITION, false, 2, null);
        assertEquals(List.of("DT-ATTR-04", "DT-ATTR-05"), ids(input(changed, previous, null, List.of(TOKEN), List.of())));
        DtSummaryRecord wasDeprecated = dt("Text", NAMESPACE, DEFINITION, true, 1, null);
        DtValidationInput undone = input(dt("Text", NAMESPACE, DEFINITION, false, 2, null), wasDeprecated, STANDARD, List.of(TOKEN), List.of());
        assertEquals(List.of("DT-ATTR-06"), ids(undone));
        assertTrue(RULES.stream().flatMap(r -> r.evaluate(undone)).allMatch(v -> v.severity() == Severity.WARNING));
    }

    @Test
    void a_missing_definition_is_a_warning_on_the_dt_and_on_each_sc() {
        DtValidationInput input = input(dt("Text", NAMESPACE, new Definition(" ", null), false, 1, null), null, STANDARD, List.of(TOKEN),
                List.of(sc(2, "List", "Name", 0, 1, null, List.of(TOKEN))));
        assertEquals(List.of("DT-ATTR-07", "DT-SC-03"), ids(input));
    }

    @Test
    void a_data_type_has_value_domains_and_one_of_them_is_the_default() {
        assertEquals(List.of("DT-VD-01"), ids(input(dt(), null, STANDARD, List.of(), List.of())));
        assertEquals(List.of("DT-VD-02"), ids(input(dt(), null, STANDARD, List.of(STRING, INTEGER), List.of())));
        ValueDomain secondDefault = new ValueDomain("Integer", new XbtManifestId(BigInteger.TEN), null, null, true);
        assertEquals(List.of("DT-VD-02"), ids(input(dt(), null, STANDARD, List.of(TOKEN, secondDefault), List.of())));
    }

    @Test
    void the_same_value_domain_is_not_twice_in_a_holder() {
        assertEquals(List.of("DT-VD-03"), ids(input(dt(), null, STANDARD, List.of(TOKEN, new ValueDomain("Token", TOKEN.xbtManifestId(), null, null, false)), List.of())));
    }

    @Test
    void a_code_list_needs_a_primitive_that_is_like_a_string() {
        assertTrue(ids(input(dt(), null, STANDARD, List.of(TOKEN, CODE_LIST), List.of())).isEmpty());
        ValueDomain defaultString = new ValueDomain("String", new XbtManifestId(BigInteger.TWO), null, null, true);
        assertTrue(ids(input(dt(), null, STANDARD, List.of(defaultString, CODE_LIST), List.of())).isEmpty());
        ValueDomain defaultNormalized = new ValueDomain("NormalizedString", new XbtManifestId(BigInteger.valueOf(3)), null, null, true);
        assertTrue(ids(input(dt(), null, STANDARD, List.of(defaultNormalized, CODE_LIST), List.of())).isEmpty());
        ValueDomain defaultInteger = new ValueDomain("Integer", new XbtManifestId(BigInteger.TEN), null, null, true);
        assertEquals(List.of("DT-VD-04"), ids(input(dt(), null, STANDARD, List.of(defaultInteger, CODE_LIST), List.of())));
        assertEquals(List.of("DT-VD-04"), ids(input(dt(), null, STANDARD, List.of(CODE_LIST), List.of())).stream().filter(i -> i.equals("DT-VD-04")).toList());
    }

    @Test
    void the_value_domains_of_an_sc_are_checked_like_the_ones_of_the_dt() {
        SupplementaryComponent none = sc(2, "List", "Name", 0, 1, DEFINITION, List.of());
        DtValidationInput input = input(dt(), null, STANDARD, List.of(TOKEN), List.of(none));
        List<Violation> found = RULES.stream().flatMap(r -> r.evaluate(input)).toList();
        assertEquals(1, found.size());
        assertEquals("DT-VD-01", found.get(0).ruleId());
        assertEquals(List.of("DT", "DT_SC"), found.get(0).locations().get(0).path().stream().map(PathSegment::type).toList());
    }

    @Test
    void two_scs_with_the_same_terms_are_a_warning_and_the_same_property_term_alone_is_not() {
        DtValidationInput same = input(dt(), null, STANDARD, List.of(TOKEN), List.of(sc(2, "List", "Name"), sc(3, "List", "Name")));
        List<Violation> found = RULES.stream().flatMap(r -> r.evaluate(same)).toList();
        assertEquals(1, found.size());
        assertEquals("DT-SC-01", found.get(0).ruleId());
        assertEquals(Severity.WARNING, found.get(0).severity());
        assertEquals(2, found.get(0).locations().size());
        assertTrue(ids(input(dt(), null, STANDARD, List.of(TOKEN), List.of(sc(2, "List", "Name"), sc(3, "List", "Identifier")))).isEmpty());
    }

    @Test
    void the_cardinality_of_an_sc_is_valid() {
        assertEquals(List.of("DT-SC-02", "DT-SC-02", "DT-SC-02"), ids(input(dt(), null, STANDARD, List.of(TOKEN), List.of(
                sc(2, "A", "Name", -1, 1, DEFINITION, List.of(TOKEN)),
                sc(3, "B", "Name", 0, -2, DEFINITION, List.of(TOKEN)),
                sc(4, "C", "Name", 3, 2, DEFINITION, List.of(TOKEN))))));
        assertTrue(ids(input(dt(), null, STANDARD, List.of(TOKEN), List.of(sc(2, "A", "Name", 0, -1, DEFINITION, List.of(TOKEN))))).isEmpty());
    }
}

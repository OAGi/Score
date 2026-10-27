package org.oagi.score.gateway.http.api.cc_management.validator;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.Definition;
import org.oagi.score.gateway.http.api.cc_management.model.ValueConstraint;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.acc.OagisComponentType;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpType;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.validator.rules.*;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceSummaryRecord;
import org.oagi.score.gateway.http.api.validation.Severity;
import org.oagi.score.gateway.http.api.validation.ValidationRule;
import org.oagi.score.gateway.http.api.validation.Violation;
import org.oagi.score.gateway.http.common.model.ScoreRole;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The rules of an ASCCP and of a BCCP, run on a input written by hand. */
class AsccpAndBccpRulesTest {

    private static final NamespaceId NAMESPACE = new NamespaceId(BigInteger.ONE);
    private static final NamespaceSummaryRecord STANDARD = new NamespaceSummaryRecord(NAMESPACE, "u", "p", true);
    private static final Definition DEFINITION = new Definition("d", null);
    private static final UserSummaryRecord DEVELOPER =
            new UserSummaryRecord(new UserId(BigInteger.ONE), "dev", "Dev", List.of(ScoreRole.DEVELOPER));

    private static AsccpSummaryRecord asccp(String term, NamespaceId namespace, Definition definition, boolean deprecated,
                                            boolean nillable, int revision, UserSummaryRecord owner) {
        return new AsccpSummaryRecord(null, null, new AsccpManifestId(BigInteger.ONE), null, null, AsccpType.Default, null,
                term + ". X", term, true, deprecated, nillable, CcState.WIP, namespace, definition, revision, owner, null, null);
    }

    private static AsccpSummaryRecord asccp() {
        return asccp("P", NAMESPACE, DEFINITION, false, false, 1, null);
    }

    private static BccpSummaryRecord bccp(String term, NamespaceId namespace, Definition definition, boolean deprecated,
                                          boolean nillable, ValueConstraint valueConstraint, int revision, UserSummaryRecord owner) {
        return new BccpSummaryRecord(null, null, new BccpManifestId(BigInteger.ONE), null, null, null, term + ". Text", term, "Text",
                deprecated, nillable, CcState.WIP, namespace, valueConstraint, definition, revision, owner, null, null);
    }

    private static BccpSummaryRecord bccp() {
        return bccp("T", NAMESPACE, DEFINITION, false, false, null, 1, null);
    }

    private static AccSummaryRecord acc(boolean deprecated) {
        return new AccSummaryRecord(null, null, new AccManifestId(BigInteger.TEN), null, null, "Default", null, "A. Details", "A", null,
                OagisComponentType.Semantics, false, deprecated, CcState.WIP, NAMESPACE, DEFINITION, 1, null, null, null);
    }

    private static DtSummaryRecord dt(boolean deprecated) {
        return new DtSummaryRecord(null, null, new DtManifestId(BigInteger.TEN), null, null, null, "Text. Type", "Text", null, "Text",
                null, false, deprecated, CcState.WIP, NAMESPACE, null, DEFINITION, 1, null, null, null);
    }

    private static List<ValidationRule<AsccpValidationInput>> asccpRules() {
        return new AsccpSubjectValidator().rules();
    }

    private static List<ValidationRule<BccpValidationInput>> bccpRules() {
        return new BccpSubjectValidator().rules();
    }

    private static List<String> ids(AsccpValidationInput input) {
        return asccpRules().stream().flatMap(r -> r.evaluate(input)).map(Violation::ruleId).sorted().collect(Collectors.toList());
    }

    private static List<String> ids(BccpValidationInput input) {
        return bccpRules().stream().flatMap(r -> r.evaluate(input)).map(Violation::ruleId).sorted().collect(Collectors.toList());
    }

    @Test
    void a_plain_property_has_nothing_to_report() {
        assertTrue(ids(new AsccpValidationInput(asccp(), null, STANDARD, acc(false))).isEmpty());
        assertTrue(ids(new BccpValidationInput(bccp(), null, STANDARD, dt(false))).isEmpty());
    }

    @Test
    void the_ids_are_those_of_the_kind_of_subject_and_have_no_gaps() {
        List<String> asccpIds = asccpRules().stream().map(ValidationRule::id).toList();
        assertEquals(List.of("ASCCP-ATTR-01", "ASCCP-ATTR-02", "ASCCP-ATTR-03", "ASCCP-ATTR-04", "ASCCP-ATTR-05", "ASCCP-ATTR-06",
                "ASCCP-ATTR-07", "ASCCP-ATTR-08", "ASCCP-ATTR-09", "ASCCP-RI-01"), asccpIds);
        List<String> bccpIds = bccpRules().stream().map(ValidationRule::id).toList();
        assertEquals(List.of("BCCP-ATTR-01", "BCCP-ATTR-02", "BCCP-ATTR-03", "BCCP-ATTR-04", "BCCP-ATTR-05", "BCCP-ATTR-06",
                "BCCP-ATTR-07", "BCCP-ATTR-08", "BCCP-ATTR-09", "BCCP-ATTR-10", "BCCP-ATTR-11", "BCCP-RI-01"), bccpIds);
    }

    @Test
    void the_property_term_and_the_namespace_are_required() {
        assertEquals(List.of("ASCCP-ATTR-01", "ASCCP-ATTR-02"),
                ids(new AsccpValidationInput(asccp(" ", null, DEFINITION, false, false, 1, null), null, null, acc(false))));
        assertEquals(List.of("BCCP-ATTR-01", "BCCP-ATTR-02"),
                ids(new BccpValidationInput(bccp(null, null, DEFINITION, false, false, null, 1, null), null, null, dt(false))));
    }

    @Test
    void a_missing_definition_is_a_warning() {
        AsccpValidationInput asccp = new AsccpValidationInput(asccp("P", NAMESPACE, new Definition(" ", null), false, false, 1, null), null, STANDARD, acc(false));
        assertEquals(List.of("ASCCP-ATTR-09"), ids(asccp));
        assertTrue(asccpRules().stream().flatMap(r -> r.evaluate(asccp)).allMatch(v -> v.severity() == Severity.WARNING));
        assertEquals(List.of("BCCP-ATTR-09"),
                ids(new BccpValidationInput(bccp("T", NAMESPACE, null, false, false, null, 1, null), null, STANDARD, dt(false))));
    }

    @Test
    void a_property_of_a_developer_has_a_standard_namespace() {
        AsccpSummaryRecord developers = asccp("P", NAMESPACE, DEFINITION, false, false, 1, DEVELOPER);
        NamespaceSummaryRecord endUsers = new NamespaceSummaryRecord(NAMESPACE, "u", "p", false);
        assertEquals(List.of("ASCCP-ATTR-03"), ids(new AsccpValidationInput(developers, null, endUsers, acc(false))));
        assertTrue(ids(new AsccpValidationInput(developers, null, STANDARD, acc(false))).isEmpty());
        assertEquals(List.of("BCCP-ATTR-03"),
                ids(new BccpValidationInput(bccp("T", NAMESPACE, DEFINITION, false, false, null, 1, DEVELOPER), null, endUsers, dt(false))));
    }

    @Test
    void a_first_revision_is_not_deprecated() {
        assertEquals(List.of("ASCCP-ATTR-04"),
                ids(new AsccpValidationInput(asccp("P", NAMESPACE, DEFINITION, true, false, 1, null), null, STANDARD, acc(true))));
        assertTrue(ids(new AsccpValidationInput(asccp("P", NAMESPACE, DEFINITION, true, false, 2, null), null, STANDARD, acc(true))).isEmpty());
        assertEquals(List.of("BCCP-ATTR-04"),
                ids(new BccpValidationInput(bccp("T", NAMESPACE, DEFINITION, true, false, null, 1, null), null, STANDARD, dt(true))));
    }

    @Test
    void a_revision_keeps_the_namespace_and_the_property_term() {
        AsccpSummaryRecord previous = asccp();
        AsccpSummaryRecord changed = asccp("Q", new NamespaceId(BigInteger.TEN), DEFINITION, false, false, 2, null);
        assertEquals(List.of("ASCCP-ATTR-05", "ASCCP-ATTR-06"), ids(new AsccpValidationInput(changed, previous, null, acc(false))));
        assertTrue(ids(new AsccpValidationInput(asccp("P", NAMESPACE, DEFINITION, false, false, 2, null), previous, STANDARD, acc(false))).isEmpty());

        BccpSummaryRecord bccpPrevious = bccp();
        BccpSummaryRecord bccpChanged = bccp("Q", new NamespaceId(BigInteger.TEN), DEFINITION, false, false, null, 2, null);
        assertEquals(List.of("BCCP-ATTR-05", "BCCP-ATTR-06"), ids(new BccpValidationInput(bccpChanged, bccpPrevious, null, dt(false))));
    }

    @Test
    void deprecation_and_nillable_going_back_are_warnings() {
        AsccpSummaryRecord previous = asccp("P", NAMESPACE, DEFINITION, true, true, 1, null);
        AsccpValidationInput input = new AsccpValidationInput(asccp("P", NAMESPACE, DEFINITION, false, false, 2, null), previous, STANDARD, acc(false));
        assertEquals(List.of("ASCCP-ATTR-07", "ASCCP-ATTR-08"), ids(input));
        assertTrue(asccpRules().stream().flatMap(r -> r.evaluate(input)).allMatch(v -> v.severity() == Severity.WARNING));
        // false to true is allowed
        assertTrue(ids(new AsccpValidationInput(asccp("P", NAMESPACE, DEFINITION, true, true, 2, null), asccp(), STANDARD, acc(true))).isEmpty());
    }

    @Test
    void the_acc_of_an_asccp_that_is_deprecated_is_a_warning_unless_the_asccp_is_deprecated_too() {
        assertEquals(List.of("ASCCP-RI-01"), ids(new AsccpValidationInput(asccp(), null, STANDARD, acc(true))));
        assertTrue(ids(new AsccpValidationInput(asccp("P", NAMESPACE, DEFINITION, true, false, 2, null), null, STANDARD, acc(true))).isEmpty());
        assertTrue(ids(new AsccpValidationInput(asccp(), null, STANDARD, null)).isEmpty());
    }

    @Test
    void a_fixed_value_is_kept_and_excludes_nillable() {
        BccpSummaryRecord fixed = bccp("T", NAMESPACE, DEFINITION, false, false, new ValueConstraint(null, "x"), 1, null);
        BccpSummaryRecord otherFixed = bccp("T", NAMESPACE, DEFINITION, false, false, new ValueConstraint(null, "y"), 2, null);
        assertEquals(List.of("BCCP-ATTR-10"), ids(new BccpValidationInput(otherFixed, fixed, STANDARD, dt(false))));
        assertTrue(ids(new BccpValidationInput(fixed, fixed, STANDARD, dt(false))).isEmpty());

        BccpSummaryRecord nillableFixed = bccp("T", NAMESPACE, DEFINITION, false, true, new ValueConstraint(null, "x"), 1, null);
        assertEquals(List.of("BCCP-ATTR-11"), ids(new BccpValidationInput(nillableFixed, null, STANDARD, dt(false))));
    }

    @Test
    void the_bdt_of_a_bccp_that_is_deprecated_is_a_warning_unless_the_bccp_is_deprecated_too() {
        assertEquals(List.of("BCCP-RI-01"), ids(new BccpValidationInput(bccp(), null, STANDARD, dt(true))));
        assertTrue(ids(new BccpValidationInput(bccp("T", NAMESPACE, DEFINITION, true, false, null, 2, null), null, STANDARD, dt(true))).isEmpty());
    }

    @Test
    void a_violation_is_at_the_root_of_the_tree_or_at_the_node_it_is_about() {
        Violation atRoot = asccpRules().get(1).evaluate(
                new AsccpValidationInput(asccp("P", null, DEFINITION, false, false, 1, null), null, null, acc(false))).findFirst().orElseThrow();
        assertEquals("ASCCP", atRoot.locations().get(0).path().get(0).type());
        Violation atAcc = asccpRules().get(9).evaluate(new AsccpValidationInput(asccp(), null, STANDARD, acc(true))).findFirst().orElseThrow();
        assertEquals(List.of("ASCCP", "ACC"), atAcc.locations().get(0).path().stream().map(s -> s.type()).toList());
    }
}

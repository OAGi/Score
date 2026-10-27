package org.oagi.score.gateway.http.api.cc_management.validator;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.Cardinality;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.Definition;
import org.oagi.score.gateway.http.api.cc_management.model.ValueConstraint;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.acc.OagisComponentType;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpType;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.EntityType;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.validator.rules.AccAssociationRules;
import org.oagi.score.gateway.http.api.cc_management.validator.rules.AccAttributeRules;
import org.oagi.score.gateway.http.api.cc_management.validator.rules.AccBaseRules;
import org.oagi.score.gateway.http.api.cc_management.validator.rules.AccReferenceRules;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceSummaryRecord;
import org.oagi.score.gateway.http.api.validation.PathSegment;
import org.oagi.score.gateway.http.api.validation.Severity;
import org.oagi.score.gateway.http.api.validation.ValidationRule;
import org.oagi.score.gateway.http.api.validation.Violation;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The rules that compare an ACC with its previous revision, look at who owns what, or at what is not given. */
class AccRevisionAndReferenceRulesTest {

    private static final NamespaceId NAMESPACE = new NamespaceId(BigInteger.ONE);
    private static final Definition DEFINITION = new Definition("d", null);
    private static final List<PathSegment> PATH = List.of(new PathSegment("ACC", "1"), new PathSegment("X", "2"));

    private static final UserSummaryRecord DEVELOPER =
            new UserSummaryRecord(new UserId(BigInteger.ONE), "dev", "Dev", List.of(ScoreRole.DEVELOPER));
    private static final UserSummaryRecord END_USER =
            new UserSummaryRecord(new UserId(BigInteger.TWO), "eu", "Eu", List.of(ScoreRole.END_USER));

    private static final List<ValidationRule<AccValidationInput>> RULES = rules();

    @SuppressWarnings("unchecked")
    private static List<ValidationRule<AccValidationInput>> rules() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
                AccAttributeRules.class, AccAssociationRules.class, AccBaseRules.class, AccReferenceRules.class)) {
            return context.getBeansOfType(ValidationRule.class).values().stream()
                    .map(r -> (ValidationRule<AccValidationInput>) r).toList();
        }
    }

    private static AccSummaryRecord acc(String term, OagisComponentType type, boolean isAbstract, NamespaceId namespace,
                                        Definition definition, UserSummaryRecord owner) {
        return new AccSummaryRecord(null, null, new AccManifestId(BigInteger.ONE), null, null, "Default", null,
                term + ". Details", term, null, type, isAbstract, false, CcState.WIP, namespace, definition, 2, owner, null, null);
    }

    private static AccSummaryRecord acc() {
        return acc("Root", OagisComponentType.Semantics, false, NAMESPACE, DEFINITION, null);
    }

    private static BccSummaryRecord bcc(EntityType entityType, Integer min, Integer max, boolean deprecated,
                                        ValueConstraint valueConstraint, Definition definition) {
        return new BccSummaryRecord(null, null, new BccManifestId(BigInteger.ONE), null, null, null, null, null, null, null,
                entityType, "bcc", new Cardinality(min, max), deprecated, false, CcState.WIP, valueConstraint, definition,
                1, null, null, null);
    }

    private static BccpSummaryRecord bccp(boolean deprecated, UserSummaryRecord owner) {
        return new BccpSummaryRecord(null, null, new BccpManifestId(BigInteger.ONE), null, null, null, "T. Text", "T", "Text",
                deprecated, false, CcState.WIP, null, null, DEFINITION, 1, owner, null, null);
    }

    private static AsccSummaryRecord ascc(Integer min, Integer max, boolean deprecated, Definition definition) {
        return new AsccSummaryRecord(null, null, new AsccManifestId(BigInteger.ONE), null, null, null, null, null, null, null,
                "ascc", new Cardinality(min, max), deprecated, CcState.WIP, definition, 1, null, null, null);
    }

    private static AsccpSummaryRecord asccp(boolean reusable, boolean deprecated, UserSummaryRecord owner) {
        return new AsccpSummaryRecord(null, null, new AsccpManifestId(BigInteger.ONE), null, null, AsccpType.Default, null,
                "P. X", "P", reusable, deprecated, false, CcState.WIP, null, DEFINITION, 1, owner, null, null);
    }

    private static AccValidationInput input(AccSummaryRecord root, AccSummaryRecord previous, NamespaceSummaryRecord namespace,
                                           List<AccValidationInput.OwnAscc> asccs, List<AccValidationInput.OwnBcc> bccs) {
        return new AccValidationInput(root, previous, namespace, List.of(), List.of(), asccs, bccs, List.of());
    }

    private static List<String> ids(AccValidationInput input) {
        return RULES.stream().flatMap(r -> r.evaluate(input)).map(Violation::ruleId).sorted().collect(Collectors.toList());
    }

    private static AccValidationInput withBcc(BccSummaryRecord bcc, BccSummaryRecord previous, BccpSummaryRecord bccp, boolean sc) {
        return input(acc(), null, null, List.of(), List.of(new AccValidationInput.OwnBcc(bcc, bccp, previous, sc, PATH)));
    }

    private static AccValidationInput withAscc(AsccSummaryRecord ascc, AsccSummaryRecord previous, AsccpSummaryRecord asccp, int references) {
        return input(acc(), null, null, List.of(new AccValidationInput.OwnAscc(ascc, asccp, null, previous, references, PATH)), List.of());
    }

    @Test
    void a_revision_keeps_what_the_previous_revision_has() {
        AccSummaryRecord previous = acc("Root", OagisComponentType.Semantics, false, NAMESPACE, DEFINITION, null);
        assertTrue(ids(input(acc(), previous, null, List.of(), List.of())).isEmpty());

        AccSummaryRecord other = acc("Other", OagisComponentType.Base, false, new NamespaceId(BigInteger.TEN), DEFINITION, null);
        assertEquals(List.of("ACC-ATTR-05", "ACC-ATTR-07", "ACC-ATTR-08"), ids(input(other, previous, null, List.of(), List.of())));
    }

    @Test
    void semantics_does_not_become_abstract_in_a_revision() {
        AccSummaryRecord previous = acc();
        AccSummaryRecord abstractNow = acc("Root", OagisComponentType.Semantics, true, NAMESPACE, DEFINITION, null);
        assertEquals(List.of("ACC-ATTR-06"), ids(input(abstractNow, previous, null, List.of(), List.of())));
        assertTrue(ids(input(previous, abstractNow, null, List.of(), List.of())).isEmpty());
    }

    @Test
    void a_missing_definition_is_a_warning_on_the_acc_and_on_each_association() {
        AccSummaryRecord noDefinition = acc("Root", OagisComponentType.Semantics, false, NAMESPACE, new Definition(" ", null), null);
        AccValidationInput input = input(noDefinition, null, null,
                List.of(new AccValidationInput.OwnAscc(ascc(0, 1, false, null), asccp(true, false, null), null, PATH)),
                List.of(new AccValidationInput.OwnBcc(bcc(EntityType.Element, 0, 1, false, null, new Definition("", null)), bccp(false, null), PATH)));
        assertEquals(List.of("ACC-ASSOC-05", "ACC-ASSOC-05", "ACC-ATTR-11"), ids(input));
        assertTrue(RULES.stream().flatMap(r -> r.evaluate(input)).allMatch(v -> v.severity() == Severity.WARNING));
    }

    @Test
    void only_an_association_of_the_previous_revision_is_deprecated() {
        BccSummaryRecord deprecated = bcc(EntityType.Element, 0, 1, true, null, DEFINITION);
        assertEquals(List.of("ACC-ATTR-10"), ids(withBcc(deprecated, null, bccp(false, null), false)));
        assertTrue(ids(withBcc(deprecated, deprecated, bccp(false, null), false)).isEmpty());
        assertEquals(List.of("ACC-ATTR-10"), ids(withAscc(ascc(0, 1, true, DEFINITION), null, asccp(true, false, null), 1)));
    }

    @Test
    void an_element_has_no_default_value_and_an_attribute_has_no_supplementary_component() {
        assertEquals(List.of("ACC-ATTR-12"), ids(withBcc(
                bcc(EntityType.Element, 0, 1, false, new ValueConstraint("x", null), DEFINITION), null, bccp(false, null), false)));
        assertTrue(ids(withBcc(
                bcc(EntityType.Attribute, 0, 1, false, new ValueConstraint("x", null), DEFINITION), null, bccp(false, null), false)).isEmpty());
        assertEquals(List.of("ACC-ATTR-13"), ids(withBcc(
                bcc(EntityType.Attribute, 0, 1, false, null, DEFINITION), null, bccp(false, null), true)));
    }

    @Test
    void an_acc_of_a_developer_has_a_standard_namespace() {
        AccSummaryRecord developers = acc("Root", OagisComponentType.Semantics, false, NAMESPACE, DEFINITION, DEVELOPER);
        NamespaceSummaryRecord endUsers = new NamespaceSummaryRecord(NAMESPACE, "u", "p", false);
        assertEquals(List.of("ACC-ATTR-14"), ids(input(developers, null, endUsers, List.of(), List.of())));
        assertTrue(ids(input(developers, null, new NamespaceSummaryRecord(NAMESPACE, "u", "p", true), List.of(), List.of())).isEmpty());
        assertTrue(ids(input(acc(), null, endUsers, List.of(), List.of())).isEmpty());
    }

    @Test
    void a_deprecated_property_is_a_warning_unless_the_association_is_deprecated_too() {
        assertEquals(List.of("ACC-ASSOC-04"), ids(withBcc(
                bcc(EntityType.Element, 0, 1, false, null, DEFINITION), null, bccp(true, null), false)));
        assertEquals(List.of("ACC-ASSOC-04"), ids(withAscc(ascc(0, 1, false, DEFINITION), null, asccp(true, true, null), 1)));
        assertTrue(ids(withBcc(bcc(EntityType.Element, 0, 1, true, null, DEFINITION), bcc(EntityType.Element, 0, 1, true, null, DEFINITION),
                bccp(true, null), false)).isEmpty());
    }

    @Test
    void cardinality_only_relaxes_from_one_revision_to_the_next() {
        BccSummaryRecord previous = bcc(EntityType.Element, 1, 3, false, null, DEFINITION);
        assertEquals(List.of("ACC-CARD-06"), ids(withBcc(bcc(EntityType.Element, 2, 3, false, null, DEFINITION), previous, bccp(false, null), false)));
        assertEquals(List.of("ACC-CARD-08"), ids(withBcc(bcc(EntityType.Element, 1, 2, false, null, DEFINITION), previous, bccp(false, null), false)));
        assertTrue(ids(withBcc(bcc(EntityType.Element, 0, -1, false, null, DEFINITION), previous, bccp(false, null), false)).isEmpty());

        AsccSummaryRecord unbounded = ascc(0, -1, false, DEFINITION);
        assertEquals(List.of("ACC-CARD-07"), ids(withAscc(ascc(0, 5, false, DEFINITION), unbounded, asccp(true, false, null), 1)));
        assertTrue(ids(withAscc(ascc(0, -1, false, DEFINITION), unbounded, asccp(true, false, null), 1)).isEmpty());
    }

    @Test
    void a_property_that_is_not_reusable_is_pointed_at_once() {
        assertTrue(ids(withAscc(ascc(0, 1, false, DEFINITION), null, asccp(false, false, null), 1)).isEmpty());
        assertEquals(List.of("ACC-RI-01"), ids(withAscc(ascc(0, 1, false, DEFINITION), null, asccp(false, false, null), 2)));
        assertTrue(ids(withAscc(ascc(0, 1, false, DEFINITION), null, asccp(true, false, null), 5)).isEmpty());
    }

    @Test
    void an_acc_of_a_developer_points_at_no_property_of_an_end_user() {
        AccSummaryRecord developers = acc("Root", OagisComponentType.Semantics, false, NAMESPACE, DEFINITION, DEVELOPER);
        AccValidationInput input = input(developers, null, null,
                List.of(new AccValidationInput.OwnAscc(ascc(0, 1, false, DEFINITION), asccp(true, false, END_USER), null, null, 1, PATH)),
                List.of(new AccValidationInput.OwnBcc(bcc(EntityType.Element, 0, 1, false, null, DEFINITION), bccp(false, END_USER), null, false, PATH)));
        assertEquals(List.of("ACC-RI-02", "ACC-RI-02"), ids(input));
        assertTrue(ids(input(acc(), null, null, input.ownAsccs(), input.ownBccs())).isEmpty());
    }
}

package org.oagi.score.gateway.http.api.cc_management.validator;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.cc_management.model.Cardinality;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
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
import org.oagi.score.gateway.http.api.validation.PathSegment;
import org.oagi.score.gateway.http.api.validation.Severity;
import org.oagi.score.gateway.http.api.validation.ValidationRule;
import org.oagi.score.gateway.http.api.validation.Violation;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The rules about what an ACC is and has by itself, run on a input written by hand. */
class AccStateRulesTest {

    private static final org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId NAMESPACE =
            new org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId(BigInteger.ONE);

    private static final org.oagi.score.gateway.http.api.cc_management.model.Definition DEFINITION =
            new org.oagi.score.gateway.http.api.cc_management.model.Definition("d", null);

    private final List<ValidationRule<AccValidationInput>> rules = rules();
    private final List<AccValidationInput.OwnAscc> asccs = new ArrayList<>();
    private final List<AccValidationInput.OwnBcc> bccs = new ArrayList<>();
    private final List<AccValidationInput.Base> bases = new ArrayList<>();

    @SuppressWarnings("unchecked")
    private static List<ValidationRule<AccValidationInput>> rules() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
                AccAttributeRules.class, AccAssociationRules.class, AccBaseRules.class, AccReferenceRules.class)) {
            return context.getBeansOfType(ValidationRule.class).values().stream()
                    .map(r -> (ValidationRule<AccValidationInput>) r).toList();
        }
    }

    private static AccSummaryRecord acc(long id, String term, OagisComponentType type, boolean isAbstract, boolean deprecated,
                                        NamespaceIdHolder namespace, int revision) {
        return new AccSummaryRecord(null, null, new AccManifestId(BigInteger.valueOf(id)), null, null, "Default", null,
                term + ". Details", term, null, type, isAbstract, deprecated, CcState.WIP,
                namespace.id, DEFINITION, revision, null, null, null);
    }

    private record NamespaceIdHolder(org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId id) {
    }

    private static AccSummaryRecord valid(OagisComponentType type) {
        return acc(1, "Root", type, false, false, new NamespaceIdHolder(NAMESPACE), 1);
    }

    private void bcc(long id, EntityType entityType, Integer min, Integer max) {
        BccpSummaryRecord bccp = new BccpSummaryRecord(null, null, new BccpManifestId(BigInteger.valueOf(id)), null, null, null,
                "T" + id + ". Text", "T" + id, "Text", false, false, CcState.WIP, null, null, null, 1, null, null, null);
        BccSummaryRecord bcc = new BccSummaryRecord(null, null, new BccManifestId(BigInteger.valueOf(id)), null, null, null,
                bccp.bccpManifestId(), null, null, null, entityType, "bcc" + id, new Cardinality(min, max), false, false,
                CcState.WIP, null, DEFINITION, 1, null, null, null);
        bccs.add(new AccValidationInput.OwnBcc(bcc, bccp, List.of(new PathSegment("ACC", "1"),
                new PathSegment("BCC", String.valueOf(id)), new PathSegment("BCCP", String.valueOf(id)))));
    }

    private void ascc(long id, AsccpType type, Integer min, Integer max) {
        AsccpSummaryRecord asccp = new AsccpSummaryRecord(null, null,
                new AsccpManifestId(BigInteger.valueOf(id)), null, null, type, null, "P" + id + ". X", "P" + id, true,
                false, false, CcState.WIP, null, null, 1, null, null, null);
        AsccSummaryRecord ascc = new AsccSummaryRecord(null, null, new AsccManifestId(BigInteger.valueOf(id)), null, null, null,
                null, null, null, null, "ascc" + id, new Cardinality(min, max), false, CcState.WIP, DEFINITION, 1, null, null, null);
        asccs.add(new AccValidationInput.OwnAscc(ascc, asccp, null, List.of(new PathSegment("ACC", "1"),
                new PathSegment("ASCC", String.valueOf(id)))));
    }

    private List<String> ids(AccSummaryRecord root) {
        AccValidationInput input = new AccValidationInput(root, List.of(), List.of(), asccs, bccs, bases);
        return rules.stream().flatMap(r -> r.evaluate(input)).map(Violation::ruleId).sorted().collect(Collectors.toList());
    }

    @Test
    void a_plain_acc_has_nothing_to_report() {
        bcc(10, EntityType.Element, 0, 1);
        bcc(11, EntityType.Attribute, 0, 1);
        assertTrue(ids(valid(OagisComponentType.Semantics)).isEmpty());
    }

    @Test
    void the_name_and_the_namespace_are_required() {
        AccSummaryRecord acc = acc(1, " ", OagisComponentType.Semantics, false, false, new NamespaceIdHolder(null), 1);
        assertEquals(List.of("ACC-ATTR-01", "ACC-ATTR-02"), ids(acc));
    }

    @Test
    void the_component_type_decides_whether_an_acc_can_be_abstract() {
        assertEquals(List.of("ACC-ATTR-03"), ids(acc(1, "G", OagisComponentType.SemanticGroup, true, false,
                new NamespaceIdHolder(NAMESPACE), 1)));
    }

    @Test
    void a_first_revision_is_not_deprecated() {
        assertEquals(List.of("ACC-ATTR-09"), ids(acc(1, "R", OagisComponentType.Semantics, false, true,
                new NamespaceIdHolder(NAMESPACE), 1)));
        assertTrue(ids(acc(1, "R", OagisComponentType.Semantics, false, true, new NamespaceIdHolder(NAMESPACE), 2)).isEmpty());
    }

    @Test
    void an_attribute_group_holds_attributes_only() {
        bcc(10, EntityType.Attribute, 0, 1);
        bcc(11, EntityType.Element, 0, 1);
        ascc(12, AsccpType.Default, 0, 1);
        assertEquals(List.of("ACC-ATTR-04", "ACC-ATTR-04"), ids(valid(OagisComponentType.AttributeGroup)));
    }

    @Test
    void cardinality_is_checked_on_each_association() {
        bcc(10, EntityType.Element, -1, 1);
        bcc(11, EntityType.Element, 0, -2);
        bcc(12, EntityType.Element, 3, 2);
        bcc(13, EntityType.Attribute, 2, 1);
        bcc(14, EntityType.Attribute, 0, -1);
        ascc(15, AsccpType.Default, 2, 1);
        assertEquals(List.of("ACC-CARD-01", "ACC-CARD-02", "ACC-CARD-03", "ACC-CARD-03", "ACC-CARD-03", "ACC-CARD-04",
                "ACC-CARD-05"), ids(valid(OagisComponentType.Semantics)));
    }

    @Test
    void unbounded_is_not_a_cardinality_error() {
        bcc(10, EntityType.Element, 0, -1);
        bcc(11, EntityType.Element, 1, 1);
        assertTrue(ids(valid(OagisComponentType.Semantics)).isEmpty());
    }

    @Test
    void an_acc_has_one_extension() {
        ascc(10, AsccpType.Extension, 0, -1);
        assertTrue(ids(valid(OagisComponentType.Semantics)).isEmpty());
        ascc(11, AsccpType.Extension, 0, 1);
        assertEquals(List.of("ACC-ASSOC-01", "ACC-ASSOC-01"), ids(valid(OagisComponentType.Semantics)));
    }

    @Test
    void a_choice_has_two_members() {
        bcc(10, EntityType.Element, 0, 1);
        assertEquals(List.of("ACC-ASSOC-03"), ids(valid(OagisComponentType.Choice)));
        bcc(11, EntityType.Element, 0, 1);
        assertTrue(ids(valid(OagisComponentType.Choice)).isEmpty());
    }

    @Test
    void a_deprecated_base_is_a_warning_unless_the_acc_is_deprecated_too() {
        AccSummaryRecord base = acc(2, "Base", OagisComponentType.Base, true, true, new NamespaceIdHolder(NAMESPACE), 2);
        bases.add(new AccValidationInput.Base(base, List.of(new PathSegment("ACC", "1"), new PathSegment("ACC", "2"))));
        AccValidationInput input = new AccValidationInput(valid(OagisComponentType.Semantics), List.of(), List.of(), asccs, bccs, bases);
        List<Violation> found = rules.stream().flatMap(r -> r.evaluate(input)).toList();
        assertEquals(1, found.size());
        assertEquals("ACC-BASE-02", found.get(0).ruleId());
        assertEquals(Severity.WARNING, found.get(0).severity());
    }
}

package org.oagi.score.gateway.http.api.cc_management.validator;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.cc_management.model.CcAssociation;
import org.oagi.score.gateway.http.api.cc_management.model.CcDocument;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.acc.OagisComponentType;
import org.oagi.score.gateway.http.api.cc_management.validator.rules.AccGroupDuplicateRule;
import org.oagi.score.gateway.http.api.cc_management.validator.rules.AccInheritedDuplicateRule;
import org.oagi.score.gateway.http.api.cc_management.validator.rules.AccOwnDuplicateRule;
import org.oagi.score.gateway.http.api.validation.Severity;
import org.oagi.score.gateway.http.api.validation.ValidationEngine;
import org.oagi.score.gateway.http.api.validation.ValidationRule;
import org.oagi.score.gateway.http.api.validation.Violation;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** What a change of structure would break, asked of ACCs written by hand. */
class AccStructureChangeTest {

    private final AccFixture fixture = new AccFixture();

    private static AccManifestId id(long id) {
        return new AccManifestId(BigInteger.valueOf(id));
    }

    private AccStructureChangeValidationService service() {
        AccSubjectValidator validator = new AccSubjectValidator();
        ReflectionTestUtils.setField(validator, "rules",
                List.<ValidationRule<AccValidationInput>>of(new AccOwnDuplicateRule(), new AccInheritedDuplicateRule(), new AccGroupDuplicateRule()));
        AccStructureChangeValidationService service = new AccStructureChangeValidationService();
        ReflectionTestUtils.setField(service, "validator", validator);
        ReflectionTestUtils.setField(service, "validationEngine", new ValidationEngine(List.of()));
        return service;
    }

    private static List<String> labels(StructureChangeReport result) {
        return result.report().violations().stream()
                .map(v -> v.subject().label() + ": " + v.params().get("propertyTerm")).collect(Collectors.toList());
    }

    private CcAssociation firstOf(CcDocument document, AccSummaryRecord acc) {
        return document.getAssociations(acc).get(0);
    }

    /** The issue's case: a property is moved to a base ACC, and an ACC that has the base has the same term of another property. */
    @Test
    void a_property_moved_to_a_base_acc_that_an_other_acc_has_under_the_same_term_is_found_in_that_acc() {
        AccSummaryRecord base = fixture.acc(1, "Base", OagisComponentType.Base, null);
        AccSummaryRecord source = fixture.acc(2, "Source", OagisComponentType.Semantics, 1L);
        AccSummaryRecord other = fixture.acc(3, "Other", OagisComponentType.Semantics, 1L);
        AccSummaryRecord unrelated = fixture.acc(4, "Unrelated", OagisComponentType.Semantics, null);
        fixture.bcc(source, 10, 100, "Amount");
        fixture.bcc(other, 11, 200, "Amount");      // another BCCP with the same property term
        fixture.bcc(unrelated, 12, 300, "Amount");
        CcDocument document = fixture.document();

        CcAssociation moved = firstOf(document, source);
        AccStructureChange change = AccStructureChange.refactor(document, moved, base.accManifestId(), List.of(moved));
        StructureChangeReport result = service().check(document, source.accManifestId(), change);

        assertEquals(List.of("Other. Details: Amount"), labels(result));
        Violation violation = result.report().violations().get(0);
        assertEquals("DUP-002", violation.ruleId());
        assertEquals(Severity.ERROR, violation.severity());
        assertEquals("3", violation.subject().id());
        assertEquals(3, result.checkedCount(), "the base ACC, the source and the other ACC; not the unrelated one");
    }

    @Test
    void a_property_moved_to_a_base_acc_with_nothing_in_the_way_breaks_nothing() {
        AccSummaryRecord base = fixture.acc(1, "Base", OagisComponentType.Base, null);
        AccSummaryRecord source = fixture.acc(2, "Source", OagisComponentType.Semantics, 1L);
        AccSummaryRecord other = fixture.acc(3, "Other", OagisComponentType.Semantics, 1L);
        fixture.bcc(source, 10, 100, "Amount");
        fixture.bcc(other, 11, 200, "Name");
        CcDocument document = fixture.document();

        CcAssociation moved = firstOf(document, source);
        StructureChangeReport result = service().check(document, source.accManifestId(),
                AccStructureChange.refactor(document, moved, base.accManifestId(), List.of(moved)));

        assertTrue(result.report().violations().isEmpty());
        assertEquals(3, result.checkedCount());
    }

    @Test
    void what_the_acc_had_before_the_change_is_not_what_the_change_breaks() {
        AccSummaryRecord base = fixture.acc(1, "Base", OagisComponentType.Base, null);
        AccSummaryRecord source = fixture.acc(2, "Source", OagisComponentType.Semantics, 1L);
        AccSummaryRecord other = fixture.acc(3, "Other", OagisComponentType.Semantics, 1L);
        fixture.bcc(source, 10, 100, "Amount");
        fixture.bcc(other, 11, 200, "Name");
        fixture.bcc(other, 12, 201, "Name");        // a duplicate that is there already
        CcDocument document = fixture.document();

        CcAssociation moved = firstOf(document, source);
        StructureChangeReport result = service().check(document, source.accManifestId(),
                AccStructureChange.refactor(document, moved, base.accManifestId(), List.of(moved)));

        assertTrue(result.report().violations().isEmpty());
    }

    @Test
    void every_acc_down_the_chain_of_base_acc_is_checked_however_many_there_are() {
        AccSummaryRecord base = fixture.acc(1, "Base", OagisComponentType.Base, null);
        AccSummaryRecord source = fixture.acc(2, "Source", OagisComponentType.Semantics, 1L);
        fixture.bcc(source, 10, 100, "Amount");
        for (long i = 0; i < 300; i++) {
            AccSummaryRecord derived = fixture.acc(100 + i, "Derived" + i, OagisComponentType.Semantics, 1L);
            AccSummaryRecord deeper = fixture.acc(1000 + i, "Deeper" + i, OagisComponentType.Semantics, 100 + i);
            fixture.bcc(i % 2 == 0 ? derived : deeper, 500 + i, 5000 + i, i % 3 == 0 ? "Amount" : "Other" + i);
        }
        CcDocument document = fixture.document();

        CcAssociation moved = firstOf(document, source);
        StructureChangeReport result = service().check(document, source.accManifestId(),
                AccStructureChange.refactor(document, moved, base.accManifestId(), List.of(moved)));

        assertEquals(2 + 600, result.checkedCount());
        // the ACC that has "Amount" and every ACC below it have the term twice: the derived ACC and the one below it, or the one below only
        long expected = java.util.stream.LongStream.range(0, 300).filter(i -> i % 3 == 0).map(i -> i % 2 == 0 ? 2 : 1).sum();
        assertEquals(expected, result.report().violations().size());
    }

    @Test
    void an_acc_that_contains_a_group_that_gets_the_property_is_checked_too() {
        AccSummaryRecord group = fixture.acc(1, "Group", OagisComponentType.SemanticGroup, null);
        AccSummaryRecord source = fixture.acc(2, "Source", OagisComponentType.Semantics, 1L);
        AccSummaryRecord container = fixture.acc(3, "Container", OagisComponentType.Semantics, null);
        fixture.bcc(source, 10, 100, "Amount");
        fixture.bcc(container, 11, 200, "Amount");
        fixture.ascc(container, 12, 300, "Group", group);
        CcDocument document = fixture.document();

        CcAssociation moved = firstOf(document, source);
        StructureChangeReport result = service().check(document, source.accManifestId(),
                AccStructureChange.refactor(document, moved, group.accManifestId(), List.of(moved)));

        assertEquals(List.of("Container. Details: Amount"), labels(result));
        assertEquals("DUP-003", result.report().violations().get(0).ruleId());
        assertEquals(Set.of(id(1), id(2), id(3)), AccStructureChange.affectedBy(document, Set.of(id(1))));
    }

    private static org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId bccpId(long id) {
        return new org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId(BigInteger.valueOf(id));
    }

    /** What the issue asks for: a property is appended to a base ACC, and an ACC that has the base has the same term. */
    @Test
    void a_property_appended_to_a_base_acc_that_an_other_acc_has_under_the_same_term_is_found_in_that_acc() {
        AccSummaryRecord base = fixture.acc(1, "Base", OagisComponentType.Base, null);
        AccSummaryRecord derived = fixture.acc(2, "Derived", OagisComponentType.Semantics, 1L);
        AccSummaryRecord holder = fixture.acc(3, "Holder", OagisComponentType.Semantics, null);
        fixture.bcc(derived, 10, 200, "Amount");
        fixture.bcc(holder, 11, 100, "Amount");      // the property to append: the BCCP exists, and has the term
        CcDocument document = fixture.document();

        StructureChangeReport result = service().check(document, base.accManifestId(),
                AccStructureChange.append(document, base.accManifestId(), document.getBccp(bccpId(100))));

        assertEquals(List.of("Derived. Details: Amount"), labels(result));
        assertEquals(2, result.checkedCount(), "the base ACC and the one that has it as its base ACC");
    }

    @Test
    void a_property_appended_to_the_acc_itself_that_it_has_already_is_found_in_the_acc() {
        AccSummaryRecord acc = fixture.acc(1, "Acc", OagisComponentType.Semantics, null);
        AccSummaryRecord holder = fixture.acc(3, "Holder", OagisComponentType.Semantics, null);
        fixture.bcc(acc, 10, 200, "Amount");
        fixture.bcc(holder, 11, 100, "Amount");
        CcDocument document = fixture.document();

        StructureChangeReport result = service().check(document, acc.accManifestId(),
                AccStructureChange.append(document, acc.accManifestId(), document.getBccp(bccpId(100))));

        assertEquals(List.of("Acc. Details: Amount"), labels(result));
        assertEquals("DUP-001", result.report().violations().get(0).ruleId());
    }

    @Test
    void one_more_property_under_a_term_that_is_twice_there_already_is_a_problem_that_is_new() {
        AccSummaryRecord acc = fixture.acc(1, "Acc", OagisComponentType.Semantics, null);
        AccSummaryRecord holder = fixture.acc(3, "Holder", OagisComponentType.Semantics, null);
        fixture.bcc(acc, 10, 200, "Amount");
        fixture.bcc(acc, 11, 201, "Amount");         // twice there already
        fixture.bcc(holder, 12, 100, "Amount");
        CcDocument document = fixture.document();

        StructureChangeReport result = service().check(document, acc.accManifestId(),
                AccStructureChange.append(document, acc.accManifestId(), document.getBccp(bccpId(100))));

        assertEquals(List.of("Acc. Details: Amount"), labels(result));
        assertEquals(3, result.report().violations().get(0).locations().size());
    }

    @Test
    void a_property_appended_that_the_acc_does_not_have_breaks_nothing() {
        AccSummaryRecord acc = fixture.acc(1, "Acc", OagisComponentType.Semantics, null);
        AccSummaryRecord holder = fixture.acc(3, "Holder", OagisComponentType.Semantics, null);
        fixture.bcc(acc, 10, 200, "Name");
        fixture.bcc(holder, 11, 100, "Amount");
        CcDocument document = fixture.document();

        StructureChangeReport result = service().check(document, acc.accManifestId(),
                AccStructureChange.append(document, acc.accManifestId(), document.getBccp(bccpId(100))));

        assertTrue(result.report().violations().isEmpty());
    }

    @Test
    void a_base_acc_that_has_a_term_the_acc_and_the_acc_below_it_have_is_found_in_both() {
        AccSummaryRecord base = fixture.acc(1, "Base", OagisComponentType.Base, null);
        AccSummaryRecord acc = fixture.acc(2, "Acc", OagisComponentType.Semantics, null);
        AccSummaryRecord below = fixture.acc(3, "Below", OagisComponentType.Semantics, 2L);
        AccSummaryRecord clean = fixture.acc(4, "Clean", OagisComponentType.Semantics, null);
        fixture.bcc(base, 10, 100, "Amount");
        fixture.bcc(acc, 11, 200, "Amount");
        fixture.bcc(clean, 12, 300, "Name");
        CcDocument document = fixture.document();

        StructureChangeReport result = service().check(document, acc.accManifestId(),
                AccStructureChange.setBase(document, acc.accManifestId(), base.accManifestId()));

        assertEquals(List.of("Acc. Details: Amount", "Below. Details: Amount"), labels(result));
        assertEquals(2, result.checkedCount(), "the ACC and the one below it; the base ACC does not change");
        assertTrue(below.basedAccManifestId().equals(acc.accManifestId()));
    }
}

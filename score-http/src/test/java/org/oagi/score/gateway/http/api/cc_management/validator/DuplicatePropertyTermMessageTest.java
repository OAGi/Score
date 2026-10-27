package org.oagi.score.gateway.http.api.cc_management.validator;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.cc_management.model.CcDocument;
import org.oagi.score.gateway.http.api.cc_management.model.CcDocumentFactory;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.acc.OagisComponentType;
import org.oagi.score.gateway.http.api.cc_management.repository.AccQueryRepository;
import org.oagi.score.gateway.http.api.cc_management.validator.rules.AccGroupDuplicateRule;
import org.oagi.score.gateway.http.api.cc_management.validator.rules.AccInheritedDuplicateRule;
import org.oagi.score.gateway.http.api.cc_management.validator.rules.AccOwnDuplicateRule;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.validation.*;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * What the Validation Results panel shows of a duplicate property term is what these rules write: the sentence, the
 * name of each node and how the nodes relate. The panel prints them as they come, so they are tested as they are.
 */
class DuplicatePropertyTermMessageTest {

    private final ScoreUser requester = new ScoreUser(new UserId(BigInteger.TEN), "tester", "Tester",
            "tester@example.com", false, List.of(ScoreRole.END_USER));

    private final AccFixture graph = new AccFixture();

    private List<Violation> validate(AccSummaryRecord root, ValidationTrigger trigger) {
        CcDocument document = graph.document();
        RepositoryFactory repositoryFactory = mock(RepositoryFactory.class);
        AccQueryRepository accQuery = mock(AccQueryRepository.class);
        CcDocumentFactory documentFactory = mock(CcDocumentFactory.class);
        when(repositoryFactory.accQueryRepository(any())).thenReturn(accQuery);
        when(accQuery.getAccSummary(root.accManifestId())).thenReturn(root);
        when(repositoryFactory.ccDocumentFactory()).thenReturn(documentFactory);
        when(documentFactory.create(any(ScoreUser.class), any(ReleaseId.class))).thenReturn(document);

        AccValidationInputLoader loader = new AccValidationInputLoader();
        ReflectionTestUtils.setField(loader, "repositoryFactory", repositoryFactory);
        AccSubjectValidator validator = new AccSubjectValidator();
        ReflectionTestUtils.setField(validator, "loader", loader);
        ReflectionTestUtils.setField(validator, "rules", List.of(
                new AccOwnDuplicateRule(), new AccInheritedDuplicateRule(), new AccGroupDuplicateRule()));
        return new ValidationEngine(List.of(validator)).validate(requester,
                new ValidationSubject(SubjectType.ACC, root.accManifestId().toString()), trigger).violations();
    }

    private static List<String> labelsOf(Violation violation) {
        return violation.locations().stream().map(Location::label).collect(Collectors.toList());
    }

    @Test
    void two_associations_of_one_acc_say_which_term_and_that_the_acc_uses_it_twice() {
        AccSummaryRecord root = graph.acc(1, "Root", OagisComponentType.Semantics, null);
        graph.bcc(root, 10, 100, "Amount");
        graph.bcc(root, 11, 101, "Amount");

        Violation violation = validate(root, ValidationTrigger.ON_DEMAND).get(0);

        assertEquals("ACC-ASSOC-02", violation.ruleId());
        assertEquals(Severity.ERROR, violation.severity());
        assertEquals("The property term \"Amount\" is used by more than one association of this ACC.", violation.message());
        assertEquals("Amount", violation.params().get("propertyTerm"));
    }

    @Test
    void a_term_of_the_base_acc_says_that_both_the_acc_and_its_base_acc_use_it() {
        AccSummaryRecord base = graph.acc(2, "Base", OagisComponentType.Base, null);
        AccSummaryRecord root = graph.acc(1, "Root", OagisComponentType.Semantics, 2L);
        graph.bcc(base, 20, 200, "Name");
        graph.bcc(root, 10, 100, "Name");

        Violation violation = validate(root, ValidationTrigger.ON_DEMAND).get(0);

        assertEquals("ACC-BASE-01", violation.ruleId());
        assertEquals("The property term \"Name\" is used by this ACC and also by one of its base ACCs.", violation.message());
    }

    @Test
    void a_term_a_group_brings_says_that_it_comes_through_a_group() {
        AccSummaryRecord base = graph.acc(2, "Base", OagisComponentType.Base, null);
        AccSummaryRecord group = graph.acc(3, "Choice Group", OagisComponentType.SemanticGroup, null);
        AccSummaryRecord root = graph.acc(1, "Root", OagisComponentType.Semantics, 2L);
        graph.bcc(base, 20, 200, "Name");
        graph.bcc(group, 30, 300, "Name");
        graph.ascc(root, 10, 100, "Group", group);

        Violation violation = validate(root, ValidationTrigger.ON_DEMAND).get(0);

        assertEquals("ACC-GRP-01", violation.ruleId());
        assertEquals("The property term \"Name\" is brought in through a group, and this ACC or its base ACC already has it.",
                violation.message());
    }

    @Test
    void each_node_is_named_by_its_term_and_the_acc_it_is_in() {
        AccSummaryRecord base = graph.acc(2, "Base", OagisComponentType.Base, null);
        AccSummaryRecord root = graph.acc(1, "Root", OagisComponentType.Semantics, 2L);
        graph.bcc(base, 20, 200, "Name");
        graph.bcc(root, 10, 100, "Name");

        Violation violation = validate(root, ValidationTrigger.ON_DEMAND).get(0);

        assertEquals(List.of("Name (Root. Details)", "Name (Base. Details)"), labelsOf(violation));
        assertEquals(List.of("first occurrence", "conflicts with"),
                violation.locations().stream().map(Location::role).collect(Collectors.toList()));
    }

    @Test
    void a_third_node_conflicts_with_the_first_too() {
        AccSummaryRecord root = graph.acc(1, "Root", OagisComponentType.Semantics, null);
        graph.bcc(root, 10, 100, "Amount");
        graph.bcc(root, 11, 101, "Amount");
        graph.bcc(root, 12, 102, "Amount");

        Violation violation = validate(root, ValidationTrigger.ON_DEMAND).get(0);

        assertEquals(3, violation.locations().size());
        assertEquals(List.of("first occurrence", "conflicts with", "conflicts with"),
                violation.locations().stream().map(Location::role).collect(Collectors.toList()));
    }

    @Test
    void two_terms_are_two_problems_each_with_its_own_sentence() {
        AccSummaryRecord root = graph.acc(1, "Root", OagisComponentType.Semantics, null);
        graph.bcc(root, 10, 100, "Amount");
        graph.bcc(root, 11, 101, "Amount");
        graph.bcc(root, 12, 102, "Name");
        graph.bcc(root, 13, 103, "Name");

        List<Violation> violations = validate(root, ValidationTrigger.ON_DEMAND);

        assertEquals(List.of("Amount", "Name"), violations.stream()
                .map(v -> v.params().get("propertyTerm")).sorted().collect(Collectors.toList()));
        assertTrue(violations.stream().anyMatch(v -> v.message().contains("\"Amount\"")));
        assertTrue(violations.stream().anyMatch(v -> v.message().contains("\"Name\"")));
    }

    @Test
    void a_report_about_the_acc_itself_names_no_other_subject() {
        AccSummaryRecord root = graph.acc(1, "Root", OagisComponentType.Semantics, null);
        graph.bcc(root, 10, 100, "Amount");
        graph.bcc(root, 11, 101, "Amount");

        assertNull(validate(root, ValidationTrigger.ON_DEMAND).get(0).subject());
    }
}

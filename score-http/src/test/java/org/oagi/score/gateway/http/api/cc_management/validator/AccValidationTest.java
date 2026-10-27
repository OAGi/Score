package org.oagi.score.gateway.http.api.cc_management.validator;

import org.oagi.score.gateway.http.api.cc_management.validator.rules.*;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.cc_management.model.CcDocument;
import org.oagi.score.gateway.http.api.cc_management.model.CcDocumentFactory;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.acc.OagisComponentType;
import org.oagi.score.gateway.http.api.cc_management.repository.AccQueryRepository;
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

class AccValidationTest {

    private final ScoreUser requester = new ScoreUser(new UserId(BigInteger.TEN), "tester", "Tester",
            "tester@example.com", false, List.of(ScoreRole.END_USER));

    private final AccFixture graph = new AccFixture();

    private ValidationReport validate(AccSummaryRecord root, ValidationTrigger trigger) {
        CcDocument document = graph.document();
        RepositoryFactory repositoryFactory = mock(RepositoryFactory.class);
        AccQueryRepository accQuery = mock(AccQueryRepository.class);
        CcDocumentFactory documentFactory = mock(CcDocumentFactory.class);
        when(repositoryFactory.accQueryRepository(any())).thenReturn(accQuery);
        when(accQuery.getAccSummary(root.accManifestId())).thenReturn(root);
        when(repositoryFactory.ccDocumentFactory()).thenReturn(documentFactory);
        when(documentFactory.create(any(ScoreUser.class), any(org.oagi.score.gateway.http.api.release_management.model.ReleaseId.class)))
                .thenReturn(document);

        AccValidationInputLoader loader = new AccValidationInputLoader();
        ReflectionTestUtils.setField(loader, "repositoryFactory", repositoryFactory);
        AccSubjectValidator validator = new AccSubjectValidator();
        ReflectionTestUtils.setField(validator, "loader", loader);
        ReflectionTestUtils.setField(validator, "rules", List.of(
                new AccOwnDuplicateRule(), new AccInheritedDuplicateRule(), new AccGroupDuplicateRule(), new AccBaseCycleRule()));
        return new ValidationEngine(List.of(validator)).validate(requester,
                new ValidationSubject(SubjectType.ACC, root.accManifestId().toString()), trigger);
    }

    private static String pathOf(Location location) {
        return location.path().stream().map(s -> s.type() + "-" + s.manifestId()).collect(Collectors.joining(">"));
    }

    @Test
    void a_clean_acc_has_no_violations() {
        AccSummaryRecord root = graph.acc(1, "Root", OagisComponentType.Semantics, null);
        graph.bcc(root, 10, 100, "Name");
        graph.bcc(root, 11, 101, "Amount");

        ValidationReport report = validate(root, ValidationTrigger.ON_DEMAND);

        assertEquals(ReportStatus.VALID, report.status());
        assertTrue(report.violations().isEmpty());
    }

    @Test
    void two_associations_of_one_acc_with_the_same_term_are_one_violation_with_two_nodes() {
        AccSummaryRecord root = graph.acc(1, "Root", OagisComponentType.Semantics, null);
        AccSummaryRecord target = graph.acc(2, "Target", OagisComponentType.Semantics, null);
        graph.bcc(root, 10, 100, "Amount");
        graph.ascc(root, 11, 101, "Amount", target);

        ValidationReport report = validate(root, ValidationTrigger.STATE_TRANSITION);

        assertEquals(ReportStatus.HAS_ERRORS, report.status());
        assertEquals(1, report.violations().size());
        Violation violation = report.violations().get(0);
        assertEquals("DUP-001", violation.ruleId());
        assertEquals(List.of("ACC-1>BCC-10>BCCP-100", "ACC-1>ASCC-11>ASCCP-101"),
                violation.locations().stream().map(AccValidationTest::pathOf).toList());
        assertEquals(List.of("first occurrence", "conflicts with"),
                violation.locations().stream().map(Location::role).toList());
    }

    @Test
    void a_term_that_a_base_acc_has_is_a_duplicate_under_the_base_node() {
        AccSummaryRecord base = graph.acc(2, "Base", OagisComponentType.Base, null);
        AccSummaryRecord root = graph.acc(1, "Root", OagisComponentType.Semantics, 2L);
        graph.bcc(base, 20, 200, "Name");
        graph.bcc(root, 10, 100, "Name");

        Violation violation = validate(root, ValidationTrigger.ON_DEMAND).violations().get(0);

        assertEquals("DUP-002", violation.ruleId());
        assertEquals(List.of("ACC-1>BCC-10>BCCP-100", "ACC-1>ACC-2>BCC-20>BCCP-200"),
                violation.locations().stream().map(AccValidationTest::pathOf).toList());
    }

    @Test
    void a_group_that_brings_a_term_the_base_acc_has_is_found_under_the_group_node() {
        AccSummaryRecord base = graph.acc(2, "Base", OagisComponentType.Base, null);
        AccSummaryRecord group = graph.acc(3, "Choice Group", OagisComponentType.SemanticGroup, null);
        AccSummaryRecord root = graph.acc(1, "Root", OagisComponentType.Semantics, 2L);
        graph.bcc(base, 20, 200, "Name");
        graph.bcc(group, 30, 300, "Name");
        graph.ascc(root, 10, 100, "Group", group);

        Violation violation = validate(root, ValidationTrigger.ON_DEMAND).violations().get(0);

        assertEquals("DUP-003", violation.ruleId());
        assertEquals(List.of("ACC-1>ASCC-10>ASCCP-100>ACC-3>BCC-30>BCCP-300", "ACC-1>ACC-2>BCC-20>BCCP-200"),
                violation.locations().stream().map(AccValidationTest::pathOf).toList());
    }

    @Test
    void a_user_extension_group_is_not_a_node_of_the_tree() {
        AccSummaryRecord group = graph.acc(3, "Ext", OagisComponentType.UserExtensionGroup, null);
        AccSummaryRecord root = graph.acc(1, "Root", OagisComponentType.Semantics, null);
        graph.bcc(group, 30, 300, "Name");
        graph.bcc(root, 10, 100, "Name");
        graph.ascc(root, 11, 101, "Root User Extension Group", group);

        Violation violation = validate(root, ValidationTrigger.ON_DEMAND).violations().get(0);

        assertEquals(List.of("ACC-1>BCC-10>BCCP-100", "ACC-1>BCC-30>BCCP-300"),
                violation.locations().stream().map(AccValidationTest::pathOf).toList());
    }

    @Test
    void a_group_inside_itself_does_not_loop() {
        AccSummaryRecord group = graph.acc(3, "Loop", OagisComponentType.SemanticGroup, null);
        AccSummaryRecord root = graph.acc(1, "Root", OagisComponentType.Semantics, null);
        graph.ascc(group, 30, 300, "Again", group);
        graph.ascc(root, 10, 100, "Group", group);

        assertEquals(ReportStatus.VALID, validate(root, ValidationTrigger.ON_DEMAND).status());
    }

    @Test
    void a_cycle_of_base_acc_ends_and_is_reported() {
        AccSummaryRecord root = graph.acc(1, "Root", OagisComponentType.Semantics, 2L);
        graph.acc(2, "Other", OagisComponentType.Semantics, 1L);

        ValidationReport report = validate(root, ValidationTrigger.ON_DEMAND);

        assertEquals(1, report.violations().size());
        assertEquals("BAS-002", report.violations().get(0).ruleId());
        assertEquals("ACC-1>ACC-2>ACC-1", pathOf(report.violations().get(0).locations().get(0)));
    }

    @Test
    void an_acc_that_does_not_exist_is_a_bad_request() {
        AccSummaryRecord root = graph.acc(1, "Root", OagisComponentType.Semantics, null);
        RepositoryFactory repositoryFactory = mock(RepositoryFactory.class);
        when(repositoryFactory.accQueryRepository(any())).thenReturn(mock(AccQueryRepository.class));
        AccValidationInputLoader loader = new AccValidationInputLoader();
        ReflectionTestUtils.setField(loader, "repositoryFactory", repositoryFactory);

        assertThrows(IllegalArgumentException.class,
                () -> loader.load(requester, new ValidationSubject(SubjectType.ACC, root.accManifestId().toString())));
        assertThrows(IllegalArgumentException.class,
                () -> loader.load(requester, new ValidationSubject(SubjectType.ACC, "abc")));
    }
}

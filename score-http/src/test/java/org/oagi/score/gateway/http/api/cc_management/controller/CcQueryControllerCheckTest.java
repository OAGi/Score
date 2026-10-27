package org.oagi.score.gateway.http.api.cc_management.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.validator.AccStructureChangeValidationService;
import org.oagi.score.gateway.http.api.cc_management.validator.StructureChangeReport;
import org.oagi.score.gateway.http.api.validation.*;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.configuration.handler.ScoreResponseEntityExceptionHandler;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The questions the ACC page asks before it refactors, appends or sets a base ACC: where they are, what they take and
 * what they answer. The page reads the answer as it is written here, so the names of the fields are the contract.
 */
class CcQueryControllerCheckTest {

    private final ScoreUser requester = new ScoreUser(new UserId(BigInteger.TEN), "tester", "Tester",
            "tester@example.com", false, List.of(ScoreRole.END_USER));

    private AccStructureChangeValidationService service;
    private MockMvc mvc;

    private static AccManifestId acc(long id) {
        return new AccManifestId(BigInteger.valueOf(id));
    }

    private static StructureChangeReport broken() {
        Violation violation = new Violation("ACC-BASE-01", Severity.ERROR,
                "The property term \"Amount\" is used by this ACC and also by one of its base ACCs.",
                Map.of("propertyTerm", "Amount"),
                List.of(new Location("Amount (Other. Details)", List.of(new PathSegment("ACC", "3"), new PathSegment("BCC", "11")), "first occurrence"),
                        new Location("Amount (Base. Details)", List.of(new PathSegment("ACC", "3"), new PathSegment("ACC", "1"), new PathSegment("BCC", "12")), "conflicts with")),
                new SubjectRef(SubjectType.ACC, "3", "Other. Details"));
        return new StructureChangeReport(new ValidationReport(new ValidationSubject(SubjectType.ACC, "2"),
                new Date(), ReportStatus.HAS_ERRORS, List.of(violation)), 3);
    }

    private static StructureChangeReport clean() {
        return new StructureChangeReport(new ValidationReport(new ValidationSubject(SubjectType.ACC, "2"),
                new Date(), ReportStatus.VALID, List.of()), 1);
    }

    @BeforeEach
    void setUp() {
        service = mock(AccStructureChangeValidationService.class);
        SessionService sessionService = mock(SessionService.class);
        when(sessionService.asScoreUser(any())).thenReturn(requester);
        CcQueryController controller = new CcQueryController();
        ReflectionTestUtils.setField(controller, "structureChangeValidationService", service);
        ReflectionTestUtils.setField(controller, "sessionService", sessionService);
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setControllerAdvice(new ScoreResponseEntityExceptionHandler())
                .build();
    }

    @Test
    void a_base_acc_is_asked_about_with_the_acc_and_the_base_acc() throws Exception {
        when(service.setBase(any(), eq(acc(2)), eq(acc(1)))).thenReturn(broken());

        mvc.perform(get("/core-components/acc/2/base/check").param("basedAccManifestId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.checkedCount").value(3));

        verify(service).setBase(requester, acc(2), acc(1));
    }

    @Test
    void the_answer_has_what_the_panel_shows() throws Exception {
        when(service.setBase(any(), any(), any())).thenReturn(broken());

        mvc.perform(get("/core-components/acc/2/base/check").param("basedAccManifestId", "1"))
                .andExpect(jsonPath("$.report.status").value("HAS_ERRORS"))
                .andExpect(jsonPath("$.report.violations.length()").value(1))
                .andExpect(jsonPath("$.report.violations[0].ruleId").value("ACC-BASE-01"))
                .andExpect(jsonPath("$.report.violations[0].severity").value("ERROR"))
                .andExpect(jsonPath("$.report.violations[0].message").value(
                        "The property term \"Amount\" is used by this ACC and also by one of its base ACCs."))
                .andExpect(jsonPath("$.report.violations[0].params.propertyTerm").value("Amount"))
                .andExpect(jsonPath("$.report.violations[0].subject.type").value("ACC"))
                .andExpect(jsonPath("$.report.violations[0].subject.id").value("3"))
                .andExpect(jsonPath("$.report.violations[0].subject.label").value("Other. Details"))
                .andExpect(jsonPath("$.report.violations[0].locations[0].label").value("Amount (Other. Details)"))
                .andExpect(jsonPath("$.report.violations[0].locations[0].role").value("first occurrence"))
                .andExpect(jsonPath("$.report.violations[0].locations[0].path[0].type").value("ACC"))
                .andExpect(jsonPath("$.report.violations[0].locations[0].path[0].manifestId").value("3"))
                .andExpect(jsonPath("$.report.violations[0].locations[1].role").value("conflicts with"))
                .andExpect(jsonPath("$.report.violations[0].locations[1].path.length()").value(3));
    }

    @Test
    void a_change_that_breaks_nothing_answers_with_no_problems() throws Exception {
        when(service.setBase(any(), any(), any())).thenReturn(clean());

        mvc.perform(get("/core-components/acc/2/base/check").param("basedAccManifestId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.report.status").value("VALID"))
                .andExpect(jsonPath("$.report.violations.length()").value(0))
                .andExpect(jsonPath("$.checkedCount").value(1));
    }

    @Test
    void the_base_acc_is_required() throws Exception {
        mvc.perform(get("/core-components/acc/2/base/check")).andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void an_asccp_is_appended_by_the_asccp_manifest_id() throws Exception {
        when(service.appendAscc(any(), eq(acc(2)), eq(new AsccpManifestId(BigInteger.valueOf(5))))).thenReturn(broken());

        mvc.perform(get("/core-components/acc/2/append/check").param("asccpManifestId", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.report.violations.length()").value(1));

        verify(service).appendAscc(requester, acc(2), new AsccpManifestId(BigInteger.valueOf(5)));
        verify(service, never()).appendBcc(any(), any(), any());
    }

    @Test
    void a_bccp_is_appended_by_the_bccp_manifest_id() throws Exception {
        when(service.appendBcc(any(), eq(acc(2)), eq(new BccpManifestId(BigInteger.valueOf(6))))).thenReturn(clean());

        mvc.perform(get("/core-components/acc/2/append/check").param("bccpManifestId", "6"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.report.violations.length()").value(0));

        verify(service).appendBcc(requester, acc(2), new BccpManifestId(BigInteger.valueOf(6)));
        verify(service, never()).appendAscc(any(), any(), any());
    }

    @Test
    void an_append_without_a_property_is_a_bad_request() throws Exception {
        mvc.perform(get("/core-components/acc/2/append/check")).andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void an_acc_that_is_not_a_number_is_not_this_path() throws Exception {
        mvc.perform(get("/core-components/acc/two/append/check").param("bccpManifestId", "6"))
                .andExpect(status().isNotFound());

        verifyNoInteractions(service);
    }

    @Test
    void an_ascc_is_refactored_by_the_ascc_and_the_base_acc() throws Exception {
        when(service.refactorAscc(any(), eq(new AsccManifestId(BigInteger.valueOf(7))), eq(acc(1)))).thenReturn(broken());

        mvc.perform(get("/core-components/ascc/refactor/check")
                        .param("targetManifestId", "7").param("destinationManifestId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.report.violations[0].subject.label").value("Other. Details"));

        verify(service).refactorAscc(requester, new AsccManifestId(BigInteger.valueOf(7)), acc(1));
        verify(service, never()).refactorBcc(any(), any(), any());
    }

    @Test
    void a_bcc_is_refactored_by_the_bcc_and_the_base_acc() throws Exception {
        when(service.refactorBcc(any(), eq(new BccManifestId(BigInteger.valueOf(8))), eq(acc(1)))).thenReturn(clean());

        mvc.perform(get("/core-components/bcc/refactor/check")
                        .param("targetManifestId", "8").param("destinationManifestId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.report.status").value("VALID"));

        verify(service).refactorBcc(requester, new BccManifestId(BigInteger.valueOf(8)), acc(1));
        verify(service, never()).refactorAscc(any(), any(), any());
    }

    @Test
    void a_refactoring_needs_both_ends() throws Exception {
        mvc.perform(get("/core-components/bcc/refactor/check").param("targetManifestId", "8"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/core-components/ascc/refactor/check").param("destinationManifestId", "1"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void something_that_is_not_there_is_a_bad_request_with_what_is_missing() throws Exception {
        when(service.setBase(any(), any(), any()))
                .thenThrow(new IllegalArgumentException("The ACC manifest record with ID 2 could not be found."));

        mvc.perform(get("/core-components/acc/2/base/check").param("basedAccManifestId", "1"))
                .andExpect(status().isBadRequest());
    }
}

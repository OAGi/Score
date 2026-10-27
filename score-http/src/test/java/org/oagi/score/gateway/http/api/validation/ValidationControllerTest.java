package org.oagi.score.gateway.http.api.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
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

/** What Validate on a page asks of the server, and what it gets: the report is read by the panel as it is written. */
class ValidationControllerTest {

    private final ScoreUser requester = new ScoreUser(new UserId(BigInteger.TEN), "tester", "Tester",
            "tester@example.com", false, List.of(ScoreRole.END_USER));

    private ValidationEngine engine;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        engine = mock(ValidationEngine.class);
        SessionService sessionService = mock(SessionService.class);
        when(sessionService.asScoreUser(any())).thenReturn(requester);
        ValidationController controller = new ValidationController();
        ReflectionTestUtils.setField(controller, "validationEngine", engine);
        ReflectionTestUtils.setField(controller, "sessionService", sessionService);
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setControllerAdvice(new ScoreResponseEntityExceptionHandler())
                .build();
    }

    private static ValidationReport report(ReportStatus status, Violation... violations) {
        return new ValidationReport(new ValidationSubject(SubjectType.ACC, "12"), new Date(), status, List.of(violations));
    }

    @Test
    void an_acc_is_validated_on_request_unless_the_page_says_otherwise() throws Exception {
        when(engine.validate(any(), any(), any())).thenReturn(report(ReportStatus.VALID));

        mvc.perform(get("/validations/acc/12")).andExpect(status().isOk());

        verify(engine).validate(requester, new ValidationSubject(SubjectType.ACC, "12"), ValidationTrigger.ON_DEMAND);
    }

    @Test
    void the_kind_of_object_is_read_in_any_case() throws Exception {
        when(engine.validate(any(), any(), any())).thenReturn(report(ReportStatus.VALID));

        mvc.perform(get("/validations/BCCP/5")).andExpect(status().isOk());
        mvc.perform(get("/validations/dt/6")).andExpect(status().isOk());

        verify(engine).validate(requester, new ValidationSubject(SubjectType.BCCP, "5"), ValidationTrigger.ON_DEMAND);
        verify(engine).validate(requester, new ValidationSubject(SubjectType.DT, "6"), ValidationTrigger.ON_DEMAND);
    }

    @Test
    void the_trigger_can_be_given() throws Exception {
        when(engine.validate(any(), any(), any())).thenReturn(report(ReportStatus.VALID));

        mvc.perform(get("/validations/acc/12").param("trigger", "STATE_TRANSITION")).andExpect(status().isOk());

        verify(engine).validate(eq(requester), any(), eq(ValidationTrigger.STATE_TRANSITION));
    }

    @Test
    void a_report_with_problems_is_still_an_answer_and_has_what_the_panel_shows() throws Exception {
        Violation violation = new Violation("DUP-001", Severity.WARNING,
                "The property term \"Amount\" is used by more than one association of this ACC.",
                Map.of("propertyTerm", "Amount"),
                List.of(new Location("Amount (Root. Details)", List.of(new PathSegment("ACC", "12"), new PathSegment("BCC", "10")), "first occurrence")));
        when(engine.validate(any(), any(), any())).thenReturn(report(ReportStatus.HAS_WARNINGS, violation));

        mvc.perform(get("/validations/acc/12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subject.type").value("ACC"))
                .andExpect(jsonPath("$.subject.id").value("12"))
                .andExpect(jsonPath("$.status").value("HAS_WARNINGS"))
                .andExpect(jsonPath("$.violations[0].severity").value("WARNING"))
                .andExpect(jsonPath("$.violations[0].message").value(
                        "The property term \"Amount\" is used by more than one association of this ACC."))
                .andExpect(jsonPath("$.violations[0].locations[0].label").value("Amount (Root. Details)"))
                .andExpect(jsonPath("$.violations[0].locations[0].path[1].manifestId").value("10"))
                .andExpect(jsonPath("$.violations[0].subject").doesNotExist());
    }

    @Test
    void a_kind_of_object_that_is_not_known_is_a_bad_request() throws Exception {
        mvc.perform(get("/validations/widget/12")).andExpect(status().isBadRequest());

        verifyNoInteractions(engine);
    }

    @Test
    void a_trigger_that_is_not_known_is_a_bad_request() throws Exception {
        mvc.perform(get("/validations/acc/12").param("trigger", "SOMETIME")).andExpect(status().isBadRequest());

        verifyNoInteractions(engine);
    }
}

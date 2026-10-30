package org.oagi.score.gateway.http.api.comment_management.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.comment_management.service.CommentCommandService;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.configuration.handler.ScoreResponseEntityExceptionHandler;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.http.MediaType;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigInteger;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** A comment without text is rejected before anything is stored. */
class CommentCommandControllerTest {

    private final ScoreUser requester = new ScoreUser(new UserId(BigInteger.TEN), "tester", "Tester",
            "tester@example.com", false, List.of(ScoreRole.END_USER));

    private CommentCommandService service;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        service = mock(CommentCommandService.class);
        SessionService sessionService = mock(SessionService.class);
        when(sessionService.asScoreUser(any())).thenReturn(requester);
        CommentCommandController controller = new CommentCommandController();
        ReflectionTestUtils.setField(controller, "commentCommandService", service);
        ReflectionTestUtils.setField(controller, "sessionService", sessionService);
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setControllerAdvice(new ScoreResponseEntityExceptionHandler())
                .build();
    }

    private void postComment(String body, int expectedStatus) throws Exception {
        mvc.perform(post("/comments/ACC-1").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().is(expectedStatus));
    }

    @Test
    void postRejectsEmptyWhitespaceAndMissingText() throws Exception {
        postComment("{\"text\":\"\"}", 400);
        postComment("{\"text\":\"   \"}", 400);
        postComment("{}", 400);
        verifyNoInteractions(service);
    }

    @Test
    void postStoresAComment() throws Exception {
        postComment("{\"text\":\"hello\"}", 200);
        verify(service).postComments(eq(requester), eq("ACC-1"), eq("hello"), any());
    }

    @Test
    void updateRejectsBlankText() throws Exception {
        mvc.perform(put("/comments/5").contentType(MediaType.APPLICATION_JSON).content("{\"text\":\" \"}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }
}

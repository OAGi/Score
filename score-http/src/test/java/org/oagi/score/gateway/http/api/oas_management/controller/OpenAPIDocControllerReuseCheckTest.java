package org.oagi.score.gateway.http.api.oas_management.controller;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.oas_management.controller.payload.GetBieForOasDocRequest;
import org.oagi.score.gateway.http.api.oas_management.model.BieForOasDoc;
import org.oagi.score.gateway.http.api.oas_management.model.OasDocId;
import org.oagi.score.gateway.http.api.oas_management.service.OpenAPIDocService;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OpenAPIDocControllerReuseCheckTest {

    @Test
    void the_reuse_check_reads_all_the_operations_of_the_document() {
        var controller = new OpenAPIDocController();
        var oasDocService = mock(OpenAPIDocService.class);
        var sessionService = mock(SessionService.class);
        ReflectionTestUtils.setField(controller, "oasDocService", oasDocService);
        ReflectionTestUtils.setField(controller, "sessionService", sessionService);

        var requester = new ScoreUser(new UserId(BigInteger.valueOf(42)), "tester", "Tester",
                "tester@example.com", true, List.of(ScoreRole.DEVELOPER));
        when(sessionService.asScoreUser(any(org.springframework.security.core.AuthenticatedPrincipal.class))).thenReturn(requester);

        var selected = new BieForOasDoc();
        selected.setTopLevelAsbiepId(new TopLevelAsbiepId(BigInteger.ONE));
        selected.setVerb("POST");
        selected.setMessageBody("Request");
        selected.setOperationId("createOrder");

        controller.checkBIEReusedAcrossOperations(mock(org.springframework.security.core.AuthenticatedPrincipal.class), selected, new OasDocId(BigInteger.TEN));

        var captor = ArgumentCaptor.forClass(GetBieForOasDocRequest.class);
        verify(oasDocService).getBieForOasDoc(any(), captor.capture());
        assertFalse(captor.getValue().isPagination());
    }
}

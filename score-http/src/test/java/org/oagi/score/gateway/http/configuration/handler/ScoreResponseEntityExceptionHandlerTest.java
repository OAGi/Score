package org.oagi.score.gateway.http.configuration.handler;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScoreResponseEntityExceptionHandlerTest {

    private final ScoreResponseEntityExceptionHandler handler = new ScoreResponseEntityExceptionHandler();

    private ResponseEntity<Object> handle(Throwable cause) {
        var ex = new HttpMessageNotReadableException("Failed to read", cause, new MockHttpInputMessage(new byte[0]));
        return handler.handleHttpMessageNotReadable(ex, new HttpHeaders(), HttpStatus.BAD_REQUEST,
                new ServletWebRequest(new MockHttpServletRequest()));
    }

    @Test
    void aValueTheServerRefusesWhileReadingTheBodyIsToldWithItsReason() {
        // the converter of the value refuses it (here the name of an attribute a search cannot look in)
        var response = handle(new RuntimeException("wrapped by the json mapper",
                new IllegalArgumentException("Unknown search attribute: password")));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Unknown search attribute: password", response.getBody());
        assertEquals("Unknown search attribute: password", response.getHeaders().getFirst("X-Error-Message"));
    }

    @Test
    void aBodyThatCannotBeReadForAnotherReasonIsToldAsBefore() {
        var response = handle(new java.io.IOException("Unexpected end-of-input"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Failed to read request", ((org.springframework.http.ProblemDetail) response.getBody()).getDetail());
    }
}

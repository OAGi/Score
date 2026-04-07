package org.oagi.score.gateway.http.configuration.handler;

import org.oagi.score.gateway.http.api.DataAccessForbiddenException;
import org.oagi.score.gateway.http.common.model.AccessControlException;
import org.oagi.score.gateway.http.common.model.NotFoundException;
import org.oagi.score.gateway.http.common.model.base.ScoreDataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.sql.SQLSyntaxErrorException;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class ScoreResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Flattens backend error messages so they are safe to expose through HTTP
     * headers such as X-Error-Message, which cannot contain CR/LF characters.
     */
    private String toHeaderValue(String message) {
        if (message == null) {
            return null;
        }
        return message
                .replace("\r\n", " ")
                .replace('\n', ' ')
                .replace('\r', ' ')
                .trim();
    }

    private ResponseEntity<String> errorResponse(HttpStatus status, String message) {
        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.set("X-Error-Message", toHeaderValue(message));
        return new ResponseEntity<>(message, headers, status);
    }

    private ResponseEntity<String> errorResponse(HttpStatus status, String message, String errorMessageId) {
        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.set("X-Error-Message", toHeaderValue(message));
        if (errorMessageId != null) {
            headers.set("X-Error-Message-Id", errorMessageId);
        }
        return new ResponseEntity<>(message, headers, status);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity handleAuthenticationException(
            AuthenticationException ex, WebRequest webRequest) {
        logger.debug(ex.getMessage(), ex);
        return new ResponseEntity(HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(EmptyResultDataAccessException.class)
    public ResponseEntity handleEmptyResultDataAccessException(
            EmptyResultDataAccessException ex, WebRequest webRequest) {
        logger.debug(ex.getMessage(), ex);
        return new ResponseEntity(HttpStatus.NOT_FOUND);
    }

    /**
     * Handles controller-level not-found signals
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity handleNotFoundException(
            NotFoundException ex, WebRequest webRequest) {
        logger.debug(ex.getMessage(), ex);
        return new ResponseEntity(HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AccessControlException.class)
    public ResponseEntity handleAccessControlException(
            AccessControlException ex, WebRequest webRequest) {
        logger.debug(ex.getMessage(), ex);
        return errorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest webRequest) {
        logger.debug(ex.getMessage(), ex);
        return errorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity handleIllegalStateException(
            IllegalStateException ex, WebRequest webRequest) {
        logger.debug(ex.getMessage(), ex);
        return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    @ExceptionHandler(DataAccessForbiddenException.class)
    public ResponseEntity handleDataAccessForbiddenException(
            DataAccessForbiddenException ex, WebRequest webRequest) {
        logger.debug(ex.getMessage(), ex);
        return errorResponse(HttpStatus.FORBIDDEN, ex.getMessage(),
                ex.getErrorMessageId() != null ? ex.getErrorMessageId().toString() : null);
    }

    @ExceptionHandler(BadSqlGrammarException.class)
    public ResponseEntity handleBadSqlGrammarException(
            BadSqlGrammarException ex, WebRequest webRequest) {
        logger.debug(ex.getMessage(), ex);
        return errorResponse(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
    }

    @ExceptionHandler(SQLSyntaxErrorException.class)
    public ResponseEntity handleSQLSyntaxErrorException(
            SQLSyntaxErrorException ex, WebRequest webRequest) {
        logger.debug(ex.getMessage(), ex);
        return errorResponse(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
    }

    @ExceptionHandler(ScoreDataAccessException.class)
    public ResponseEntity handleScoreDataAccessException(
            ScoreDataAccessException ex, WebRequest webRequest) {
        logger.debug(ex.getMessage(), ex);
        return errorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(OutOfMemoryError.class)
    public ResponseEntity handleOutOfMemoryError(
            OutOfMemoryError ex, WebRequest webRequest) {
        logger.debug(ex.getMessage(), ex);
        return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "Not enough memory to perform the request: " + ex.getMessage() + ". " +
                        "Please consider either increasing available heap space or cleaning data to reduce the size of the request.");
    }

}

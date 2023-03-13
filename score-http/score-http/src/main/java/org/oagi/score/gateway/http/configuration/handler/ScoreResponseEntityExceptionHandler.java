package org.oagi.score.gateway.http.configuration.handler;

import org.oagi.score.gateway.http.api.DataAccessForbiddenException;
import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.security.AccessControlException;
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
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
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

    @ExceptionHandler(AccessControlException.class)
    public ResponseEntity handleAccessControlException(
            AccessControlException ex, WebRequest webRequest) {
        logger.debug(ex.getMessage(), ex);

        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.set("X-Error-Message", ex.getMessage());
        return new ResponseEntity(headers, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest webRequest) {
        logger.debug(ex.getMessage(), ex);

        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.set("X-Error-Message", ex.getMessage());
        return new ResponseEntity(headers, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity handleIllegalStateException(
            IllegalStateException ex, WebRequest webRequest) {
        logger.debug(ex.getMessage(), ex);

        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.set("X-Error-Message", ex.getMessage());
        return new ResponseEntity(headers, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(DataAccessForbiddenException.class)
    public ResponseEntity handleDataAccessForbiddenException(
            DataAccessForbiddenException ex, WebRequest webRequest) {
        logger.debug(ex.getMessage(), ex);

        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.set("X-Error-Message", ex.getMessage());
        if (ex.getErrorMessageId() != null) {
            headers.set("X-Error-Message-Id", ex.getErrorMessageId().toString());
        }
        return new ResponseEntity(headers, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(BadSqlGrammarException.class)
    public ResponseEntity handleBadSqlGrammarException(
            BadSqlGrammarException ex, WebRequest webRequest) {
        logger.debug(ex.getMessage(), ex);

        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.set("X-Error-Message", ex.getMessage());
        return new ResponseEntity(headers, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(SQLSyntaxErrorException.class)
    public ResponseEntity handleSQLSyntaxErrorException(
            SQLSyntaxErrorException ex, WebRequest webRequest) {
        logger.debug(ex.getMessage(), ex);

        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.set("X-Error-Message", ex.getMessage());
        return new ResponseEntity(headers, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(ScoreDataAccessException.class)
    public ResponseEntity handleScoreDataAccessException(
            ScoreDataAccessException ex, WebRequest webRequest) {
        logger.debug(ex.getMessage(), ex);

        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.set("X-Error-Message", ex.getMessage());
        return new ResponseEntity(headers, HttpStatus.BAD_REQUEST);
    }

}

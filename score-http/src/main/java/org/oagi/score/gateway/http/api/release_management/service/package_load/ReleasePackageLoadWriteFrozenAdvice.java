package org.oagi.score.gateway.http.api.release_management.service.package_load;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/** A write that waited for the gate for longer than it may is told to come again: 503 with the time, in the same way for every endpoint. */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ReleasePackageLoadWriteFrozenAdvice {

    @ExceptionHandler(ReleasePackageLoadWriteFrozenException.class)
    public ResponseEntity<Map<String, String>> writeFrozen(ReleasePackageLoadWriteFrozenException e) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).header(HttpHeaders.RETRY_AFTER, "30")
                .body(Map.of("code", "WRITE_FROZEN", "message", e.getMessage()));
    }
}

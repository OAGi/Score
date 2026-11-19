package org.oagi.score.gateway.http.api.release_management.service.package_load;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** A load that is refused (not a package, another one goes on) is a conflict with what the application is, and says why, as the others of the application do. */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ReleasePackageLoadAdvice {

    @ExceptionHandler(ReleasePackageLoadRefusedException.class)
    public ResponseEntity<String> refused(ReleasePackageLoadRefusedException e) {
        HttpHeaders headers = new HttpHeaders();
        String message = e.getMessage() == null ? "" : e.getMessage().replace("\r\n", " ").replace('\n', ' ').replace('\r', ' ').trim();
        headers.set("X-Error-Message", message);
        return new ResponseEntity<>(message, headers, HttpStatus.CONFLICT);
    }
}

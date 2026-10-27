package org.oagi.score.gateway.http.api.validation;

import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
public class ValidationController {

    @Autowired
    private ValidationEngine validationEngine;

    @Autowired
    private SessionService sessionService;

    /** Violations are data, so a report with problems is still a 200. */
    @RequestMapping(value = "/validations/{subjectType}/{id}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ValidationReport validate(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                     @PathVariable("subjectType") String subjectType,
                                     @PathVariable("id") String id,
                                     @RequestParam(value = "trigger", defaultValue = "ON_DEMAND") ValidationTrigger trigger) {
        SubjectType type;
        try {
            type = SubjectType.valueOf(subjectType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown subject type '" + subjectType + "'.");
        }
        return validationEngine.validate(sessionService.asScoreUser(user), new ValidationSubject(type, id), trigger);
    }
}

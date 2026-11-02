package org.oagi.score.gateway.http.api.validation;

import java.util.Date;
import java.util.List;

public record ValidationReport(ValidationSubject subject,
                               Date validatedAt,
                               ReportStatus status,
                               List<Violation> violations) {

    public boolean hasErrors() {
        return status == ReportStatus.HAS_ERRORS;
    }

    public long count(Severity severity) {
        return violations.stream().filter(v -> v.severity() == severity).count();
    }
}

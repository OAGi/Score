package org.oagi.score.gateway.http.api.oas_management.model;

import java.util.List;

/**
 * Issue #1729: one Security Requirement Object — a single element of a {@code security} array (the OR
 * alternatives). Its {@code schemes} are ANDed together. An {@code anonymous} requirement carries no
 * schemes and serializes as an empty object {@code &#123;&#125;} (optional / anonymous access).
 *
 * <p>Used for both the document root-level security ({@code OasDoc.securityRequirements}) and the
 * per-operation security ({@code BieForOasDoc.securityRequirements}). Persisted flat into
 * oas_doc_security / oas_operation_security (one row per scheme; requirement_group = list index).
 */
public class OasSecurityRequirement {
    private boolean anonymous;
    private List<OasSecurityRequirementScheme> schemes;

    public OasSecurityRequirement() {
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    public void setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
    }

    public List<OasSecurityRequirementScheme> getSchemes() {
        return schemes;
    }

    public void setSchemes(List<OasSecurityRequirementScheme> schemes) {
        this.schemes = schemes;
    }
}

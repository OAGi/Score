package org.oagi.score.gateway.http.api.business_term_management.service;

import static org.oagi.score.gateway.http.common.util.Utility.isValidURI;
import static org.springframework.util.StringUtils.hasLength;

/**
 * Single source of truth for business-term field validation, shared by the JSON create/update
 * endpoints and the batch import. The Angular import dialog mirrors these exact rules (and error
 * messages) client-side so the preview can pre-flag rows that the server would reject, but the
 * server remains authoritative.
 *
 * <p>Limits mirror the {@code business_term} table columns:
 * {@code business_term} {@code varchar(255)}, {@code external_ref_id} {@code varchar(100)},
 * {@code external_ref_uri} {@code text} (65535).</p>
 */
final class BusinessTermInputValidator {

    static final int BUSINESS_TERM_MAX_LENGTH = 255;
    static final int EXTERNAL_REFERENCE_ID_MAX_LENGTH = 100;
    static final int EXTERNAL_REFERENCE_URI_MAX_LENGTH = 65535;

    private BusinessTermInputValidator() {
    }

    static void validate(String businessTerm, String externalReferenceId, String externalReferenceUri) {
        if (!hasLength(businessTerm)) {
            throw new IllegalArgumentException("The business term is required.");
        }
        if (businessTerm.length() > BUSINESS_TERM_MAX_LENGTH) {
            throw new IllegalArgumentException(
                    businessTerm + " is longer than " + BUSINESS_TERM_MAX_LENGTH + " characters limit.");
        }
        if (!hasLength(externalReferenceUri)) {
            throw new IllegalArgumentException("The external reference URI is required.");
        }
        if (externalReferenceUri.length() > EXTERNAL_REFERENCE_URI_MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "The external reference URI is longer than " + EXTERNAL_REFERENCE_URI_MAX_LENGTH + " characters limit.");
        }
        if (!isValidURI(externalReferenceUri)) {
            throw new IllegalArgumentException(externalReferenceUri + " is not a valid URI.");
        }
        if (hasLength(externalReferenceId) && externalReferenceId.length() > EXTERNAL_REFERENCE_ID_MAX_LENGTH) {
            throw new IllegalArgumentException(
                    externalReferenceId + " is longer than " + EXTERNAL_REFERENCE_ID_MAX_LENGTH + " characters limit.");
        }
    }
}

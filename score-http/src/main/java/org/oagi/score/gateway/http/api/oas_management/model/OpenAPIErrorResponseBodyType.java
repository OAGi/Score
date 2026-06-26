package org.oagi.score.gateway.http.api.oas_management.model;

/**
 * Issue #1347: the body carried by the defaulted 4xx/5xx error responses of an
 * OAS Doc operation. Persisted per operation on {@code oas_operation.error_response_body_type}
 * as the enum {@link #name()} (no fragile ordinal). Default is {@link #NONE} so existing
 * documents regenerate byte-for-byte until an author opts in.
 *
 * <ul>
 *   <li>{@link #PROBLEM_DETAILS} - {@code application/problem+json} referencing a hardcoded
 *       RFC 9457 {@code ProblemDetails} schema (no library component).</li>
 *   <li>{@link #CONFIRM_MESSAGE} - {@code application/json} referencing a user-picked
 *       ConfirmMessage BIE schema (the {@code error_confirm_top_level_asbiep_id}).</li>
 *   <li>{@link #NONE} - status code + {@code description} only, no response body.</li>
 * </ul>
 */
public enum OpenAPIErrorResponseBodyType {
    PROBLEM_DETAILS,
    CONFIRM_MESSAGE,
    NONE;

    /**
     * Null/blank-tolerant parse that falls back to {@link #NONE} for legacy or unrecognized
     * values, so a missing column or an unexpected string never changes generation behavior.
     */
    public static OpenAPIErrorResponseBodyType from(String value) {
        if (value == null) {
            return NONE;
        }
        try {
            return valueOf(value.trim());
        } catch (IllegalArgumentException e) {
            return NONE;
        }
    }
}

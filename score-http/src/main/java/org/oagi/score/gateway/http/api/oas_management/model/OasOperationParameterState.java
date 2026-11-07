package org.oagi.score.gateway.http.api.oas_management.model;

import java.util.List;

/**
 * What of an operation decides the request parameters that the system registers for it (Issue #1791): whether it is a
 * GET that returns an array (then it has {@code sinceLastDateTime}), and the path parameters of its path.
 */
public record OasOperationParameterState(boolean sinceLastDateTime, List<String> pathParameters) {

    /** An operation that does not exist yet. */
    public static final OasOperationParameterState NONE = new OasOperationParameterState(false, List.of());

    public static OasOperationParameterState of(String verb, boolean returnsArray, String resolvedResourceName) {
        return new OasOperationParameterState("get".equalsIgnoreCase(verb) && returnsArray,
                OasResourcePaths.parameterNames(resolvedResourceName));
    }
}

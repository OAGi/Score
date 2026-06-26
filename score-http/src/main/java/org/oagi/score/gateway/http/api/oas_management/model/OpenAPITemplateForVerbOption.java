package org.oagi.score.gateway.http.api.oas_management.model;

import lombok.Data;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepSummaryRecord;

import java.math.BigInteger;
import java.util.List;

@Data
public class OpenAPITemplateForVerbOption {

    private TopLevelAsbiepId topLevelAsbiepId;
    private TopLevelAsbiepSummaryRecord topLevelAsbiep;
    private String schemaName;
    private Operation verbOption;
    private String resourceName;
    private String operationId;

    // The owning oas_operation's id. One operation produces up to two templates (Request + Response) that
    // share this id, so it lets generation tell that normal Request/Response split apart from a real
    // collision: two DISTINCT operations resolving to the same (path, verb) — an illegal collapse in the
    // emitted document (a path-item can hold only one operation per verb).
    private OasOperationId oasOperationId;
    private String tagName;
    private String messageBodyType;
    private Integer httpStatusCode;
    private boolean securityOverridden;
    private List<OasSecurityRequirement> securityRequirements;

    // Issue #1347: per-operation error-response body type (PROBLEM_DETAILS | CONFIRM_MESSAGE | NONE,
    // default NONE) and, for CONFIRM_MESSAGE, the picked ConfirmMessage BIE. Keyed by the oas_operation,
    // so the Request-entry and Response-entry templates of one operation carry the same values.
    private String errorResponseBodyType;
    private BigInteger confirmMessageTopLevelAsbiepId;

    private boolean arrayForJsonExpression;
    private boolean suppressRootProperty;

    private boolean includeMetaHeader;
    private BigInteger metaHeaderTopLevelAsbiepId;
    private boolean includePaginationResponse;
    private BigInteger paginationResponseTopLevelAsbiepId;

    public OpenAPITemplateForVerbOption(Operation verbOption) {
        this.verbOption = verbOption;
    }

}

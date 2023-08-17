package org.oagi.score.gateway.http.api.oas_management.data;

import org.apache.commons.lang3.StringUtils;

public class UpdateOperationIdWhenVerbChanged {
    private String verb;
    private String newOperationId;
    private String oldOperationId;
    private boolean isArray;

    public UpdateOperationIdWhenVerbChanged(String changedVerb, String oldOperationId, boolean isArray) {
        this.verb = changedVerb;
        this.oldOperationId = oldOperationId;
        this.isArray = isArray;
    }

    public String verbToOperationId() {
        String[] splittedBiePropertyTerms = StringUtils.split(this.oldOperationId, "_");
        String biePropertyTermCamelCase = splittedBiePropertyTerms[0];
        String biePropertyTerm = Character.toUpperCase(biePropertyTermCamelCase.charAt(0)) + biePropertyTermCamelCase.substring(1);
        switch (this.verb) {
            case "GET":
                this.newOperationId = biePropertyTermCamelCase + "_get" + ((isArray) ? biePropertyTerm + "List" :
                        biePropertyTerm);
                break;
            case "POST":
                this.newOperationId = biePropertyTermCamelCase + "_create" + ((isArray) ? biePropertyTerm + "List" :
                        biePropertyTerm);
                break;
            case "PUT":
                this.newOperationId = biePropertyTermCamelCase + "_update" + ((isArray) ? biePropertyTerm + "List" :
                        biePropertyTerm);
                break;
            case "PATCH":
                this.newOperationId = biePropertyTermCamelCase + "_update" + ((isArray) ? biePropertyTerm + "List" :
                        biePropertyTerm);
                break;
            case "DELETE":
                this.newOperationId = biePropertyTermCamelCase + "_delete" + ((isArray) ? biePropertyTerm + "List" :
                        biePropertyTerm);
                break;
            case "OPTIONS":
                this.newOperationId = biePropertyTermCamelCase + "_options" + ((isArray) ? biePropertyTerm + "List" :
                        biePropertyTerm);
                break;
            case "HEAD":
                this.newOperationId = biePropertyTermCamelCase + "_head" + ((isArray) ? biePropertyTerm + "List" :
                        biePropertyTerm);
                break;
            case "TRACE":
                this.newOperationId = biePropertyTermCamelCase + "_trace" + ((isArray) ? biePropertyTerm + "List" :
                        biePropertyTerm);
                break;
            default:
                throw new IllegalArgumentException("Unknown verb option: " + this.verb);
        }
        return this.newOperationId;
    }
}

package org.oagi.score.gateway.http.api.oas_management.data;

import static org.oagi.score.gateway.http.api.oas_management.service.generate_openapi_expression.Helper.camelCase;

public class SetOperationIdWithVerb {
    private String verb;
    private String biePropertyTerm;
    private String operationId;
    private boolean isArray;
    public SetOperationIdWithVerb(String changedVerb, String assignedBiePropertyName, boolean isArray){
        this.verb = changedVerb;
        this.biePropertyTerm = assignedBiePropertyName;
        this.isArray = isArray;
    }
    public String verbToOperationId() {
        String biePropertyTermCamelCase = camelCase(this.biePropertyTerm);
        String biePropertyTermWithoutSpace = this.biePropertyTerm.replaceAll("\\s", "");
        switch (this.verb) {
            case "GET":
                this.operationId = biePropertyTermCamelCase + "_get" + ((isArray) ? biePropertyTermWithoutSpace + "List" :
                        biePropertyTermWithoutSpace);
                break;
            case "POST":
                this.operationId = biePropertyTermCamelCase + "_create" + ((isArray) ? biePropertyTermWithoutSpace + "List" :
                        biePropertyTermWithoutSpace);
                break;
            case "PUT":
                this.operationId = biePropertyTermCamelCase + "_update" + ((isArray) ? biePropertyTermWithoutSpace + "List" :
                        biePropertyTermWithoutSpace);
                break;
            case "PATCH":
                this.operationId = biePropertyTermCamelCase + "_update" + ((isArray) ? biePropertyTermWithoutSpace + "List" :
                        biePropertyTermWithoutSpace);
                break;
            case "DELETE":
                this.operationId = biePropertyTermCamelCase + "_delete" + ((isArray) ? biePropertyTermWithoutSpace + "List" :
                        biePropertyTermWithoutSpace);
                break;
            case "OPTIONS":
                this.operationId = biePropertyTermCamelCase + "_options" + ((isArray) ? biePropertyTermWithoutSpace + "List" :
                        biePropertyTermWithoutSpace);
                break;
            case "HEAD":
                this.operationId = biePropertyTermCamelCase + "_head" + ((isArray) ?  biePropertyTermWithoutSpace + "List" :
                        biePropertyTermWithoutSpace);
                break;
            case "TRACE":
                this.operationId = biePropertyTermCamelCase + "_trace" + ((isArray) ? biePropertyTermWithoutSpace + "List" :
                        biePropertyTermWithoutSpace);
                break;
            default:
                throw new IllegalArgumentException("Unknown verb option: " + this.verb);
        }
        return this.operationId;
    }
}

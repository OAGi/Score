package org.oagi.score.gateway.http.api.oas_management.data;

import static org.oagi.score.gateway.http.api.oas_management.service.generate_openapi_expression.Helper.camelCase;

public class SetOperationIdWithVerb {
    private String verb;
    private String biePropertyTerm;
    private String businessContext;
    private String operationId;
    private boolean isArray;
    public SetOperationIdWithVerb(String changedVerb, String businessContext, String assignedBiePropertyName, boolean isArray){
        this.verb = changedVerb;
        this.businessContext = businessContext;
        this.biePropertyTerm = assignedBiePropertyName;
        this.isArray = isArray;
    }
    public String verbToOperationId() {
        String biePropertyTermCamelCase = camelCase(this.biePropertyTerm);
        String biePropertyTermWithoutSpace = this.biePropertyTerm.replaceAll("\\s", "");
        switch (this.verb) {
            case "GET":
                this.operationId = this.businessContext + "_query" + ((isArray) ? biePropertyTermWithoutSpace + "List" :
                        biePropertyTermWithoutSpace);
                break;
            case "POST":
                this.operationId = this.businessContext + "_create" + ((isArray) ? biePropertyTermWithoutSpace + "List" :
                        biePropertyTermWithoutSpace);
                break;
            case "PUT":
                this.operationId = this.businessContext + "_replace" + ((isArray) ? biePropertyTermWithoutSpace + "List" :
                        biePropertyTermWithoutSpace);
                break;
            case "PATCH":
                this.operationId = this.businessContext + "_update" + ((isArray) ? biePropertyTermWithoutSpace + "List" :
                        biePropertyTermWithoutSpace);
                break;
            case "DELETE":
                this.operationId = this.businessContext + "_delete" + ((isArray) ? biePropertyTermWithoutSpace + "List" :
                        biePropertyTermWithoutSpace);
                break;
            case "OPTIONS":
                this.operationId = this.businessContext + "_options" + ((isArray) ? biePropertyTermWithoutSpace + "List" :
                        biePropertyTermWithoutSpace);
                break;
            case "HEAD":
                this.operationId = this.businessContext + "_head" + ((isArray) ?  biePropertyTermWithoutSpace + "List" :
                        biePropertyTermWithoutSpace);
                break;
            case "TRACE":
                this.operationId = this.businessContext + "_trace" + ((isArray) ? biePropertyTermWithoutSpace + "List" :
                        biePropertyTermWithoutSpace);
                break;
            default:
                throw new IllegalArgumentException("Unknown verb option: " + this.verb);
        }
        return this.operationId;
    }
}

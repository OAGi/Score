package org.oagi.score.e2e.suite;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.oagi.score.e2e.TS_43_OpenAPIDocument.TC_43_1_DefineOpenAPIDocumentDefinition;
import org.oagi.score.e2e.TS_43_OpenAPIDocument.TC_43_2_AddBIEToOpenAPIDocument;
import org.oagi.score.e2e.TS_43_OpenAPIDocument.TC_43_3_DiscardButtonPlacement;
import org.oagi.score.e2e.TS_43_OpenAPIDocument.TC_43_4_AddOperationToOpenAPIDocument;
import org.oagi.score.e2e.TS_43_OpenAPIDocument.TC_43_5_GenerateOpenAPIWithBodylessOperations;
import org.oagi.score.e2e.TS_43_OpenAPIDocument.TC_43_6_OperationIdNaming;
import org.oagi.score.e2e.TS_43_OpenAPIDocument.TC_43_7_AvoidDuplicateBIESchema;
import org.oagi.score.e2e.TS_43_OpenAPIDocument.TC_43_8_ConfigureOpenAPISecuritySchemes;
import org.oagi.score.e2e.TS_43_OpenAPIDocument.TC_43_9_GenerateOpenAPI31;
import org.oagi.score.e2e.TS_43_OpenAPIDocument.TC_43_10_DeleteRequestBody;

@Suite
@SuiteDisplayName("Test Suite 43")
@SelectClasses({
        TC_43_1_DefineOpenAPIDocumentDefinition.class,
        TC_43_2_AddBIEToOpenAPIDocument.class,
        TC_43_3_DiscardButtonPlacement.class,
        TC_43_4_AddOperationToOpenAPIDocument.class,
        TC_43_5_GenerateOpenAPIWithBodylessOperations.class,
        TC_43_6_OperationIdNaming.class,
        TC_43_7_AvoidDuplicateBIESchema.class,
        TC_43_8_ConfigureOpenAPISecuritySchemes.class,
        TC_43_9_GenerateOpenAPI31.class,
        TC_43_10_DeleteRequestBody.class
})
public class TS_43 {
}

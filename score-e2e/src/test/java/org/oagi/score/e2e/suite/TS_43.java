package org.oagi.score.e2e.suite;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.oagi.score.e2e.TS_43_OpenAPIDocument.TC_43_1_DefineOpenAPIDocumentDefinition;

@Suite
@SuiteDisplayName("Test Suite 43")
@SelectClasses({
        TC_43_1_DefineOpenAPIDocumentDefinition.class
})
public class TS_43 {
}

package org.oagi.srt.test.testsuite;

import junit.framework.TestSuite;
import org.oagi.srt.test.testcase.contextCategory.CreateContextCategoryTestCase;
import org.oagi.srt.test.testcase.contextCategory.DiscardContextCategoryTestCase;
import org.oagi.srt.test.testcase.contextScheme.*;
import org.oagi.srt.test.testcase.user.UserLogInTestCase;

public class ContextSchemeManagementTestSuite {

    public static TestSuite suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(UserLogInTestCase.class);
        suite.addTestSuite(CreateContextCategoryTestCase.class);
        suite.addTestSuite(CreateContextSchemeTestCase.class);
        suite.addTestSuite(CreateDuplicateContextSchemeTestCase.class);
        suite.addTestSuite(CreateInvalidContextSchemeTestCase.class);
        suite.addTestSuite(UpdateContextSchemeTestCase.class);
        suite.addTestSuite(DiscardContextSchemeTestCase.class);
        suite.addTestSuite(DiscardContextCategoryTestCase.class);
        return suite;
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}


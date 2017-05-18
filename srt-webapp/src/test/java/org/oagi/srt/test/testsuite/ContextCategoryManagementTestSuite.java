package org.oagi.srt.test.testsuite;

import junit.framework.TestSuite;
import org.oagi.srt.test.testcase.contextCategory.*;
import org.oagi.srt.test.testcase.user.UserLogInTestCase;

public class ContextCategoryManagementTestSuite {

    public static TestSuite suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(UserLogInTestCase.class);
        suite.addTestSuite(CreateContextCategoryTestCase.class);
        suite.addTestSuite(CreateDuplicateContextCategoryTestCase.class);
        suite.addTestSuite(CreateContextCategoryNoNameTestCase.class);
        suite.addTestSuite(UpdateContextCategoryTestCase.class);
        suite.addTestSuite(DiscardContextCategoryTestCase.class);
        return suite;
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}


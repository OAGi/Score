package org.oagi.srt.test.testsuite;

import junit.framework.TestSuite;
import org.oagi.srt.test.helper.ChromeDriverSingleton;
import org.oagi.srt.test.testcase.*;

/**
 * Created by Miroslav Ljubicic.
 */
public class BusinessContextManagementTestSuite {

    public static TestSuite suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(UserLogInTestCase.class);
        suite.addTestSuite(CreateContextCategoryTestCase.class);
        suite.addTestSuite(CreateContextSchemeTestCase.class);
        suite.addTestSuite(CreateContextCategoryIntegrationTestCase.class);
        suite.addTestSuite(CreateContextSchemeIntegrationTestCase.class);
        suite.addTestSuite(CreateBusinessContextTestCase.class);
        suite.addTestSuite(UpdateBusinessContextTestCase.class);
        suite.addTestSuite(DiscardBusinessContextTestCase.class);
        suite.addTestSuite(DiscardContextSchemeIntegrationTestCase.class);
        suite.addTestSuite(DiscardContextCategoryIntegrationTestCase.class);
        suite.addTestSuite(DiscardContextSchemeTestCase.class);
        suite.addTestSuite(DiscardContextCategoryTestCase.class);
        return suite;
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}


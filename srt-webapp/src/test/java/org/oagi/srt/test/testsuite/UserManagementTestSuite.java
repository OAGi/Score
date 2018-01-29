package org.oagi.srt.test.testsuite;

import junit.framework.TestSuite;
import org.oagi.srt.test.testcase.user.*;

public class UserManagementTestSuite {

    public static TestSuite suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(CreateUserTestCase.class);
        suite.addTestSuite(UserLogInAndOutTestCase.class);
        suite.addTestSuite(InvalidUserLogInTestCase.class);
        suite.addTestSuite(CreateDuplicateUserTestCase.class);
        suite.addTestSuite(CreateUserWithTooShortPasswordTestCase.class);
        suite.addTestSuite(CreateUserWithUnmatchedPasswordsTestCase.class);
        return suite;
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}


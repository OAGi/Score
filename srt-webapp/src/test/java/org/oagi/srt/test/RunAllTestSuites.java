package org.oagi.srt.test;

import org.oagi.srt.test.helper.ChromeDriverSingleton;
import org.oagi.srt.test.testsuite.BusinessContextManagementTestSuite;
import org.oagi.srt.test.testsuite.ContextCategoryManagementTestSuite;
import org.oagi.srt.test.testsuite.ContextSchemeManagementTestSuite;
import org.oagi.srt.test.testsuite.UserManagementTestSuite;

/**
 * Created by Miroslav Ljubicic.
 */
public class RunAllTestSuites {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(UserManagementTestSuite.suite());
        junit.textui.TestRunner.run(ContextCategoryManagementTestSuite.suite());
        junit.textui.TestRunner.run(ContextSchemeManagementTestSuite.suite());
        junit.textui.TestRunner.run(BusinessContextManagementTestSuite.suite());

    }
}

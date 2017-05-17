package org.oagi.srt.test;

import org.oagi.srt.test.testsuite.UserManagementTestSuite;

/**
 * Created by Miroslav Ljubicic on 5/16/2017.
 */
public class RunAllTestSuites {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(UserManagementTestSuite.suite());
    }
}

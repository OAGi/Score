package org.oagi.score.e2e.TS_10_WorkingBranchCoreComponentManagementBehaviorsForDeveloper.acc;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.obj.AppUserObject;

import java.util.ArrayList;
import java.util.List;

@Execution(ExecutionMode.CONCURRENT)
public class TC_10_7_EditingAssociationsRevisionDeveloperACC extends BaseTest {

    private List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();
    }

    @AfterEach
    public void tearDown() {
        super.tearDown();

        // Delete random accounts
        this.randomAccounts.forEach(randomAccount -> {
            getAPIFactory().getAppUserAPI().deleteAppUserByLoginId(randomAccount.getLoginId());
        });
    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    public void test_TA_10_7_1_a() {

    }

    @Test
    public void test_TA_10_7_1_b() {

    }

    @Test
    public void test_TA_10_7_1_c() {

    }

    @Test
    public void test_TA_10_7_1_d() {

    }


    @Test
    public void test_TA_10_7_1_e() {

    }

    @Test
    public void test_TA_10_7_1_f() {

    }

    @Test
    public void test_TA_10_7_2() {

    }

    @Test
    public void test_TA_10_7_3_a() {

    }


    @Test
    public void test_TA_10_7_3_b() {

    }


    @Test
    public void test_TA_10_7_3_c() {

    }


    @Test
    public void test_TA_10_7_3_d() {

    }


    @Test
    public void test_TA_10_7_3_e() {

    }

    @Test
    public void test_TA_10_7_3_f() {

    }




}

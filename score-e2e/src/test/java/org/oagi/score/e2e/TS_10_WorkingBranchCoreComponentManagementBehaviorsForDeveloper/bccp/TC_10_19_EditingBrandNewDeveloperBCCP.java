package org.oagi.score.e2e.TS_10_WorkingBranchCoreComponentManagementBehaviorsForDeveloper.bccp;

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
public class TC_10_19_EditingBrandNewDeveloperBCCP extends BaseTest {

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
    public void test_TA_10_19_1_a() {

    }

    @Test
    public void test_TA_10_19_1_b() {

    }

    @Test
    public void test_TA_10_19_1_c() {

    }

    @Test
    public void test_TA_10_19_1_d() {

    }

    @Test
    public void test_TA_10_19_1_e() {

    }

    @Test
    public void test_TA_10_19_1_f() {

    }

    @Test
    public void test_TA_10_19_1_g() {

    }

    @Test
    public void test_TA_10_19_1_h() {

    }

    @Test
    public void test_TA_10_19_2() {

    }

    @Test
    public void test_TA_10_19_3() {

    }

    @Test
    public void test_TA_10_19_4() {

    }

    @Test
    public void test_TA_10_19_5_a() {

    }

    @Test
    public void test_TA_10_19_5_b() {

    }

}

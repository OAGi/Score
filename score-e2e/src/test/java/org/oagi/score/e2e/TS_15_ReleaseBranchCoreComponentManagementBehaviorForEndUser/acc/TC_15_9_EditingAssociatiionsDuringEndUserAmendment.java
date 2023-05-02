package org.oagi.score.e2e.TS_15_ReleaseBranchCoreComponentManagementBehaviorForEndUser.acc;

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
public class TC_15_9_EditingAssociatiionsDuringEndUserAmendment extends BaseTest {
    private final List<AppUserObject> randomAccounts = new ArrayList<>();

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
    public void test_TA_15_9_1_a() {

    }

    @Test
    public void test_TA_15_9_1_b() {

    }

    @Test
    public void test_TA_15_9_1_c() {

    }


    @Test
    public void test_TA_15_9_1_d() {

    }

    @Test
    public void test_TA_15_9_1_e() {

    }

    @Test
    public void test_TA_15_9_2() {

    }

    @Test
    public void test_TA_15_9_3_a() {

    }

    @Test
    public void test_TA_15_9_3_b() {

    }

    @Test
    public void test_TA_15_9_3_c() {

    }

    @Test
    public void test_TA_15_9_3_d() {

    }

    @Test
    public void test_TA_15_9_3_e() {

    }

    @Test
    public void test_TA_15_9_3_f() {

    }

    @Test
    public void test_TA_15_9_4_a() {

    }

    @Test
    public void test_TA_15_9_4_b() {

    }

    @Test
    public void test_TA_15_9_4_c() {

    }

    @Test
    public void test_TA_15_9_4_d() {

    }

    @Test
    public void test_TA_15_9_4_e() {

    }

    @Test
    public void test_TA_15_9_4_f() {

    }

    @Test
    public void test_TA_15_9_5() {

    }

    @Test
    public void test_TA_15_9_6_a() {

    }

    @Test
    public void test_TA_15_9_6_b() {

    }

    @Test
    public void test_TA_15_9_6_c() {

    }

    @Test
    public void test_TA_15_9_6_d() {

    }

    @Test
    public void test_TA_15_9_6_e() {

    }

    @Test
    public void test_TA_15_9_6_f() {

    }

    @Test
    public void test_TA_15_9_7() {

    }

    @Test
    public void test_TA_15_9_8() {

    }

    @Test
    public void test_TA_15_9_9() {

    }

    @Test
    public void test_TA_15_9_10_a() {

    }

    @Test
    public void test_TA_15_9_10_b() {

    }

    @Test
    public void test_TA_15_9_10_c() {

    }

    @Test
    public void test_TA_15_9_10_d() {

    }

    @Test
    public void test_TA_15_9_10_e() {

    }

    @Test
    public void test_TA_15_9_11() {

    }

    @Test
    public void test_TA_15_9_12() {

    }

    @Test
    public void test_TA_15_9_13() {

    }

    @Test
    public void test_TA_15_9_14_a() {

    }

    @Test
    public void test_TA_15_9_14_b() {

    }

    @Test
    public void test_TA_15_9_14_c() {

    }

    @Test
    public void test_TA_15_9_14_d() {

    }

    @Test
    public void test_TA_15_9_15() {

    }
}

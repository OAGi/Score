package org.oagi.score.e2e.TS_19_ReleaseManagement;

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
public class TC_19_1_ReleaseManagement extends BaseTest {
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
    public void test_TA_19_1_1() {

    }
    @Test
    public void test_TA_19_1_2() {

    }
    @Test
    public void test_TA_19_1_3a() {

    }
    @Test
    public void test_TA_19_1_3b() {

    }
    @Test
    public void test_TA_19_1_3c() {

    }
    @Test
    public void test_TA_19_1_3d() {

    }

    @Test
    public void test_TA_19_1_3e() {

    }

    @Test
    public void test_TA_19_1_3f() {

    }

    @Test
    public void test_TA_19_1_3g() {

    }

    @Test
    public void test_TA_19_1_3h() {

    }

    @Test
    public void test_TA_19_1_3i() {

    }

    @Test
    public void test_TA_19_1_3j() {

    }

    @Test
    public void test_TA_19_1_3k() {

    }

    @Test
    public void test_TA_19_1_4() {

    }

    @Test
    public void test_TA_19_1_5() {

    }

    @Test
    public void test_TA_19_1_6() {

    }

    @Test
    public void test_TA_19_1_7() {

    }

    @Test
    public void test_TA_19_1_8() {

    }

    @Test
    public void test_TA_19_1_9() {

    }

    @Test
    public void test_TA_19_1_10() {

    }

    @Test
    public void test_TA_19_1_11() {

    }

    @Test
    public void test_TA_19_1_12() {

    }

    @Test
    public void test_TA_19_1_13() {

    }

    @Test
    public void test_TA_19_1_14() {

    }
}

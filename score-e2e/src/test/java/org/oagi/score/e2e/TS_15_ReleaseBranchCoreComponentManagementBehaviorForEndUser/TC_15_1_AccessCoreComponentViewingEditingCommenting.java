package org.oagi.score.e2e.TS_15_ReleaseBranchCoreComponentManagementBehaviorForEndUser;

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
public class TC_15_1_AccessCoreComponentViewingEditingCommenting extends BaseTest {
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
    public void test_TA_15_1_1() {

    }

    @Test
    public void test_TA_15_1_2() {

    }

    @Test
    public void test_TA_15_1_3() {

    }

    @Test
    public void test_TA_15_1_4() {

    }


    @Test
    public void test_TA_15_1_5() {

    }

    @Test
    public void test_TA_15_1_6() {

    }

    @Test
    public void test_TA_15_1_7() {

    }

    @Test
    public void test_TA_15_1_8() {

    }

    @Test
    public void test_TA_15_1_9() {

    }

    @Test
    public void test_TA_15_1_10_a() {

    }


}

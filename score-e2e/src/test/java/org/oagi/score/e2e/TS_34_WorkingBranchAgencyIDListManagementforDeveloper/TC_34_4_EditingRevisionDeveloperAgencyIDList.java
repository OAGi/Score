package org.oagi.score.e2e.TS_34_WorkingBranchAgencyIDListManagementforDeveloper;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.obj.AppUserObject;

import java.util.ArrayList;
import java.util.List;

@Execution(ExecutionMode.CONCURRENT)
public class TC_34_4_EditingRevisionDeveloperAgencyIDList extends BaseTest {

    private List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();
    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @AfterEach
    public void tearDown() {
        super.tearDown();
        // Delete random accounts
        this.randomAccounts.forEach(newUser -> {
            getAPIFactory().getAppUserAPI().deleteAppUserByLoginId(newUser.getLoginId());
        });
    }

    @Test
    @DisplayName("TC_34_4_1_a")
    public void TA_1_a() {

    }

    @Test
    @DisplayName("TC_34_4_1_b")
    public void TA_1_b() {

    }

    @Test
    @DisplayName("TC_34_4_1_c")
    public void TA_1_c() {

    }

    @Test
    @DisplayName("TC_34_4_2")
    public void TA_2() {

    }

    @Test
    @DisplayName("TC_34_4_3")
    public void TA_3() {

    }

    @Test
    @DisplayName("TC_34_4_4")
    public void TA_4() {

    }

    @Test
    @DisplayName("TC_34_4_5")
    public void TA_5() {

    }

    @Test
    @DisplayName("TC_34_4_6")
    public void TA_6() {

    }

}

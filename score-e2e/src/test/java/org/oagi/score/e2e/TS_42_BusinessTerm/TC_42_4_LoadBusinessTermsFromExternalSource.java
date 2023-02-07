package org.oagi.score.e2e.TS_42_BusinessTerm;

import org.awaitility.Awaitility;
import org.awaitility.core.ConditionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.menu.BIEMenu;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.business_term.UploadBusinssTermsPage;
import org.oagi.score.e2e.page.business_term.ViewEditBusinessTermPage;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.oagi.score.e2e.impl.PageHelper.click;

@Execution(ExecutionMode.CONCURRENT)
public class TC_42_4_LoadBusinessTermsFromExternalSource extends BaseTest {

    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @BeforeEach
    public void init() {
        super.init();
    }

    @Test
    @DisplayName("TC_42_4_1")
    public void end_user_can_download_a_template_for_external_csv_file_to_be_uploaded_in_business_term_page() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBusinessTermPage viewEditBusinessTermPage = bieMenu.openViewEditBusinessTermSubMenu();
        UploadBusinssTermsPage uploadBusinssTermsPage = viewEditBusinessTermPage.hitUploadBusinessTermsButton();
        click(uploadBusinssTermsPage.getDownloadTemplateButton());

        //Call Awaitility library for asysnc download wait
        File targetFolder = new File(System.getProperty("user.home"), "Downloads");
        ConditionFactory await = Awaitility.await().atMost(Duration.ofSeconds(1));
        File csvFile = new File(targetFolder, "businessTermTemplateWithExample.csv");
        await.until(() -> csvFile.exists());
    }

    private static String getDownloadPath(){
        File fileDestination = new File(System.getProperty("user.home"), "Downloads");
        return fileDestination.getAbsolutePath();
    }

    @Test
    @DisplayName("TC_42_4_2")
    public void end_user_can_upload_and_attach_the_csv_file_with_correct_format_in_business_term_page() {

    }

    @Test
    @DisplayName("TC_42_4_3")
    public void end_user_cannot_upload_the_csv_file_with_incorrect_format_in_business_term_page() {
    }

    @Test
    @DisplayName("TC_42_4_4")
    public void new_business_term_will_be_created_if_the_business_term_is_uploaded_with_new_external_reference_uri() {
    }

    @Test
    @DisplayName("TC_42_4_5")
    public void previous_business_term_will_be_updated_with_new_information_if_the_business_term_is_uploaded_with_an_exitent_external_reference_uri() {
    }

    @AfterEach
    public void tearDown() {
        super.tearDown();

        // Delete random accounts
        this.randomAccounts.forEach(randomAccount -> {
            getAPIFactory().getAppUserAPI().deleteAppUserByLoginId(randomAccount.getLoginId());
        });
    }
}
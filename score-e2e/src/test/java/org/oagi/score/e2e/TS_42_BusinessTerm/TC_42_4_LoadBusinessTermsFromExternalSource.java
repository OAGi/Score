package org.oagi.score.e2e.TS_42_BusinessTerm;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.menu.BIEMenu;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.BusinessTermObject;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.business_term.UploadBusinessTermsPage;
import org.oagi.score.e2e.page.business_term.ViewEditBusinessTermPage;
import org.openqa.selenium.By;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static java.time.Duration.ofSeconds;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.oagi.score.e2e.impl.PageHelper.click;
import static org.oagi.score.e2e.impl.PageHelper.waitFor;

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
        File targetFolder = new File(System.getProperty("user.home"), "Downloads");
        File csvFile = new File(targetFolder, "businessTermTemplateWithExample.csv");
        if (csvFile.exists()) {
            csvFile.delete();
        }

        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBusinessTermPage viewEditBusinessTermPage = bieMenu.openViewEditBusinessTermSubMenu();
        UploadBusinessTermsPage uploadBusinessTermsPage = viewEditBusinessTermPage.hitUploadBusinessTermsButton();
        click(uploadBusinessTermsPage.getDownloadTemplateButton());

        // Call Awaitility library for async download wait
        await().atMost(Duration.ofSeconds(1))
                .until(() -> csvFile.exists());
    }

    @Test
    @DisplayName("TC_42_4_2")
    public void end_user_can_upload_and_attach_the_csv_file_with_correct_format_in_business_term_page() throws IOException {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBusinessTermPage viewEditBusinessTermPage = bieMenu.openViewEditBusinessTermSubMenu();
        UploadBusinessTermsPage uploadBusinessTermsPage = viewEditBusinessTermPage.hitUploadBusinessTermsButton();

        //generate three random Business Terms for testing
        List<BusinessTermObject> businessTerms = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            BusinessTermObject randomBusinessTerm = getAPIFactory().getBusinessTermAPI().createRandomBusinessTerm(endUser);
            businessTerms.add(randomBusinessTerm);
        }

        File targetFolder = new File(System.getProperty("user.home"), "Downloads");
        // write test business terms into csv file and save into a different name for upload
        File csvFileForUpload = new File(targetFolder, "businessTermTemplateWithExampleForUpload_" + randomAlphabetic(5, 10) + ".csv");
        if (csvFileForUpload.exists()) {
            csvFileForUpload.delete();
        }

        try {
            try (BufferedWriter writer = Files.newBufferedWriter(csvFileForUpload.toPath());
                 CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                         .withHeader("businessTerm", "externalReferenceUri", "externalReferenceId", "definition", "comment"))) {

                for (int i = 0; i < 3; i++) {
                    csvPrinter.printRecord(businessTerms.get(i).getBusinessTerm(), businessTerms.get(i).getExternalReferenceUri(),
                            businessTerms.get(i).getExternalReferenceId(), businessTerms.get(i).getDefinition(), businessTerms.get(i).getComment());
                }
                csvPrinter.flush();
            }

            // upload the modified csv file
            uploadBusinessTermsPage.getFileUploadInput().sendKeys(csvFileForUpload.getAbsolutePath());
            waitFor(ofSeconds(2L));

            //Verify that all test business terms have been saved through bulk upload
            ViewEditBusinessTermPage viewEditBusinessTermPageForCheck = homePage.getBIEMenu().openViewEditBusinessTermSubMenu();
            for (int i = 0; i < 3; i++) {
                viewEditBusinessTermPageForCheck.openPage();
                viewEditBusinessTermPageForCheck.setTerm(businessTerms.get(i).getBusinessTerm());
                viewEditBusinessTermPageForCheck.hitSearchButton();
                assertTrue(viewEditBusinessTermPageForCheck.getSelectCheckboxAtIndex(1).isDisplayed());
            }
        } finally {
            csvFileForUpload.delete();
        }
    }

    @Test
    @DisplayName("TC_42_4_3")
    public void end_user_cannot_upload_the_csv_file_with_incorrect_format_in_business_term_page() {
    }

    @Test
    @DisplayName("TC_42_4_4")
    public void new_business_term_will_be_created_if_the_business_term_is_uploaded_with_new_external_reference_uri() throws IOException {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBusinessTermPage viewEditBusinessTermPage = bieMenu.openViewEditBusinessTermSubMenu();
        UploadBusinessTermsPage uploadBusinessTermsPage = viewEditBusinessTermPage.hitUploadBusinessTermsButton();

        //generate three random Business Terms for testing
        List<BusinessTermObject> businessTerms = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            BusinessTermObject randomBusinessTerm = getAPIFactory().getBusinessTermAPI().createRandomBusinessTerm(endUser);
            businessTerms.add(randomBusinessTerm);
        }

        File targetFolder = new File(System.getProperty("user.home"), "Downloads");
        // write test business terms into csv file and save into a different name for upload
        File csvFileForUpload = new File(targetFolder, "businessTermTemplateWithExampleForUpload_" + randomAlphabetic(5, 10) + ".csv");
        if (csvFileForUpload.exists()) {
            csvFileForUpload.delete();
        }

        try {
            try (BufferedWriter writer = Files.newBufferedWriter(csvFileForUpload.toPath());
                 CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                         .withHeader("businessTerm", "externalReferenceUri", "externalReferenceId", "definition", "comment"))) {

                for (int i = 0; i < 3; i++) {
                    csvPrinter.printRecord(businessTerms.get(i).getBusinessTerm(), businessTerms.get(i).getExternalReferenceUri(),
                            businessTerms.get(i).getExternalReferenceId(), businessTerms.get(i).getDefinition(), businessTerms.get(i).getComment());
                }
                csvPrinter.flush();
            }

            // upload the modified csv file
            uploadBusinessTermsPage.getFileUploadInput().sendKeys(csvFileForUpload.getAbsolutePath());
            waitFor(ofSeconds(2L));

            //Verify that all test business terms have been saved through bulk upload
            ViewEditBusinessTermPage viewEditBusinessTermPageForCheck = homePage.getBIEMenu().openViewEditBusinessTermSubMenu();
            for (int i = 0; i < 3; i++) {
                viewEditBusinessTermPageForCheck.openPage();
                viewEditBusinessTermPageForCheck.setExternalReferenceURI(businessTerms.get(i).getExternalReferenceUri());
                viewEditBusinessTermPageForCheck.hitSearchButton();
                assertTrue(viewEditBusinessTermPageForCheck.getSelectCheckboxAtIndex(1).isDisplayed());
            }
        } finally {
            csvFileForUpload.delete();
        }

    }

    @Test
    @DisplayName("TC_42_4_5")
    public void previous_business_term_will_be_updated_with_new_information_if_the_business_term_is_uploaded_with_an_existent_external_reference_uri() throws IOException {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBusinessTermPage viewEditBusinessTermPage = bieMenu.openViewEditBusinessTermSubMenu();
        UploadBusinessTermsPage uploadBusinessTermsPage = viewEditBusinessTermPage.hitUploadBusinessTermsButton();

        //generate three random Business Terms for testing
        List<BusinessTermObject> businessTerms = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            BusinessTermObject randomBusinessTerm = getAPIFactory().getBusinessTermAPI().createRandomBusinessTerm(endUser);
            businessTerms.add(randomBusinessTerm);
        }

        File targetFolder = new File(System.getProperty("user.home"), "Downloads");
        // write test business terms into csv file and save into a different name for upload
        File csvFileForUpload = new File(targetFolder, "businessTermTemplateWithExampleForUpload_" + randomAlphabetic(5, 10) + ".csv");
        if (csvFileForUpload.exists()) {
            csvFileForUpload.delete();
        }

        try {
            try (BufferedWriter writer = Files.newBufferedWriter(csvFileForUpload.toPath());
                 CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                         .withHeader("businessTerm", "externalReferenceUri", "externalReferenceId", "definition", "comment"))) {

                for (int i = 0; i < 2; i++) {
                    csvPrinter.printRecord(businessTerms.get(i).getBusinessTerm(), businessTerms.get(i).getExternalReferenceUri(),
                            businessTerms.get(i).getExternalReferenceId(), businessTerms.get(i).getDefinition(), businessTerms.get(i).getComment());
                }

                //change the last record URI to be the same as the first record
                csvPrinter.printRecord(businessTerms.get(2).getBusinessTerm(), businessTerms.get(0).getExternalReferenceUri(),
                        businessTerms.get(2).getExternalReferenceId(), businessTerms.get(2).getDefinition(), businessTerms.get(2).getComment());
                csvPrinter.flush();
            }

            // upload the modified CSV file
            uploadBusinessTermsPage.getFileUploadInput().sendKeys(csvFileForUpload.getAbsolutePath());
            waitFor(ofSeconds(2L));

            //Verify that all test business terms have been saved through bulk upload
            ViewEditBusinessTermPage viewEditBusinessTermPageForCheck = homePage.getBIEMenu().openViewEditBusinessTermSubMenu();
            viewEditBusinessTermPageForCheck.setTerm(businessTerms.get(2).getBusinessTerm());
            viewEditBusinessTermPageForCheck.hitSearchButton();
            assertTrue(viewEditBusinessTermPageForCheck.getSelectCheckboxAtIndex(1).isDisplayed());

            viewEditBusinessTermPageForCheck.openPage();
            viewEditBusinessTermPageForCheck.setExternalReferenceURI(businessTerms.get(1).getExternalReferenceUri());
            viewEditBusinessTermPageForCheck.hitSearchButton();
            assertTrue(viewEditBusinessTermPageForCheck.getSelectCheckboxAtIndex(1).isDisplayed());

            viewEditBusinessTermPageForCheck.openPage();
            String oldBusinessTermName = businessTerms.get(0).getBusinessTerm();
            viewEditBusinessTermPageForCheck.setTerm(oldBusinessTermName);
            viewEditBusinessTermPageForCheck.hitSearchButton();
            assertEquals(1, getDriver().findElements(By.xpath("//table//*[contains(text(), \"" + oldBusinessTermName + "\")]")).size());
        } finally {
            csvFileForUpload.delete();
        }
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
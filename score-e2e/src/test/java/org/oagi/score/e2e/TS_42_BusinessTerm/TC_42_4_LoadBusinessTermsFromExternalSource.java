package org.oagi.score.e2e.TS_42_BusinessTerm;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.time.Duration.ofMillis;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.oagi.score.e2e.impl.PageHelper.*;

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
            BusinessTermObject randomBusinessTerm = BusinessTermObject.createRandomBusinessTerm(endUser, "bt");
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
                 CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withEscape('\\').withQuoteMode(QuoteMode.ALL)
                         .withHeader("businessTerm", "externalReferenceUri", "externalReferenceId", "definition", "comment"))) {

                for (int i = 0; i < 3; i++) {
                    csvPrinter.printRecord(businessTerms.get(i).getBusinessTerm(), businessTerms.get(i).getExternalReferenceUri(),
                            businessTerms.get(i).getExternalReferenceId(), businessTerms.get(i).getDefinition(), businessTerms.get(i).getComment());
                }
                csvPrinter.flush();
            }

            // upload the modified csv file
            uploadBusinessTermsPage.getFileUploadInput().sendKeys(csvFileForUpload.getAbsolutePath());
            waitFor(ofMillis(1000L));
            assertTrue(getSnackBarMessage(getDriver()).contains("Uploaded"));

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
    public void no_business_term_will_be_created_if_invalid_format_in_uploaded_csv_file() throws IOException {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBusinessTermPage viewEditBusinessTermPage = bieMenu.openViewEditBusinessTermSubMenu();
        UploadBusinessTermsPage uploadBusinessTermsPage = viewEditBusinessTermPage.hitUploadBusinessTermsButton();

        //generate three random Business Terms for testing
        List<BusinessTermObject> businessTerms = new ArrayList<>();

        String randomBT_noTerm_URI = "http://www.hSxyz.com";
        String randomBT_invalidUri = "http://api.google.com/q?exp=a|b";
        BusinessTermObject randomBT = BusinessTermObject.createRandomBusinessTerm(endUser, "bt");
        businessTerms.add(randomBT);

        File targetFolder = new File(System.getProperty("user.home"), "Downloads");
        // write test business terms into csv file and save into a different name for upload
        File csvFileForUpload = new File(targetFolder, "businessTermTemplateWithExampleForUpload_" + randomAlphabetic(5, 10) + ".csv");
        if (csvFileForUpload.exists()) {
            csvFileForUpload.delete();
        }

        try {
            try (BufferedWriter writer = Files.newBufferedWriter(csvFileForUpload.toPath());
                 CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withEscape('\\').withQuoteMode(QuoteMode.ALL)
                         .withHeader("businessTerm", "externalReferenceUri", "externalReferenceId", "definition", "comment"))) {

                csvPrinter.printRecord("", randomBT_noTerm_URI, "683053", ">&!Hi0[lP?}AXNgIir`{,R Q}JMsyc%){sO|>?VuY6YeyiY-2S%g=VknYE4Z+4xPZ&=H+'e*N{`>LT v!`0|gItaw7",
                        "+Am&) v;M@opO>fi43^VYFdn+;RwpvU2Bt@vl7oW7Di#ro1");
                csvPrinter.printRecord("hstlbt", randomBT_invalidUri, "68", ">&!Hi0[lP?}AXNgIir`g=VknYE4Z+4xPZ&=H+'e*N{`>LT v!`0|gItaw7",
                        "+Am&) v;M@opO>fi43^#ro1");
                csvPrinter.printRecord(randomBT.getBusinessTerm(), randomBT.getExternalReferenceUri(),
                        randomBT.getExternalReferenceId(), randomBT.getDefinition(), randomBT.getComment());
                csvPrinter.flush();
            }

            // upload the modified csv file
            uploadBusinessTermsPage.getFileUploadInput().sendKeys(csvFileForUpload.getAbsolutePath());
            waitFor(ofMillis(1000L));
            assertTrue(getSnackBarMessage(getDriver()).contains("Fail to parse CSV file"));

            // Verify that only valid test business terms have been saved through bulk upload
            viewEditBusinessTermPage.openPage();
            viewEditBusinessTermPage.setExternalReferenceURI(randomBT_noTerm_URI);
            viewEditBusinessTermPage.hitSearchButton();
            assertEquals(0, viewEditBusinessTermPage.getTotalNumberOfItems());

            viewEditBusinessTermPage.openPage();
            viewEditBusinessTermPage.setExternalReferenceURI(randomBT_invalidUri);
            viewEditBusinessTermPage.hitSearchButton();
            assertEquals(0, viewEditBusinessTermPage.getTotalNumberOfItems());

            viewEditBusinessTermPage.openPage();
            viewEditBusinessTermPage.setExternalReferenceURI(randomBT.getExternalReferenceUri());
            viewEditBusinessTermPage.hitSearchButton();
            assertTrue(viewEditBusinessTermPage.getSelectCheckboxAtIndex(1).isDisplayed());

        } finally {
            csvFileForUpload.delete();
        }
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
            BusinessTermObject randomBusinessTerm = BusinessTermObject.createRandomBusinessTerm(endUser, "bt");
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
                 CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withEscape('\\').withQuoteMode(QuoteMode.ALL)
                         .withHeader("businessTerm", "externalReferenceUri", "externalReferenceId", "definition", "comment"))) {

                for (int i = 0; i < 3; i++) {
                    csvPrinter.printRecord(businessTerms.get(i).getBusinessTerm(), businessTerms.get(i).getExternalReferenceUri(),
                            businessTerms.get(i).getExternalReferenceId(), businessTerms.get(i).getDefinition(), businessTerms.get(i).getComment());
                }
                csvPrinter.flush();
            }

            // upload the modified csv file
            uploadBusinessTermsPage.getFileUploadInput().sendKeys(csvFileForUpload.getAbsolutePath());
            waitFor(ofMillis(1000L));
            assertTrue(getSnackBarMessage(getDriver()).contains("Uploaded"));

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
        int sizeOfSamples = 3;
        for (int i = 0; i < sizeOfSamples; i++) {
            BusinessTermObject randomBusinessTerm = BusinessTermObject.createRandomBusinessTerm(endUser, "bt");
            businessTerms.add(randomBusinessTerm);
        }
        // change the last record URI to be the same as the first record
        businessTerms.get(businessTerms.size() - 1).setExternalReferenceUri(
                businessTerms.get(0).getExternalReferenceUri()
        );

        File targetFolder = new File(System.getProperty("user.home"), "Downloads");
        // write test business terms into csv file and save into a different name for upload
        File csvFileForUpload = new File(targetFolder, "businessTermTemplateWithExampleForUpload_" + randomAlphabetic(5, 10) + ".csv");
        if (csvFileForUpload.exists()) {
            csvFileForUpload.delete();
        }

        try {
            try (BufferedWriter writer = Files.newBufferedWriter(csvFileForUpload.toPath());
                 CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withEscape('\\').withQuoteMode(QuoteMode.ALL)
                         .withHeader("businessTerm", "externalReferenceUri", "externalReferenceId", "definition", "comment"))) {

                for (BusinessTermObject businessTerm : businessTerms) {
                    csvPrinter.printRecord(businessTerm.getBusinessTerm(), businessTerm.getExternalReferenceUri(),
                            businessTerm.getExternalReferenceId(), businessTerm.getDefinition(), businessTerm.getComment());
                }
                csvPrinter.flush();
            }

            // upload the modified CSV file
            uploadBusinessTermsPage.getFileUploadInput().sendKeys(csvFileForUpload.getAbsolutePath());
            waitFor(ofMillis(1000L));
            assertTrue(getSnackBarMessage(getDriver()).contains("Uploaded"));

            int totalNumberOfItems = 0;
            // Either the first BT or the last BT should be existed.
            for (BusinessTermObject businessTerm : businessTerms) {
                viewEditBusinessTermPage.openPage();
                viewEditBusinessTermPage.setTerm(businessTerm.getBusinessTerm());
                viewEditBusinessTermPage.hitSearchButton();
                totalNumberOfItems += viewEditBusinessTermPage.getTotalNumberOfItems();
            }

            assertEquals(businessTerms.size() - 1, totalNumberOfItems);
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
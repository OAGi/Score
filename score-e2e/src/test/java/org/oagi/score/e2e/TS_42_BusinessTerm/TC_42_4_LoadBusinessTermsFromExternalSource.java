package org.oagi.score.e2e.TS_42_BusinessTerm;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
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
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
    public void end_user_can_upload_and_attach_the_csv_file_with_correct_format_in_business_term_page() throws IOException {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBusinessTermPage viewEditBusinessTermPage = bieMenu.openViewEditBusinessTermSubMenu();
        UploadBusinssTermsPage uploadBusinssTermsPage = viewEditBusinessTermPage.hitUploadBusinessTermsButton();

        //Download csv file template
        click(uploadBusinssTermsPage.getDownloadTemplateButton());

        //Call Awaitility library for async download wait
        File targetFolder = new File(System.getProperty("user.home"), "Downloads");
        ConditionFactory await = Awaitility.await().atMost(Duration.ofSeconds(1));
        File csvFile = new File(targetFolder, "businessTermTemplateWithExample.csv");
        await.until(() -> csvFile.exists());

        //write test business terms into csv file and save into a different name for upload
        File csvFileForUpload = new File(targetFolder, "businessTermTemplateWithExampleForUpload.csv");
        try(
            BufferedWriter writer = Files.newBufferedWriter(csvFileForUpload.toPath());

            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                    .withHeader("businessTerm", "externalReferenceUri", "externalReferenceId", "definition", "comment"));
        ){
            csvPrinter.printRecord("bt_bulk_upload1", "http://btupload1.com", "1", "business term 1 through bulk upload","business term 1 through bulk upload" );
            csvPrinter.printRecord("bt_bulk_upload2", "http://btupload2.com", "2", "business term 2 through bulk upload","business term 2 through bulk upload" );
            csvPrinter.printRecord("bt_bulk_upload3", "http://btupload3.com", "3", "business term 3 through bulk upload","business term 3 through bulk upload");

            csvPrinter.flush();
        }

        //upload the modified csv file
        uploadBusinssTermsPage.getAttachButton().sendKeys(csvFileForUpload.getAbsolutePath());
        getDriver().manage().timeouts().implicitlyWait(100, TimeUnit.MILLISECONDS);

        //Verify that all test business terms have been saved through bulk upload
        ViewEditBusinessTermPage viewEditBusinessTermPageForCheck = homePage.getBIEMenu().openViewEditBusinessTermSubMenu();
        viewEditBusinessTermPageForCheck.setTerm("bt_bulk_upload1");
        viewEditBusinessTermPageForCheck.hitSearchButton();
        assertTrue(viewEditBusinessTermPageForCheck.getSelectCheckboxAtIndex(1).isDisplayed());
        viewEditBusinessTermPageForCheck.setExternalReferenceURI("http://btupload2.com");
        viewEditBusinessTermPageForCheck.hitSearchButton();
        assertTrue(viewEditBusinessTermPageForCheck.getSelectCheckboxAtIndex(1).isDisplayed());
        viewEditBusinessTermPageForCheck.setTerm("bt_bulk_upload3");
        viewEditBusinessTermPageForCheck.hitSearchButton();
        assertTrue(viewEditBusinessTermPageForCheck.getSelectCheckboxAtIndex(1).isDisplayed());
    }

    @Test
    @DisplayName("TC_42_4_3")
    public void end_user_cannot_upload_the_csv_file_with_incorrect_format_in_business_term_page() {
    }

    @Test
    @DisplayName("TC_42_4_4")
    public void new_business_term_will_be_created_if_the_business_term_is_uploaded_with_new_external_reference_uri() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBusinessTermPage viewEditBusinessTermPage = bieMenu.openViewEditBusinessTermSubMenu();
        UploadBusinssTermsPage uploadBusinssTermsPage = viewEditBusinessTermPage.hitUploadBusinessTermsButton();

        File targetFolder = new File(System.getProperty("user.home"), "Downloads");

        //write test business terms into csv file and save into a different name for upload
        File csvFileForUpload = new File(targetFolder, "businessTermTemplateWithExampleForUpload.csv");
        try(
                BufferedWriter writer = Files.newBufferedWriter(csvFileForUpload.toPath());

                CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                        .withHeader("businessTerm", "externalReferenceUri", "externalReferenceId", "definition", "comment"));
        ){
            csvPrinter.printRecord("bt_bulk_upload1", "http://btupload1.com", "1", "business term 1 through bulk upload","business term 1 through bulk upload" );
            csvPrinter.printRecord("bt_bulk_upload2", "http://btupload2.com", "2", "business term 2 through bulk upload","business term 2 through bulk upload" );
            csvPrinter.printRecord("bt_bulk_upload3", "http://btupload3.com", "3", "business term 3 through bulk upload","business term 3 through bulk upload");

            csvPrinter.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //upload the modified csv file
        WebElement chooseFile = click(uploadBusinssTermsPage.getAttachButton());
        chooseFile.sendKeys(csvFileForUpload.getAbsolutePath());
        //Verify that all test business terms have been saved through bulk upload
        ViewEditBusinessTermPage viewEditBusinessTermPageForCheck = homePage.getBIEMenu().openViewEditBusinessTermSubMenu();
        viewEditBusinessTermPageForCheck.setTerm("bt_bulk_upload1");
        viewEditBusinessTermPageForCheck.hitSearchButton();
        assertTrue(viewEditBusinessTermPageForCheck.getSelectCheckboxAtIndex(1).isDisplayed());
        viewEditBusinessTermPageForCheck.setExternalReferenceURI("http://btupload2.com");
        viewEditBusinessTermPageForCheck.hitSearchButton();
        assertTrue(viewEditBusinessTermPageForCheck.getSelectCheckboxAtIndex(1).isDisplayed());
        viewEditBusinessTermPageForCheck.setTerm("bt_bulk_upload3");
        viewEditBusinessTermPageForCheck.hitSearchButton();
        assertTrue(viewEditBusinessTermPageForCheck.getSelectCheckboxAtIndex(1).isDisplayed());
    }

    @Test
    @DisplayName("TC_42_4_5")
    public void previous_business_term_will_be_updated_with_new_information_if_the_business_term_is_uploaded_with_an_exitent_external_reference_uri() {

        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBusinessTermPage viewEditBusinessTermPage = bieMenu.openViewEditBusinessTermSubMenu();
        UploadBusinssTermsPage uploadBusinssTermsPage = viewEditBusinessTermPage.hitUploadBusinessTermsButton();

        File targetFolder = new File(System.getProperty("user.home"), "Downloads");

        //write test business terms into csv file and save into a different name for upload
        File csvFileForUpload = new File(targetFolder, "businessTermTemplateWithExampleForUpload.csv");
        try(
                BufferedWriter writer = Files.newBufferedWriter(csvFileForUpload.toPath());

                CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                        .withHeader("businessTerm", "externalReferenceUri", "externalReferenceId", "definition", "comment"));
        ){
            csvPrinter.printRecord("bt_bulk_upload1", "http://btupload1.com", "1", "business term 1 through bulk upload","business term 1 through bulk upload" );
            csvPrinter.printRecord("bt_bulk_upload2", "http://btupload2.com", "2", "business term 2 through bulk upload","business term 2 through bulk upload" );
            csvPrinter.printRecord("bt_bulk_upload3", "http://btupload1.com", "3", "business term 3 through bulk upload","business term 3 through bulk upload");

            csvPrinter.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //upload the modified csv file
        WebElement chooseFile = click(uploadBusinssTermsPage.getAttachButton());
        chooseFile.sendKeys(csvFileForUpload.getAbsolutePath());
        //Verify that all test business terms have been saved through bulk upload
        ViewEditBusinessTermPage viewEditBusinessTermPageForCheck = homePage.getBIEMenu().openViewEditBusinessTermSubMenu();
        viewEditBusinessTermPageForCheck.setTerm("bt_bulk_upload3");
        viewEditBusinessTermPageForCheck.hitSearchButton();
        assertTrue(viewEditBusinessTermPageForCheck.getSelectCheckboxAtIndex(1).isDisplayed());
        viewEditBusinessTermPageForCheck.setExternalReferenceURI("http://btupload2.com");
        viewEditBusinessTermPageForCheck.hitSearchButton();
        assertTrue(viewEditBusinessTermPageForCheck.getSelectCheckboxAtIndex(1).isDisplayed());
        assertThrows(NoSuchElementException.class, () -> {
            viewEditBusinessTermPageForCheck.openEditBusinessTermPageByTerm("bt_bulk_upload1");
        });
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
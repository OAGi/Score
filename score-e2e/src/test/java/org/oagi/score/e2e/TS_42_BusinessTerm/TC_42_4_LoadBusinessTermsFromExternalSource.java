package org.oagi.score.e2e.TS_42_BusinessTerm;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.condition.DisabledIfBusinessTermProperty;
import org.oagi.score.e2e.menu.BIEMenu;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.BusinessTermObject;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.business_term.EditBusinessTermPage;
import org.oagi.score.e2e.page.business_term.UploadBusinessTermsPage;
import org.oagi.score.e2e.page.business_term.ViewEditBusinessTermPage;
import org.openqa.selenium.By;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.oagi.score.e2e.impl.PageHelper.*;

@Execution(ExecutionMode.CONCURRENT)
@DisabledIfBusinessTermProperty(value = false)
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
    public void end_user_can_download_business_term_upload_template() {
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
    public void end_user_can_upload_business_terms_using_valid_csv_format() throws IOException {
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
        File csvFileForUpload = new File(targetFolder, "businessTermTemplateWithExampleForUpload_" + RandomStringUtils.secure().nextAlphabetic(5, 10) + ".csv");
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

            // upload the file, advance through the auto-mapped preview, and import all valid rows
            uploadBusinessTermsPage.uploadFile(csvFileForUpload.getAbsolutePath());
            uploadBusinessTermsPage.proceedToPreview();
            assertTrue(uploadBusinessTermsPage.isImportButtonEnabled());
            uploadBusinessTermsPage.hitImportButton();
            assertTrue(uploadBusinessTermsPage.getResultSummaryText().contains("3 created"));

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
    public void invalid_business_term_csv_does_not_create_any_records() throws IOException {
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
        File csvFileForUpload = new File(targetFolder, "businessTermTemplateWithExampleForUpload_" + RandomStringUtils.secure().nextAlphabetic(5, 10) + ".csv");
        if (csvFileForUpload.exists()) {
            csvFileForUpload.delete();
        }

        try {
            try (BufferedWriter writer = Files.newBufferedWriter(csvFileForUpload.toPath());
                 CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                         .withHeader("businessTerm", "externalReferenceUri", "externalReferenceId", "definition", "comment"))) {

                csvPrinter.printRecord("", randomBT_noTerm_URI, "683053", ">&!Hi0[lP?}AXNgIir`{,R Q}JMsyc%){sO|>?VuY6YeyiY-2S%g=VknYE4Z+4xPZ&=H+'e*N{`>LT v!`0|gItaw7",
                        "+Am&) v;M@opO>fi43^VYFdn+;RwpvU2Bt@vl7oW7Di#ro1");
                csvPrinter.printRecord("hstlbt", randomBT_invalidUri, "68", ">&!Hi0[lP?}AXNgIir`g=VknYE4Z+4xPZ&=H+'e*N{`>LT v!`0|gItaw7",
                        "+Am&) v;M@opO>fi43^#ro1");
                csvPrinter.printRecord(randomBT.getBusinessTerm(), randomBT.getExternalReferenceUri(),
                        randomBT.getExternalReferenceId(), randomBT.getDefinition(), randomBT.getComment());
                csvPrinter.flush();
            }

            // upload the file and advance to the preview
            uploadBusinessTermsPage.uploadFile(csvFileForUpload.getAbsolutePath());
            uploadBusinessTermsPage.proceedToPreview();

            // the row with no business term and the row with an invalid URI are flagged for review
            // and de-selected; only the single valid row is ready to import
            assertTrue(uploadBusinessTermsPage.getNeedReviewChipText().contains("2"));
            assertTrue(uploadBusinessTermsPage.getReadyChipText().contains("1"));
            uploadBusinessTermsPage.hitImportButton();

            // the two malformed rows were never imported ...
            viewEditBusinessTermPage.openPage();
            viewEditBusinessTermPage.showAdvancedSearchPanel();
            viewEditBusinessTermPage.setExternalReferenceURI(randomBT_noTerm_URI);
            viewEditBusinessTermPage.hitSearchButton();
            assertEquals(0, viewEditBusinessTermPage.getTotalNumberOfItems());

            viewEditBusinessTermPage.openPage();
            viewEditBusinessTermPage.showAdvancedSearchPanel();
            viewEditBusinessTermPage.setExternalReferenceURI(randomBT_invalidUri);
            viewEditBusinessTermPage.hitSearchButton();
            assertEquals(0, viewEditBusinessTermPage.getTotalNumberOfItems());

            // ... but the one valid row was imported
            viewEditBusinessTermPage.openPage();
            viewEditBusinessTermPage.showAdvancedSearchPanel();
            viewEditBusinessTermPage.setExternalReferenceURI(randomBT.getExternalReferenceUri());
            viewEditBusinessTermPage.hitSearchButton();
            assertEquals(1, viewEditBusinessTermPage.getTotalNumberOfItems());

        } finally {
            csvFileForUpload.delete();
        }
    }

    @Test
    @DisplayName("TC_42_4_4")
    public void upload_creates_new_business_terms_when_external_reference_uri_is_new() throws IOException {
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
        File csvFileForUpload = new File(targetFolder, "businessTermTemplateWithExampleForUpload_" + RandomStringUtils.secure().nextAlphabetic(5, 10) + ".csv");
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

            // upload the file, advance through the auto-mapped preview, and import all valid rows
            uploadBusinessTermsPage.uploadFile(csvFileForUpload.getAbsolutePath());
            uploadBusinessTermsPage.proceedToPreview();
            uploadBusinessTermsPage.hitImportButton();
            assertTrue(uploadBusinessTermsPage.getResultSummaryText().contains("3 created"));

            //Verify that all test business terms have been saved through bulk upload
            ViewEditBusinessTermPage viewEditBusinessTermPageForCheck = homePage.getBIEMenu().openViewEditBusinessTermSubMenu();
            for (int i = 0; i < 3; i++) {
                viewEditBusinessTermPageForCheck.openPage();
                viewEditBusinessTermPageForCheck.showAdvancedSearchPanel();
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
    public void upload_updates_existing_business_term_when_external_reference_uri_already_exists() throws IOException {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        // Seed an existing business term, then upload a row with the SAME external reference URI but
        // different values; the upsert-by-URI import must UPDATE the existing record, not add one.
        BusinessTermObject originalBusinessTerm =
                getAPIFactory().getBusinessTermAPI().createRandomBusinessTerm(endUser, "bt");
        BusinessTermObject updatedBusinessTerm = BusinessTermObject.createRandomBusinessTerm(endUser, "bt");
        updatedBusinessTerm.setExternalReferenceUri(originalBusinessTerm.getExternalReferenceUri());

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBusinessTermPage viewEditBusinessTermPage = bieMenu.openViewEditBusinessTermSubMenu();
        UploadBusinessTermsPage uploadBusinessTermsPage = viewEditBusinessTermPage.hitUploadBusinessTermsButton();

        File targetFolder = new File(System.getProperty("user.home"), "Downloads");
        // write the test business term into a csv file and save into a different name for upload
        File csvFileForUpload = new File(targetFolder, "businessTermTemplateWithExampleForUpload_" + RandomStringUtils.secure().nextAlphabetic(5, 10) + ".csv");
        if (csvFileForUpload.exists()) {
            csvFileForUpload.delete();
        }

        try {
            try (BufferedWriter writer = Files.newBufferedWriter(csvFileForUpload.toPath());
                 CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                         .withHeader("businessTerm", "externalReferenceUri", "externalReferenceId", "definition", "comment"))) {

                csvPrinter.printRecord(updatedBusinessTerm.getBusinessTerm(), updatedBusinessTerm.getExternalReferenceUri(),
                        updatedBusinessTerm.getExternalReferenceId(), updatedBusinessTerm.getDefinition(), updatedBusinessTerm.getComment());
                csvPrinter.flush();
            }

            // upload, advance to the preview, and import the single row
            uploadBusinessTermsPage.uploadFile(csvFileForUpload.getAbsolutePath());
            uploadBusinessTermsPage.proceedToPreview();
            uploadBusinessTermsPage.hitImportButton();
            assertTrue(uploadBusinessTermsPage.getResultSummaryText().contains("1 updated"));

            // the existing record now carries the uploaded values, keyed by the shared URI
            viewEditBusinessTermPage.openPage();
            viewEditBusinessTermPage.showAdvancedSearchPanel();
            viewEditBusinessTermPage.setExternalReferenceURI(updatedBusinessTerm.getExternalReferenceUri());
            viewEditBusinessTermPage.hitSearchButton();
            assertEquals(1, viewEditBusinessTermPage.getTotalNumberOfItems());
            assertEquals(updatedBusinessTerm.getBusinessTerm(),
                    viewEditBusinessTermPage.getColumnByName(viewEditBusinessTermPage.getTableRecordAtIndex(1), "businessTerm")
                            .findElement(By.cssSelector("a > span")).getText());

            EditBusinessTermPage editBusinessTermPage =
                    viewEditBusinessTermPage.openEditBusinessTermPageByTerm(updatedBusinessTerm.getBusinessTerm());
            assertEquals(updatedBusinessTerm.getExternalReferenceUri(), editBusinessTermPage.getExternalReferenceURIFieldText());
            assertEquals(updatedBusinessTerm.getExternalReferenceId(), editBusinessTermPage.getExternalReferenceIDFieldText());
            assertEquals(updatedBusinessTerm.getDefinition(), editBusinessTermPage.getDefinitionFieldText());
            assertEquals(updatedBusinessTerm.getComment(), editBusinessTermPage.getCommentFieldText());

            // the original business-term name no longer resolves (the record was overwritten)
            viewEditBusinessTermPage.openPage();
            viewEditBusinessTermPage.setTerm(originalBusinessTerm.getBusinessTerm());
            viewEditBusinessTermPage.hitSearchButton();
            assertEquals(0, viewEditBusinessTermPage.getTotalNumberOfItems());
        } finally {
            csvFileForUpload.delete();
        }
    }

    @Test
    @DisplayName("TC_42_4_6")
    public void multi_worksheet_file_stops_on_upload_and_imports_the_chosen_worksheet() throws IOException {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        UploadBusinessTermsPage uploadBusinessTermsPage =
                homePage.getBIEMenu().openViewEditBusinessTermSubMenu().hitUploadBusinessTermsButton();

        List<BusinessTermObject> businessTerms = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            businessTerms.add(BusinessTermObject.createRandomBusinessTerm(endUser, "bt"));
        }
        File xlsxFile = writeMultiSheetWorkbook(businessTerms);
        try {
            // A multi-worksheet workbook does not auto-advance: the upload step shows a worksheet
            // picker (the default sheet is the decoy "Report", so the user must switch worksheets).
            uploadBusinessTermsPage.uploadMultiSheetFile(xlsxFile.getAbsolutePath());
            assertTrue(uploadBusinessTermsPage.isWorksheetSelectVisible());
            // The server's smart sheet-pick lands on the decoy "Report" sheet (1 row, 1 column) by
            // default, so the user genuinely has to switch worksheets. Pinning this guards the test from
            // silently degrading to a no-op switch if that default ever changes to "Business Terms".
            uploadBusinessTermsPage.waitForParsedSummary(1, 1);

            // Switching to the "Business Terms" worksheet re-parses the file (3 rows, 5 columns).
            uploadBusinessTermsPage.selectWorksheet("Business Terms");
            uploadBusinessTermsPage.waitForParsedSummary(3, 5);
            assertTrue(uploadBusinessTermsPage.isNextButtonEnabled());

            uploadBusinessTermsPage.hitNextButton();    // upload -> map
            uploadBusinessTermsPage.proceedToPreview(); // map -> review & select
            assertTrue(uploadBusinessTermsPage.getReadyChipText().contains("3 ready"));
            uploadBusinessTermsPage.hitImportButton();
            assertTrue(uploadBusinessTermsPage.getResultSummaryText().contains("3 created"));
        } finally {
            xlsxFile.delete();
        }
    }

    @Test
    @DisplayName("TC_42_4_7")
    public void removing_the_selected_file_returns_to_the_drop_zone() throws IOException {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        UploadBusinessTermsPage uploadBusinessTermsPage =
                homePage.getBIEMenu().openViewEditBusinessTermSubMenu().hitUploadBusinessTermsButton();

        File csvFileForUpload = writeNativeCsv(List.of(BusinessTermObject.createRandomBusinessTerm(endUser, "bt")));
        try {
            // A single-worksheet file auto-advances to the Map step; step Back to the upload step where
            // the removable file tile lives.
            uploadBusinessTermsPage.uploadFile(csvFileForUpload.getAbsolutePath());
            uploadBusinessTermsPage.hitBackButton();
            assertTrue(uploadBusinessTermsPage.isFileTileVisible());
            assertEquals(csvFileForUpload.getName(), uploadBusinessTermsPage.getSelectedFileName());

            // Removing the file resets the parse-derived state and restores the drag-and-drop zone.
            uploadBusinessTermsPage.removeSelectedFile();
            assertTrue(uploadBusinessTermsPage.isDropZoneVisible());
            assertFalse(uploadBusinessTermsPage.isFileTileVisible());
        } finally {
            csvFileForUpload.delete();
        }
    }

    @Test
    @DisplayName("TC_42_4_8")
    public void a_rejected_replacement_pick_keeps_the_existing_valid_selection() throws IOException {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        UploadBusinessTermsPage uploadBusinessTermsPage =
                homePage.getBIEMenu().openViewEditBusinessTermSubMenu().hitUploadBusinessTermsButton();

        File csvFileForUpload = writeNativeCsv(List.of(BusinessTermObject.createRandomBusinessTerm(endUser, "bt")));
        // An unsupported replacement file (.txt) that acceptFile() must reject without discarding the CSV.
        File txtFile = new File(new File(System.getProperty("user.home"), "Downloads"),
                "not-a-table_" + RandomStringUtils.secure().nextAlphabetic(5, 10) + ".txt");
        Files.write(txtFile.toPath(), "just some text".getBytes(StandardCharsets.UTF_8));
        try {
            uploadBusinessTermsPage.uploadFile(csvFileForUpload.getAbsolutePath());
            uploadBusinessTermsPage.hitBackButton(); // return to the upload step to attempt a replacement
            assertEquals(csvFileForUpload.getName(), uploadBusinessTermsPage.getSelectedFileName());

            // Picking an unsupported file reports a snackbar but keeps the already-valid CSV selected,
            // so Next stays enabled on the still-valid selection.
            uploadBusinessTermsPage.sendFileToInput(txtFile.getAbsolutePath());
            assertTrue(uploadBusinessTermsPage.getSnackBarMessage().contains("Unsupported file type"));
            assertTrue(uploadBusinessTermsPage.isFileTileVisible());
            assertEquals(csvFileForUpload.getName(), uploadBusinessTermsPage.getSelectedFileName());
            assertTrue(uploadBusinessTermsPage.isNextButtonEnabled());
        } finally {
            csvFileForUpload.delete();
            txtFile.delete();
        }
    }

    @Test
    @DisplayName("TC_42_4_9")
    public void commercial_export_without_a_uri_column_synthesizes_the_uri_from_a_base_url() throws IOException {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditBusinessTermPage viewEditBusinessTermPage = homePage.getBIEMenu().openViewEditBusinessTermSubMenu();
        UploadBusinessTermsPage uploadBusinessTermsPage = viewEditBusinessTermPage.hitUploadBusinessTermsButton();

        // A Collibra-style export: a term name + an ID but NO URI column, so the dialog auto-selects the
        // "Build from base URL + ID" (synthesize) strategy and needs only a base URL to proceed.
        String id = RandomStringUtils.secure().nextAlphanumeric(10);
        String term = "bt" + RandomStringUtils.secure().nextAlphanumeric(8);
        String baseUrl = "https://collibra.example.com/term/";
        String expectedUri = baseUrl + id; // base ends with '/', id is alphanumeric -> no encoding/slash change

        File targetFolder = new File(System.getProperty("user.home"), "Downloads");
        File csvFile = new File(targetFolder, "collibraExport_" + RandomStringUtils.secure().nextAlphabetic(5, 10) + ".csv");
        if (csvFile.exists()) {
            csvFile.delete();
        }
        try {
            try (BufferedWriter writer = Files.newBufferedWriter(csvFile.toPath());
                 CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                         .withHeader("ID", "Name", "Asset type", "Domain", "Community", "Definition"))) {
                csvPrinter.printRecord(id, term, "Business Term", "Sales", "Data Governance Council", "A random test term.");
                csvPrinter.flush();
            }

            uploadBusinessTermsPage.uploadFile(csvFile.getAbsolutePath());
            // The map step prompts the user to verify the auto-mapping and is in synthesize mode. Poll
            // the notice text: reading it can race the stepper's slide-in animation under load.
            await().atMost(Duration.ofSeconds(10))
                    .until(() -> uploadBusinessTermsPage.getMappingNoticeText().contains("Review the column mapping"));
            assertTrue(uploadBusinessTermsPage.isSynthesizeModeActive());
            // Without a base URL the mapping is incomplete, so Next stays disabled until one is entered.
            assertFalse(uploadBusinessTermsPage.isNextButtonEnabled());
            uploadBusinessTermsPage.setSynthesizeBaseUrl(baseUrl);
            // Typing the base URL enables Next once Angular recomputes the mapping-complete state.
            await().atMost(Duration.ofSeconds(10)).until(uploadBusinessTermsPage::isNextButtonEnabled);

            uploadBusinessTermsPage.proceedToPreview();
            uploadBusinessTermsPage.hitImportButton();
            assertTrue(uploadBusinessTermsPage.getResultSummaryText().contains("1 created"));

            // The imported term carries the synthesized "<base><ID>" external reference URI.
            viewEditBusinessTermPage.openPage();
            viewEditBusinessTermPage.showAdvancedSearchPanel();
            viewEditBusinessTermPage.setExternalReferenceURI(expectedUri);
            viewEditBusinessTermPage.hitSearchButton();
            assertEquals(1, viewEditBusinessTermPage.getTotalNumberOfItems());
        } finally {
            csvFile.delete();
        }
    }

    @Test
    @DisplayName("TC_42_4_10")
    public void the_close_button_cancels_the_import_before_the_result_step() throws IOException {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditBusinessTermPage viewEditBusinessTermPage = homePage.getBIEMenu().openViewEditBusinessTermSubMenu();
        UploadBusinessTermsPage uploadBusinessTermsPage = viewEditBusinessTermPage.hitUploadBusinessTermsButton();

        BusinessTermObject businessTerm = BusinessTermObject.createRandomBusinessTerm(endUser, "bt");
        File csvFile = writeNativeCsv(List.of(businessTerm));
        try {
            // Advance to the review step, then cancel via the top-right X: nothing is imported.
            uploadBusinessTermsPage.uploadFile(csvFile.getAbsolutePath());
            uploadBusinessTermsPage.proceedToPreview();
            uploadBusinessTermsPage.cancelViaCloseButton();
            assertTrue(uploadBusinessTermsPage.isClosed());

            // The row that would have been imported was never created.
            viewEditBusinessTermPage.openPage();
            viewEditBusinessTermPage.showAdvancedSearchPanel();
            viewEditBusinessTermPage.setExternalReferenceURI(businessTerm.getExternalReferenceUri());
            viewEditBusinessTermPage.hitSearchButton();
            assertEquals(0, viewEditBusinessTermPage.getTotalNumberOfItems());
        } finally {
            csvFile.delete();
        }
    }

    @Test
    @DisplayName("TC_42_4_11")
    public void intra_batch_duplicate_external_reference_uri_is_flagged_and_not_imported() throws IOException {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditBusinessTermPage viewEditBusinessTermPage = homePage.getBIEMenu().openViewEditBusinessTermSubMenu();
        UploadBusinessTermsPage uploadBusinessTermsPage = viewEditBusinessTermPage.hitUploadBusinessTermsButton();

        // Two rows share the SAME External Reference URI within one import. The first is valid; the
        // second (later) row repeating that URI must be flagged as a duplicate and left unselected so
        // two rows in one file cannot silently overwrite the same record.
        String sharedUri = "http://test." + RandomStringUtils.secure().nextAlphabetic(5) + ".com";
        String firstName = "bt_" + RandomStringUtils.secure().nextAlphanumeric(8);
        String secondName = "bt_" + RandomStringUtils.secure().nextAlphanumeric(8);

        File targetFolder = new File(System.getProperty("user.home"), "Downloads");
        File csvFileForUpload = new File(targetFolder, "businessTermIntraDup_" + RandomStringUtils.secure().nextAlphabetic(5, 10) + ".csv");
        if (csvFileForUpload.exists()) {
            csvFileForUpload.delete();
        }
        try {
            try (BufferedWriter writer = Files.newBufferedWriter(csvFileForUpload.toPath());
                 CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                         .withHeader("businessTerm", "externalReferenceUri", "externalReferenceId", "definition", "comment"))) {
                csvPrinter.printRecord(firstName, sharedUri, "1", "first definition", "first comment");
                csvPrinter.printRecord(secondName, sharedUri, "2", "second definition", "second comment");
                csvPrinter.flush();
            }

            uploadBusinessTermsPage.uploadFile(csvFileForUpload.getAbsolutePath());
            uploadBusinessTermsPage.proceedToPreview();

            // The later duplicate is flagged for review and unselected; only the first row is ready.
            assertTrue(uploadBusinessTermsPage.getNeedReviewChipText().contains("1"));
            assertTrue(uploadBusinessTermsPage.getReadyChipText().contains("1"));
            uploadBusinessTermsPage.hitImportButton();

            // Only one record exists for the shared URI (the first row); the duplicate never imported.
            viewEditBusinessTermPage.openPage();
            viewEditBusinessTermPage.showAdvancedSearchPanel();
            viewEditBusinessTermPage.setExternalReferenceURI(sharedUri);
            viewEditBusinessTermPage.hitSearchButton();
            assertEquals(1, viewEditBusinessTermPage.getTotalNumberOfItems());
        } finally {
            csvFileForUpload.delete();
        }
    }

    @Test
    @DisplayName("TC_42_4_12")
    public void one_invalid_row_alongside_valid_rows_imports_the_valid_rows_and_reports_a_single_failure() throws IOException {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditBusinessTermPage viewEditBusinessTermPage = homePage.getBIEMenu().openViewEditBusinessTermSubMenu();
        UploadBusinessTermsPage uploadBusinessTermsPage = viewEditBusinessTermPage.hitUploadBusinessTermsButton();

        // Three valid rows and one invalid row (invalid URI). The valid rows import; the bad row is
        // isolated per-row (reported as a failure) and does NOT roll back the good rows.
        List<BusinessTermObject> validTerms = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            validTerms.add(BusinessTermObject.createRandomBusinessTerm(endUser, "bt"));
        }
        String invalidUri = "http://api.google.com/q?exp=a|b";

        File targetFolder = new File(System.getProperty("user.home"), "Downloads");
        File csvFileForUpload = new File(targetFolder, "businessTermPartialFail_" + RandomStringUtils.secure().nextAlphabetic(5, 10) + ".csv");
        if (csvFileForUpload.exists()) {
            csvFileForUpload.delete();
        }
        try {
            try (BufferedWriter writer = Files.newBufferedWriter(csvFileForUpload.toPath());
                 CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                         .withHeader("businessTerm", "externalReferenceUri", "externalReferenceId", "definition", "comment"))) {
                for (BusinessTermObject bt : validTerms) {
                    csvPrinter.printRecord(bt.getBusinessTerm(), bt.getExternalReferenceUri(),
                            bt.getExternalReferenceId(), bt.getDefinition(), bt.getComment());
                }
                csvPrinter.printRecord("bt_invalid_" + RandomStringUtils.secure().nextAlphanumeric(6), invalidUri, "9", "def", "com");
                csvPrinter.flush();
            }

            uploadBusinessTermsPage.uploadFile(csvFileForUpload.getAbsolutePath());
            uploadBusinessTermsPage.proceedToPreview();

            // The invalid row is flagged for review (unselected); the three valid rows are ready.
            assertTrue(uploadBusinessTermsPage.getReadyChipText().contains("3"));
            assertTrue(uploadBusinessTermsPage.getNeedReviewChipText().contains("1"));
            uploadBusinessTermsPage.hitImportButton();

            // The valid rows imported ...
            assertTrue(uploadBusinessTermsPage.getResultSummaryText().contains("3 created"));

            // ... and each valid row is present (no rollback of the good rows).
            for (BusinessTermObject bt : validTerms) {
                viewEditBusinessTermPage.openPage();
                viewEditBusinessTermPage.showAdvancedSearchPanel();
                viewEditBusinessTermPage.setExternalReferenceURI(bt.getExternalReferenceUri());
                viewEditBusinessTermPage.hitSearchButton();
                assertEquals(1, viewEditBusinessTermPage.getTotalNumberOfItems());
            }
        } finally {
            csvFileForUpload.delete();
        }
    }

    @Test
    @DisplayName("TC_42_4_13")
    public void oversized_or_unsupported_replacement_keeps_valid_selection_while_first_unsupported_pick_shows_inline_error() throws IOException {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        UploadBusinessTermsPage uploadBusinessTermsPage =
                homePage.getBIEMenu().openViewEditBusinessTermSubMenu().hitUploadBusinessTermsButton();

        File csvFileForUpload = writeNativeCsv(List.of(BusinessTermObject.createRandomBusinessTerm(endUser, "bt")));
        // An unsupported replacement file (.txt) that must be rejected without discarding the CSV.
        File txtFile = new File(new File(System.getProperty("user.home"), "Downloads"),
                "not-a-table_" + RandomStringUtils.secure().nextAlphabetic(5, 10) + ".txt");
        Files.write(txtFile.toPath(), "just some text".getBytes(StandardCharsets.UTF_8));
        try {
            // With a valid file already selected, an unsupported/oversized replacement is reported via a
            // snackbar and the valid selection is kept.
            uploadBusinessTermsPage.uploadFile(csvFileForUpload.getAbsolutePath());
            uploadBusinessTermsPage.hitBackButton(); // return to the upload step to attempt a replacement
            assertEquals(csvFileForUpload.getName(), uploadBusinessTermsPage.getSelectedFileName());

            uploadBusinessTermsPage.sendFileToInput(txtFile.getAbsolutePath());
            assertTrue(uploadBusinessTermsPage.getSnackBarMessage().contains("Unsupported file type"));
            assertTrue(uploadBusinessTermsPage.isFileTileVisible());
            assertEquals(csvFileForUpload.getName(), uploadBusinessTermsPage.getSelectedFileName());
        } finally {
            csvFileForUpload.delete();
            txtFile.delete();
        }

        // An unsupported file chosen as the FIRST selection (no valid file yet) shows the inline
        // drop-zone error instead of a snackbar.
        UploadBusinessTermsPage freshDialog =
                homePage.getBIEMenu().openViewEditBusinessTermSubMenu().hitUploadBusinessTermsButton();
        File txtFileFirst = new File(new File(System.getProperty("user.home"), "Downloads"),
                "first-pick_" + RandomStringUtils.secure().nextAlphabetic(5, 10) + ".txt");
        Files.write(txtFileFirst.toPath(), "just some text".getBytes(StandardCharsets.UTF_8));
        try {
            freshDialog.sendFileToInput(txtFileFirst.getAbsolutePath());
            assertTrue(freshDialog.getDropZoneErrorText().toLowerCase().contains("unsupported"));
        } finally {
            txtFileFirst.delete();
        }
    }

    @Test
    @DisplayName("TC_42_4_14")
    public void reimport_omitting_optional_columns_does_not_blank_existing_fields() throws IOException {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        // Seed an existing term with a full set of optional fields.
        BusinessTermObject existing = getAPIFactory().getBusinessTermAPI().createRandomBusinessTerm(endUser, "bt");

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditBusinessTermPage viewEditBusinessTermPage = homePage.getBIEMenu().openViewEditBusinessTermSubMenu();
        UploadBusinessTermsPage uploadBusinessTermsPage = viewEditBusinessTermPage.hitUploadBusinessTermsButton();

        // Re-import a row carrying the SAME External Reference URI but a header set that OMITS the
        // definition, comment, and externalReferenceId columns entirely. The upsert-by-URI update must
        // NOT blank those existing values (blank-clobber guard). Only the business term name changes.
        String updatedName = "bt_" + RandomStringUtils.secure().nextAlphanumeric(8);

        File targetFolder = new File(System.getProperty("user.home"), "Downloads");
        File csvFileForUpload = new File(targetFolder, "businessTermReimport_" + RandomStringUtils.secure().nextAlphabetic(5, 10) + ".csv");
        if (csvFileForUpload.exists()) {
            csvFileForUpload.delete();
        }
        try {
            try (BufferedWriter writer = Files.newBufferedWriter(csvFileForUpload.toPath());
                 CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                         .withHeader("businessTerm", "externalReferenceUri"))) {
                csvPrinter.printRecord(updatedName, existing.getExternalReferenceUri());
                csvPrinter.flush();
            }

            uploadBusinessTermsPage.uploadFile(csvFileForUpload.getAbsolutePath());
            uploadBusinessTermsPage.proceedToPreview();
            uploadBusinessTermsPage.hitImportButton();
            assertTrue(uploadBusinessTermsPage.getResultSummaryText().contains("1 updated"));

            // Verify via the DB API: the omitted columns keep their original values; only the name changed.
            BusinessTermObject reloaded = getAPIFactory().getBusinessTermAPI().getBusinessTermByName(updatedName);
            assertEquals(existing.getExternalReferenceUri(), reloaded.getExternalReferenceUri());
            assertEquals(existing.getDefinition(), reloaded.getDefinition());
            assertEquals(existing.getComment(), reloaded.getComment());
            assertEquals(existing.getExternalReferenceId(), reloaded.getExternalReferenceId());
        } finally {
            csvFileForUpload.delete();
        }
    }

    /** Write the given business terms to a native-template CSV in the Downloads folder for upload. */
    private File writeNativeCsv(List<BusinessTermObject> businessTerms) throws IOException {
        File targetFolder = new File(System.getProperty("user.home"), "Downloads");
        File csvFile = new File(targetFolder,
                "businessTermTemplateWithExampleForUpload_" + RandomStringUtils.secure().nextAlphabetic(5, 10) + ".csv");
        if (csvFile.exists()) {
            csvFile.delete();
        }
        try (BufferedWriter writer = Files.newBufferedWriter(csvFile.toPath());
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                     .withHeader("businessTerm", "externalReferenceUri", "externalReferenceId", "definition", "comment"))) {
            for (BusinessTermObject bt : businessTerms) {
                csvPrinter.printRecord(bt.getBusinessTerm(), bt.getExternalReferenceUri(),
                        bt.getExternalReferenceId(), bt.getDefinition(), bt.getComment());
            }
            csvPrinter.flush();
        }
        return csvFile;
    }

    /**
     * Build a two-worksheet .xlsx in the Downloads folder: a non-banner decoy sheet ("Report", which
     * the server's smart sheet-pick selects by default) followed by a "Business Terms" data sheet with
     * the native template headers and the given rows. This exercises the worksheet picker + re-parse.
     */
    private File writeMultiSheetWorkbook(List<BusinessTermObject> businessTerms) throws IOException {
        File targetFolder = new File(System.getProperty("user.home"), "Downloads");
        File xlsxFile = new File(targetFolder,
                "businessTermMultiSheet_" + RandomStringUtils.secure().nextAlphabetic(5, 10) + ".xlsx");
        if (xlsxFile.exists()) {
            xlsxFile.delete();
        }
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet decoy = workbook.createSheet("Report");
            decoy.createRow(0).createCell(0).setCellValue("Info");
            decoy.createRow(1).createCell(0).setCellValue("Generated for testing");

            Sheet data = workbook.createSheet("Business Terms");
            String[] headers = {"businessTerm", "externalReferenceUri", "externalReferenceId", "definition", "comment"};
            Row headerRow = data.createRow(0);
            for (int c = 0; c < headers.length; c++) {
                headerRow.createCell(c).setCellValue(headers[c]);
            }
            for (int i = 0; i < businessTerms.size(); i++) {
                BusinessTermObject bt = businessTerms.get(i);
                Row row = data.createRow(i + 1);
                row.createCell(0).setCellValue(bt.getBusinessTerm());
                row.createCell(1).setCellValue(bt.getExternalReferenceUri());
                row.createCell(2).setCellValue(bt.getExternalReferenceId());
                row.createCell(3).setCellValue(bt.getDefinition());
                row.createCell(4).setCellValue(bt.getComment());
            }
            try (FileOutputStream out = new FileOutputStream(xlsxFile)) {
                workbook.write(out);
            }
        }
        return xlsxFile;
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

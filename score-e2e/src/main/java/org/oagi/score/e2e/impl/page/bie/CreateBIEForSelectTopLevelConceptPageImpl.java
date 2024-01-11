package org.oagi.score.e2e.impl.page.bie;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.obj.BusinessContextObject;
import org.oagi.score.e2e.obj.TopLevelASBIEPObject;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.bie.CreateBIEForSelectTopLevelConceptPage;
import org.oagi.score.e2e.page.bie.EditBIEPage;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import java.math.BigInteger;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import static org.oagi.score.e2e.impl.PageHelper.*;

public class CreateBIEForSelectTopLevelConceptPageImpl extends BasePageImpl implements CreateBIEForSelectTopLevelConceptPage {

    private static final By SELECTED_BUSINESS_CONTEXTS_FIELD_LOCATOR =
            By.xpath("//*[contains(text(), \"Selected Business Contexts\")]//ancestor::mat-form-field[1]//mat-select");
    private static final By BRANCH_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(), \"Branch\")]//ancestor::mat-form-field[1]//mat-select");
    private static final By STATE_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(), \"State\")]//ancestor::mat-form-field[1]//mat-select");
    private static final By DEPRECATED_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(), \"Deprecated\")]//ancestor::mat-form-field[1]//mat-select");
    private static final By OWNER_SELECT_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Owner\")]//ancestor::div[1]/mat-select[1]");
    private static final By UPDATER_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(), \"Updater\")]//ancestor::div[1]/mat-select[1]");
    private static final By DROPDOWN_SEARCH_FIELD_LOCATOR =
            By.xpath("//input[@aria-label=\"dropdown search\"]");
    private static final By UPDATED_START_DATE_FIELD_LOCATOR =
            By.xpath("//input[contains(@placeholder, \"Updated start date\")]");
    private static final By UPDATED_END_DATE_FIELD_LOCATOR =
            By.xpath("//input[contains(@placeholder, \"Updated end date\")]");
    private static final By DEN_FIELD_LOCATOR =
            By.xpath("//input[contains(@placeholder, \"DEN\")]");
    private static final By DEFINITION_FIELD_LOCATOR =
            By.xpath("//input[contains(@placeholder, \"Definition\")]");
    private static final By MODULE_FIELD_LOCATOR =
            By.xpath("//input[contains(@placeholder, \"Module\")]");
    private static final By SEARCH_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Search\")]//ancestor::button[1]");
    private static final By CREATE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Create\")]//ancestor::button[1]");
    private final List<BusinessContextObject> selectedBusinessContexts;

    public CreateBIEForSelectTopLevelConceptPageImpl(BasePage parent, List<BusinessContextObject> selectedBusinessContexts) {
        super(parent);
        this.selectedBusinessContexts = selectedBusinessContexts;
    }

    @Override
    public void openPage() {
        String url = getPageUrl();
        getDriver().get(url);
        assert getText(getTitle()).contains("Create BIE");
        assert getText(getSubtitle()).contains("Select Top-Level Concept");
    }

    @Override
    public WebElement getTitle() {
        return visibilityOfElementLocated(getDriver(), By.className("title"));
    }

    @Override
    protected String getPageUrl() {
        String selectedBusinessContextIdList = this.selectedBusinessContexts.stream()
                .map(e -> e.getBusinessContextId().toString()).collect(Collectors.joining(","));
        return getConfig().getBaseUrl().resolve("/profile_bie/create/asccp?businessContextIdList=" + selectedBusinessContextIdList).toString();
    }

    @Override
    public WebElement getSubtitle() {
        return visibilityOfElementLocated(getDriver(), By.className("mat-mdc-card-subtitle"));
    }

    @Override
    public WebElement getSelectedBusinessContextsSelectField() {
        return elementToBeClickable(getDriver(), SELECTED_BUSINESS_CONTEXTS_FIELD_LOCATOR);
    }

    @Override
    public WebElement getBranchSelectField() {
        return visibilityOfElementLocated(getDriver(), BRANCH_SELECT_FIELD_LOCATOR);
    }

    @Override
    public void setBranch(String branch) {
        retry(() -> {
            click(getBranchSelectField());
            sendKeys(visibilityOfElementLocated(getDriver(), DROPDOWN_SEARCH_FIELD_LOCATOR), branch);
            WebElement optionField = visibilityOfElementLocated(getDriver(),
                    By.xpath("//mat-option//span[text() = \"" + branch + "\"]"));
            click(optionField);
        });
    }

    @Override
    public WebElement getStateSelectField() {
        return visibilityOfElementLocated(getDriver(), STATE_SELECT_FIELD_LOCATOR);
    }

    @Override
    public WebElement getDeprecatedSelectField() {
        return visibilityOfElementLocated(getDriver(), DEPRECATED_SELECT_FIELD_LOCATOR);
    }

    @Override
    public void setDeprecated(boolean deprecated) {
        click(getDeprecatedSelectField());
        WebElement optionField = visibilityOfElementLocated(getDriver(),
                By.xpath("//mat-option//span[contains(text(), \"" + (deprecated ? "True" : "False") + "\")]"));
        click(optionField);
    }

    @Override
    public WebElement getOwnerSelectField() {
        return visibilityOfElementLocated(getDriver(), OWNER_SELECT_FIELD_LOCATOR);
    }

    @Override
    public void setOwner(String owner) {
        click(getOwnerSelectField());
        sendKeys(visibilityOfElementLocated(getDriver(), DROPDOWN_SEARCH_FIELD_LOCATOR), owner);
        WebElement searchedSelectField = visibilityOfElementLocated(getDriver(),
                By.xpath("//mat-option//span[contains(text(), \"" + owner + "\")]"));
        click(searchedSelectField);
        escape(getDriver());
    }

    @Override
    public WebElement getUpdaterSelectField() {
        return visibilityOfElementLocated(getDriver(), UPDATER_SELECT_FIELD_LOCATOR);
    }

    @Override
    public void setUpdater(String updater) {
        click(getUpdaterSelectField());
        sendKeys(visibilityOfElementLocated(getDriver(), DROPDOWN_SEARCH_FIELD_LOCATOR), updater);
        WebElement searchedSelectField = visibilityOfElementLocated(getDriver(),
                By.xpath("//mat-option//span[contains(text(), \"" + updater + "\")]"));
        click(searchedSelectField);
        escape(getDriver());
    }

    @Override
    public WebElement getUpdatedStartDateField() {
        return visibilityOfElementLocated(getDriver(), UPDATED_START_DATE_FIELD_LOCATOR);
    }

    @Override
    public void setUpdatedStartDate(LocalDateTime updatedStartDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        sendKeys(getUpdatedStartDateField(), formatter.format(updatedStartDate));
    }

    @Override
    public WebElement getUpdatedEndDateField() {
        return visibilityOfElementLocated(getDriver(), UPDATED_END_DATE_FIELD_LOCATOR);
    }

    @Override
    public void setUpdatedEndDate(LocalDateTime updatedEndDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        sendKeys(getUpdatedEndDateField(), formatter.format(updatedEndDate));
    }

    @Override
    public WebElement getDENField() {
        return visibilityOfElementLocated(getDriver(), DEN_FIELD_LOCATOR);
    }

    @Override
    public void setDEN(String den) {
        sendKeys(getDENField(), den);
    }

    @Override
    public WebElement getDefinitionField() {
        return visibilityOfElementLocated(getDriver(), DEFINITION_FIELD_LOCATOR);
    }

    @Override
    public void setDefinition(String definition) {
        sendKeys(getDefinitionField(), definition);
    }

    @Override
    public WebElement getModuleField() {
        return visibilityOfElementLocated(getDriver(), MODULE_FIELD_LOCATOR);
    }

    @Override
    public void setModule(String module) {
        sendKeys(getModuleField(), module);
    }

    @Override
    public WebElement getSearchButton() {
        return visibilityOfElementLocated(getDriver(), SEARCH_BUTTON_LOCATOR);
    }

    @Override
    public void hitSearchButton() {
        retry(() -> click(getSearchButton()));
        invisibilityOfLoadingContainerElement(getDriver());
    }

    @Override
    public WebElement getTableRecordAtIndex(int idx) {
        return visibilityOfElementLocated(getDriver(), By.xpath("//tbody/tr[" + idx + "]"));
    }

    @Override
    public WebElement getTableRecordByValue(String value) {
        return visibilityOfElementLocated(getDriver(), By.xpath("//td[contains(text(), \"" + value + "\")]/ancestor::tr"));
    }

    @Override
    public WebElement getColumnByName(WebElement tableRecord, String columnName) {
        return tableRecord.findElement(By.className("mat-column-" + columnName));
    }

    @Override
    public void selectCoreComponentByDEN(String ccDEN) throws NoSuchElementException {
        sendKeys(getDENField(), ccDEN);
        click(getSearchButton());
        retry(() -> {
            WebElement td;
            WebElement tr;
            try {
                tr = getTableRecordAtIndex(1);
                td = getColumnByName(tr, "den");
            } catch (TimeoutException e) {
                throw new NoSuchElementException("Cannot locate a Core Component using " + ccDEN, e);
            }
            if (!ccDEN.equals(getDENFieldFromTheTable(td))) {
                throw new NoSuchElementException("Cannot locate a Core Component using " + ccDEN);
            }
            click(tr);
        });
    }

    private String getDENFieldFromTheTable(WebElement tableData) {
        return tableData.findElement(By.tagName("a")).getText();
    }

    @Override
    public WebElement getCreateButton() {
        return elementToBeClickable(getDriver(), CREATE_BUTTON_LOCATOR);
    }

    @Override
    public EditBIEPage createBIE(String asccpDEN, String branch) {
        setDEN(asccpDEN);
        setBranch(branch);
        waitFor(Duration.ofMillis(1000));
        hitSearchButton();

        WebElement tr;
        WebElement td;
        try {
            tr = getTableRecordAtIndex(1);
            td = getColumnByName(tr, "den");
        } catch (TimeoutException e) {
            throw new NoSuchElementException("Cannot locate a BIE using " + asccpDEN, e);
        }
        if (!asccpDEN.equals(getText(td.findElement(By.tagName("a"))))) {
            throw new NoSuchElementException("Cannot locate a BIE using " + asccpDEN);
        }
        WebElement select = getColumnByName(tr, "select");
        click(select);
        click(getCreateButton());
        waitFor(Duration.ofMillis(500));
        assert "Created".equals(getSnackBarMessage(getDriver()));

        // Wait for BIE creation
        invisibilityOfLoadingContainerElement(getDriver());
        String currentUrl = getDriver().getCurrentUrl();
        BigInteger topLevelAsbiepId = new BigInteger(currentUrl.substring(currentUrl.lastIndexOf("/") + 1));

        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .getTopLevelASBIEPByID(topLevelAsbiepId);
        EditBIEPage editBIEPage = new EditBIEPageImpl(this, topLevelASBIEP);
        assert editBIEPage.isOpened();
        return editBIEPage;
    }

}

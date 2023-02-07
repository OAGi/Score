package org.oagi.score.e2e.impl.page.business_term;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.obj.BusinessTermObject;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.business_term.AssignBusinessTermBTPage;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.oagi.score.e2e.impl.PageHelper.*;

public class AssignBusinessTermBTPageImpl extends BasePageImpl implements AssignBusinessTermBTPage {

    private static final By UPDATER_SELECT_FIELD_LOCATOR = By.xpath("//*[contains(text(), \"Updater\")]//ancestor::div[1]/mat-select[1]");

    private static final By DROPDOWN_SEARCH_FIELD_LOCATOR = By.xpath("//input[@aria-label=\"dropdown search\"]");

    private static final By UPDATED_START_DATE_FIELD_LOCATOR = By.xpath("//input[contains(@data-placeholder, \"Updated start date\")]");

    private static final By UPDATED_END_DATE_FIELD_LOCATOR = By.xpath("//input[contains(@data-placeholder, \"Updated end date\")]");

    private static final By BUSINESS_TERM_FIELD_LOCATOR = By.xpath("//span[contains(text(), \"Business Term\")]//ancestor::div[1]/input");

    private static final By EXTERNAL_REFERENCE_URI_FIELD_LOCATOR = By.xpath("//span[contains(text(), \"External Reference URI\")]//ancestor::div[1]/input");

    private static final By EXTERNAL_REFERENCE_ID_FIELD_LOCATOR = By.xpath("//span[contains(text(), \"External Reference ID\")]//ancestor::div[1]/input");

    private static final By FILTER_BY_SAME_CC_CHECKBOX_LOCATOR = By.xpath("//span[contains(text(), \"Filter by same CC\")]//ancestor::mat-checkbox[1]");

    private static final By SEARCH_BUTTON_LOCATOR = By.xpath("//span[contains(text(), \"Search\")]//ancestor::button[1]");

    private static final By TYPE_CODE_FIELD_LOCATOR = By.xpath("//span[contains(text(), \"Type Code\")]//ancestor::div[1]/input");

    private static final By PREFERRED_BUSINESS_TERM_CHECKBOX_LOCATOR = By.xpath("//span[contains(text(), \"Preferred Business Term\")]//ancestor::mat-checkbox[1]");

    private static final By CREATE_BUTTON_LOCATOR = By.xpath("//span[contains(text(), \"Create\")]//ancestor::button[1]");

    private final BasePage parent;

    private final List<String> bieTypes;

    private final BigInteger bieId;

    public AssignBusinessTermBTPageImpl(BasePage parent, List<String> bieTypes, BigInteger bieId) {
        super(parent);
        this.parent = parent;
        this.bieTypes = bieTypes;
        this.bieId = bieId;
    }

    @Override
    protected String getPageUrl() {
        List<String> queries = new ArrayList<>();
        if (bieId != null) {
            queries.add("bieIds=" + this.bieId);
        }
        queries.add("bieTypes=" + String.join(",", this.bieTypes));
        String path = "/business_term_management/assign_business_term/create/bt";
        if (!queries.isEmpty()) {
            path += "?" + queries.stream().collect(Collectors.joining("&"));
        }
        return getConfig().getBaseUrl().resolve(path).toString();
    }

    @Override
    public void openPage() {
        String url = getPageUrl();
        getDriver().get(url);
        assert "Assign Business Term".equals(getText(getTitle()));
        assert "Select Business Term".equals(getText(getSubTitle()));
    }

    @Override
    public WebElement getTitle() {
        return visibilityOfElementLocated(getDriver(), By.className("mat-card-title"));
    }

    public WebElement getSubTitle() {
        return visibilityOfElementLocated(getDriver(), By.className("mat-card-subtitle"));
    }

    @Override
    public WebElement getUpdaterSelectField() {
        return visibilityOfElementLocated(getDriver(), UPDATER_SELECT_FIELD_LOCATOR);
    }

    @Override
    public void setUpdater(String updater) {
        click(getUpdaterSelectField());
        sendKeys(visibilityOfElementLocated(getDriver(), DROPDOWN_SEARCH_FIELD_LOCATOR), updater);
        WebElement searchedSelectField = visibilityOfElementLocated(getDriver(), By.xpath("//mat-option//span[contains(text(), \"" + updater + "\")]"));
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
    public WebElement getBusinessTermField() {
        return visibilityOfElementLocated(getDriver(), BUSINESS_TERM_FIELD_LOCATOR);
    }

    @Override
    public void setBusinessTerm(String termName) {
        sendKeys(getBusinessTermField(), termName);
    }

    @Override
    public WebElement getExternalReferenceURIField() {
        return visibilityOfElementLocated(getDriver(), EXTERNAL_REFERENCE_URI_FIELD_LOCATOR);
    }

    @Override
    public void setExternalReferenceURI(String externalReferenceURI) {
        sendKeys(getExternalReferenceURIField(), externalReferenceURI);
    }

    @Override
    public WebElement getExternalReferenceIDField() {
        return visibilityOfElementLocated(getDriver(), EXTERNAL_REFERENCE_ID_FIELD_LOCATOR);
    }

    @Override
    public void setExternalReferenceID(String externalReferenceID) {
        sendKeys(getExternalReferenceIDField(), externalReferenceID);
    }

    @Override
    public WebElement getFilterBySameCCCheckbox() {
        return visibilityOfElementLocated(getDriver(), FILTER_BY_SAME_CC_CHECKBOX_LOCATOR);
    }

    @Override
    public WebElement getSearchButton() {
        return elementToBeClickable(getDriver(), SEARCH_BUTTON_LOCATOR);
    }

    @Override
    public void hitSearchButton() {
        retry(() -> click(getSearchButton()));
    }

    @Override
    public WebElement getTableRecordAtIndex(int idx) {
        return visibilityOfElementLocated(getDriver(), By.xpath("//tbody/tr[" + idx + "]"));
    }

    @Override
    public WebElement getColumnByName(WebElement tableRecord, String columnName) {
        return tableRecord.findElement(By.className("mat-column-" + columnName));
    }

    @Override
    public WebElement getSelectCheckboxAtIndex(int idx) {
        WebElement tr = getTableRecordAtIndex(idx);
        WebElement td = getColumnByName(tr, "select");
        return td.findElement(By.xpath("mat-checkbox/label/span[1]"));
    }

    @Override
    public void goToNextPage() {
        ((JavascriptExecutor) getDriver()).executeScript("window.scrollTo(0, document.body.scrollHeight)");
        click(elementToBeClickable(getDriver(), By.xpath("//button[@aria-label='Next page']")));

    }

    @Override
    public void goToPreviousPage() {
        ((JavascriptExecutor) getDriver()).executeScript("window.scrollTo(0, document.body.scrollHeight)");
        click(elementToBeClickable(getDriver(), By.xpath("//button[@aria-label='Previous page']")));
    }

    @Override
    public WebElement getTypeCodeField() {
        return visibilityOfElementLocated(getDriver(), TYPE_CODE_FIELD_LOCATOR);
    }

    @Override
    public void setTypeCode(String typeCode) {
        sendKeys(getTypeCodeField(), typeCode);
    }

    @Override
    public WebElement getPreferredBusinessTermCheckbox() {
        return visibilityOfElementLocated(getDriver(), PREFERRED_BUSINESS_TERM_CHECKBOX_LOCATOR);
    }

    @Override
    public WebElement getCreateButton() {
        return elementToBeClickable(getDriver(), CREATE_BUTTON_LOCATOR);
    }

    @Override
    public void create(BusinessTermObject businessTerm) {
        setBusinessTerm(businessTerm.getBusinessTerm());
        hitSearchButton();
        retry(() -> {
            click(getSelectCheckboxAtIndex(1));
            click(getCreateButton());
        });
    }
}

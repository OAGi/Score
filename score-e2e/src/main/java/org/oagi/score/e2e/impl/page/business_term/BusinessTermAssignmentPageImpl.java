package org.oagi.score.e2e.impl.page.business_term;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.obj.BusinessTermObject;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.business_term.AssignBusinessTermBIEPage;
import org.oagi.score.e2e.page.business_term.BusinessTermAssignmentPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.time.Duration.ofMillis;
import static org.oagi.score.e2e.impl.PageHelper.*;

public class BusinessTermAssignmentPageImpl extends BasePageImpl implements BusinessTermAssignmentPage {

    private static final By UPDATER_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(), \"Updater\")]//ancestor::div[1]/mat-select[1]");

    private static final By DROPDOWN_SEARCH_FIELD_LOCATOR =
            By.xpath("//input[@aria-label=\"dropdown search\"]");

    private static final By UPDATED_START_DATE_FIELD_LOCATOR =
            By.xpath("//input[contains(@placeholder, \"Updated start date\")]");

    private static final By UPDATED_END_DATE_FIELD_LOCATOR =
            By.xpath("//input[contains(@placeholder, \"Updated end date\")]");

    private static final By TYPE_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(), \"Type\")]//ancestor::div[1]/mat-select[1]");

    private static final By BIE_DEN_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"BIE DEN\")]//ancestor::div[1]/input");

    private static final By BUSINESS_TERM_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Business Term\")]//ancestor::div[1]/input");

    private static final By EXTERNAL_REFERENCE_URI_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"External Reference URI\")]//ancestor::div[1]/input");

    private static final By TYPE_CODE_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Type Code\")]//ancestor::div[1]/input");

    private static final By PREFERRED_ONLY_CHECKBOX_LOCATOR =
            By.xpath("//label[contains(text(), \"Preferred Only\")]//ancestor::mat-checkbox[1]");

    private static final By SEARCH_BUTTON_LOCATOR =
            By.xpath("//mat-icon[contains(text(), \"search\")]//ancestor::button[1]");
    private static final By TURNOFF_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Turn off\")]//ancestor::button[1]");

    private static final By ASSIGN_BUSINESS_TERM_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Assign Business Term\")]//ancestor::button[1]");

    private static final By SEARCH_BY_SELECTED_BIE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Search by Selected BIE\")]//ancestor::button[1]");

    private static final By DISCARD_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Discard\")]//ancestor::button[1]");

    private static final By DISCARD_BUTTON_IN_DIALOG_LOCATOR =
            By.xpath("//mat-dialog-container//span[contains(text(), \"Discard\")]//ancestor::button");

    private final List<String> bieTypes;

    private final BigInteger bieId;

    public BusinessTermAssignmentPageImpl(BasePage parent, List<String> bieTypes, BigInteger bieId) {
        super(parent);
        this.bieTypes = bieTypes;
        this.bieId = bieId;
    }

    @Override
    protected String getPageUrl() {
        List<String> queries = new ArrayList<>();
        if (bieId != null) {
            queries.add("bieId=" + this.bieId);
        }
        queries.add("bieType=" + String.join(",", this.bieTypes));
        String path = "/business_term_management/assign_business_term";
        if (!queries.isEmpty()) {
            path += "?" + queries.stream().collect(Collectors.joining("&"));
        }
        return getConfig().getBaseUrl().resolve(path).toString();
    }

    @Override
    public void openPage() {
        String url = getPageUrl();
        getDriver().get(url);
        assert "Business Term Assignment".equals(getText(getTitle()));
    }

    @Override
    public WebElement getTitle() {
        return visibilityOfElementLocated(getDriver(), By.className("title"));
    }

    @Override
    public List<String> getBIETypes() {
        return this.bieTypes;
    }

    @Override
    public BigInteger getBIEId() {
        return this.bieId;
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
    public WebElement getTypeField() {
        return visibilityOfElementLocated(getDriver(), TYPE_SELECT_FIELD_LOCATOR);
    }

    @Override
    public void setType(String bieType) {
        click(getUpdaterSelectField());
        sendKeys(visibilityOfElementLocated(getDriver(), DROPDOWN_SEARCH_FIELD_LOCATOR), bieType);
        WebElement searchedSelectField = visibilityOfElementLocated(getDriver(),
                By.xpath("//mat-option//span[contains(text(), \"" + bieType + "\")]"));
        click(searchedSelectField);
        escape(getDriver());
    }

    @Override
    public WebElement getBIEDenField() {
        return visibilityOfElementLocated(getDriver(), BIE_DEN_FIELD_LOCATOR);
    }

    @Override
    public void setBIEDenField(String bieDen) {
        sendKeys(getBIEDenField(), bieDen);
    }

    @Override
    public WebElement getBusinessTermField() {
        return visibilityOfElementLocated(getDriver(), BUSINESS_TERM_FIELD_LOCATOR);
    }

    @Override
    public void setBusinessTerm(String businessTerm) {
        sendKeys(getBusinessTermField(), businessTerm);
    }

    @Override
    public String getBusinessTermFieldText() {
        return getText(getBusinessTermField());
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
    public String getExternalReferenceURIFieldText() {
        return getText(getExternalReferenceURIField());
    }

    @Override
    public WebElement getTypeCodeField() {
        return visibilityOfElementLocated(getDriver(), TYPE_CODE_FIELD_LOCATOR);
    }

    @Override
    public void setTypeCodeField(String typeCode) {
        sendKeys(getTypeCodeField(), typeCode);
    }

    @Override
    public String getTypeCodeFieldText() {
        return getText(getTypeCodeField());
    }

    @Override
    public WebElement getPreferredOnlyCheckbox() {
        return visibilityOfElementLocated(getDriver(), PREFERRED_ONLY_CHECKBOX_LOCATOR);
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
    public WebElement getTurnOffButton() {
        return elementToBeClickable(getDriver(), TURNOFF_BUTTON_LOCATOR);
    }

    @Override
    public WebElement getAssignBusinessTermButton() {
        return elementToBeClickable(getDriver(), ASSIGN_BUSINESS_TERM_BUTTON_LOCATOR);
    }

    @Override
    public AssignBusinessTermBIEPage assignBusinessTerm() {
        click(getAssignBusinessTermButton());
        AssignBusinessTermBIEPage assignBusinessTermPage =
                new AssignBusinessTermBIEPageImpl(this);
        assert assignBusinessTermPage.isOpened();
        return assignBusinessTermPage;

    }

    @Override
    public WebElement getSearchBySelectedBIEButton() {
        return elementToBeClickable(getDriver(), SEARCH_BY_SELECTED_BIE_BUTTON_LOCATOR);
    }

    @Override
    public WebElement getDiscardButton(boolean enabled) {
        if (enabled) {
            return elementToBeClickable(getDriver(), DISCARD_BUTTON_LOCATOR);
        } else {
            return visibilityOfElementLocated(getDriver(), DISCARD_BUTTON_LOCATOR);
        }
    }

    @Override
    public void discardAssignment(String bieDEN, BusinessTermObject businessTerm, String typeCode) {
        setBIEDenField(bieDEN);
        setBusinessTerm(businessTerm.getBusinessTerm());
        setTypeCodeField(typeCode);
        click(getSearchButton());
        invisibilityOfLoadingContainerElement(getDriver());
        waitFor(ofMillis(500L));
        WebElement tr = getTableRecordByValue(businessTerm.getBusinessTerm());
        WebElement td = getColumnByName(tr, "select");
        click(td);
        click(getDiscardButton(true));
        click(elementToBeClickable(getDriver(), By.xpath(
                "//mat-dialog-container//span[contains(text(), \"Discard\")]//ancestor::button[1]")));

        assert "Discarded".equals(getSnackBarMessage(getDriver()));
    }

    @Override
    public WebElement getTableRecordAtIndex(int idx) {
        return visibilityOfElementLocated(getDriver(), By.xpath("//tbody/tr[" + idx + "]"));
    }

    @Override
    public WebElement getTableRecordByValue(String value) {
        return visibilityOfElementLocated(getDriver(), By.xpath("//td//span[contains(text(), \"" + value + "\")]/ancestor::tr"));
    }

    @Override
    public WebElement getColumnByName(WebElement tableRecord, String columnName) {
        return tableRecord.findElement(By.className("mat-column-" + columnName));
    }

    @Override
    public WebElement getSelectCheckboxAtIndex(int idx) {
        WebElement tr = getTableRecordAtIndex(idx);
        WebElement td = getColumnByName(tr, "select");
        return td.findElement(By.xpath("mat-checkbox"));
    }
}

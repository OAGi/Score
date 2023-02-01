package org.oagi.score.e2e.impl.page.business_term;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.page.business_term.AssignBusinessTermBIEPage;
import org.oagi.score.e2e.page.business_term.AssignBusinessTermBTPage;
import org.openqa.selenium.WebElement;

import java.time.LocalDateTime;

public class AssignBusinessTermBTPageImpl  extends BasePageImpl implements AssignBusinessTermBTPage {

    private final AssignBusinessTermBIEPage parent;

    public AssignBusinessTermBTPageImpl(AssignBusinessTermBIEPage parent) {
        super(parent);
        this.parent = parent;
    }
    @Override
    protected String getPageUrl() {
        return null;
    }

    @Override
    public void openPage() {

    }

    @Override
    public WebElement getTitle() {
        return null;
    }

    @Override
    public WebElement getUpdaterSelectField() {
        return null;
    }

    @Override
    public void setUpdater(String updater) {

    }

    @Override
    public WebElement getUpdatedStartDateField() {
        return null;
    }

    @Override
    public void setUpdatedStartDate(LocalDateTime updatedStartDate) {

    }

    @Override
    public WebElement getUpdatedEndDateField() {
        return null;
    }

    @Override
    public void setUpdatedEndDate(LocalDateTime updatedEndDate) {

    }

    @Override
    public WebElement getBusinessTermField() {
        return null;
    }

    @Override
    public void setBusinessTerm(String termName) {

    }

    @Override
    public WebElement getExternalReferenceURIField() {
        return null;
    }

    @Override
    public void setExternalReferenceURI(String externalReferenceURI) {

    }

    @Override
    public WebElement getExternalReferenceIDField() {
        return null;
    }

    @Override
    public void setExternalReferenceID(String externalReferenceID) {

    }

    @Override
    public WebElement getFilterBySameCCCheckbox() {
        return null;
    }

    @Override
    public WebElement getSearchButton() {
        return null;
    }

    @Override
    public void hitSearchButton() {

    }

    @Override
    public WebElement getTableRecordAtIndex(int idx) {
        return null;
    }

    @Override
    public WebElement getColumnByName(WebElement tableRecord, String columnName) {
        return null;
    }

    @Override
    public void goToNextPage() {

    }

    @Override
    public void goToPreviousPage() {

    }
}

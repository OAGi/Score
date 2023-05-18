package org.oagi.score.e2e.impl.page.agency_id_list;

import org.oagi.score.e2e.impl.PageHelper;
import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.obj.AgencyIDListObject;
import org.oagi.score.e2e.obj.AgencyIDListValueObject;
import org.oagi.score.e2e.obj.NamespaceObject;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.agency_id_list.AddCommentDialog;
import org.oagi.score.e2e.page.agency_id_list.EditAgencyIDListPage;
import org.oagi.score.e2e.page.agency_id_list.EditAgencyIDListValueDialog;
import org.openqa.selenium.*;

import java.time.Duration;

import static java.time.Duration.ofMillis;
import static java.time.Duration.ofSeconds;
import static org.oagi.score.e2e.impl.PageHelper.*;

public class EditAgencyIDListPageImpl extends BasePageImpl implements EditAgencyIDListPage {

    private static final By CORE_COMPONENT_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Core Component\")]//ancestor::mat-form-field//input");
    private static final By GUID_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"GUID\")]//ancestor::mat-form-field//input");
    private static final By RELEASE_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Release\")]//ancestor::mat-form-field//input");
    private static final By REVISION_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Revision\")]//ancestor::mat-form-field//input");
    private static final By STATE_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"State\")]//ancestor::mat-form-field//input");
    private static final By OWNER_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Owner\")]//ancestor::mat-form-field//input");
    private static final By AGENCY_ID_LIST_NAME_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Name\")]//ancestor::mat-form-field//input");
    private static final By LIST_ID_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"List ID\")]//ancestor::mat-form-field//input");
    private static final By VERSION_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Version\")]//ancestor::mat-form-field//input");
    private static final By AGENCY_ID_LIST_VALUE_SELECT_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Agency ID List Value\")]//ancestor::mat-form-field//mat-select");
    private static final By NAMESPACE_SELECT_FIELD_LOCATOR =
            By.xpath("//mat-select[@placeholder = \"Namespace\"]");
    private static final By DEFINITION_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Definition\")]//ancestor::mat-form-field//textarea");
    private static final By DEFINITION_SOURCE_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Definition Source\")]//ancestor::mat-form-field//input");
    private static final By UPDATE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Update\")]//ancestor::button[1]");
    private static final By DELETE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Delete\")]//ancestor::button[1]");
    public static final By CONTINUE_TO_CHANGE_STATE_BUTTON_IN_DIALOG_LOCATOR =
            By.xpath("//mat-dialog-container//span[contains(text(), \"Update\")]//ancestor::button/span");
    private static final By REVISE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Revise\")]//ancestor::button[1]");
    public static final By CONTINUE_REVISE_BUTTON_IN_DIALOG_LOCATOR =
            By.xpath("//mat-dialog-container//span[contains(text(), \"Revise\")]//ancestor::button/span");
    private static final By CANCEL_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Cancel\")]//ancestor::button[1]");
    public static final By CONTINUE_CANCEL_BUTTON_IN_DIALOG_LOCATOR =
            By.xpath("//mat-dialog-container//span[contains(text(), \"Okay\")]//ancestor::button/span");
    private static final By MOVE_TO_DRAFT_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Move to Draft\")]//ancestor::button[1]");
    private static final By MOVE_TO_CANDIDATE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Move to Candidate\")]//ancestor::button[1]");
    private static final By MOVE_TO_QA_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Move to QA\")]//ancestor::button[1]");
    private static final By MOVE_TO_PRODUCTION_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Move to Production\")]//ancestor::button[1]");
    private static final By BACK_TO_WIP_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Back to WIP\")]//ancestor::button[1]");
    private static final By ADD_AGENCY_ID_LIST_VALUE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Add\")]//ancestor::button[1]");
    private static final By REMOVE_AGENCY_ID_LIST_VALUE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Remove\")]//ancestor::button[1]");
    private static final By SEARCH_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Search\")]//ancestor::mat-form-field//input");
    private static final By SEARCH_BUTTON_LOCATOR =
            By.xpath("//mat-icon[text() = \"search\"]");
    private static final By COMMENT_BUTTON_LOCATOR =
            By.xpath("//mat-icon[text() = \"comments\"]");

    private final AgencyIDListObject agencyIDList;

    public EditAgencyIDListPageImpl(BasePage parent, AgencyIDListObject agencyIDList) {
        super(parent);
        this.agencyIDList = agencyIDList;
    }

    @Override
    protected String getPageUrl() {
        if (this.agencyIDList.getAgencyIDListManifestId()!=null){
            return getConfig().getBaseUrl().resolve("/agency_id_list/" + this.agencyIDList.getAgencyIDListManifestId()).toString();
        }else{
            return getConfig().getBaseUrl().resolve("/agency_id_list/" + this.agencyIDList.getAgencyIDListId()).toString();
        }
    }

    @Override
    public void openPage() {
        String url = getPageUrl();
        getDriver().get(url);
        invisibilityOfLoadingContainerElement(getDriver());
        assert getText(getTitle()).equals("Edit Agency ID List");
    }

    @Override
    public WebElement getTitle() {
        invisibilityOfLoadingContainerElement(getDriver());
        return visibilityOfElementLocated(PageHelper.wait(getDriver(), Duration.ofSeconds(10L), ofMillis(100L)),
                By.xpath("//mat-card-title/span[1]"));
    }

    @Override
    public void setName(String agencyIDListName) {
        sendKeys(getAgencyIDListNameField(), agencyIDListName);
    }

    @Override
    public WebElement getListIDField() {
        return visibilityOfElementLocated(getDriver(), LIST_ID_FIELD_LOCATOR);
    }

    @Override
    public WebElement getCoreComponentField() {
        return visibilityOfElementLocated(getDriver(), CORE_COMPONENT_FIELD_LOCATOR);
    }

    @Override
    public WebElement getGUIDField() {
        return visibilityOfElementLocated(getDriver(), GUID_FIELD_LOCATOR);
    }

    @Override
    public WebElement getReleaseField() {
        return visibilityOfElementLocated(getDriver(), RELEASE_FIELD_LOCATOR);
    }

    @Override
    public WebElement getRevisionField() {
        return visibilityOfElementLocated(getDriver(), REVISION_FIELD_LOCATOR);
    }

    @Override
    public WebElement getStateField() {
        return visibilityOfElementLocated(getDriver(), STATE_FIELD_LOCATOR);
    }

    @Override
    public WebElement getOwnerField() {
        return visibilityOfElementLocated(getDriver(), OWNER_FIELD_LOCATOR);
    }

    @Override
    public WebElement getAgencyIDListNameField() {
        return visibilityOfElementLocated(getDriver(), AGENCY_ID_LIST_NAME_FIELD_LOCATOR);
    }

    @Override
    public WebElement getAgencyIDListValueSelectField() {
        return visibilityOfElementLocated(getDriver(), AGENCY_ID_LIST_VALUE_SELECT_FIELD_LOCATOR);
    }

    @Override
    public void setAgencyIDListValue(AgencyIDListValueObject agencyIDListValue) {
        retry(() -> {
            click(getAgencyIDListValueSelectField());
            waitFor(ofSeconds(2L));
            WebElement optionField = visibilityOfElementLocated(getDriver(),
                    By.xpath("//span[contains(text(), \"" + agencyIDListValue.getName() + "\")]//ancestor::mat-option[1]"));
            click(optionField);
        });
    }

    @Override
    public WebElement getNamespaceSelectField() {
        return visibilityOfElementLocated(getDriver(), NAMESPACE_SELECT_FIELD_LOCATOR);
    }

    @Override
    public void setNamespace(NamespaceObject namespace) {
        retry(() -> {
            click(getNamespaceSelectField());
            waitFor(ofSeconds(2L));
            WebElement optionField = visibilityOfElementLocated(getDriver(),
                    By.xpath("//span[contains(text(), \"" + namespace.getUri() + "\")]//ancestor::mat-option[1]"));
            click(optionField);
        });
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
    public WebElement getDefinitionSourceField() {
        return visibilityOfElementLocated(getDriver(), DEFINITION_SOURCE_FIELD_LOCATOR);
    }

    @Override
    public void setDefinitionSource(String definitionSource) {
        sendKeys(getDefinitionSourceField(), definitionSource);
    }

    @Override
    public WebElement getUpdateButton(boolean enabled) {
        if (enabled) {
            return elementToBeClickable(getDriver(), UPDATE_BUTTON_LOCATOR);
        } else {
            return visibilityOfElementLocated(getDriver(), UPDATE_BUTTON_LOCATOR);
        }
    }

    @Override
    public void hitUpdateButton() {
        retry(() -> {
            click(getUpdateButton(true));
            waitFor(ofMillis(1000L));
        });
        invisibilityOfLoadingContainerElement(getDriver());
    }

    @Override
    public WebElement getDeleteButton(boolean enabled) {
        if (enabled) {
            return elementToBeClickable(getDriver(), DELETE_BUTTON_LOCATOR);
        } else {
            return visibilityOfElementLocated(getDriver(), DELETE_BUTTON_LOCATOR);
        }
    }

    @Override
    public WebElement getCommentButton() {
        return elementToBeClickable(getDriver(), COMMENT_BUTTON_LOCATOR);
    }

    @Override
    public AddCommentDialog openCommentDialog() {
        click(getCommentButton());
        AddCommentDialog addCodeListCommentDialog = new AddCommentDialogImpl(this);
        assert addCodeListCommentDialog.isOpened();
        return addCodeListCommentDialog;
    }

    @Override
    public WebElement getReviseButton() {
        return elementToBeClickable(getDriver(), REVISE_BUTTON_LOCATOR);
    }

    @Override
    public void revise() {
        click(getReviseButton());
        click(elementToBeClickable(getDriver(), CONTINUE_REVISE_BUTTON_IN_DIALOG_LOCATOR));
        invisibilityOfLoadingContainerElement(getDriver());
        assert "Revised".equals(getSnackBarMessage(getDriver()));
    }

    @Override
    public WebElement getCancelButton() {
        return elementToBeClickable(getDriver(), CANCEL_BUTTON_LOCATOR);
    }

    @Override
    public void cancel() {
        click(getCancelButton());
        click(elementToBeClickable(getDriver(), CONTINUE_CANCEL_BUTTON_IN_DIALOG_LOCATOR));
        invisibilityOfLoadingContainerElement(getDriver());
        assert "Canceled".equals(getSnackBarMessage(getDriver()));
    }

    @Override
    public WebElement getMoveToDraftButton() {
        return elementToBeClickable(getDriver(), MOVE_TO_DRAFT_BUTTON_LOCATOR);
    }

    @Override
    public void moveToDraft() {
        retry(() -> {
            click(getMoveToDraftButton());
            waitFor(ofMillis(1000L));
            click(elementToBeClickable(getDriver(), CONTINUE_TO_CHANGE_STATE_BUTTON_IN_DIALOG_LOCATOR));
        });
        invisibilityOfLoadingContainerElement(getDriver());
        waitFor(ofMillis(500L));
    }

    @Override
    public WebElement getMoveToCandidateButton() {
        return elementToBeClickable(getDriver(), MOVE_TO_CANDIDATE_BUTTON_LOCATOR);
    }

    @Override
    public void moveToCandidate() {
        retry(() -> {
            click(getMoveToCandidateButton());
            waitFor(ofMillis(1000L));
            click(elementToBeClickable(getDriver(), CONTINUE_TO_CHANGE_STATE_BUTTON_IN_DIALOG_LOCATOR));
        });
        invisibilityOfLoadingContainerElement(getDriver());
        waitFor(ofMillis(500L));
    }

    @Override
    public WebElement getMoveToQAButton() {
        return elementToBeClickable(getDriver(), MOVE_TO_QA_BUTTON_LOCATOR);
    }

    @Override
    public void moveToQA() {
        retry(() -> {
            click(getMoveToQAButton());
            waitFor(ofMillis(1000L));
            click(elementToBeClickable(getDriver(), CONTINUE_TO_CHANGE_STATE_BUTTON_IN_DIALOG_LOCATOR));
        });
        invisibilityOfLoadingContainerElement(getDriver());
        waitFor(ofMillis(500L));
    }

    @Override
    public WebElement getMoveToProductionButton() {
        return elementToBeClickable(getDriver(), MOVE_TO_PRODUCTION_BUTTON_LOCATOR);
    }

    @Override
    public void moveToProduction() {
        retry(() -> {
            click(getMoveToProductionButton());
            waitFor(ofMillis(1000L));
            click(elementToBeClickable(getDriver(), CONTINUE_TO_CHANGE_STATE_BUTTON_IN_DIALOG_LOCATOR));
        });
        invisibilityOfLoadingContainerElement(getDriver());
        waitFor(ofMillis(500L));
    }

    @Override
    public WebElement getBackToWIPButton() {
        return elementToBeClickable(getDriver(), BACK_TO_WIP_BUTTON_LOCATOR);
    }

    @Override
    public void backToWIP() {
        retry(() -> {
            click(getBackToWIPButton());
            waitFor(ofMillis(1000L));
            click(elementToBeClickable(getDriver(), CONTINUE_TO_CHANGE_STATE_BUTTON_IN_DIALOG_LOCATOR));
        });
        invisibilityOfLoadingContainerElement(getDriver());
        waitFor(ofMillis(500L));
    }

    @Override
    public void setVersion(String version) {
        sendKeys(getVersionField(), version);
    }

    @Override
    public WebElement getVersionField() {
        return visibilityOfElementLocated(getDriver(), VERSION_FIELD_LOCATOR);
    }

    @Override
    public WebElement getAddAgencyIDListValueButton() {
        return elementToBeClickable(getDriver(), ADD_AGENCY_ID_LIST_VALUE_BUTTON_LOCATOR);
    }

    @Override
    public EditAgencyIDListValueDialog addAgencyIDListValue() {
        click(getAddAgencyIDListValueButton());
        EditAgencyIDListValueDialog editAgencyIDListValueDialog = new EditAgencyIDListValueDialogImpl(this);
        assert editAgencyIDListValueDialog.isOpened();
        return editAgencyIDListValueDialog;
    }

    @Override
    public WebElement getRemoveAgencyIDListValueButton() {
        return elementToBeClickable(getDriver(), REMOVE_AGENCY_ID_LIST_VALUE_BUTTON_LOCATOR);
    }

    @Override
    public void hitRemoveAgencyIDListValueButton() {
        retry(() -> {
            click(getRemoveAgencyIDListValueButton());
            click(getDialogButtonByName(getDriver(), "Remove"));
            waitFor(ofMillis(1000L));
        });
        invisibilityOfLoadingContainerElement(getDriver());
    }

    @Override
    public EditAgencyIDListValueDialog openAgencyIDListValueDialogByValue(String value) {
        setSearch(value);
        hitSearchButton();

        return retry(() -> {
            WebElement tr;
            WebElement td;
            try {
                tr = getTableRecordAtIndex(1);
                td = getColumnByName(tr, "value");
            } catch (TimeoutException e) {
                throw new NoSuchElementException("Cannot locate an agency ID list value using " + value, e);
            }

            click(tr);
            EditAgencyIDListValueDialog editAgencyIDListValueDialog = new EditAgencyIDListValueDialogImpl(this);
            assert editAgencyIDListValueDialog.isOpened();
            return editAgencyIDListValueDialog;
        });
    }

    @Override
    public WebElement getSearchField() {
        return visibilityOfElementLocated(getDriver(), SEARCH_FIELD_LOCATOR);
    }

    @Override
    public void setSearch(String search) {
        sendKeys(getSearchField(), search);
    }

    @Override
    public WebElement getSearchButton() {
        return elementToBeClickable(getDriver(), SEARCH_BUTTON_LOCATOR);
    }

    @Override
    public void hitSearchButton() {
        try {
            click(getSearchButton());
        } catch (ElementClickInterceptedException e) {
            getSearchField().sendKeys(Keys.ENTER);
        }
        waitFor(ofMillis(500L));
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
    public void setItemsPerPage(int items) {
        WebElement itemsPerPageField = elementToBeClickable(getDriver(),
                By.xpath("//div[.=\" Items per page: \"]/following::div[5]"));
        click(itemsPerPageField);
        waitFor(ofMillis(500L));
        WebElement itemField = elementToBeClickable(getDriver(),
                By.xpath("//span[contains(text(), \"" + items + "\")]//ancestor::mat-option//div[1]//preceding-sibling::span"));
        click(itemField);
        waitFor(ofMillis(500L));
    }

    @Override
    public int getTotalNumberOfItems() {
        WebElement paginatorRangeLabelElement = visibilityOfElementLocated(getDriver(),
                By.xpath("//div[@class = \"mat-paginator-range-label\"]"));
        String paginatorRangeLabel = getText(paginatorRangeLabelElement);
        return Integer.valueOf(paginatorRangeLabel.substring(paginatorRangeLabel.indexOf("of") + 2).trim());
    }

    @Override
    public WebElement getPreviousPageButton() {
        return visibilityOfElementLocated(getDriver(), By.xpath(
                "//div[contains(@class, \"mat-paginator-range-actions\")]" +
                        "//button[@aria-label = \"Previous page\"]"));
    }

    @Override
    public WebElement getNextPageButton() {
        return visibilityOfElementLocated(getDriver(), By.xpath(
                "//div[contains(@class, \"mat-paginator-range-actions\")]" +
                        "//button[@aria-label = \"Next page\"]"));
    }

}

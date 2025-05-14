package org.oagi.score.e2e.impl.page.context;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.obj.ContextCategoryObject;
import org.oagi.score.e2e.page.context.EditContextCategoryPage;
import org.oagi.score.e2e.page.context.ViewEditContextCategoryPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.oagi.score.e2e.impl.PageHelper.*;

public class EditContextCategoryPageImpl extends BasePageImpl implements EditContextCategoryPage {

    private static final By NAME_FIELD_LOCATOR = By.xpath("//mat-label[contains(text(), \"Name\")]//ancestor::mat-form-field//input[1]");

    private static final By DESCRIPTION_FIELD_LOCATOR = By.xpath("//mat-label[contains(text(), \"Description\")]//ancestor::mat-form-field//textarea[1]");

    private static final By UPDATE_BUTTON_LOCATOR = By.xpath("//span[contains(text(), \"Update\")]//ancestor::button[1]");

    private static final By DISCARD_BUTTON_LOCATOR = By.xpath("//span[contains(text(), \"Discard\")]//ancestor::button[1]");

    private static final By DISCARD_BUTTON_IN_DIALOG_LOCATOR =
            By.xpath("//mat-dialog-container//span[contains(text(), \"Discard\")]//ancestor::button[1]");

    private final ViewEditContextCategoryPageImpl parent;

    private final ContextCategoryObject contextCategory;

    public EditContextCategoryPageImpl(ViewEditContextCategoryPageImpl parent,
                                       ContextCategoryObject contextCategory) {
        super(parent);
        this.parent = parent;
        this.contextCategory = contextCategory;
    }

    @Override
    protected String getPageUrl() {
        return getConfig().getBaseUrl().resolve("/context_management/context_category/" + this.contextCategory.getContextCategoryId()).toString();
    }

    @Override
    public void openPage() {
        String url = getPageUrl();
        getDriver().get(url);
        assert "Edit Context Category".equals(getText(getTitle()));
    }

    @Override
    public WebElement getTitle() {
        return visibilityOfElementLocated(getDriver(), By.className("title"));
    }

    @Override
    public WebElement getNameField() {
        return visibilityOfElementLocated(getDriver(), NAME_FIELD_LOCATOR);
    }

    @Override
    public void setName(String name) {
        sendKeys(getNameField(), name);
    }

    @Override
    public String getNameFieldText() {
        return getText(getNameField());
    }

    @Override
    public WebElement getDescriptionField() {
        return visibilityOfElementLocated(getDriver(), DESCRIPTION_FIELD_LOCATOR);
    }

    @Override
    public void setDescription(String description) {
        sendKeys(getDescriptionField(), description);
    }

    @Override
    public String getDescriptionFieldText() {
        return getText(getDescriptionField());
    }

    @Override
    public WebElement getUpdateButton() {
        return elementToBeClickable(getDriver(), UPDATE_BUTTON_LOCATOR);
    }

    @Override
    public void updateContextCategory(ContextCategoryObject contextCategory) {
        setName(contextCategory.getName());
        setDescription(contextCategory.getDescription());
        click(getDriver(), getUpdateButton());
        assert getSnackBar(getDriver(), "Updated").isEnabled();
    }

    @Override
    public WebElement getDiscardButton() {
        return elementToBeClickable(getDriver(), DISCARD_BUTTON_LOCATOR);
    }

    @Override
    public ViewEditContextCategoryPage discardContextCategory() {
        return retry(() -> {
            click(getDriver(), getDiscardButton());
            click(getDriver(), elementToBeClickable(getDriver(), DISCARD_BUTTON_IN_DIALOG_LOCATOR));
            assert getSnackBar(getDriver(), "Discarded").isDisplayed();
            return this.parent;
        });
    }
}

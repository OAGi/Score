package org.oagi.score.e2e.impl.page.context;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.obj.ContextCategoryObject;
import org.oagi.score.e2e.page.context.CreateContextCategoryPage;
import org.oagi.score.e2e.page.context.ViewEditContextCategoryPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.oagi.score.e2e.impl.PageHelper.*;

public class CreateContextCategoryPageImpl extends BasePageImpl implements CreateContextCategoryPage {

    private static final By NAME_FIELD_LOCATOR = By.xpath("//mat-label[contains(text(), \"Name\")]//ancestor::mat-form-field//input[1]");

    private static final By DESCRIPTION_FIELD_LOCATOR = By.xpath("//mat-label[contains(text(), \"Description\")]//ancestor::mat-form-field//textarea[1]");

    private static final By CREATE_BUTTON_LOCATOR = By.xpath("//span[contains(text(), \"Create\")]//ancestor::button[1]");

    private final ViewEditContextCategoryPageImpl parent;

    public CreateContextCategoryPageImpl(ViewEditContextCategoryPageImpl parent) {
        super(parent);
        this.parent = parent;
    }

    @Override
    protected String getPageUrl() {
        return getConfig().getBaseUrl().resolve("/context_management/context_category/create").toString();
    }

    @Override
    public void openPage() {
        String url = getPageUrl();
        getDriver().get(url);
        assert "Create Context Category".equals(getText(getTitle()));
    }

    @Override
    public WebElement getTitle() {
        return visibilityOfElementLocated(getDriver(), By.className("mat-card-title"));
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
    public WebElement getDescriptionField() {
        return visibilityOfElementLocated(getDriver(), DESCRIPTION_FIELD_LOCATOR);
    }

    @Override
    public void setDescription(String description) {
        sendKeys(getDescriptionField(), description);
    }

    @Override
    public WebElement getCreateButton() {
        return elementToBeClickable(getDriver(), CREATE_BUTTON_LOCATOR);
    }

    @Override
    public ViewEditContextCategoryPage createContextCategory(ContextCategoryObject contextCategory) {
        setName(contextCategory.getName());
        setDescription(contextCategory.getDescription());
        click(getCreateButton());
        assert getSnackBar(getDriver(), "Created").isDisplayed();
        return this.parent;
    }
}

package org.oagi.score.e2e.impl.page.admin;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.admin.TenantEditPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static java.time.Duration.ofMillis;
import static org.oagi.score.e2e.impl.PageHelper.*;

public class TenantEditPageImpl extends BasePageImpl implements TenantEditPage {

    private static final By NAME_FIELD_LOCATOR =
            By.xpath("//mat-label[normalize-space(.) = \"Name\"]//ancestor::mat-form-field[1]//input");

    private static final By UPDATE_BUTTON_LOCATOR =
            By.xpath("//score-detail-page//button[normalize-space(.) = \"Update\"]");

    private static final By DISCARD_BUTTON_LOCATOR =
            By.xpath("//score-detail-page//button[normalize-space(.) = \"Discard\"]");

    private static final By CONFIRM_DIALOG_LOCATOR = By.xpath("//score-confirm-dialog");

    public TenantEditPageImpl(BasePage parent) {
        super(parent);
    }

    @Override
    protected String getPageUrl() {
        return getConfig().getBaseUrl().resolve("/tenant/").toString();
    }

    @Override
    public void openPage() {
        throw new UnsupportedOperationException("Open it from the 'Tenant' page.");
    }

    @Override
    public WebElement getTitle() {
        return visibilityOfElementLocated(getDriver(), By.className("title"));
    }

    @Override
    public String getName() {
        return retry(() -> visibilityOfElementLocated(getDriver(), NAME_FIELD_LOCATOR).getAttribute("value"));
    }

    @Override
    public void setName(String name) {
        WebElement field = visibilityOfElementLocated(getDriver(), NAME_FIELD_LOCATOR);
        clear(field);
        sendKeys(field, name);
    }

    @Override
    public String update() {
        retry(() -> click(elementToBeClickable(getDriver(), UPDATE_BUTTON_LOCATOR)));
        return getSnackBarMessage(getDriver());
    }

    @Override
    public String openDiscardDialog() {
        retry(() -> click(elementToBeClickable(getDriver(), DISCARD_BUTTON_LOCATOR)));
        return getText(visibilityOfElementLocated(getDriver(), CONFIRM_DIALOG_LOCATOR));
    }

    @Override
    public String confirmDiscard() {
        click(getDialogButtonByName(getDriver(), "Discard"));
        return getSnackBarMessage(getDriver());
    }

    @Override
    public void cancelDiscard() {
        click(getDialogButtonByName(getDriver(), "Cancel"));
        invisibilityOfElementLocated(getDriver(), CONFIRM_DIALOG_LOCATOR);
        waitFor(ofMillis(300L));
    }

}

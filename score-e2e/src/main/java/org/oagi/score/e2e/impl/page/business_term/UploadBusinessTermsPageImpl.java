package org.oagi.score.e2e.impl.page.business_term;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.page.business_term.UploadBusinssTermsPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.oagi.score.e2e.impl.PageHelper.*;

public class UploadBusinessTermsPageImpl extends BasePageImpl implements UploadBusinssTermsPage {

    private static final By DOWNLOAD_TEMPLATE_BUTTON_LOCATOR = By.xpath("//span[contains(text(), \"Download template\")]//ancestor::button[1]");

    private static final By ATTACH_BUTTON_LOCATOR = By.xpath("//mat-icon[contains(text(), \"attach_file\")]//ancestor::button[1]");

    private final ViewEditBusinessTermPageImpl parent;

    public UploadBusinessTermsPageImpl(ViewEditBusinessTermPageImpl parent) {
        super(parent);
        this.parent = parent;
    }

    @Override
    protected String getPageUrl() {
        return getConfig().getBaseUrl().resolve("/business_term_management/business_term/upload").toString();
    }

    @Override
    public void openPage() {
        String url = getPageUrl();
        getDriver().get(url);
        assert "Upload Business Terms".equals(getText(getTitle()));
    }

    @Override
    public WebElement getTitle() {
        return visibilityOfElementLocated(getDriver(), By.className("title"));
    }

    @Override
    public WebElement getDownloadTemplateButton() {
        return elementToBeClickable(getDriver(), DOWNLOAD_TEMPLATE_BUTTON_LOCATOR);
    }

    @Override
    public WebElement getAttachButton() {
        return elementToBeClickable(getDriver(), ATTACH_BUTTON_LOCATOR);
    }
}

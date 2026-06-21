package org.oagi.score.e2e.impl.page.bie;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.bie.EditBIEPackagePage;
import org.oagi.score.e2e.page.bie.ViewBIEPackagePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Wait;

import java.math.BigInteger;

import static org.oagi.score.e2e.impl.PageHelper.*;

public class ViewBIEPackagePageImpl extends BasePageImpl implements ViewBIEPackagePage {

    private static final By NEW_BIE_PACKAGE_BUTTON_LOCATOR =
            By.xpath("//button[.//span[normalize-space(.) = 'New BIE Package'] or normalize-space(.) = 'New BIE Package']");

    public ViewBIEPackagePageImpl(BasePage parent) {
        super(parent);
    }

    @Override
    protected String getPageUrl() {
        return getConfig().getBaseUrl().resolve("/bie_package").toString();
    }

    @Override
    public void openPage() {
        String url = getPageUrl();
        getDriver().get(url);
        assert "BIE Package".equals(getText(getTitle()));
    }

    @Override
    public WebElement getTitle() {
        return visibilityOfElementLocated(getDriver(), By.className("title"));
    }

    @Override
    public WebElement getNewBIEPackageButton() {
        return elementToBeClickable(getDriver(), NEW_BIE_PACKAGE_BUTTON_LOCATOR);
    }

    @Override
    public EditBIEPackagePage hitNewBIEPackageButton() {
        retry(() -> click(getDriver(), getNewBIEPackageButton()));
        // Creation navigates to /bie_package/{newId}.
        Wait<WebDriver> wait = longWait(getDriver());
        wait.until(driver -> driver.getCurrentUrl().matches(".*/bie_package/\\d+$"));
        String currentUrl = getDriver().getCurrentUrl();
        BigInteger biePackageId = new BigInteger(currentUrl.substring(currentUrl.lastIndexOf('/') + 1));
        EditBIEPackagePageImpl editPage = new EditBIEPackagePageImpl(this, biePackageId);
        assert editPage.isOpened();
        return editPage;
    }
}

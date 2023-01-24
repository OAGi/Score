package org.oagi.score.e2e.impl.page.bie;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.obj.BusinessContextObject;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.bie.CopyBIEForSelectBIEPage;
import org.oagi.score.e2e.page.bie.CopyBIEForSelectBusinessContextsPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

import static org.oagi.score.e2e.impl.PageHelper.*;

public class CopyBIEForSelectBusinessContextsPageImpl extends BasePageImpl implements CopyBIEForSelectBusinessContextsPage {

    private static final By NAME_FIELD_LOCATOR =
            By.xpath("//span[contains(text(), \"Name\")]//ancestor::div[1]/input");

    private static final By SEARCH_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Search\")]//ancestor::button[1]");

    private static final By NEXT_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Next\")]//ancestor::button[1]");

    public CopyBIEForSelectBusinessContextsPageImpl(BasePage parent) {
        super(parent);
    }

    @Override
    protected String getPageUrl() {
        return getConfig().getBaseUrl().resolve("/profile_bie/copy").toString();
    }

    @Override
    public void openPage() {
        String url = getPageUrl();
        getDriver().get(url);
        assert getText(getTitle()).contains("Copy BIE");
        assert getText(getSubtitle()).contains("Select Business Contexts");
    }

    @Override
    public WebElement getTitle() {
        return visibilityOfElementLocated(getDriver(), By.className("mat-card-title"));
    }

    @Override
    public WebElement getSubtitle() {
        return visibilityOfElementLocated(getDriver(), By.className("mat-card-subtitle"));
    }

    @Override
    public WebElement getNameField() {
        return visibilityOfElementLocated(getDriver(), NAME_FIELD_LOCATOR);
    }

    @Override
    public WebElement getSearchButton() {
        return elementToBeClickable(getDriver(), SEARCH_BUTTON_LOCATOR);
    }

    @Override
    public WebElement getTableRecordAtIndex(int idx) {
        return visibilityOfElementLocated(getDriver(), By.xpath("//tbody/tr[" + idx + "]"));
    }

    @Override
    public WebElement getTableRecordByValue(String value) {
        return visibilityOfElementLocated(getDriver(), By.xpath("//td/a/span[contains(text(), \"" + value + "\")]/ancestor::tr"));
    }

    @Override
    public WebElement getColumnByName(WebElement tableRecord, String columnName) {
        return tableRecord.findElement(By.className("mat-column-" + columnName));
    }

    @Override
    public void selectBusinessContext(BusinessContextObject businessContext) {
        sendKeys(getNameField(), businessContext.getName());
        click(getSearchButton());
        retry(() -> {
            WebElement tr = getTableRecordByValue(businessContext.getName());
            WebElement td = getColumnByName(tr, "select");
            click(td.findElement(By.xpath("mat-checkbox/label/span[1]")));
        });
    }

    @Override
    public WebElement getNextButton() {
        return elementToBeClickable(getDriver(), NEXT_BUTTON_LOCATOR);
    }

    @Override
    public CopyBIEForSelectBIEPage next(List<BusinessContextObject> businessContexts) {
        for (BusinessContextObject businessContext : businessContexts) {
            selectBusinessContext(businessContext);
        }
        click(getNextButton());
        CopyBIEForSelectBIEPage copyBIEForSelectBIEPage =
                new CopyBIEForSelectBIEPageImpl(this, businessContexts);
        assert copyBIEForSelectBIEPage.isOpened();
        return copyBIEForSelectBIEPage;
    }

}

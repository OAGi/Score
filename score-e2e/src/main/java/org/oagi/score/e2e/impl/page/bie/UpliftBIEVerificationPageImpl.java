package org.oagi.score.e2e.impl.page.bie;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.bie.UpliftBIEVerificationPage;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

import static org.oagi.score.e2e.impl.PageHelper.*;

public class UpliftBIEVerificationPageImpl extends BasePageImpl implements UpliftBIEVerificationPage {
    private static final By NEXT_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Next\")]//ancestor::button[1]");
    private static final By SOURCE_SEARCH_INPUT_LOCATOR =
            By.xpath("//mat-card-content/div[2]/div[1]//mat-placeholder[contains(text(),\"Search\")]//ancestor::div[1]//input");
    private static final By TARGET_SEARCH_INPUT_LOCATOR =
            By.xpath("//mat-card-content/div[2]/div[2]//mat-placeholder[contains(text(),\"Search\")]//ancestor::div[1]//input");

    public UpliftBIEVerificationPageImpl(BasePage parent) {
        super(parent);
    }

    @Override
    protected String getPageUrl() {
        return getConfig().getBaseUrl().resolve("/profile_bie/uplift").toString();
    }

    @Override
    public void openPage() {
        String url = getPageUrl();
        getDriver().get(url);
        assert "Uplift BIE".equals(getText(getTitle()));
    }

    @Override
    public WebElement getTitle() {
        return visibilityOfElementLocated(getDriver(), By.className("mat-card-title"));
    }

    @Override
    public WebElement getNextButton() {
        return elementToBeClickable(getDriver(), NEXT_BUTTON_LOCATOR);
    }

    public void expandNodeInSourceBIE(String node){
        By chevronRightLocator = By.xpath("//mat-card-content/div[2]/div[1]//cdk-virtual-scroll-viewport//span[contains(text(),\""+node+"\")]//ancestor::div[1]/button/span/mat-icon[contains(text(),\"chevron_right\")]//ancestor::span[1]"
                );
        click(elementToBeClickable(getDriver(), chevronRightLocator));

    }
    public void expandNodeInTargetBIE(String node){
        By chevronRightLocator = By.xpath("//mat-card-content/div[2]/div[2]//cdk-virtual-scroll-viewport//span[contains(text(),\""+node+"\")]//ancestor::div[1]/button/span/mat-icon[contains(text(),\"chevron_right\")]//ancestor::span[1]"
        );
        click(elementToBeClickable(getDriver(), chevronRightLocator));
    }

    @Override
    public WebElement goToNodeInSourceBIE(String nodePath) {
        click(getSearchInputOfSourceTree());
        WebElement node = retry(() -> {
            WebElement e = sendKeys(getSearchInputOfSourceTree(), nodePath);
            if (!nodePath.equals(getText(getSearchInputOfSourceTree()))) {
                throw new WebDriverException();
            }
            return e;
        });
        node.sendKeys(Keys.ENTER);
        click(node);
        clear(getSearchInputOfSourceTree());
        return node;

    }
    @Override
    public WebElement goToNodeInTargetBIE(String nodePath) {
        click(getSearchInputOfTargetTree());
        WebElement node = retry(() -> {
            WebElement e = sendKeys(getSearchInputOfTargetTree(), nodePath);
            if (!nodePath.equals(getText(getSearchInputOfTargetTree()))) {
                throw new WebDriverException();
            }
            return e;
        });
        node.sendKeys(Keys.ENTER);
        click(node);
        clear(getSearchInputOfTargetTree());
        return node;
    }

    @Override
    public WebElement getSearchInputOfSourceTree() {
        return visibilityOfElementLocated(getDriver(), SOURCE_SEARCH_INPUT_LOCATOR);
    }

    @Override
    public WebElement getSearchInputOfTargetTree() {
        return visibilityOfElementLocated(getDriver(), TARGET_SEARCH_INPUT_LOCATOR);
    }
    @Override
    public WebElement getCheckBoxOfNodeInTargetBIE(String node) {
        return visibilityOfElementLocated(getDriver() ,By.xpath("//mat-card-content/div[2]/div[2]//cdk-virtual-scroll-viewport//*[contains(text(),\""+node+"\")]//ancestor::div[1]/mat-checkbox[1]/label/span[1]"));
    }

    @Override
    public void next() {
        click(getNextButton());
    }
}
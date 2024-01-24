package org.oagi.score.e2e.impl.page.bie;

import org.oagi.score.e2e.impl.PageHelper;
import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.obj.TopLevelASBIEPObject;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.bie.EditBIEPage;
import org.oagi.score.e2e.page.bie.SelectProfileBIEToReuseDialog;
import org.oagi.score.e2e.page.bie.UpliftBIEVerificationPage;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;

import java.math.BigInteger;

import static java.time.Duration.ofMillis;
import static java.time.Duration.ofSeconds;
import static org.oagi.score.e2e.impl.PageHelper.*;

public class UpliftBIEVerificationPageImpl extends BasePageImpl implements UpliftBIEVerificationPage {

    private final BasePage parent;

    private static final By NEXT_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Next\")]//ancestor::button[1]");

    private static final By UPLIFT_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Uplift\")]//ancestor::button[1]");

    private static final By SOURCE_SEARCH_INPUT_LOCATOR =
            By.xpath("//mat-card-content/div[2]/div[1]//mat-label[contains(text(), \"Search\")]//ancestor::div[1]//input");

    private static final By TARGET_SEARCH_INPUT_LOCATOR =
            By.xpath("//mat-card-content/div[2]/div[2]//mat-label[contains(text(), \"Search\")]//ancestor::div[1]//input");

    public UpliftBIEVerificationPageImpl(BasePage parent) {
        super(parent);
        this.parent = parent;
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
        return visibilityOfElementLocated(getDriver(), By.className("title"));
    }

    @Override
    public WebElement getNextButton() {
        return elementToBeClickable(getDriver(), NEXT_BUTTON_LOCATOR);
    }

    public void expandNodeInSourceBIE(String node) {
        By chevronRightLocator = By.xpath("//mat-card-content[contains(@class, \"mat-mdc-card-content\")]" +
                "/div[2]/div[1]//cdk-virtual-scroll-viewport//span[contains(text(), \"" + node + "\")]" +
                "//ancestor::div[1]//mat-icon[contains(text(), \"chevron_right\")]//ancestor::button[1]"
        );
        click(elementToBeClickable(getDriver(), chevronRightLocator));

    }

    public void expandNodeInTargetBIE(String node) {
        By chevronRightLocator = By.xpath("//mat-card-content[contains(@class, \"mat-mdc-card-content\")]" +
                "/div[2]/div[2]//cdk-virtual-scroll-viewport//span[contains(text(), \"" + node + "\")]" +
                "//ancestor::div[1]//mat-icon[contains(text(), \"chevron_right\")]//ancestor::button[1]"
        );
        click(elementToBeClickable(getDriver(), chevronRightLocator));
    }

    @Override
    public WebElement goToNodeInSourceBIE(String nodePath) {
        WebElement ele = getSearchInputOfSourceTree();
        click(getDriver(), ele);
        retry(() -> {
            WebElement e = sendKeys(ele, nodePath);
            if (!nodePath.equals(getText(ele))) {
                throw new WebDriverException();
            }
            e.sendKeys(Keys.ENTER);
            e.sendKeys(Keys.ENTER);
            e.sendKeys(Keys.ENTER);
            e.sendKeys(Keys.ENTER);
            e.sendKeys(Keys.ENTER);
            e.sendKeys(Keys.ENTER);
        });

        String[] nodes = nodePath.split("/");
        String nodeName = nodes[nodes.length - 1];

        WebElement node = retry(() -> visibilityOfElementLocated(getDriver(), By.xpath(
                "//mat-card-content[contains(@class, \"mat-mdc-card-content\")]" +
                        "/div[2]/div[1]//div[contains(@class, \"mat-tree-node\")]//*[contains(text(), \"" + nodeName + "\")]")));
        click(getDriver(), node);
        clear(getSearchInputOfSourceTree());
        return node;

    }

    @Override
    public WebElement goToNodeInTargetBIE(String nodePath) {
        WebElement ele = getSearchInputOfTargetTree();
        click(getDriver(), ele);
        retry(() -> {
            WebElement e = sendKeys(ele, nodePath);
            if (!nodePath.equals(getText(ele))) {
                throw new WebDriverException();
            }
            e.sendKeys(Keys.ENTER);
            e.sendKeys(Keys.ENTER);
            e.sendKeys(Keys.ENTER);
            e.sendKeys(Keys.ENTER);
            e.sendKeys(Keys.ENTER);
            e.sendKeys(Keys.ENTER);
        });

        String[] nodes = nodePath.split("/");
        String nodeName = nodes[nodes.length - 1];

        WebElement node = retry(() -> visibilityOfElementLocated(getDriver(), By.xpath(
                "//mat-card-content[contains(@class, \"mat-mdc-card-content\")]" +
                        "/div[2]/div[2]//div[contains(@class, \"mat-tree-node\")]//*[contains(text(), \"" + nodeName + "\")]")));

        click(getDriver(), node);
        clear(getSearchInputOfTargetTree());
        return node;
    }

    @Override
    public WebElement getSearchInputOfSourceTree() {
        return elementToBeClickable(getDriver(), SOURCE_SEARCH_INPUT_LOCATOR);
    }

    @Override
    public WebElement getSearchInputOfTargetTree() {
        return elementToBeClickable(getDriver(), TARGET_SEARCH_INPUT_LOCATOR);
    }

    @Override
    public WebElement getCheckBoxOfNodeInTargetBIE(String node) {
        return visibilityOfElementLocated(getDriver(), By.xpath("//mat-card-content[contains(@class, \"mat-mdc-card-content\")]" +
                "/div[2]/div[2]//cdk-virtual-scroll-viewport//*[contains(text(),\"" + node + "\")]//ancestor::div[1]/mat-checkbox[1]"));
    }

    @Override
    public SelectProfileBIEToReuseDialog reuseBIEOnNode(String path, String nodeName) {
        WebElement nodeInTargetBIE = goToNodeInTargetBIE(path);
        try {
            click(getReusedIconOfNodeInTargetBIE(nodeName));
        } catch (TimeoutException e) {
            click(nodeInTargetBIE);
            new Actions(getDriver()).sendKeys("O").perform();
            click(getReusedIconOfNodeInTargetBIE(nodeName));
        }
        waitFor(ofMillis(1000L));

        SelectProfileBIEToReuseDialog selectProfileBIEToReuse = new SelectProfileBIEToReuseDialogImpl(this, "Reuse BIE");
        assert selectProfileBIEToReuse.isOpened();
        return selectProfileBIEToReuse;
    }

    @Override
    public WebElement getReusedIconOfNodeInTargetBIE(String nodeName) {
        return visibilityOfElementLocated(getDriver(), By.xpath("//mat-card-content[contains(@class, \"mat-mdc-card-content\")]" +
                "/div[2]/div[2]//cdk-virtual-scroll-viewport//*[contains(text(),\"" + nodeName + "\")]//ancestor::div[1]//fa-icon[@mattooltip=\"Select BIE\"]"));
    }

    @Override
    public EditBIEPage uplift() {
        click(getNextButton());
        invisibilityOfLoadingContainerElement(PageHelper.wait(getDriver(), ofSeconds(900L), ofMillis(500L)));
        click(elementToBeClickable(getDriver(), UPLIFT_BUTTON_LOCATOR));
        invisibilityOfLoadingContainerElement(PageHelper.wait(getDriver(), ofSeconds(900L), ofMillis(500L)));
        waitFor(ofMillis(1000L));

        String currentUrl = getDriver().getCurrentUrl();
        BigInteger topLevelAsbiepId = new BigInteger(currentUrl.substring(
                currentUrl.indexOf("/profile_bie/") + "/profile_bie/".length()));
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .getTopLevelASBIEPByID(topLevelAsbiepId);

        EditBIEPageImpl editBIEPage = new EditBIEPageImpl(parent, topLevelASBIEP);
        editBIEPage.openPage();
        return editBIEPage;
    }
}
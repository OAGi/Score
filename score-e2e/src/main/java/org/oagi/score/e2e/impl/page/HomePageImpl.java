package org.oagi.score.e2e.impl.page;

import org.oagi.score.e2e.impl.menu.*;
import org.oagi.score.e2e.impl.page.bie.EditBIEPageImpl;
import org.oagi.score.e2e.impl.page.bie.ViewEditBIEPageImpl;
import org.oagi.score.e2e.impl.page.core_component.ViewEditCoreComponentPageImpl;
import org.oagi.score.e2e.menu.*;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.TopLevelASBIEPObject;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.LoginPage;
import org.oagi.score.e2e.page.bie.EditBIEPage;
import org.oagi.score.e2e.page.bie.ViewEditBIEPage;
import org.oagi.score.e2e.page.core_component.ViewEditCoreComponentPage;
import org.openqa.selenium.*;

import java.math.BigInteger;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static java.time.Duration.ofMillis;
import static org.oagi.score.e2e.impl.PageHelper.*;

public class HomePageImpl extends BasePageImpl implements HomePage {

    private static final By BRANCH_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(), \"Branch\")]//ancestor::mat-form-field[1]//mat-select");

    private static final By USER_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(), \"User\")]//ancestor::mat-form-field[1]//mat-select");

    private static final By DROPDOWN_SEARCH_FIELD_LOCATOR =
            By.xpath("//input[@aria-label=\"dropdown search\"]");

    private final AppUserObject user;

    public HomePageImpl(BasePageImpl parent, AppUserObject user) {
        super(parent);
        this.user = user;
    }

    public String getLoginID() {
        return this.user.getLoginId();
    }

    @Override
    protected String getPageUrl() {
        return getConfig().getBaseUrl().resolve("/logout").toString();
    }

    @Override
    public void openPage() {
        List<String> windowHandles = new ArrayList<>(getDriver().getWindowHandles());
        if (windowHandles.size() > 1) {
            getDriver().switchTo().window(windowHandles.get(1)).close();
            getDriver().switchTo().window(windowHandles.get(0));
        }

        String loginUrl = getConfig().getBaseUrl().toString();
        getDriver().get(loginUrl);
        assert getScoreLogo().isDisplayed();
    }

    @Override
    public WebElement getTitle() {
        return getScoreLogo();
    }

    @Override
    public WebElement getScoreLogo() {
        return visibilityOfElementLocated(defaultWait(getDriver()), By.id("logo"));
    }

    @Override
    public BIEMenu getBIEMenu() {
        return new BIEMenuImpl(this);
    }

    @Override
    public ContextMenu getContextMenu() {
        return new ContextMenuImpl(this);
    }

    @Override
    public CoreComponentMenu getCoreComponentMenu() {
        return new CoreComponentMenuImpl(this);
    }

    @Override
    public ModuleMenu getModuleMenu() {
        return new ModuleMenuImpl(this, user);
    }

    @Override
    public AdminMenu getAdminMenu() {
        return new AdminMenuImpl(this);
    }

    @Override
    public HelpMenu getHelpMenu() {
        return new HelpMenuImpl(this);
    }

    @Override
    public LoginIDMenu getLoginIDMenu() {
        return new LoginIDMenuImpl(this);
    }

    @Override
    public WebElement getNISTOAGiLogo() {
        return visibilityOfElementLocated(defaultWait(getDriver()), By.xpath("//score-web/score-navbar/mat-toolbar/mat-toolbar-row/a[2]"));
    }

    @Override
    public WebElement getBIEsTab() {
        return visibilityOfElementLocated(defaultWait(getDriver()), By.xpath("//div[contains(@class, \"mat-mdc-tab\")]//span[contains(text(), \"BIEs\")]"));
    }

    @Override
    public WebElement getUserExtensionsTab() {
        return visibilityOfElementLocated(defaultWait(getDriver()), By.xpath("//div[contains(@class, \"mat-mdc-tab\")]//span[contains(text(), \"User Extensions\")]"));
    }


    @Override
    public LoginPage logout() {
        String logoutUrl = getPageUrl();
        getDriver().get(logoutUrl);
        return new LoginPageImpl(this);
    }

    @Override
    public WebElement getBranchSelectField() {
        return elementToBeClickable(getDriver(), BRANCH_SELECT_FIELD_LOCATOR);
    }

    @Override
    public void setBranch(String branch) {
        retry(() -> {
            click(getDriver(), getBranchSelectField());
            sendKeys(visibilityOfElementLocated(getDriver(), DROPDOWN_SEARCH_FIELD_LOCATOR), branch);
            WebElement optionField = visibilityOfElementLocated(getDriver(),
                    By.xpath("//div[@class = \"cdk-overlay-container\"]//mat-option//span[text() = \"" + branch + "\"]"));
            click(getDriver(), optionField);
            escape(getDriver());
        });
    }

    @Override
    public TotalBIEsByStatesPanel openTotalBIEsByStatesPanel() {
        click(getBIEsTab());
        return new TotalBIEsByStatesPanelImpl(this);
    }

    @Override
    public MyBIEsByStatesPanel openMyBIEsByStatesPanel() {
        click(getBIEsTab());
        return new MyBIEsByStatesPanelImpl(this);
    }

    @Override
    public BIEsByUsersAndStatesPanel openBIEsByUsersAndStatesPanel() {
        click(getBIEsTab());
        return new BIEsByUsersAndStatesPanelImpl(this);
    }

    @Override
    public MyRecentBIEsPanelImpl openMyRecentBIEsPanel() {
        click(getBIEsTab());
        return new MyRecentBIEsPanelImpl(this);
    }

    @Override
    public TotalUEsByStatesPanel openTotalUEsByStatesPanel() {
        click(getUserExtensionsTab());
        return new TotalUEsByStatesPanelImpl(this);
    }

    @Override
    public MyUEsByStatesPanel openMyUEsByStatesPanel() {
        click(getUserExtensionsTab());
        return new MyUEsByStatesPanelImpl(this);
    }

    @Override
    public UEsByUsersAndStatesPanel openUEsByUsersAndStatesPanel() {
        click(getUserExtensionsTab());
        return new UEsByUsersAndStatesPanelImpl(this);
    }

    @Override
    public MyUnusedUEsInBIEsPanel openMyUnusedUEsInBIEsPanel() {
        click(getUserExtensionsTab());
        return new MyUnusedUEsInBIEsPanelImpl(this);
    }

    private class TotalBIEsByStatesPanelImpl implements TotalBIEsByStatesPanel {

        private BasePage parent;

        TotalBIEsByStatesPanelImpl(BasePage parent) {
            this.parent = parent;
        }

        @Override
        public WebElement getStateProgressBarByState(String state) {
            return visibilityOfElementLocated(defaultWait(getDriver()), By.xpath("//mat-tab-body/div[1]/div[1]/div[1]//div[contains(text(), \"" + state + "\")]"));
        }

        @Override
        public ViewEditBIEPage clickStateProgressBar(String state) {
            return retry(() -> {
                WebElement stateProgressBar = getStateProgressBarByState(state);
                click(stateProgressBar);

                waitFor(ofMillis(500L));
                ViewEditBIEPage viewEditBIEPage = new ViewEditBIEPageImpl(this.parent);
                assert viewEditBIEPage.isOpened();
                return viewEditBIEPage;
            });
        }
    }

    private class MyBIEsByStatesPanelImpl implements MyBIEsByStatesPanel {
        private BasePage parent;

        MyBIEsByStatesPanelImpl(BasePage parent) {
            this.parent = parent;
        }

        @Override
        public WebElement getStateProgressBarByState(String state) {
            return visibilityOfElementLocated(defaultWait(getDriver()), By.xpath("//mat-tab-body/div[1]/div[1]/div[2]//div[contains(text(), \"" + state + "\")]"));

        }

        @Override
        public ViewEditBIEPage clickStateProgressBar(String state) {
            WebElement stateProgressBar = getStateProgressBarByState(state);
            click(stateProgressBar);
            waitFor(ofMillis(500L));

            ViewEditBIEPage viewEditBIEPage = new ViewEditBIEPageImpl(this.parent);
            assert viewEditBIEPage.isOpened();
            return viewEditBIEPage;

        }
    }

    private class MyRecentBIEsPanelImpl implements MyRecentBIEsPanel {

        private BasePage parent;

        MyRecentBIEsPanelImpl(BasePage parent) {
            this.parent = parent;
        }

        @Override
        public List<WebElement> getTableRecords() {
            return visibilityOfAllElementsLocatedBy(getDriver(),
                    By.xpath("//div[contains(@class, \"my-recent-bies\")]//tbody/tr"));
        }

        @Override
        public WebElement getTableRecordAtIndex(int idx) {
            return visibilityOfElementLocated(getDriver(), By.xpath("//div[contains(@class, \"my-recent-bies\")]//tbody/tr[" + idx + "]"));
        }

        @Override
        public WebElement getTableRecordByValue(String value) {
            return visibilityOfElementLocated(getDriver(), By.xpath("//div[contains(@class, \"my-recent-bies\")]//td//*[contains(text(), \"" + value + "\")]/ancestor::tr"));
        }

        @Override
        public WebElement getColumnByName(WebElement tableRecord, String columnName) {
            return tableRecord.findElement(By.className("mat-column-" + columnName));
        }

        @Override
        public EditBIEPage openEditBIEPageByDEN(String den) {
            WebElement tr = getTableRecordByValue(den);
            WebElement td = getColumnByName(tr, "den");
            WebElement link = td.findElement(By.tagName("a"));

            String href = link.getAttribute("href");
            String topLevelAsbiepId = href.substring(href.indexOf("/profile_bie/") + "/profile_bie/".length());
            TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI().getTopLevelASBIEPByID(new BigInteger(topLevelAsbiepId));

            try {
                click(link);
            } catch (ElementClickInterceptedException e) {
                getDriver().get(href);
            }
            waitFor(ofMillis(500L));

            EditBIEPage editBIEPage = new EditBIEPageImpl(this.parent, topLevelASBIEP);
            assert editBIEPage.isOpened();
            return editBIEPage;
        }
    }

    private class BIEsByUsersAndStatesPanelImpl implements BIEsByUsersAndStatesPanel {

        private BasePage parent;

        BIEsByUsersAndStatesPanelImpl(BasePage parent) {
            this.parent = parent;
        }

        @Override
        public WebElement getUsernameSelectField() {
            return elementToBeClickable(getDriver(), USER_SELECT_FIELD_LOCATOR);
        }

        @Override
        public void setUsername(String username) {
            retry(() -> {
                click(getDriver(), getUsernameSelectField());
                sendKeys(visibilityOfElementLocated(getDriver(), DROPDOWN_SEARCH_FIELD_LOCATOR), username);
                WebElement searchedSelectField = visibilityOfElementLocated(getDriver(),
                        By.xpath("//mat-option//span[contains(text(), \"" + username + "\")]"));
                click(getDriver(), searchedSelectField);
                escape(getDriver());
            });
        }

        @Override
        public List<WebElement> getTableRecords() {
            return visibilityOfAllElementsLocatedBy(getDriver(),
                    By.xpath("//div[contains(@class, \"bies-by-users-and-states\")]//tbody/tr"));
        }

        @Override
        public WebElement getTableRecordAtIndex(int idx) {
            return visibilityOfElementLocated(getDriver(), By.xpath("//div[contains(@class, \"bies-by-users-and-states\")]//tbody/tr[" + idx + "]"));
        }

        @Override
        public WebElement getTableRecordByValue(String value) {
            return visibilityOfElementLocated(getDriver(), By.xpath("//div[contains(@class, \"bies-by-users-and-states\")]//*[contains(text(), \"" + value + "\")]/ancestor::tr"));
        }

        @Override
        public WebElement getColumnByName(WebElement tableRecord, String columnName) {
            return tableRecord.findElement(By.className("mat-column-" + columnName));
        }

        @Override
        public void setItemsPerPage(int items) {
            try {
                retry(() -> {
                    WebElement itemsPerPageField = elementToBeClickable(getDriver(),
                            By.xpath("//div[contains(@class, \"bies-by-users-and-states\")]//div[.=\" Items per page: \"]/following::mat-form-field//mat-select"));
                    click(getDriver(), itemsPerPageField);
                    waitFor(Duration.ofMillis(500L));
                    WebElement itemField = elementToBeClickable(getDriver(),
                            By.xpath("//div[contains(@class, \"bies-by-users-and-states\")]//span[contains(text(), \"" + items + "\")]//ancestor::mat-option//div[1]//preceding-sibling::span"));
                    click(getDriver(), itemField);
                    waitFor(Duration.ofMillis(500L));
                });
            } catch (WebDriverException e) {
                // ignore
            }
        }

        @Override
        public ViewEditBIEPage openViewEditBIEPageByUsernameAndColumnName(String user, String columnName) {
            WebElement tr = getTableRecordByValue(user);
            WebElement td = getColumnByName(tr, columnName);
            click(getDriver(), td.findElement(By.tagName("a")));
            waitFor(ofMillis(500L));

            ViewEditBIEPage viewEditBIEPage = new ViewEditBIEPageImpl(this.parent);
            assert viewEditBIEPage.isOpened();
            return viewEditBIEPage;
        }
    }

    private class TotalUEsByStatesPanelImpl implements TotalUEsByStatesPanel {

        private BasePage parent;

        TotalUEsByStatesPanelImpl(BasePage parent) {
            this.parent = parent;
        }

        @Override
        public WebElement getStateProgressBarByState(String state) {
            return visibilityOfElementLocated(defaultWait(getDriver()), By.xpath("//mat-tab-body/div[1]/div[1]/div[1]//div[contains(text(), \"" + state + "\")]"));
        }

        @Override
        public ViewEditCoreComponentPage clickStateProgressBar(String state) {
            return retry(() -> {
                WebElement stateProgressBar = getStateProgressBarByState(state);
                click(stateProgressBar);

                waitFor(ofMillis(500L));
                ViewEditCoreComponentPage viewEditCoreComponentPage = new ViewEditCoreComponentPageImpl(this.parent);
                assert viewEditCoreComponentPage.isOpened();
                return viewEditCoreComponentPage;
            });
        }
    }

    private class MyUEsByStatesPanelImpl implements MyUEsByStatesPanel {

        private BasePage parent;

        MyUEsByStatesPanelImpl(BasePage parent) {
            this.parent = parent;
        }

        @Override
        public WebElement getStateProgressBarByState(String state) {
            return visibilityOfElementLocated(defaultWait(getDriver()), By.xpath("//mat-tab-body/div[1]/div[1]/div[2]//div[contains(text(), \"" + state + "\")]"));
        }

        @Override
        public ViewEditCoreComponentPage clickStateProgressBar(String state) {
            return retry(() -> {
                WebElement stateProgressBar = getStateProgressBarByState(state);
                click(stateProgressBar);

                waitFor(ofMillis(500L));
                ViewEditCoreComponentPage viewEditCoreComponentPage = new ViewEditCoreComponentPageImpl(this.parent);
                assert viewEditCoreComponentPage.isOpened();
                return viewEditCoreComponentPage;
            });
        }
    }

    private class UEsByUsersAndStatesPanelImpl implements UEsByUsersAndStatesPanel {

        private BasePage parent;

        UEsByUsersAndStatesPanelImpl(BasePage parent) {
            this.parent = parent;
        }

        @Override
        public WebElement getUsernameSelectField() {
            return visibilityOfElementLocated(getDriver(), USER_SELECT_FIELD_LOCATOR);
        }

        @Override
        public void setUsername(String username) {
            retry(() -> {
                click(getUsernameSelectField());
                sendKeys(visibilityOfElementLocated(getDriver(), DROPDOWN_SEARCH_FIELD_LOCATOR), username);
                WebElement searchedSelectField = visibilityOfElementLocated(getDriver(),
                        By.xpath("//mat-option//span[contains(text(), \"" + username + "\")]"));
                click(searchedSelectField);
                escape(getDriver());
            });
        }

        @Override
        public List<WebElement> getTableRecords() {
            return visibilityOfAllElementsLocatedBy(getDriver(),
                    By.xpath("//div[contains(@class, \"cc-exts-by-users-and-states\")]//tbody/tr"));
        }

        @Override
        public WebElement getTableRecordAtIndex(int idx) {
            return visibilityOfElementLocated(getDriver(), By.xpath("//div[contains(@class, \"cc-exts-by-users-and-states\")]//tbody/tr[" + idx + "]"));
        }

        @Override
        public WebElement getTableRecordByValue(String value) {
            return visibilityOfElementLocated(getDriver(), By.xpath("//div[contains(@class, \"cc-exts-by-users-and-states\")]//*[contains(text(), \"" + value + "\")]/ancestor::tr"));
        }

        @Override
        public WebElement getColumnByName(WebElement tableRecord, String columnName) {
            return tableRecord.findElement(By.className("mat-column-" + columnName));
        }

        @Override
        public void setItemsPerPage(int items) {
            try {
                retry(() -> {
                    WebElement itemsPerPageField = elementToBeClickable(getDriver(),
                            By.xpath("//div[contains(@class, \"cc-exts-by-users-and-states\")]//div[.=\" Items per page: \"]/following::mat-form-field//mat-select"));
                    click(getDriver(), itemsPerPageField);
                    waitFor(Duration.ofMillis(500L));
                    WebElement itemField = elementToBeClickable(getDriver(),
                            By.xpath("//div[contains(@class, \"cc-exts-by-users-and-states\")]//span[contains(text(), \"" + items + "\")]//ancestor::mat-option//div[1]//preceding-sibling::span"));
                    click(getDriver(), itemField);
                    waitFor(Duration.ofMillis(500L));
                });
            } catch (WebDriverException e) {
                // ignore
            }
        }

        @Override
        public ViewEditCoreComponentPage openViewEditCCPageByUsernameAndColumnName(String user, String columnName) {
            WebElement tr = getTableRecordByValue(user);
            WebElement td = getColumnByName(tr, columnName);
            click(td.findElement(By.tagName("a")));
            waitFor(ofMillis(500L));

            ViewEditCoreComponentPage viewEditCoreComponentPage = new ViewEditCoreComponentPageImpl(this.parent);
            assert viewEditCoreComponentPage.isOpened();
            return viewEditCoreComponentPage;
        }
    }

    private class MyUnusedUEsInBIEsPanelImpl implements MyUnusedUEsInBIEsPanel {

        private BasePage parent;

        MyUnusedUEsInBIEsPanelImpl(BasePage parent) {
            this.parent = parent;
        }

        @Override
        public WebElement getTableRecordByUEAndDEN(String ueName, String assocDEN) {
            return visibilityOfElementLocated(getDriver(), By.xpath("//div[@class='ellipsis']//ancestor::tr//td[2]//span[contains(text(),\"" + ueName + "\")]"));
        }

        @Override
        public ViewEditCoreComponentPage openViewEditCCPageByUEAndDEN(String ueName, String assocDEN) {
            WebElement td = getTableRecordByUEAndDEN(ueName, assocDEN);
            click(td.findElement(By.tagName("a")));
            waitFor(ofMillis(500L));

            ViewEditCoreComponentPage viewEditCoreComponentPage = new ViewEditCoreComponentPageImpl(this.parent);
            assert viewEditCoreComponentPage.isOpened();
            return viewEditCoreComponentPage;
        }
    }

}

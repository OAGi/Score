package org.oagi.score.e2e.impl.page;

import org.oagi.score.e2e.impl.menu.*;
import org.oagi.score.e2e.impl.page.bie.EditBIEPageImpl;
import org.oagi.score.e2e.impl.page.bie.ViewEditBIEPageImpl;
import org.oagi.score.e2e.impl.page.core_component.ViewEditCoreComponentPageImpl;
import org.oagi.score.e2e.impl.page.message.MessageListPageImpl;
import org.oagi.score.e2e.menu.*;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.TopLevelASBIEPObject;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.LoginPage;
import org.oagi.score.e2e.page.bie.EditBIEPage;
import org.oagi.score.e2e.page.bie.ViewEditBIEPage;
import org.oagi.score.e2e.page.core_component.ViewEditCoreComponentPage;
import org.oagi.score.e2e.page.message.MessageListPage;
import org.openqa.selenium.*;

import java.math.BigInteger;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static java.time.Duration.ofMillis;
import static org.oagi.score.e2e.impl.PageHelper.*;

public class HomePageImpl extends BasePageImpl implements HomePage {

    private static final By LIBRARY_SELECT_FIELD_LOCATOR =
            By.xpath("//score-title-with-library-selector//button");

    private static final By BRANCH_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(), \"Branch\")]//ancestor::mat-form-field[1]//mat-select");

    private static final By USER_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(), \"User\")]//ancestor::mat-form-field[1]//mat-select");

    private static final By DROPDOWN_SEARCH_FIELD_LOCATOR =
            By.xpath("//input[@aria-label=\"dropdown search\"]");

    private static final By BROWSE_STANDARD_MENU_LOCATOR =
            By.xpath("//mat-toolbar-row/button/span[contains(text(), \"Browse Standard\")]//ancestor::button[1]");

    private static final By CONTEXT_MENU_LOCATOR =
            By.xpath("//mat-toolbar-row/button/span[contains(text(), \"Context\")]//ancestor::button[1]");

    private static final By CORE_COMPONENT_MENU_LOCATOR =
            By.xpath("//mat-toolbar-row/button/span[contains(text(), \"Core Component\")]//ancestor::button[1]");

    private static final By MODULE_MENU_LOCATOR =
            By.xpath("//mat-toolbar-row/button/span[contains(text(), \"Module\")]//ancestor::button[1]");

    private static final By LIBRARY_MENU_LOCATOR =
            By.xpath("//mat-toolbar-row/button/span[contains(text(), \"Library\")]//ancestor::button[1]");

    private static final By NOTIFICATION_ICON_LOCATOR =
            By.xpath("//mat-toolbar-row//mat-icon[contains(@class, \"notIcon\")]");

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
        assert getConnectCenterLogo().isDisplayed();
    }

    @Override
    public WebElement getTitle() {
        return getConnectCenterLogo();
    }

    @Override
    public WebElement getConnectCenterLogo() {
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
    public LibraryMenu getLibraryMenu() {
        return new LibraryMenuImpl(this);
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
    public WebElement getBrowseStandardMenu() {
        return elementToBeClickable(getDriver(), BROWSE_STANDARD_MENU_LOCATOR);
    }

    @Override
    public boolean hasBrowseStandardMenu() {
        return !getDriver().findElements(BROWSE_STANDARD_MENU_LOCATOR).isEmpty();
    }

    @Override
    public boolean hasContextMenu() {
        return !getDriver().findElements(CONTEXT_MENU_LOCATOR).isEmpty();
    }

    @Override
    public boolean hasCoreComponentMenu() {
        return !getDriver().findElements(CORE_COMPONENT_MENU_LOCATOR).isEmpty();
    }

    @Override
    public boolean hasModuleMenu() {
        return !getDriver().findElements(MODULE_MENU_LOCATOR).isEmpty();
    }

    @Override
    public boolean hasLibraryMenu() {
        return !getDriver().findElements(LIBRARY_MENU_LOCATOR).isEmpty();
    }

    @Override
    public ViewEditCoreComponentPage openBrowseStandardMenu() {
        retry(() -> click(getBrowseStandardMenu()));
        invisibilityOfLoadingContainerElement(getDriver());
        ViewEditCoreComponentPage viewEditCoreComponentPage = new ViewEditCoreComponentPageImpl(this);
        assert viewEditCoreComponentPage.isOpened();
        return viewEditCoreComponentPage;
    }

    @Override
    public WebElement getNotificationIcon() {
        return elementToBeClickable(getDriver(), NOTIFICATION_ICON_LOCATOR);
    }

    @Override
    public MessageListPage openMessageListPage() {
        retry(() -> click(getDriver(), getNotificationIcon()));
        invisibilityOfLoadingContainerElement(getDriver());
        MessageListPage messageListPage = new MessageListPageImpl(this);
        assert messageListPage.isOpened();
        return messageListPage;
    }


    @Override
    public LoginPage logout() {
        String logoutUrl = getPageUrl();
        getDriver().get(logoutUrl);
        return new LoginPageImpl(this);
    }

    @Override
    public WebElement getLibrarySelectorField() {
        return elementToBeClickable(getDriver(), LIBRARY_SELECT_FIELD_LOCATOR);
    }

    @Override
    public void setLibrary(String library) {
        retry(() -> {
            click(getDriver(), getLibrarySelectorField());
            WebElement optionField = visibilityOfElementLocated(getDriver(),
                    By.xpath("//div[contains(@class, \"cdk-overlay-container\")]//mat-radio-button" +
                            "//span[text() = \"" + library + "\"]//ancestor::mat-radio-button"));
            click(getDriver(), optionField);
            escape(getDriver());
        });
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
                    By.xpath("//mat-option//span[text() = \"" + branch + "\"]"));
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
                invisibilityOfLoadingContainerElement(getDriver());
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
            return retry(() -> {
                WebElement stateProgressBar = getStateProgressBarByState(state);
                click(stateProgressBar);
                invisibilityOfLoadingContainerElement(getDriver());

                ViewEditBIEPage viewEditBIEPage = new ViewEditBIEPageImpl(this.parent);
                assert viewEditBIEPage.isOpened();
                return viewEditBIEPage;
            });
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
            invisibilityOfLoadingContainerElement(getDriver());

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
            return retry(() -> {
                WebElement tr = getTableRecordByValue(user);
                WebElement td = getColumnByName(tr, columnName);
                click(getDriver(), td.findElement(By.tagName("a")));
                invisibilityOfLoadingContainerElement(getDriver());

                ViewEditBIEPage viewEditBIEPage = new ViewEditBIEPageImpl(this.parent);
                assert viewEditBIEPage.isOpened();
                return viewEditBIEPage;
            });
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
                invisibilityOfLoadingContainerElement(getDriver());
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
                invisibilityOfLoadingContainerElement(getDriver());
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
            return retry(() -> {
                WebElement tr = getTableRecordByValue(user);
                WebElement td = getColumnByName(tr, columnName);
                click(td.findElement(By.tagName("a")));
                invisibilityOfLoadingContainerElement(getDriver());

                ViewEditCoreComponentPage viewEditCoreComponentPage = new ViewEditCoreComponentPageImpl(this.parent);
                assert viewEditCoreComponentPage.isOpened();
                return viewEditCoreComponentPage;
            });
        }
    }

    private class MyUnusedUEsInBIEsPanelImpl implements MyUnusedUEsInBIEsPanel {

        private BasePage parent;

        MyUnusedUEsInBIEsPanelImpl(BasePage parent) {
            this.parent = parent;
        }

        @Override
        public WebElement getTableRecordByUEAndDEN(String ueName, String assocDEN) {
            return visibilityOfElementLocated(getDriver(), By.xpath(
                    "//p[normalize-space() = 'My unused extensions in BIEs']" +
                            "//ancestor::div[contains(@class, 'box')][1]" +
                            "//tr[td[2]//span[contains(normalize-space(), \"" + ueName + "\")]" +
                            " and td[6]//div[contains(normalize-space(), \"" + assocDEN + "\")]]"));
        }

        @Override
        public ViewEditCoreComponentPage openViewEditCCPageByUEAndDEN(String ueName, String assocDEN) {
            return retry(() -> {
                WebElement td = getTableRecordByUEAndDEN(ueName, assocDEN);
                click(td.findElement(By.tagName("a")));
                invisibilityOfLoadingContainerElement(getDriver());

                ViewEditCoreComponentPage viewEditCoreComponentPage = new ViewEditCoreComponentPageImpl(this.parent);
                assert viewEditCoreComponentPage.isOpened();
                return viewEditCoreComponentPage;
            });
        }
    }

}

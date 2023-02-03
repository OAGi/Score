package org.oagi.score.e2e.impl.menu;

import org.oagi.score.e2e.impl.page.DelegateBasePageImpl;
import org.oagi.score.e2e.impl.page.HomePageImpl;
import org.oagi.score.e2e.impl.page.bie.*;
import org.oagi.score.e2e.impl.page.business_term.BusinessTermAssignmentPageImpl;
import org.oagi.score.e2e.impl.page.business_term.ViewEditBusinessTermPageImpl;
import org.oagi.score.e2e.impl.page.code_list.UpliftCodeListPageImpl;
import org.oagi.score.e2e.impl.page.code_list.ViewEditCodeListPageImpl;
import org.oagi.score.e2e.menu.BIEMenu;
import org.oagi.score.e2e.page.bie.*;
import org.oagi.score.e2e.page.business_term.BusinessTermAssignmentPage;
import org.oagi.score.e2e.page.business_term.ViewEditBusinessTermPage;
import org.oagi.score.e2e.page.code_list.UpliftCodeListPage;
import org.oagi.score.e2e.page.code_list.ViewEditCodeListPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.Collections;
import java.util.List;

import static org.oagi.score.e2e.impl.PageHelper.*;

public class BIEMenuImpl extends DelegateBasePageImpl implements BIEMenu {

    private final By BIE_MENU_LOCATOR =
            By.xpath("//mat-toolbar-row/button/span[contains(text(), \"BIE\")]//ancestor::button[1]");

    private final By VIEW_EDIT_BIE_SUB_MENU_LOCATOR =
            By.xpath("//button[contains(text(), \"View/Edit BIE\")]");

    private final By CREATE_BIE_SUB_MENU_LOCATOR =
            By.xpath("//button[contains(text(), \"Create BIE\")]");

    private final By COPY_BIE_SUB_MENU_LOCATOR =
            By.xpath("//button[contains(text(), \"Copy BIE\")]");

    private final By UPLIFT_BIE_SUB_MENU_LOCATOR =
            By.xpath("//button[contains(text(), \"Uplift BIE\")]");

    private final By EXPRESS_BIE_SUB_MENU_LOCATOR =
            By.xpath("//button[contains(text(), \"Express BIE\")]");

    private final By REUSE_REPORT_SUB_MENU_LOCATOR =
            By.xpath("//button[contains(text(), \"Reuse Report\")]");

    private final By VIEW_EDIT_BUSINESS_TERM_SUB_MENU_LOCATOR =
            By.xpath("//button[contains(text(), \"View/Edit Business Term\")]");

    private final By BUSINESS_TERM_ASSIGNMENT_SUB_MENU_LOCATOR =
            By.xpath("//button[contains(text(), \"Business Term Assignment\")]");

    private final By VIEW_EDIT_CODE_LIST_SUB_MENU_LOCATOR =
            By.xpath("//button[contains(text(), \"View/Edit Code List\")]");

    private final By UPLIFT_CODE_LIST_SUB_MENU_LOCATOR =
            By.xpath("//button[contains(text(), \"Uplift Code List\")]");

    public BIEMenuImpl(HomePageImpl homePage) {
        super(homePage);
    }

    private boolean isExpanded() {
        return retry(() -> elementToBeClickable(shortWait(getDriver()), VIEW_EDIT_BIE_SUB_MENU_LOCATOR).isEnabled(), false);
    }

    @Override
    public WebElement getBIEMenu() {
        return elementToBeClickable(getDriver(), BIE_MENU_LOCATOR);
    }

    @Override
    public void expandBIEMenu() {
        click(getBIEMenu());
        assert getViewEditBIESubMenu().isEnabled();
    }

    @Override
    public WebElement getViewEditBIESubMenu() {
        if (!isExpanded()) {
            expandBIEMenu();
        }
        return elementToBeClickable(getDriver(), VIEW_EDIT_BIE_SUB_MENU_LOCATOR);
    }

    @Override
    public ViewEditBIEPage openViewEditBIESubMenu() {
        retry(() -> click(getViewEditBIESubMenu()));
        ViewEditBIEPage viewEditBIEPage = new ViewEditBIEPageImpl(this);
        assert viewEditBIEPage.isOpened();
        return viewEditBIEPage;
    }

    @Override
    public WebElement getCreateBIESubMenu() {
        if (!isExpanded()) {
            expandBIEMenu();
        }
        return elementToBeClickable(getDriver(), CREATE_BIE_SUB_MENU_LOCATOR);
    }

    @Override
    public CreateBIEForSelectBusinessContextsPage openCreateBIESubMenu() {
        retry(() -> click(getCreateBIESubMenu()));
        CreateBIEForSelectBusinessContextsPageImpl createBIEPage = new CreateBIEForSelectBusinessContextsPageImpl(this);
        assert createBIEPage.isOpened();
        return createBIEPage;
    }

    @Override
    public WebElement getCopyBIESubMenu() {
        if (!isExpanded()) {
            expandBIEMenu();
        }
        return elementToBeClickable(getDriver(), COPY_BIE_SUB_MENU_LOCATOR);
    }

    @Override
    public CopyBIEForSelectBusinessContextsPage openCopyBIESubMenu() {
        retry(() -> click(getCopyBIESubMenu()));
        CopyBIEForSelectBusinessContextsPage copyBIEForSelectBusinessContextsPage = new CopyBIEForSelectBusinessContextsPageImpl(this);
        assert copyBIEForSelectBusinessContextsPage.isOpened();
        return copyBIEForSelectBusinessContextsPage;
    }

    @Override
    public WebElement getUpliftBIESubMenu() {
        if (!isExpanded()) {
            expandBIEMenu();
        }
        return elementToBeClickable(getDriver(), UPLIFT_BIE_SUB_MENU_LOCATOR);
    }

    @Override
    public UpliftBIEPage openUpliftBIESubMenu() {
        retry(() -> click(getUpliftBIESubMenu()));
        UpliftBIEPage upliftBIEPage = new UpliftBIEPageImpl(this);
        assert upliftBIEPage.isOpened();
        return upliftBIEPage;
    }

    @Override
    public WebElement getExpressBIESubMenu() {
        if (!isExpanded()) {
            expandBIEMenu();
        }
        return elementToBeClickable(getDriver(), EXPRESS_BIE_SUB_MENU_LOCATOR);
    }

    @Override
    public ExpressBIEPage openExpressBIESubMenu() {
        retry(() -> click(getExpressBIESubMenu()));
        ExpressBIEPage expressBIEPage = new ExpressBIEPageImpl(this);
        assert expressBIEPage.isOpened();
        return expressBIEPage;
    }

    @Override
    public WebElement getReuseReportSubMenu() {
        if (!isExpanded()) {
            expandBIEMenu();
        }
        return elementToBeClickable(getDriver(), REUSE_REPORT_SUB_MENU_LOCATOR);
    }

    @Override
    public ReuseReportPage openReuseReportSubMenu() {
        retry(() -> click(getReuseReportSubMenu()));
        ReuseReportPage reuseReportPage = new ReuseReportPageImpl(this);
        assert reuseReportPage.isOpened();
        return reuseReportPage;
    }

    @Override
    public WebElement getViewEditBusinessTermSubMenu() {
        if (!isExpanded()) {
            expandBIEMenu();
        }
        return elementToBeClickable(getDriver(), VIEW_EDIT_BUSINESS_TERM_SUB_MENU_LOCATOR);
    }

    @Override
    public ViewEditBusinessTermPage openViewEditBusinessTermSubMenu() {
        retry(() -> click(getViewEditBusinessTermSubMenu()));
        ViewEditBusinessTermPage viewEditBusinessTermPage = new ViewEditBusinessTermPageImpl(this);
        assert viewEditBusinessTermPage.isOpened();
        return viewEditBusinessTermPage;
    }

    @Override
    public WebElement getBusinessTermAssignmentSubMenu() {
        if (!isExpanded()) {
            expandBIEMenu();
        }
        return elementToBeClickable(getDriver(), BUSINESS_TERM_ASSIGNMENT_SUB_MENU_LOCATOR);
    }

    @Override
    public BusinessTermAssignmentPage openBusinessTermAssignmentSubMenu() {
        retry(() -> click(getBusinessTermAssignmentSubMenu()));
        List<String> emptyBIEType = Collections.<String>emptyList();
        BusinessTermAssignmentPage businessTermAssignmentPage = new BusinessTermAssignmentPageImpl(this, emptyBIEType, null);
        return businessTermAssignmentPage;
    }

    @Override
    public WebElement getViewEditCodeListSubMenu() {
        if (!isExpanded()) {
            expandBIEMenu();
        }
        return elementToBeClickable(getDriver(), VIEW_EDIT_CODE_LIST_SUB_MENU_LOCATOR);
    }

    @Override
    public ViewEditCodeListPage openViewEditCodeListSubMenu() {
        retry(() -> click(getViewEditCodeListSubMenu()));
        ViewEditCodeListPage viewEditCodeListPage = new ViewEditCodeListPageImpl(this);
        assert viewEditCodeListPage.isOpened();
        return viewEditCodeListPage;
    }

    @Override
    public WebElement getUpliftCodeListSubMenu(boolean clickable) {
        if (!isExpanded()) {
            expandBIEMenu();
        }
        By locator = UPLIFT_CODE_LIST_SUB_MENU_LOCATOR;
        return (clickable) ? elementToBeClickable(getDriver(), locator) : visibilityOfElementLocated(getDriver(), locator);
    }

    @Override
    public UpliftCodeListPage openUpliftCodeListSubMenu() {
        retry(() -> click(getUpliftCodeListSubMenu(true)));
        UpliftCodeListPage upliftCodeListPage = new UpliftCodeListPageImpl(this);
        assert upliftCodeListPage.isOpened();
        return upliftCodeListPage;
    }

    @Override
    public String getBIEMenuButtonTitle() {
        return getBIEMenu().getAttribute("Title");
    }

    @Override
    public String getCreateBIESubMenuButtonTitle() {
        return getCreateBIESubMenu().getAttribute("Title");
    }

    @Override
    public String getViewEditBIESubMenuButtonTitle() {
        return getViewEditBIESubMenu().getAttribute("Title");
    }

    @Override
    public String getCopyBIESubMenuButtonTitle() {
        return getCopyBIESubMenu().getAttribute("Title");
    }

    @Override
    public String getGenerateExpressionButtonTitle() {
        return getExpressBIESubMenu().getAttribute("Title");
    }
}



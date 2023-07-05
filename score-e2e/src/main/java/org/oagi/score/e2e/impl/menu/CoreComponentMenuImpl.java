package org.oagi.score.e2e.impl.menu;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.impl.page.DelegateBasePageImpl;
import org.oagi.score.e2e.impl.page.agency_id_list.ViewEditAgencyIDListPageImpl;
import org.oagi.score.e2e.impl.page.code_list.ViewEditCodeListPageImpl;
import org.oagi.score.e2e.impl.page.core_component.ViewEditCoreComponentPageImpl;
import org.oagi.score.e2e.impl.page.namespace.ViewEditNamespacePageImpl;
import org.oagi.score.e2e.impl.page.release.ViewEditReleasePageImpl;
import org.oagi.score.e2e.menu.CoreComponentMenu;
import org.oagi.score.e2e.page.agency_id_list.ViewEditAgencyIDListPage;
import org.oagi.score.e2e.page.code_list.ViewEditCodeListPage;
import org.oagi.score.e2e.page.core_component.ViewEditCoreComponentPage;
import org.oagi.score.e2e.page.namespace.ViewEditNamespacePage;
import org.oagi.score.e2e.page.release.ViewEditReleasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.time.Duration;

import static org.oagi.score.e2e.impl.PageHelper.*;

public class CoreComponentMenuImpl extends DelegateBasePageImpl implements CoreComponentMenu {

    private final By CORE_COMPONENT_MENU_LOCATOR =
            By.xpath("//mat-toolbar-row/button/span[contains(text(), \"Core Component\")]//ancestor::button[1]");

    private final By VIEW_EDIT_CORE_COMPONENT_SUB_MENU_LOCATOR =
            By.xpath("//button[contains(text(), \"View/Edit Core Component\")]");

    private final By VIEW_EDIT_CODE_LIST_SUB_MENU_LOCATOR =
            By.xpath("//button[contains(text(), \"View/Edit Code List\")]");

    private final By VIEW_EDIT_AGENCY_ID_LIST_SUB_MENU_LOCATOR =
            By.xpath("//button[contains(text(), \"View/Edit Agency ID List\")]");

    private final By VIEW_EDIT_RELEASE_SUB_MENU_LOCATOR =
            By.xpath("//button[contains(text(), \"View/Edit Release\")]");

    private final By VIEW_EDIT_NAMESPACE_SUB_MENU_LOCATOR =
            By.xpath("//button[contains(text(), \"View/Edit Namespace\")]");

    public CoreComponentMenuImpl(BasePageImpl basePageImpl) {
        super(basePageImpl);
    }

    private boolean isExpanded() {
        return retry(() -> elementToBeClickable(shortWait(getDriver()), VIEW_EDIT_CORE_COMPONENT_SUB_MENU_LOCATOR).isEnabled(), false);
    }

    @Override
    public WebElement getCoreComponentMenu() {
        return elementToBeClickable(getDriver(), CORE_COMPONENT_MENU_LOCATOR);
    }

    @Override
    public void expandCoreComponentMenu() {
        click(getCoreComponentMenu());
        assert getViewEditCoreComponentSubMenu().isEnabled();
    }

    @Override
    public WebElement getViewEditCoreComponentSubMenu() {
        if (!isExpanded()) {
            expandCoreComponentMenu();
        }
        return elementToBeClickable(getDriver(), VIEW_EDIT_CORE_COMPONENT_SUB_MENU_LOCATOR);
    }

    @Override
    public ViewEditCoreComponentPage openViewEditCoreComponentSubMenu() {
        retry(() -> click(getViewEditCoreComponentSubMenu()));
        invisibilityOfLoadingContainerElement(getDriver());
        ViewEditCoreComponentPage viewEditCoreComponentPage = new ViewEditCoreComponentPageImpl(this);
        assert viewEditCoreComponentPage.isOpened();
        return viewEditCoreComponentPage;
    }

    @Override
    public WebElement getViewEditCodeListSubMenu() {
        if (!isExpanded()) {
            expandCoreComponentMenu();
        }
        return elementToBeClickable(getDriver(), VIEW_EDIT_CODE_LIST_SUB_MENU_LOCATOR);
    }

    @Override
    public ViewEditCodeListPage openViewEditCodeListSubMenu() {
        retry(() -> click(getViewEditCodeListSubMenu()));
        ViewEditCodeListPage viewEditCodeListPage = new ViewEditCodeListPageImpl(this);
        waitFor(Duration.ofMillis(2000));
        assert viewEditCodeListPage.isOpened();
        return viewEditCodeListPage;
    }

    @Override
    public WebElement getViewEditAgencyIDListSubMenu() {
        if (!isExpanded()) {
            expandCoreComponentMenu();
        }
        return elementToBeClickable(getDriver(), VIEW_EDIT_AGENCY_ID_LIST_SUB_MENU_LOCATOR);
    }

    @Override
    public ViewEditAgencyIDListPage openViewEditAgencyIDListSubMenu() {
        retry(() -> click(getViewEditAgencyIDListSubMenu()));
        ViewEditAgencyIDListPage viewEditAgencyIDListPage = new ViewEditAgencyIDListPageImpl(this);
        assert viewEditAgencyIDListPage.isOpened();
        return viewEditAgencyIDListPage;
    }

    @Override
    public WebElement getViewEditReleaseSubMenu() {
        if (!isExpanded()) {
            expandCoreComponentMenu();
        }
        return elementToBeClickable(getDriver(), VIEW_EDIT_RELEASE_SUB_MENU_LOCATOR);
    }

    @Override
    public ViewEditReleasePage openViewEditReleaseSubMenu() {
        retry(() -> click(getViewEditReleaseSubMenu()));
        ViewEditReleasePage viewEditReleasePage = new ViewEditReleasePageImpl(this);
        assert viewEditReleasePage.isOpened();
        return viewEditReleasePage;
    }

    @Override
    public WebElement getViewEditNamespaceSubMenu() {
        if (!isExpanded()) {
            expandCoreComponentMenu();
        }
        return elementToBeClickable(getDriver(), VIEW_EDIT_NAMESPACE_SUB_MENU_LOCATOR);
    }

    @Override
    public ViewEditNamespacePage openViewEditNamespaceSubMenu() {
        retry(() -> click(getViewEditNamespaceSubMenu()));
        ViewEditNamespacePage viewEditNamespacePage = new ViewEditNamespacePageImpl(this);
        assert viewEditNamespacePage.isOpened();
        return viewEditNamespacePage;
    }

    @Override
    public String getCoreComponentMenuButtonTitle() {
        return getCoreComponentMenu().getAttribute("Title");
    }
}

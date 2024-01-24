package org.oagi.score.e2e.impl.menu;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.impl.page.DelegateBasePageImpl;
import org.oagi.score.e2e.impl.page.module.ViewEditModuleSetPageImpl;
import org.oagi.score.e2e.impl.page.module.ViewEditModuleSetReleasePageImpl;
import org.oagi.score.e2e.menu.ModuleMenu;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.page.module.ViewEditModuleSetPage;
import org.oagi.score.e2e.page.module.ViewEditModuleSetReleasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.oagi.score.e2e.impl.PageHelper.*;

public class ModuleMenuImpl extends DelegateBasePageImpl implements ModuleMenu {

    private final By MODULE_MENU_LOCATOR =
            By.xpath("//mat-toolbar-row/button/span[contains(text(), \"Module\")]//ancestor::button[1]");

    private final AppUserObject user;

    public ModuleMenuImpl(BasePageImpl basePageImpl, AppUserObject user) {
        super(basePageImpl);
        this.user = user;
    }

    private By VIEW_EDIT_MODULE_SET_SUB_MENU_LOCATOR() {
        String menuName = this.user.isDeveloper() ? "View/Edit Module Set" : "View Module Set";
        return By.xpath("//span[contains(text(), \"" + menuName + "\")]");
    }

    private By VIEW_EDIT_MODULE_SET_RELEASE_SUB_MENU_LOCATOR() {
        String menuName = this.user.isDeveloper() ? "View/Edit Module Set Release" : "View Module Set Release";
        return By.xpath("//span[contains(text(), \"" + menuName + "\")]");
    }

    private boolean isExpanded() {
        return retry(() -> elementToBeClickable(shortWait(getDriver()), VIEW_EDIT_MODULE_SET_SUB_MENU_LOCATOR()).isEnabled(), false);
    }

    @Override
    public WebElement getModuleMenu() {
        return elementToBeClickable(getDriver(), MODULE_MENU_LOCATOR);
    }

    @Override
    public void expandModuleMenu() {
        click(getModuleMenu());
        assert getViewEditModuleSetSubMenu().isEnabled();
    }

    @Override
    public WebElement getViewEditModuleSetSubMenu() {
        if (!isExpanded()) {
            expandModuleMenu();
        }
        return elementToBeClickable(getDriver(), VIEW_EDIT_MODULE_SET_SUB_MENU_LOCATOR());
    }

    @Override
    public ViewEditModuleSetPage openViewEditModuleSetSubMenu() {
        retry(() -> click(getViewEditModuleSetSubMenu()));
        ViewEditModuleSetPage viewEditModuleSetPage = new ViewEditModuleSetPageImpl(this);
        assert viewEditModuleSetPage.isOpened();
        return viewEditModuleSetPage;
    }

    @Override
    public WebElement getViewEditModuleSetReleaseSubMenu() {
        if (!isExpanded()) {
            expandModuleMenu();
        }
        return elementToBeClickable(getDriver(), VIEW_EDIT_MODULE_SET_RELEASE_SUB_MENU_LOCATOR());
    }

    @Override
    public ViewEditModuleSetReleasePage openViewEditModuleSetReleaseSubMenu() {
        retry(() -> click(getViewEditModuleSetReleaseSubMenu()));
        ViewEditModuleSetReleasePage viewEditModuleSetReleasePage = new ViewEditModuleSetReleasePageImpl(this);
        assert viewEditModuleSetReleasePage.isOpened();
        return viewEditModuleSetReleasePage;
    }
}

package org.oagi.score.e2e.impl.menu;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.impl.page.DelegateBasePageImpl;
import org.oagi.score.e2e.impl.page.library.ViewLibraryPageImpl;
import org.oagi.score.e2e.menu.LibraryMenu;
import org.oagi.score.e2e.page.library.ViewLibraryPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.oagi.score.e2e.impl.PageHelper.*;

public class LibraryMenuImpl extends DelegateBasePageImpl implements LibraryMenu {

    private final By LIBRARY_MENU_LOCATOR =
            By.xpath("//mat-toolbar-row/button/span[contains(text(), \"Library\")]//ancestor::button[1]");

    private final By VIEW_LIBRARY_SUB_MENU_LOCATOR =
            By.xpath("//span[contains(text(), \"View Library\")]//ancestor::button[1]");

    public LibraryMenuImpl(BasePageImpl basePageImpl) {
        super(basePageImpl);
    }

    private boolean isExpanded() {
        return retry(() -> elementToBeClickable(shortWait(getDriver()), VIEW_LIBRARY_SUB_MENU_LOCATOR).isEnabled(), false);
    }

    @Override
    public WebElement getLibraryMenu() {
        return elementToBeClickable(getDriver(), LIBRARY_MENU_LOCATOR);
    }

    @Override
    public void expandLibraryMenu() {
        click(getLibraryMenu());
        assert getViewLibrarySubMenu().isEnabled();
    }

    @Override
    public WebElement getViewLibrarySubMenu() {
        if (!isExpanded()) {
            expandLibraryMenu();
        }
        return elementToBeClickable(getDriver(), VIEW_LIBRARY_SUB_MENU_LOCATOR);
    }

    @Override
    public ViewLibraryPage openViewLibrarySubMenu() {
        retry(() -> click(getViewLibrarySubMenu()));
        ViewLibraryPage viewLibraryPage = new ViewLibraryPageImpl(this);
        assert viewLibraryPage.isOpened();
        return viewLibraryPage;
    }
}

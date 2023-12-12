package org.oagi.score.e2e.impl.menu;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.impl.page.DelegateBasePageImpl;
import org.oagi.score.e2e.impl.page.help.AboutPageImpl;
import org.oagi.score.e2e.impl.page.help.UserGuidePageImpl;
import org.oagi.score.e2e.menu.HelpMenu;
import org.oagi.score.e2e.page.help.AboutPage;
import org.oagi.score.e2e.page.help.UserGuidePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.oagi.score.e2e.impl.PageHelper.*;

public class HelpMenuImpl extends DelegateBasePageImpl implements HelpMenu {

    private final By HELP_MENU_LOCATOR =
            By.xpath("//mat-toolbar-row/button/span[contains(text(), \"Help\")]//ancestor::button[1]");

    private final By ABOUT_SUB_MENU_LOCATOR =
            By.xpath("//button/span[contains(text(), \"About\")]");

    private final By USER_GUIDE_SUB_MENU_LOCATOR =
            By.xpath("//button/span[contains(text(), \"User Guide\")]");

    public HelpMenuImpl(BasePageImpl basePageImpl) {
        super(basePageImpl);
    }

    private boolean isExpanded() {
        return retry(() -> elementToBeClickable(shortWait(getDriver()), ABOUT_SUB_MENU_LOCATOR).isEnabled(), false);
    }

    @Override
    public WebElement getHelpMenu() {
        return elementToBeClickable(getDriver(), HELP_MENU_LOCATOR);
    }

    @Override
    public void expandHelpMenu() {
        click(getHelpMenu());
        assert getAboutSubMenu().isEnabled();
    }

    @Override
    public WebElement getAboutSubMenu() {
        if (!isExpanded()) {
            expandHelpMenu();
        }
        return elementToBeClickable(getDriver(), ABOUT_SUB_MENU_LOCATOR);
    }

    @Override
    public AboutPage openAboutSubMenu() {
        retry(() -> click(getAboutSubMenu()));
        AboutPage aboutPage = new AboutPageImpl(this);
        assert aboutPage.isOpened();
        return aboutPage;
    }

    @Override
    public WebElement getUserGuideSubMenu() {
        if (!isExpanded()) {
            expandHelpMenu();
        }
        return elementToBeClickable(getDriver(), USER_GUIDE_SUB_MENU_LOCATOR);
    }

    @Override
    public UserGuidePage openUserGuideSubMenu() {
        retry(() -> click(getUserGuideSubMenu()));
        switchToNextTab(getDriver());
        UserGuidePage userGuidePage = new UserGuidePageImpl(this);
        assert userGuidePage.isOpened();
        return userGuidePage;
    }
}

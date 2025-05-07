package org.oagi.score.e2e.impl.menu;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.impl.page.DelegateBasePageImpl;
import org.oagi.score.e2e.impl.page.context.ViewEditBusinessContextPageImpl;
import org.oagi.score.e2e.impl.page.context.ViewEditContextCategoryPageImpl;
import org.oagi.score.e2e.impl.page.context.ViewEditContextSchemePageImpl;
import org.oagi.score.e2e.menu.ContextMenu;
import org.oagi.score.e2e.page.context.ViewEditBusinessContextPage;
import org.oagi.score.e2e.page.context.ViewEditContextCategoryPage;
import org.oagi.score.e2e.page.context.ViewEditContextSchemePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.oagi.score.e2e.impl.PageHelper.*;

public class ContextMenuImpl extends DelegateBasePageImpl implements ContextMenu {

    private final By CONTEXT_MENU_LOCATOR =
            By.xpath("//mat-toolbar-row/button/span[contains(text(), \"Context\")]//ancestor::button[1]");

    private final By VIEW_EDIT_CONTEXT_CATEGORY_SUB_MENU_LOCATOR =
            By.xpath("//button/span[contains(text(), \"View/Edit Context Category\")]");

    private final By VIEW_EDIT_CONTEXT_SCHEME_SUB_MENU_LOCATOR =
            By.xpath("//button/span[contains(text(), \"View/Edit Context Scheme\")]");

    private final By VIEW_EDIT_BUSINESS_CONTEXT_SUB_MENU_LOCATOR =
            By.xpath("//button/span[contains(text(), \"View/Edit Business Context\")]");

    public ContextMenuImpl(BasePageImpl basePageImpl) {
        super(basePageImpl);
    }

    private boolean isExpanded() {
        return retry(() -> elementToBeClickable(shortWait(getDriver()), VIEW_EDIT_CONTEXT_CATEGORY_SUB_MENU_LOCATOR).isEnabled(), false);
    }

    @Override
    public WebElement getContextMenu() {
        return elementToBeClickable(getDriver(), CONTEXT_MENU_LOCATOR);
    }

    @Override
    public void expandContextMenu() {
        click(getContextMenu());
        assert getViewEditContextCategorySubMenu().isEnabled();
    }

    @Override
    public WebElement getViewEditContextCategorySubMenu() {
        if (!isExpanded()) {
            expandContextMenu();
        }
        return elementToBeClickable(getDriver(), VIEW_EDIT_CONTEXT_CATEGORY_SUB_MENU_LOCATOR);
    }

    @Override
    public ViewEditContextCategoryPage openViewEditContextCategorySubMenu() {
        retry(() -> click(getDriver(), getViewEditContextCategorySubMenu()));
        ViewEditContextCategoryPage viewEditContextCategoryPage = new ViewEditContextCategoryPageImpl(this);
        assert viewEditContextCategoryPage.isOpened();
        return viewEditContextCategoryPage;
    }

    @Override
    public WebElement getViewEditContextSchemeSubMenu() {
        if (!isExpanded()) {
            expandContextMenu();
        }
        return elementToBeClickable(getDriver(), VIEW_EDIT_CONTEXT_SCHEME_SUB_MENU_LOCATOR);
    }

    @Override
    public ViewEditContextSchemePage openViewEditContextSchemeSubMenu() {
        retry(() -> click(getDriver(), getViewEditContextSchemeSubMenu()));
        ViewEditContextSchemePage viewEditContextSchemePage = new ViewEditContextSchemePageImpl(this);
        assert viewEditContextSchemePage.isOpened();
        return viewEditContextSchemePage;
    }

    @Override
    public WebElement getViewEditBusinessContextSubMenu() {
        if (!isExpanded()) {
            expandContextMenu();
        }
        return elementToBeClickable(getDriver(), VIEW_EDIT_BUSINESS_CONTEXT_SUB_MENU_LOCATOR);
    }

    @Override
    public ViewEditBusinessContextPage openViewEditBusinessContextSubMenu() {
        retry(() -> click(getDriver(), getViewEditBusinessContextSubMenu()));
        ViewEditBusinessContextPage viewEditBusinessContextPage = new ViewEditBusinessContextPageImpl(this);
        assert viewEditBusinessContextPage.isOpened();
        return viewEditBusinessContextPage;
    }
}

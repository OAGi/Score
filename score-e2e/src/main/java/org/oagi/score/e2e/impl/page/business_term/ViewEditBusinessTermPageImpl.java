package org.oagi.score.e2e.impl.page.business_term;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.impl.page.context.CreateContextCategoryPageImpl;
import org.oagi.score.e2e.impl.page.context.EditContextCategoryPageImpl;
import org.oagi.score.e2e.obj.ContextCategoryObject;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.business_term.CreateBusinessTermPage;
import org.oagi.score.e2e.page.business_term.EditBusinessTermPage;
import org.oagi.score.e2e.page.business_term.ViewEditBusinessTermPage;
import org.oagi.score.e2e.page.context.CreateContextCategoryPage;
import org.oagi.score.e2e.page.context.EditContextCategoryPage;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import static org.oagi.score.e2e.impl.PageHelper.*;

public class ViewEditBusinessTermPageImpl extends BasePageImpl implements ViewEditBusinessTermPage {

    private static final By NEW_BUSINESS_TERM_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"New Business Term\")]//ancestor::button[1]");
    public ViewEditBusinessTermPageImpl(BasePage parent){ super(parent);}

    @Override
    protected String getPageUrl() {
        return getConfig().getBaseUrl().resolve("/business_term").toString();
    }

    @Override
    public void openPage() {
        String url = getPageUrl();
        getDriver().get(url);
        assert "Business Term".equals(getText(getTitle()));
    }

    @Override
    public WebElement getTitle() {
        return visibilityOfElementLocated(getDriver(), By.className("title"));
    }

    @Override
    public WebElement getNewBusinessTermButton() {
        return elementToBeClickable(getDriver(), NEW_BUSINESS_TERM_BUTTON_LOCATOR);
    }

    @Override
    public void hitNewBusinessTermButton() {
        click(getNewBusinessTermButton());
        invisibilityOfLoadingContainerElement(getDriver());
    }

    @Override
    public CreateBusinessTermPage openCreateBusinessTermPage() {
        click(getNewBusinessTermButton());
        CreateBusinessTermPage createBusinessTermPage = new CreateBusinessTermPageImpl(this);
        assert createBusinessTermPage.isOpened();
        return createBusinessTermPage;
    }

    @Override
    public EditBusinessTermPage openEditBusinessTermPageByTerm(String businessTermName) throws NoSuchElementException{
        setName(contextCategoryName);
        hitSearchButton();

        return retry(() -> {
            WebElement td;
            try {
                WebElement tr = getTableRecordAtIndex(1);
                td = getColumnByName(tr, "name");
            } catch (TimeoutException e) {
                throw new NoSuchElementException("Cannot locate a context category using " + contextCategoryName, e);
            }
            if (!contextCategoryName.equals(getText(td.findElement(By.cssSelector("a > span"))))) {
                throw new NoSuchElementException("Cannot locate a context category using " + contextCategoryName);
            }
            WebElement tdName = td.findElement(By.tagName("a"));
            click(tdName);

            ContextCategoryObject contextCategory =
                    getAPIFactory().getContextCategoryAPI().getContextCategoryByName(contextCategoryName);
            EditContextCategoryPage editContextCategoryPage =
                    new EditContextCategoryPageImpl(this, contextCategory);
            assert editContextCategoryPage.isOpened();
            return editContextCategoryPage;
        });
    }

}

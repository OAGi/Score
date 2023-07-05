package org.oagi.score.e2e.impl.page.code_list;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.page.code_list.AddCommentDialog;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import static java.time.Duration.ofMillis;
import static org.oagi.score.e2e.impl.PageHelper.*;

public class AddCommentDialogImpl implements AddCommentDialog {
    private static final By COMMENT_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Comment\")]//ancestor::button[1]");
    private static final By COMMENT_FIELD_LOCATOR =
            By.xpath("//mat-sidenav//mat-form-field//textarea");
    private static final By CLOSE_BUTTON_LOCATOR =
            By.xpath("//div/mat-icon[contains(text(), \"close\")]");

    private final BasePageImpl parent;

    public AddCommentDialogImpl(BasePageImpl parent) {
        this.parent = parent;
    }

    private WebDriver getDriver() {
        return this.parent.getDriver();
    }

    @Override
    public boolean isOpened() {
        try {
            getTitle();
        } catch (TimeoutException e) {
            return false;
        }
        return true;
    }

    @Override
    public WebElement getTitle() {
        return visibilityOfElementLocated(getDriver(), By.xpath("//mat-sidenav//h3"));
    }

    @Override
    public void setComment(String comment) {
        sendKeys(getCommentField(), comment);
        hitCommentButton();
    }

    @Override
    public WebElement getCommentField() {
        return visibilityOfElementLocated(getDriver(), COMMENT_FIELD_LOCATOR);
    }

    @Override
    public void hitCommentButton() {
        retry(() -> {
            click(getCommentButton());
            waitFor(ofMillis(1000L));
        });
        invisibilityOfLoadingContainerElement(getDriver());
    }

    @Override
    public WebElement getCommentButton() {
        return elementToBeClickable(getDriver(), COMMENT_BUTTON_LOCATOR);
    }

    @Override
    public void hitCloseButton() {
        retry(() -> {
            click(getCloseButton());
            waitFor(ofMillis(1000L));
        });
        invisibilityOfLoadingContainerElement(getDriver());
    }

    @Override
    public WebElement getCloseButton() {
        return elementToBeClickable(getDriver(), CLOSE_BUTTON_LOCATOR);
    }
}

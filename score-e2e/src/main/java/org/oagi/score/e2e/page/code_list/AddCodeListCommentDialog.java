package org.oagi.score.e2e.page.code_list;

import org.oagi.score.e2e.page.Dialog;
import org.openqa.selenium.WebElement;

public interface AddCodeListCommentDialog extends Dialog {
    void setComment(String comment);

    WebElement getCommentField();

    void hitCommentButton();

    WebElement getCommentButton();

    void hitCloseButton();

    WebElement getCloseButton();
}

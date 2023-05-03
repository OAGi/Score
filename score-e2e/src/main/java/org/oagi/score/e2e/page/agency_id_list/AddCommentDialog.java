package org.oagi.score.e2e.page.agency_id_list;

import org.oagi.score.e2e.page.Dialog;
import org.openqa.selenium.WebElement;

public interface AddCommentDialog extends Dialog {

    /**
     * Return the UI element of the 'Comment' field.
     *
     * @return the UI element of the 'Comment' field
     */
    WebElement getCommentField();

    /**
     * Set the 'Comment' field with the given text.
     *
     * @param comment Comment
     */
    void setComment(String comment);

    /**
     * Return the UI element of the content at the given index, which starts from 1.
     *
     * @param idx The index of the content.
     * @return the content.
     */
    CommentContent getContent(int idx);

    /**
     * Return the UI element of the 'Comment' button.
     *
     * @return the UI element of the 'Comment' button
     */
    WebElement getCommentButton();

    /**
     * Hit the 'Comment' button.
     */
    void hitCommentButton();

    /**
     * Return the UI element of the 'Close' button.
     *
     * @return the UI element of the 'Close' button
     */
    WebElement getCloseButton();

    /**
     * Hit the 'Close' button.
     */
    void hitCloseButton();

    interface CommentContent {

        String getCreator();

        String getCommentText();

    }

}

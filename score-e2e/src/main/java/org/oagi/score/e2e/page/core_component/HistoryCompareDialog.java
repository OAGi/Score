package org.oagi.score.e2e.page.core_component;

import org.oagi.score.e2e.page.Dialog;
import org.openqa.selenium.WebElement;

public interface HistoryCompareDialog extends Dialog {

    /**
     * Return the history record panel on the left.
     *
     * @return the history record panel on the left
     */
    HistoryRecordPanel getLeftHistoryRecordPanel();

    /**
     * Return the history record panel on the right.
     *
     * @return the history record panel on the right
     */
    HistoryRecordPanel getRightHistoryRecordPanel();

    interface HistoryRecordPanel {

        /**
         * Return the history item panel at the given index, which starts from 0.
         *
         * @param idx The index of the history item panel.
         * @return the history item panel at the given index
         */
        HistoryItemPanel getHistoryItemPanel(int idx);

    }

    interface HistoryItemPanel {

        /**
         * Return the UI element of the 'Title' field.
         *
         * @return the UI element of the 'Title' field
         */
        WebElement getTitleField();

        /**
         * Return the UI element of the 'GUID' field.
         *
         * @return the UI element of the 'GUID' field
         */
        WebElement getGUIDField();

        /**
         * Return the UI element of the 'Owner' field.
         *
         * @return the UI element of the 'Owner' field
         */
        WebElement getOwnerField();

        /**
         * Return the UI element of the 'DEN' field.
         *
         * @return the UI element of the 'DEN' field
         */
        WebElement getDENField();

    }

}

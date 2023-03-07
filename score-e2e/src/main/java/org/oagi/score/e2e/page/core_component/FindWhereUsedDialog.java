package org.oagi.score.e2e.page.core_component;

import org.oagi.score.e2e.page.Dialog;
import org.openqa.selenium.WebElement;

public interface FindWhereUsedDialog extends Dialog {

    /**
     * Return the UI element of the table record containing the given value.
     *
     * @param value value
     * @return the UI element of the table record
     */
    WebElement getTableRecordByValue(String value);
}

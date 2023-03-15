package org.oagi.score.e2e.page.code_list;

import org.oagi.score.e2e.page.Dialog;
import org.openqa.selenium.WebElement;

public interface EditCodeListValueDialog extends Dialog {
    void setCode(String code);

    void setMeaning(String meaning);

    void hitAddButton();

    WebElement getCodeField();

    WebElement getMeaningField();

    WebElement getDefinitionField();

    WebElement getDefinitionSourceField();

    WebElement getAddCodeListValueButton();
}

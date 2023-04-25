package org.oagi.score.e2e.page.agency_id_list;

import org.oagi.score.e2e.page.Dialog;
import org.openqa.selenium.WebElement;

public interface EditAgencyIDListValueDialog extends Dialog {
    void setValue(String value);

    void setMeaning(String meaning);

    void hitAddButton();

    WebElement getValueField();

    WebElement getMeaningField();

    WebElement getDefinitionField();

    WebElement getDefinitionSourceField();

    WebElement getAddCodeListValueButton();

    WebElement getDeprecatedSelectField();

    void setDefinition(String definition);

    void setDefinitionSource(String definitionSource);

    void hitSaveButton();

    WebElement getSaveButton();
}

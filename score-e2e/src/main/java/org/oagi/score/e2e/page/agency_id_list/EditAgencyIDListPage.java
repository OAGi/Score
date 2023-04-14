package org.oagi.score.e2e.page.agency_id_list;

import org.oagi.score.e2e.obj.NamespaceObject;
import org.oagi.score.e2e.page.Page;
import org.openqa.selenium.WebElement;

public interface EditAgencyIDListPage extends Page {
    void setName(String agencyIDListName);

    WebElement getAgencyIDListNameField();

    WebElement getNamespaceSelectField();

    WebElement getDefinitionField();

    void setNamespace(NamespaceObject namespace);

    void setDefinition(String definition);

    void hitUpdateButton();

    WebElement getUpdateButton();

    void moveToQA();

    WebElement getMoveToQAButton();

    void moveToProduction();

    WebElement getMoveToProductionButton();

    void setVersion(String version);

    WebElement getVersionField();

    EditAgencyIDListValueDialog addAgencyIDListValue();

    WebElement getAddAgencyIDListValueButton();
}

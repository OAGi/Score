package org.oagi.score.e2e.page.bie;

import org.oagi.score.e2e.obj.TopLevelASBIEPObject;
import org.oagi.score.e2e.page.Dialog;
import org.oagi.score.e2e.page.SearchBarPage;
import org.openqa.selenium.WebElement;

import java.time.LocalDateTime;
import java.util.List;

public interface SelectBaseProfileBIEDialog extends Dialog, SearchBarPage {

    WebElement getOwnerSelectField();

    void setOwner(String owner);

    /** Selects all the owners in the one opening of the owner field, as the field takes more than one. */
    void setOwners(List<String> owners);

    WebElement getUpdaterSelectField();

    void setUpdater(String updater);

    WebElement getUpdatedStartDateField();

    void setUpdatedStartDate(LocalDateTime updatedStartDate);

    WebElement getUpdatedEndDateField();

    void setUpdatedEndDate(LocalDateTime updatedEndDate);

    void hitSearchButton();

    void selectBaseBIE(TopLevelASBIEPObject bie);

    void selectBaseBIE(String topLevelBIEPropertyName);

    /** The owners of the BIEs that the table lists now, one for each BIE; empty when it lists none. */
    List<String> getOwnerLoginIdsOfTableRecords();

    WebElement getTableRecordByValue(String value);

    WebElement getColumnByName(WebElement tableRecord, String columnName);

    WebElement getSelectButton();
}

package org.oagi.score.e2e.impl.page.core_component;

import org.oagi.score.e2e.obj.LogObject;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.core_component.HistoryCompareDialog;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

import static org.oagi.score.e2e.impl.PageHelper.getText;
import static org.oagi.score.e2e.impl.PageHelper.visibilityOfAllElementsLocatedBy;

public class HistoryCompareDialogImpl implements HistoryCompareDialog {

    private BasePage parent;

    private LogObject logObject;

    public HistoryCompareDialogImpl(BasePage parent, LogObject logObject) {
        this.parent = parent;
        this.logObject = logObject;
    }

    private WebDriver getDriver() {
        return this.parent.getDriver();
    }

    @Override
    public boolean isOpened() {
        WebElement title;
        try {
            title = getTitle();
        } catch (TimeoutException e) {
            return false;
        }
        assert this.logObject.getType().equals(getText(title));
        return true;
    }

    @Override
    public WebElement getTitle() {
        return getLeftHistoryRecordPanel().getHistoryItemPanel(0).getTitleField();
    }

    private List<WebElement> getDialogContents() {
        return visibilityOfAllElementsLocatedBy(getDriver(),
                By.xpath("//score-log-compare-dialog//mat-dialog-content[contains(@class, \"mat-mdc-dialog-content\")]"));
    }

    @Override
    public HistoryRecordPanel getLeftHistoryRecordPanel() {
        List<WebElement> webElements = getDialogContents();
        return new HistoryRecordPanelImpl(webElements.get(0));
    }

    @Override
    public HistoryRecordPanel getRightHistoryRecordPanel() {
        List<WebElement> webElements = getDialogContents();
        return new HistoryRecordPanelImpl(webElements.get(1));
    }

    private class HistoryRecordPanelImpl implements HistoryRecordPanel {

        private final WebElement baseElement;

        public HistoryRecordPanelImpl(WebElement baseElement) {
            this.baseElement = baseElement;
        }

        @Override
        public HistoryItemPanel getHistoryItemPanel(int idx) {
            return new HistoryItemPanelImpl(
                    this.baseElement.findElements(By.xpath("//mat-card-title")).get(idx),
                    this.baseElement.findElements(By.xpath("//mat-card-content")).get(idx)
            );
        }

    }

    private class HistoryItemPanelImpl implements HistoryItemPanel {

        private final WebElement titleElement;

        private final WebElement contentElement;

        public HistoryItemPanelImpl(WebElement titleElement, WebElement contentElement) {
            this.titleElement = titleElement;
            this.contentElement = contentElement;
        }

        private WebElement getInputFieldByName(String name) {
            return this.contentElement.findElement(By.xpath(
                    "//*[contains(text(), \"" + name + "\")]//ancestor::div[1]//input"));
        }

        @Override
        public WebElement getTitleField() {
            return this.titleElement;
        }

        @Override
        public WebElement getGUIDField() {
            return getInputFieldByName("GUID");
        }

        @Override
        public WebElement getOwnerField() {
            return getInputFieldByName("Owner");
        }

        @Override
        public WebElement getDENField() {
            return getInputFieldByName("DEN");
        }

    }

}

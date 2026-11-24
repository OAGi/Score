package org.oagi.score.e2e.page.business_term;

import org.oagi.score.e2e.page.Dialog;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.Map;

/**
 * An interface of the 'Where Used' dialog of the Business Term list: the BIEs a business term is assigned to. Each BIE is a
 * bordered group (a {@code fieldset}) with its release as the legend, when it was updated and by whom, the state of the BIE,
 * and its tree, which is loaded when the group is expanded. A tree has only the nodes that have the term (and the way to them),
 * and a badge with the term, its Type Code and a star if it is the Preferred one on each of them.
 *
 * <p>The groups are counted from 1, in the order they are shown: the most recently updated BIE first.
 */
public interface BusinessTermWhereUsedDialog extends Dialog {

    /** The title of the dialog as it is shown, e.g. {@code Where Used: 3 BIEs}. */
    String getTitleText();

    /** The number of BIEs (groups) the dialog shows. */
    int getNumberOfBIEs();

    /** The release of the BIE, which is the legend of its group. */
    String getRelease(int bieIndex);

    /** What is said of the update of the BIE, with the line break of a wide dialog as a space: {@code Updated 2026-01-01 09:00 by login}. */
    String getUpdatedText(int bieIndex);

    /** The state of the BIE as its badge shows it. */
    String getState(int bieIndex);

    /** The element that says when the BIE was updated and by whom. */
    WebElement getUpdatedElement(int bieIndex);

    /** The group of the BIE. */
    WebElement getBIEGroup(int bieIndex);

    /** Expands the BIE and waits for its tree. */
    void expand(int bieIndex);

    /** The labels of the rows of the tree of an expanded BIE, from the top. */
    List<String> getNodeLabels(int bieIndex);

    /** The badge of the business term on the row of the tree with the label. A row has more than one if the term is assigned to it more than once. */
    List<Badge> getBadges(int bieIndex, String nodeLabel);

    /** The number of badges the whole tree of the BIE has. */
    int getNumberOfBadges(int bieIndex);

    /** Clicks the row of the tree with the label. A row that has the term opens the BIE at it in a new tab. */
    void clickNode(int bieIndex, String nodeLabel);

    /** The body of the dialog, which is what scrolls. */
    WebElement getBody();

    /**
     * What the dialog scrolls: {@code body.scrollHeight}, {@code body.clientHeight}, {@code body.overflowY}, and the same of the
     * dialog container and of the overlay pane ({@code container.*}, {@code pane.*}).
     */
    Map<String, Object> getScrollMetrics();

    /** The computed style (font, size, weight, height, colour) of the first row of the tree of the BIE, and of its label. */
    Map<String, String> getTreeRowStyle(int bieIndex);

    /** Closes the dialog. */
    void close();

    /** A badge of a business term on a node. */
    record Badge(String term, boolean preferred, String typeCode) {
    }
}

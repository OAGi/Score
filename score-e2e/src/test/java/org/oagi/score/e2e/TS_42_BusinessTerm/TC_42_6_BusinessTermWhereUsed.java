package org.oagi.score.e2e.TS_42_BusinessTerm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jooq.CloseableDSLContext;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.jooq.types.ULong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.condition.DisabledIfBusinessTermProperty;
import org.oagi.score.e2e.impl.AuthenticatedApiClient;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.bie.EditBIEPage;
import org.oagi.score.e2e.page.business_term.BusinessTermWhereUsedDialog;
import org.oagi.score.e2e.page.business_term.BusinessTermWhereUsedDialog.Badge;
import org.oagi.score.e2e.page.business_term.ViewEditBusinessTermPage;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebElement;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.oagi.score.e2e.impl.PageHelper.*;
import static org.oagi.score.e2e.impl.api.jooq.entity.Tables.BBIE;
import static org.oagi.score.e2e.impl.api.jooq.entity.Tables.TOP_LEVEL_ASBIEP;

/**
 * Test Case 42.6 — Where Used on the Business Term list (the BIEs a Business Term is assigned to).
 *
 * <p>The row menu of the Business Term list opens a dialog with a bordered group for each BIE that has the term: its release,
 * who updated it and when, its state, and its tree (as the BIE editor draws it) with only the nodes that have the term and the
 * way to them, each with a badge. Each @Test maps to an assertion #42.6.1–#42.6.10.
 */
@Execution(ExecutionMode.CONCURRENT)
@DisabledIfBusinessTermProperty(value = false)
public class TC_42_6_BusinessTermWhereUsed extends BaseTest {

    private static final int WIDE_WIDTH = 1280;
    private static final int WIDE_HEIGHT = 1200;

    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    private AppUserObject developer;
    private AppUserObject endUser;
    private CloseableDSLContext dsl;

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @BeforeEach
    public void init() {
        super.init();
        developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
    }

    // ----- #42.6.1 -----

    /**
     * #42.6.1 — the BIEs a term is assigned to are shown, and only those: a BIE that has another term, or none, is not.
     */
    @Test
    @DisplayName("TC_42_6_1")
    public void assigned_bies_of_the_term_are_shown() {
        BusinessTermObject term = newTerm();
        BusinessTermObject otherTerm = newTerm();
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());

        TopLevelASBIEPObject assigned = bieWithBbie(homePage);
        assign(term, assigned, false, null);
        TopLevelASBIEPObject ofOtherTerm = bieWithBbie(homePage);
        assign(otherTerm, ofOtherTerm, false, null);
        bieWithBbie(homePage); // a BIE the term is not assigned to

        BusinessTermWhereUsedDialog dialog = openWhereUsed(homePage, term);

        assertEquals("Where Used: 1 BIE", dialog.getTitleText());
        assertEquals(1, dialog.getNumberOfBIEs());
        assertEquals("10.8.3", dialog.getRelease(1));
        // The root row of the group names the BIE.
        dialog.expand(1);
        assertEquals(assigned.getPropertyTerm(), dialog.getNodeLabels(1).get(0));
    }

    /**
     * #42.6.1 (cont.) — a term that is assigned to no BIE says so.
     */
    @Test
    @DisplayName("TC_42_6_1_b")
    public void term_assigned_to_no_bie_says_nothing_was_found() {
        BusinessTermObject term = newTerm();
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());

        BusinessTermWhereUsedDialog dialog = openWhereUsed(homePage, term);

        assertEquals("Nothing found", dialog.getTitleText());
        assertEquals(0, dialog.getNumberOfBIEs());
    }

    // ----- #42.6.2 -----

    /**
     * #42.6.2 — the tree of a BIE has only the nodes that have the term and the way to them, expanded, with a badge on each of
     * the nodes and none on the way.
     */
    @Test
    @DisplayName("TC_42_6_2")
    public void tree_has_only_the_assigned_nodes_and_the_way_to_them() {
        BusinessTermObject term = newTerm();
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        TopLevelASBIEPObject bie = bieWithBbie(homePage);
        assign(term, bie, false, null);

        BusinessTermWhereUsedDialog dialog = openWhereUsed(homePage, term);
        dialog.expand(1);

        // The BIE is much larger than this: what has no term is not drawn.
        assertEquals(List.of(bie.getPropertyTerm(), "Note"), dialog.getNodeLabels(1));
        assertTrue(dialog.getBadges(1, bie.getPropertyTerm()).isEmpty(), "no badge on the way to an assigned node");
        List<Badge> badges = dialog.getBadges(1, "Note");
        assertEquals(1, badges.size());
        assertEquals(term.getBusinessTerm(), badges.get(0).term());
    }

    // ----- #42.6.3 -----

    /**
     * #42.6.3 — clicking a node that has the term opens the BIE at that node in a new tab; the top-level node opens the BIE.
     */
    @Test
    @DisplayName("TC_42_6_3")
    public void clicking_a_node_opens_the_bie_at_the_node() {
        BusinessTermObject term = newTerm();
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        TopLevelASBIEPObject bie = bieWithBbie(homePage);
        assign(term, bie, false, null);

        BusinessTermWhereUsedDialog dialog = openWhereUsed(homePage, term);
        dialog.expand(1);
        dialog.clickNode(1, "Note");

        retry(() -> assertEquals(2, getDriver().getWindowHandles().size()));
        switchToNextTab(getDriver());
        retry(() -> assertTrue(getDriver().getCurrentUrl().contains("/profile_bie/" + bie.getTopLevelAsbiepId() + "/"),
                getDriver().getCurrentUrl()));
        By selected = By.xpath("//div[contains(@class, \"tree-node-selected\")]//*[contains(@class, \"node-label\")]");
        retry(() -> assertEquals("Note", getText(visibilityOfElementLocated(getDriver(), selected))));
        getDriver().close();
        switchToMainTab(getDriver());

        // The top-level node opens the BIE itself.
        dialog.clickNode(1, bie.getPropertyTerm());
        retry(() -> assertEquals(2, getDriver().getWindowHandles().size()));
        switchToNextTab(getDriver());
        retry(() -> assertTrue(getDriver().getCurrentUrl().endsWith("/profile_bie/" + bie.getTopLevelAsbiepId()),
                getDriver().getCurrentUrl()));
        getDriver().close();
        switchToMainTab(getDriver());
    }

    // ----- #42.6.4 -----

    /**
     * #42.6.4 — the number of BIEs in the title and the number of badges are right: a BIE is one group however many of its nodes
     * have the term, and a node has a badge for each assignment.
     */
    @Test
    @DisplayName("TC_42_6_4")
    public void counts_of_bies_and_badges_are_right() {
        BusinessTermObject term = newTerm();
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        TopLevelASBIEPObject first = bieWithBbie(homePage);
        TopLevelASBIEPObject second = bieWithBbie(homePage);
        TopLevelASBIEPObject third = bieWithBbie(homePage);
        assign(term, first, true, "Synonym");
        assign(term, first, false, "Legal");  // the same node, a second assignment
        assign(term, second, false, null);
        assign(term, third, false, null);

        BusinessTermWhereUsedDialog dialog = openWhereUsed(homePage, term);

        assertEquals("Where Used: 3 BIEs", dialog.getTitleText());
        assertEquals(3, dialog.getNumberOfBIEs());
        int badges = 0;
        for (int i = 1; i <= 3; i++) {
            dialog.expand(i);
            badges += dialog.getNumberOfBadges(i);
        }
        assertEquals(4, badges, "one badge for each assignment");
    }

    // ----- #42.6.5 -----

    /**
     * #42.6.5 — the tree is drawn as in the BIE editor: a row has the same size, font and colour.
     */
    @Test
    @DisplayName("TC_42_6_5")
    public void tree_style_is_the_same_as_the_bie_editor() {
        BusinessTermObject term = newTerm();
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        TopLevelASBIEPObject bie = bieWithBbie(homePage);
        assign(term, bie, false, null);

        BusinessTermWhereUsedDialog dialog = openWhereUsed(homePage, term);
        dialog.expand(1);
        Map<String, String> inDialog = dialog.getTreeRowStyle(1);

        homePage.getBIEMenu().openViewEditBIESubMenu().openEditBIEPage(bie);
        By editorRow = By.cssSelector("score-tree-viewport .mat-tree-node");
        WebElement row = visibilityOfElementLocated(getDriver(), editorRow);
        @SuppressWarnings("unchecked")
        Map<String, Object> inEditor = (Map<String, Object>) ((JavascriptExecutor) getDriver()).executeScript(
                "const row = arguments[0], label = row.querySelector('.node-label');"
                        + "const pick = (e, names) => Object.fromEntries(names.map(n => [n, getComputedStyle(e).getPropertyValue(n)]));"
                        + "return {...pick(row, ['height', 'font-size', 'font-family', 'font-weight', 'line-height', 'color', 'display']),"
                        + " ...Object.fromEntries(Object.entries(pick(label, ['font-size', 'font-family', 'font-weight', 'color']))"
                        + "   .map(([k, v]) => ['label ' + k, v]))};", row);

        for (Map.Entry<String, String> entry : inDialog.entrySet()) {
            assertEquals(String.valueOf(inEditor.get(entry.getKey())), entry.getValue(),
                    "the style of a row of the tree differs from the BIE editor's: " + entry.getKey());
        }
    }

    // ----- #42.6.6 -----

    /**
     * #42.6.6 — with many BIEs only the body of the dialog scrolls: the title stays, and neither the dialog nor its overlay pane
     * has a scroll bar of its own.
     */
    @Test
    @DisplayName("TC_42_6_6")
    public void only_the_body_of_the_dialog_scrolls_with_many_bies() {
        BusinessTermObject term = newTerm();
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        for (int i = 0; i < 6; i++) {
            assign(term, bieWithBbie(homePage), false, null);
        }

        getDriver().manage().window().setSize(new Dimension(WIDE_WIDTH, 420));
        BusinessTermWhereUsedDialog dialog = openWhereUsed(homePage, term);
        assertEquals(6, dialog.getNumberOfBIEs());
        dialog.expand(1);

        Map<String, Object> before = dialog.getScrollMetrics();
        Map<?, ?> body = (Map<?, ?>) before.get("body");
        assertTrue(Set_of("auto", "scroll").contains(String.valueOf(body.get("overflowY"))), "the body scrolls: " + body);
        assertTrue(number(body.get("scrollHeight")) > number(body.get("clientHeight")), "the BIEs are more than fit in the body: " + body);
        for (String outer : List.of("container", "pane")) {
            Map<?, ?> metrics = (Map<?, ?>) before.get(outer);
            assertTrue(number(metrics.get("scrollHeight")) <= number(metrics.get("clientHeight")) + 1,
                    "the " + outer + " of the dialog does not scroll: " + metrics);
            assertFalse(Set_of("auto", "scroll").contains(String.valueOf(metrics.get("overflowY"))), outer + " " + metrics);
        }

        // Scrolled to the end, the title is where it was and the last BIE is in view.
        int titleY = dialog.getTitle().getRect().getY();
        ((JavascriptExecutor) getDriver()).executeScript("arguments[0].scrollTop = arguments[0].scrollHeight;", dialog.getBody());
        waitFor(java.time.Duration.ofMillis(300L));
        assertEquals(titleY, dialog.getTitle().getRect().getY(), "the title stays");
        assertTrue(dialog.getTitle().isDisplayed());
        assertTrue(number(((JavascriptExecutor) getDriver()).executeScript("return arguments[0].scrollTop;", dialog.getBody())) > 0);
        Rectangle last = dialog.getBIEGroup(6).getRect();
        Rectangle bodyRect = dialog.getBody().getRect();
        assertTrue(last.getY() + last.getHeight() <= bodyRect.getY() + bodyRect.getHeight() + 1, "the last BIE is in the body");
        assertEquals(0L, number(((JavascriptExecutor) getDriver()).executeScript("return window.scrollY;")), "the page behind does not scroll");
    }

    // ----- #42.6.7 -----

    /**
     * #42.6.7 — the badge of an assignment has a star if it is the Preferred one, and the Type Code when it has one.
     */
    @Test
    @DisplayName("TC_42_6_7")
    public void badge_shows_preferred_and_type_code() {
        BusinessTermObject term = newTerm();
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        TopLevelASBIEPObject bie = bieWithBbie(homePage);
        assign(term, bie, true, "Synonym");
        assign(term, bie, false, "Legal");
        assign(term, bie, false, null);

        BusinessTermWhereUsedDialog dialog = openWhereUsed(homePage, term);
        dialog.expand(1);
        List<Badge> badges = dialog.getBadges(1, "Note");

        assertEquals(3, badges.size());
        Badge preferred = badges.stream().filter(Badge::preferred).findFirst().orElseThrow();
        assertEquals(1, badges.stream().filter(Badge::preferred).count(), "only one is the Preferred");
        assertEquals("Synonym", preferred.typeCode());
        assertEquals(term.getBusinessTerm(), preferred.term());
        assertTrue(badges.stream().anyMatch(b -> !b.preferred() && "Legal".equals(b.typeCode())));
        assertTrue(badges.stream().anyMatch(b -> !b.preferred() && b.typeCode() == null), "no Type Code, no chip for it");
    }

    // ----- #42.6.8 -----

    /**
     * #42.6.8 — the BIEs are listed by the time they were updated, the most recent first, not by when they were made.
     */
    @Test
    @DisplayName("TC_42_6_8")
    public void bies_are_listed_most_recently_updated_first() {
        BusinessTermObject term = newTerm();
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        TopLevelASBIEPObject made1 = bieWithBbie(homePage);
        TopLevelASBIEPObject made2 = bieWithBbie(homePage);
        TopLevelASBIEPObject made3 = bieWithBbie(homePage);
        for (TopLevelASBIEPObject bie : List.of(made1, made2, made3)) {
            assign(term, bie, false, null);
        }
        LocalDateTime base = LocalDateTime.now().minusDays(10).withNano(0);
        // Made first, updated last; and the one made last updated in between.
        setUpdated(made1, base.plusDays(5), endUser, null);
        setUpdated(made2, base.plusDays(1), endUser, null);
        setUpdated(made3, base.plusDays(3), endUser, null);

        BusinessTermWhereUsedDialog dialog = openWhereUsed(homePage, term);

        assertEquals(3, dialog.getNumberOfBIEs());
        List<String> shown = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            shown.add(dialog.getUpdatedText(i).replaceAll("^Updated (\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}) by.*$", "$1"));
        }
        List<String> newestFirst = new ArrayList<>(shown);
        newestFirst.sort(Collections.reverseOrder());
        assertEquals(newestFirst, shown, "newest first: " + shown);
        assertEquals(3, shown.stream().distinct().count());
    }

    // ----- #42.6.9 -----

    /**
     * #42.6.9 — each BIE shows its state, and when it was updated and by whom, as the BIE has them; the owner of the BIE is the
     * one the server reports.
     */
    @Test
    @DisplayName("TC_42_6_9")
    public void state_and_update_of_the_bie_are_shown() throws Exception {
        BusinessTermObject term = newTerm();
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        TopLevelASBIEPObject bie = bieWithBbie(homePage);
        assign(term, bie, false, null);
        LocalDateTime updated = LocalDateTime.now().minusDays(3).withSecond(0).withNano(0);
        setUpdated(bie, updated, developer, "Production");

        BusinessTermWhereUsedDialog dialog = openWhereUsed(homePage, term);

        assertEquals("Production", dialog.getState(1));
        AuthenticatedApiClient api = new AuthenticatedApiClient(getDriver(), getConfig().getBaseUrl());
        AuthenticatedApiClient.ApiResponse response = api.getJson("/api/business-terms/" + term.getBusinessTermId() + "/where-used");
        assertEquals(200, response.statusCode(), response.body());
        JsonNode usage = new ObjectMapper().readTree(response.body()).get(0);
        assertEquals(endUser.getLoginId(), usage.get("owner").get("loginId").asText(), "the owner of the BIE");
        assertEquals("Production", usage.get("state").asText());
        // The time as the page writes it: the time the server reports, in the time zone of the browser.
        String expectedTime = (String) ((JavascriptExecutor) getDriver()).executeScript(
                "const d = new Date(arguments[0]); const p = n => String(n).padStart(2, '0');"
                        + "return d.getFullYear() + '-' + p(d.getMonth() + 1) + '-' + p(d.getDate()) + ' ' + p(d.getHours()) + ':' + p(d.getMinutes());",
                usage.get("lastUpdated").get("when").isNumber() ? usage.get("lastUpdated").get("when").asLong()
                        : usage.get("lastUpdated").get("when").asText());
        assertEquals("Updated " + expectedTime + " by " + developer.getLoginId(), dialog.getUpdatedText(1));
    }

    // ----- #42.6.10 -----

    /**
     * #42.6.10 — in a wide dialog who updated the BIE and when is at the right end of the top row of the tree; in a narrow one
     * (a phone) it is above the tree, at the left, on one line.
     */
    @Test
    @DisplayName("TC_42_6_10")
    public void update_of_the_bie_is_above_the_tree_at_the_left_on_a_narrow_screen() {
        BusinessTermObject term = newTerm();
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        TopLevelASBIEPObject bie = bieWithBbie(homePage);
        assign(term, bie, false, null);

        BusinessTermWhereUsedDialog dialog = openWhereUsed(homePage, term);
        Rectangle group = dialog.getBIEGroup(1).getRect();
        Rectangle wide = dialog.getUpdatedElement(1).getRect();
        assertTrue(wide.getX() > group.getX() + group.getWidth() / 2, "at the right end of the row: " + wide + " in " + group);

        // The dialog stays open while the window narrows (the list has no row menu on a phone, where it shows compact rows).
        getDriver().manage().window().setSize(new Dimension(500, 900));
        assumeTrue(((Number) ((JavascriptExecutor) getDriver()).executeScript("return window.innerWidth;")).intValue() <= 520,
                "the browser cannot be made as narrow as a phone");
        BusinessTermWhereUsedDialog narrowDialog = dialog;
        waitFor(java.time.Duration.ofMillis(500L));

        WebElement updated = narrowDialog.getUpdatedElement(1);
        Rectangle narrowGroup = narrowDialog.getBIEGroup(1).getRect();
        Rectangle narrow = updated.getRect();
        Rectangle firstRow = narrowDialog.getBIEGroup(1).findElement(By.cssSelector(".mat-tree-node")).getRect();
        assertTrue(narrow.getY() + narrow.getHeight() <= firstRow.getY() + 1, "above the tree: " + narrow + " / " + firstRow);
        assertTrue(narrow.getX() < narrowGroup.getX() + 24, "at the left: " + narrow + " in " + narrowGroup);
        assertEquals("left", updated.getCssValue("text-align"));
        assertEquals(1, updated.findElements(By.xpath("./div")).stream().map(d -> d.getRect().getY()).distinct().count(),
                "on one line");
        // The state is on the row of the tree, inside the group.
        assertEquals("WIP", narrowDialog.getState(1));
    }

    // ----- fixtures -----

    private BusinessTermObject newTerm() {
        return getAPIFactory().getBusinessTermAPI().createRandomBusinessTerm(endUser);
    }

    private BusinessTermWhereUsedDialog openWhereUsed(HomePage homePage, BusinessTermObject term) {
        ViewEditBusinessTermPage page = homePage.getBIEMenu().openViewEditBusinessTermSubMenu();
        return page.openWhereUsedDialog(term.getBusinessTerm());
    }

    /** A BIE of the end user with its 'Note' BBIE used and saved (a node row exists only after a save). */
    private TopLevelASBIEPObject bieWithBbie(HomePage homePage) {
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        BusinessContextObject businessContext = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.3");
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum(library, "Source Activity. Source Activity", release.getReleaseNumber());
        TopLevelASBIEPObject bie = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Collections.singletonList(businessContext), asccp, endUser, "WIP");

        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu().openEditBIEPage(bie);
        EditBIEPage.BBIEPanel panel = editBIEPage.getBBIEPanel(editBIEPage.getNodeByPath("/" + bie.getPropertyTerm() + "/Note"));
        if (!panel.getUsedCheckbox().findElement(By.tagName("input")).isSelected()) {
            panel.toggleUsed();
        }
        editBIEPage.hitUpdateButton();
        return bie;
    }

    private BigInteger bbieIdOf(TopLevelASBIEPObject bie) {
        List<ULong> ids = dsl().select(BBIE.BBIE_ID).from(BBIE)
                .where(BBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(bie.getTopLevelAsbiepId())))
                .fetchInto(ULong.class);
        assertEquals(1, ids.size(), "exactly the saved BBIE node must be persisted: " + ids);
        return ids.get(0).toBigInteger();
    }

    /** Assigns the term to the 'Note' BBIE of the BIE, as the BIE editor does. */
    private void assign(BusinessTermObject term, TopLevelASBIEPObject bie, boolean preferred, String typeCode) {
        AuthenticatedApiClient api = new AuthenticatedApiClient(getDriver(), getConfig().getBaseUrl());
        String body = "{\"biesToAssign\":[{\"bieId\":" + bbieIdOf(bie) + ",\"bieType\":\"BBIE\"}],"
                + "\"primaryIndicator\":" + preferred + ",\"typeCode\":" + (typeCode == null ? "null" : "\"" + typeCode + "\"") + "}";
        AuthenticatedApiClient.ApiResponse response;
        try {
            response = api.postJson("/api/business-terms/" + term.getBusinessTermId() + "/assign", body);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        assertEquals(204, response.statusCode(), response.body());
    }

    /** Sets when the BIE was last updated, and by whom, and its state if one is given. */
    private void setUpdated(TopLevelASBIEPObject bie, LocalDateTime when, AppUserObject by, String state) {
        var update = dsl().update(TOP_LEVEL_ASBIEP)
                .set(TOP_LEVEL_ASBIEP.LAST_UPDATE_TIMESTAMP, when)
                .set(TOP_LEVEL_ASBIEP.LAST_UPDATED_BY, ULong.valueOf(by.getAppUserId()));
        if (state != null) {
            update.set(TOP_LEVEL_ASBIEP.STATE, state);
        }
        update.where(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(bie.getTopLevelAsbiepId()))).execute();
    }

    private static java.util.Set<String> Set_of(String... values) {
        return java.util.Set.of(values);
    }

    private static long number(Object value) {
        return ((Number) value).longValue();
    }

    private DSLContext dsl() {
        if (dsl == null) {
            dsl = DSL.using(
                    getConfig().getProperty("org.oagi.score.e2e.datasource.url"),
                    getConfig().getProperty("org.oagi.score.e2e.datasource.username"),
                    getConfig().getProperty("org.oagi.score.e2e.datasource.password"));
        }
        return dsl;
    }

    @AfterEach
    public void tearDown() {
        super.tearDown();
        if (dsl != null) {
            dsl.close();
            dsl = null;
        }
        this.randomAccounts.forEach(randomAccount ->
                getAPIFactory().getAppUserAPI().deleteAppUserByLoginId(randomAccount.getLoginId()));
    }
}

package org.oagi.score.e2e.TS_19_ReleaseManagement;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.core_component.ACCViewEditPage;
import org.oagi.score.e2e.page.core_component.ViewEditCoreComponentPage;
import org.oagi.score.e2e.page.release.EditReleasePage;
import org.oagi.score.e2e.page.release.ReleaseAssignmentPage;
import org.oagi.score.e2e.page.release.ViewEditReleasePage;
import org.openqa.selenium.By;

import java.math.BigInteger;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.impl.PageHelper.getSnackBarMessage;
import static org.oagi.score.e2e.impl.PageHelper.waitFor;

/**
 * Issue #1895 — turning a release into a draft, taking a draft back to 'Initialized' and publishing are all or nothing, tell the
 * user who asked when they fail, and leave a library alone from changes while one of its releases is being worked on.
 *
 * <p>The work of a release is made to fail the way a lost connection to the database would: the database refuses the last step of
 * it (see {@code ReleaseAPI#makeTheWorkFail}), so that everything the work did before has to be undone.</p>
 */
@Execution(ExecutionMode.SAME_THREAD)
public class TC_19_3_ReleaseWorkIsAllOrNothing extends BaseTest {

    private final List<AppUserObject> randomAccounts = new ArrayList<>();
    private final List<BigInteger> releasesWithFailure = new ArrayList<>();
    private HomePage homePage;

    @BeforeEach
    public void init() {
        super.init();
    }

    @AfterEach
    public void tearDown() {
        super.tearDown();
        // a trigger that is left would make the work of a release fail for whoever uses the database next
        releasesWithFailure.forEach(releaseId -> getAPIFactory().getReleaseAPI().makeTheWorkSucceedAgain(releaseId));
        randomAccounts.forEach(account -> getAPIFactory().getAppUserAPI().deleteAppUserByLoginId(account.getLoginId()));
    }

    private AppUserObject developer(boolean admin) {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(admin);
        randomAccounts.add(developer);
        return developer;
    }

    /** What a test works on: a new release of the library, and a component that is a Candidate and goes with it. */
    private record Work(AppUserObject developer, LibraryObject library, ReleaseObject release, ACCObject component) {
    }

    private Work newWork(boolean admin) {
        AppUserObject developer = developer(admin);
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(developer, library);
        ReleaseObject working = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "Working");
        ACCObject component = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, working, namespace, "Candidate");
        ReleaseObject release = getAPIFactory().getReleaseAPI().createRandomRelease(developer, library, namespace);
        return new Work(developer, library, release, component);
    }

    private String stateOf(Work work) {
        return getAPIFactory().getCoreComponentAPI().getACCByManifestId(work.component().getAccManifestId()).getState();
    }

    private String releaseState(Work work) {
        return getAPIFactory().getReleaseAPI().getReleaseById(work.release().getReleaseId()).getState();
    }

    /** Asks for the draft on the screen, up to the confirmation. */
    private void askForTheDraft(Work work) {
        homePage = loginPage().signIn(work.developer().getLoginId(), work.developer().getPassword());
        ViewEditReleasePage viewEditReleasePage = homePage.getCoreComponentMenu().openViewEditReleaseSubMenu();
        EditReleasePage editReleasePage =
                viewEditReleasePage.openReleaseViewEditPageByReleaseAndState(work.release().getReleaseNumber(), "Initialized");
        ReleaseAssignmentPage assignmentPage = editReleasePage.hitCreateDraftButton();
        assignmentPage.hitAssignAllButton();
        assignmentPage.hitValidateButton();
        assertEquals("All components are valid.", getSnackBarMessage(getDriver()));
        assignmentPage.hitCreateButton();
    }

    private EditReleasePage openTheRelease(Work work, String state) {
        return homePage.getCoreComponentMenu().openViewEditReleaseSubMenu()
                .openReleaseViewEditPageByReleaseAndState(work.release().getReleaseNumber(), state);
    }

    /** From a page that has nothing to do with releases: the home page first, so that the menu is as it is when a user comes to it. */
    private EditReleasePage openTheReleaseById(Work work) {
        homePage.openPage();
        return homePage.getCoreComponentMenu().openViewEditReleaseSubMenu()
                .openReleaseViewEditPageByID(work.release().getReleaseId());
    }

    private void waitForReleaseState(Work work, String expectedState) {
        long begin = System.currentTimeMillis();
        while (System.currentTimeMillis() - begin < Duration.ofSeconds(120L).toMillis()) {
            if (expectedState.equals(releaseState(work))) {
                return;
            }
            waitFor(Duration.ofSeconds(1L));
        }
        fail("Timed out waiting for release '" + work.release().getReleaseNumber() + "' to be '" + expectedState + "'; it is '"
                + releaseState(work) + "'.");
    }

    private String pageText() {
        return getDriver().findElement(By.tagName("body")).getText();
    }

    /** The message the failure left for the user: its subject is on the list, and its text when it is opened. */
    private String openTheFailureMessage(String subject) {
        By link = By.xpath("//*[contains(text(), \"" + subject + "\")]");
        long begin = System.currentTimeMillis();
        do {
            getDriver().get(getConfig().getBaseUrl().resolve("/message").toString());
            waitFor(Duration.ofSeconds(2L));
        } while (getDriver().findElements(link).isEmpty() && System.currentTimeMillis() - begin < Duration.ofSeconds(60L).toMillis());
        org.oagi.score.e2e.impl.PageHelper.click(getDriver(), org.oagi.score.e2e.impl.PageHelper
                .visibilityOfElementLocated(getDriver(), link));
        org.oagi.score.e2e.impl.PageHelper.visibilityOfElementLocated(getDriver(), By.xpath("//*[contains(., \"nothing was changed\")]"));
        return pageText();
    }

    @Test
    @DisplayName("TC_19_3_TA_1")
    public void the_list_of_releases_follows_the_draft_without_searching() {
        Work work = newWork(false);
        askForTheDraft(work);

        // the list is open and has not been searched again: the release is a draft when the work is done
        homePage.getCoreComponentMenu().openViewEditReleaseSubMenu();
        waitForReleaseState(work, "Draft");
        org.oagi.score.e2e.impl.PageHelper.visibilityOfElementLocated(getDriver(), By.xpath(
                "//tr[.//*[contains(text(), \"" + work.release().getReleaseNumber() + "\")]]//*[contains(text(), \"Draft\")]"));
        assertEquals("ReleaseDraft", stateOf(work));
    }

    @Test
    @DisplayName("TC_19_3_TA_2")
    public void a_draft_that_fails_leaves_nothing_and_tells_the_user_without_a_reference_number() {
        Work work = newWork(false);
        getAPIFactory().getReleaseAPI().makeTheWorkFail(work.release().getReleaseId());
        releasesWithFailure.add(work.release().getReleaseId());

        askForTheDraft(work);
        waitForReleaseState(work, "Initialized");
        assertEquals("Candidate", stateOf(work), "The component is as it was.");
        assertEquals(0, getAPIFactory().getReleaseAPI().countManifests(work.release().getReleaseId()), "Nothing is copied.");

        // the user is told in their messages, whatever page they are on when the work ends
        String text = openTheFailureMessage("could not be made a draft");
        assertTrue(text.contains("It is Initialized again, and nothing was changed."), text);
        assertFalse(text.contains("Reference"), "A number that cannot be looked up is not told: " + text);
    }

    @Test
    @DisplayName("TC_19_3_TA_3")
    public void a_draft_that_cannot_go_back_to_initialized_stays_a_draft_with_everything_it_holds() {
        Work work = newWork(false);
        askForTheDraft(work);
        waitForReleaseState(work, "Draft");
        int held = getAPIFactory().getReleaseAPI().countManifests(work.release().getReleaseId());
        assertTrue(held > 0, "Pre-condition: the draft holds what was copied.");

        getAPIFactory().getReleaseAPI().makeTheWorkFail(work.release().getReleaseId());
        releasesWithFailure.add(work.release().getReleaseId());
        EditReleasePage editReleasePage = openTheRelease(work, "Draft");
        editReleasePage.backToInitialized();

        String text = openTheFailureMessage("could not be taken back to Initialized");
        assertTrue(text.contains("It is Draft again, and nothing was changed."), text);
        assertEquals("Draft", releaseState(work));
        assertEquals(held, getAPIFactory().getReleaseAPI().countManifests(work.release().getReleaseId()), "Nothing is removed.");
        assertEquals("ReleaseDraft", stateOf(work));
    }

    @Test
    @DisplayName("TC_19_3_TA_4")
    public void a_publication_that_fails_leaves_a_draft_whose_components_are_still_in_the_release_draft() {
        Work work = newWork(true);
        askForTheDraft(work);
        waitForReleaseState(work, "Draft");

        getAPIFactory().getReleaseAPI().makeTheWorkFail(work.release().getReleaseId());
        releasesWithFailure.add(work.release().getReleaseId());
        EditReleasePage editReleasePage = openTheRelease(work, "Draft");
        editReleasePage.publish();

        String text = openTheFailureMessage("could not be published");
        assertTrue(text.contains("It is Draft again, and nothing was changed."), text);
        assertEquals("Draft", releaseState(work));
        assertEquals("ReleaseDraft", stateOf(work));
    }

    @Test
    @DisplayName("TC_19_3_TA_5")
    public void a_release_left_in_the_middle_holds_its_library_until_it_is_given_back() {
        Work work = newWork(false);
        // the developer works on a component of the library, and on one of another library
        ReleaseObject working = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(work.library(), "Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(work.developer(), work.library());
        ACCObject sameLibrary = getAPIFactory().getCoreComponentAPI().createRandomACC(work.developer(), working, namespace, "WIP");
        LibraryObject other = getAPIFactory().getLibraryAPI().getLibraryByName("ADAPT Standard");
        NamespaceObject otherNamespace = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(work.developer(), other);
        ReleaseObject otherWorking = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(other, "Working");
        ACCObject otherLibrary = getAPIFactory().getCoreComponentAPI()
                .createRandomACC(work.developer(), otherWorking, otherNamespace, "WIP");

        getAPIFactory().getReleaseAPI().forceState(work.release().getReleaseId(), "Drafting");

        homePage = loginPage().signIn(work.developer().getLoginId(), work.developer().getPassword());
        // the list can be searched by the states of a release that is not in the middle of its work only
        EditReleasePage editReleasePage = openTheReleaseById(work);
        assertTrue(editReleasePage.getInProgressNotice().getText().contains("being made a draft"));

        // the library cannot be changed, and says why
        ViewEditCoreComponentPage coreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        ACCViewEditPage accPage = coreComponentPage.openACCViewEditPageByManifestID(sameLibrary.getAccManifestId());
        accPage.moveToDraft();
        String dialog = org.oagi.score.e2e.impl.PageHelper.visibilityOfElementLocated(getDriver(),
                By.xpath("//score-multi-actions-snack-bar")).getText();
        assertTrue(dialog.contains("A release of the library is being drafted or published"), dialog);
        assertEquals("WIP", getAPIFactory().getCoreComponentAPI().getACCByManifestId(sameLibrary.getAccManifestId()).getState());

        // another library can
        homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu()
                .openACCViewEditPageByManifestID(otherLibrary.getAccManifestId()).moveToDraft();
        assertEquals("Draft", getAPIFactory().getCoreComponentAPI().getACCByManifestId(otherLibrary.getAccManifestId()).getState());

        // given back, the release is as it was and the library can be changed again
        openTheReleaseById(work).recover();
        waitForReleaseState(work, "Initialized");
        homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu()
                .openACCViewEditPageByManifestID(sameLibrary.getAccManifestId()).moveToDraft();
        assertEquals("Draft", getAPIFactory().getCoreComponentAPI().getACCByManifestId(sameLibrary.getAccManifestId()).getState());
    }
}

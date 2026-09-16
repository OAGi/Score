package org.oagi.score.e2e.impl.page.code_list;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oagi.score.e2e.Configuration;
import org.oagi.score.e2e.api.APIFactory;
import org.oagi.score.e2e.page.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ViewEditCodeListPageImplTest {

    private WebDriver driver;
    private ViewEditCodeListPageImpl page;

    @BeforeEach
    void setUp() {
        driver = Configuration.load().newWebDriver();
        page = new ViewEditCodeListPageImpl(new StubBasePage(driver));
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void waits_for_a_delayed_overlay_after_opening_the_branch_select() {
        driver.get(dataUrl("""
                <div class="branch-selector">
                  <mat-select id="branch" aria-expanded="false" aria-controls="branch-panel"
                              style="display:block;width:160px;height:40px;background:#eee;">Branch</mat-select>
                </div>
                <div class="cdk-overlay-container"><div id="branch-panel" class="cdk-overlay-pane"></div></div>
                <script>
                  const select = document.querySelector('mat-select');
                  select.dataset.clicks = '0';
                  select.addEventListener('click', () => {
                    select.dataset.clicks = String(Number(select.dataset.clicks) + 1);
                    setTimeout(() => {
                      select.setAttribute('aria-expanded', 'true');
                      document.querySelector('#branch-panel').innerHTML =
                        '<input aria-label="dropdown search" style="display:block;width:160px;height:30px;">' +
                        '<mat-option><span>10.8.7.1</span></mat-option>';
                      document.querySelector('mat-option').addEventListener('click', () => {
                        select.dataset.selected = '10.8.7.1';
                        select.setAttribute('aria-expanded', 'false');
                      });
                    }, 350);
                  });
                </script>
                """));

        page.setBranch("10.8.7.1");

        WebElement select = driver.findElement(org.openqa.selenium.By.id("branch"));
        assertEquals("10.8.7.1", select.getAttribute("data-selected"));
        assertEquals("1", select.getAttribute("data-clicks"));
    }

    @Test
    void does_not_toggle_an_already_open_branch_select_when_the_first_attempt_fails() {
        driver.get(dataUrl("""
                <div class="branch-selector">
                  <mat-select id="branch" aria-expanded="true" aria-controls="branch-panel" data-clicks="0"
                              style="display:block;width:160px;height:40px;background:#eee;">
                    <div id="branch-panel" class="cdk-overlay-pane">
                    <input id="search" aria-label="dropdown search" disabled
                           style="display:block;width:160px;height:30px;">
                    <mat-option><span>10.8.7.1</span></mat-option>
                    </div>
                  </mat-select>
                </div>
                <div class="cdk-overlay-container"><div class="cdk-overlay-pane">Unrelated overlay</div></div>
                <script>
                  const select = document.querySelector('mat-select');
                  setTimeout(() => document.querySelector('#search').disabled = false, 350);
                  document.querySelector('mat-option').addEventListener('click', () => {
                    select.dataset.selected = '10.8.7.1';
                    select.setAttribute('aria-expanded', 'false');
                  });
                </script>
                """));

        page.setBranch("10.8.7.1");

        WebElement select = driver.findElement(org.openqa.selenium.By.id("branch"));
        assertEquals("10.8.7.1", select.getAttribute("data-selected"));
        assertEquals("0", select.getAttribute("data-clicks"));
    }

    @Test
    void waits_for_a_delayed_branch_selector_to_render() {
        driver.get(dataUrl("""
                <div class="title">Code List</div>
                <script>
                  setTimeout(() => document.body.insertAdjacentHTML('beforeend',
                    '<div class="branch-selector"><mat-select style="display:block;width:160px;height:40px;">Branch</mat-select></div>'),
                    1200);
                </script>
                """));

        long startedAt = System.nanoTime();
        page.getBranchSelectField();
        long elapsedMillis = (System.nanoTime() - startedAt) / 1_000_000;

        assertTrue(elapsedMillis >= 1000,
                "The page object must wait for the asynchronously rendered branch selector");
    }

    @Test
    void reopens_the_owned_panel_when_another_overlay_is_visible() {
        driver.get(dataUrl("""
                <div class="branch-selector">
                  <mat-select id="branch" aria-expanded="true" aria-controls="branch-panel"
                              style="display:block;width:160px;height:40px;background:#eee;">Branch</mat-select>
                </div>
                <div class="cdk-overlay-container">
                  <div class="cdk-overlay-pane">Unrelated overlay</div>
                </div>
                <script>
                  const select = document.querySelector('#branch');
                  document.addEventListener('keydown', event => {
                    if (event.key === 'Escape') select.setAttribute('aria-expanded', 'false');
                  });
                  select.addEventListener('click', () => {
                    select.setAttribute('aria-expanded', 'true');
                    document.body.insertAdjacentHTML('beforeend',
                      '<div id="branch-panel" class="cdk-overlay-pane">' +
                      '<input aria-label="dropdown search" style="display:block;width:160px;height:30px;">' +
                      '<mat-option>10.8.7.1</mat-option></div>');
                    document.querySelector('#branch-panel mat-option').addEventListener('click', () => {
                      select.setAttribute('data-selected', '10.8.7.1');
                      select.setAttribute('aria-expanded', 'false');
                    });
                  });
                </script>
                """));

        page.setBranch("10.8.7.1");

        assertEquals("10.8.7.1", driver.findElement(By.id("branch")).getAttribute("data-selected"));
    }

    private static String dataUrl(String html) {
        return "data:text/html;base64," + Base64.getEncoder()
                .encodeToString(html.getBytes(StandardCharsets.UTF_8));
    }

    private static final class StubBasePage implements BasePage {

        private final WebDriver driver;

        private StubBasePage(WebDriver driver) {
            this.driver = driver;
        }

        @Override
        public WebDriver getDriver() {
            return driver;
        }

        @Override
        public Configuration getConfig() {
            return null;
        }

        @Override
        public APIFactory getAPIFactory() {
            return null;
        }

        @Override
        public boolean isOpened() {
            return true;
        }

        @Override
        public void openPage() {
        }

        @Override
        public WebElement getTitle() {
            return null;
        }
    }

}

package org.oagi.score.e2e.impl.page.core_component;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oagi.score.e2e.Configuration;
import org.oagi.score.e2e.api.APIFactory;
import org.oagi.score.e2e.obj.NamespaceObject;
import org.oagi.score.e2e.page.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ACCExtensionViewEditPageImplTest {

    private WebDriver driver;
    private ACCExtensionViewEditPageImpl page;

    @BeforeEach
    void setUp() {
        driver = Configuration.load().newWebDriver();
        page = new ACCExtensionViewEditPageImpl(new StubBasePage(driver), null);
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void waits_for_a_delayed_namespace_dropdown_after_opening_the_select() {
        driver.get(dataUrl("""
                <mat-form-field>
                  <mat-label>Namespace</mat-label>
                  <mat-select id="namespace" aria-expanded="false" aria-controls="namespace-panel"
                              style="display:block;width:220px;height:40px;background:#eee;">Namespace</mat-select>
                </mat-form-field>
                <div class="cdk-overlay-container"><div id="namespace-panel" class="cdk-overlay-pane"></div></div>
                <script>
                  const select = document.querySelector('#namespace');
                  select.addEventListener('click', () => {
                    setTimeout(() => {
                      select.setAttribute('aria-expanded', 'true');
                      document.querySelector('#namespace-panel').innerHTML =
                        '<input aria-label="dropdown search" style="display:block;width:220px;height:30px;">' +
                        '<mat-option><span>https://example.test/ns</span></mat-option>';
                      document.querySelector('mat-option').addEventListener('click', () => {
                        select.setAttribute('data-selected', 'https://example.test/ns');
                        select.setAttribute('aria-expanded', 'false');
                      });
                    }, 350);
                  });
                </script>
                """));

        NamespaceObject namespace = new NamespaceObject();
        namespace.setUri("https://example.test/ns");
        page.setNamespace(namespace);

        WebElement select = driver.findElement(By.id("namespace"));
        assertEquals("https://example.test/ns", select.getAttribute("data-selected"));
    }

    @Test
    void selects_namespace_when_the_view_does_not_render_a_search_input() {
        driver.get(dataUrl("""
                <mat-form-field>
                  <mat-label>Namespace</mat-label>
                  <mat-select id="namespace" aria-expanded="false" aria-controls="namespace-panel"
                              style="display:block;width:220px;height:40px;background:#eee;">Namespace</mat-select>
                </mat-form-field>
                <div class="cdk-overlay-container"><div id="namespace-panel" class="cdk-overlay-pane"></div></div>
                <script>
                  const select = document.querySelector('#namespace');
                  select.addEventListener('click', () => {
                    select.setAttribute('aria-expanded', 'true');
                    document.querySelector('#namespace-panel').innerHTML =
                      '<mat-option>https://example.test/ns</mat-option>';
                    document.querySelector('mat-option').addEventListener('click', () => {
                      select.setAttribute('data-selected', 'https://example.test/ns');
                      select.setAttribute('aria-expanded', 'false');
                    });
                  });
                </script>
                """));

        NamespaceObject namespace = new NamespaceObject();
        namespace.setUri("https://example.test/ns");
        page.setNamespace(namespace);

        assertEquals("https://example.test/ns",
                driver.findElement(By.id("namespace")).getAttribute("data-selected"));
    }

    @Test
    void supports_a_select_local_popover_rendered_by_current_material() {
        driver.get(dataUrl("""
                <mat-form-field>
                  <mat-label>Namespace</mat-label>
                  <mat-select id="namespace" aria-expanded="false"
                              style="display:block;width:220px;height:40px;background:#eee;">Namespace</mat-select>
                </mat-form-field>
                <script>
                  const select = document.querySelector('#namespace');
                  select.addEventListener('click', () => {
                    select.setAttribute('aria-expanded', 'true');
                    select.innerHTML =
                      '<div class="cdk-overlay-pane">' +
                      '<input aria-label="dropdown search" style="display:block;width:220px;height:30px;">' +
                      '<mat-option>https://example.test/ns</mat-option></div>';
                    document.querySelector('mat-option').addEventListener('click', () => {
                      select.setAttribute('data-selected', 'https://example.test/ns');
                      select.setAttribute('aria-expanded', 'false');
                    });
                  });
                </script>
                """));

        NamespaceObject namespace = new NamespaceObject();
        namespace.setUri("https://example.test/ns");
        page.setNamespace(namespace);

        assertEquals("https://example.test/ns",
                driver.findElement(By.id("namespace")).getAttribute("data-selected"));
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

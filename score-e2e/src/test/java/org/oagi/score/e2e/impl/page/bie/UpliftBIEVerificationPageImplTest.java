package org.oagi.score.e2e.impl.page.bie;

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

import static org.junit.jupiter.api.Assertions.*;

class UpliftBIEVerificationPageImplTest {
    private WebDriver driver;
    private UpliftBIEVerificationPageImpl page;

    @BeforeEach
    void setUp() {
        driver = Configuration.load().newWebDriver();
        page = new UpliftBIEVerificationPageImpl(new StubBasePage(driver)) {
            @Override
            public WebElement goToNodeInSourceBIE(String path) {
                return driver.findElement(By.id("source"));
            }

            @Override
            public WebElement goToNodeInTargetBIE(String path) {
                return driver.findElement(By.id("target"));
            }
        };
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void reports_disabled_mapping_reason_and_paths() {
        for (String reason : new String[]{"BBIE attributes and elements cannot be mapped to each other",
                "Source and target node types must match", "Map the parent node first"}) {
            openFixture(true, reason);
            IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                    () -> page.mapNode("/Source", "/Target"));
            assertEquals("Cannot map '/Source' to '/Target': " + reason, error.getMessage());
            assertFalse(driver.findElement(By.id("selection")).isSelected());
        }
    }

    @Test
    void selects_an_enabled_mapping_without_confirmation() {
        openFixture(false, "Map Target");
        page.mapNode("/Source", "/Target");
        assertTrue(driver.findElement(By.id("selection")).isSelected());
    }

    private void openFixture(boolean disabled, String reason) {
        String html = """
                <mat-card-content class="mat-mdc-card-content">
                  <div></div><div>
                    <div><div id="source" class="mat-tree-node" data-query-path="Source">Source</div></div>
                    <div><div id="target" class="mat-tree-node" data-query-path="Target">
                      <mat-checkbox style="display:block">
                        <input id="selection" type="checkbox" aria-label="%s" %s>
                        <button class="mat-mdc-checkbox-touch-target"
                                onclick="document.getElementById('selection').click()">Target</button>
                      </mat-checkbox>
                    </div></div>
                  </div>
                </mat-card-content>
                """.formatted(reason, disabled ? "disabled" : "");
        driver.get("data:text/html;base64," + Base64.getEncoder()
                .encodeToString(html.getBytes(StandardCharsets.UTF_8)));
    }

    private record StubBasePage(WebDriver driver) implements BasePage {
        public WebDriver getDriver() { return driver; }
        public Configuration getConfig() { return null; }
        public APIFactory getAPIFactory() { return null; }
        public boolean isOpened() { return true; }
        public void openPage() { }
        public WebElement getTitle() { return null; }
    }
}

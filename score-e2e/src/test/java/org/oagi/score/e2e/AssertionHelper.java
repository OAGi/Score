package org.oagi.score.e2e;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.impl.PageHelper.waitFor;

public class AssertionHelper {

    private AssertionHelper() {
    }

    public static void assertChecked(WebElement element) {
        try {
            assertEquals("true", element.getAttribute("aria-checked"));
        } catch (Error e) {
            try {
                assertEquals("true", element.getAttribute("ng-reflect-checked"));
            } catch (Error retry) {
                try {
                    assertTrue(element.getAttribute("class").contains("mat-mdc-checkbox-checked"));
                } catch (Error retryfirst) {
                    assertEquals("true", element.getAttribute("aria-checked"));
                }
            }
        }
    }

    public static void assertNotChecked(WebElement element) {
        try {
            assertEquals("false", element.getAttribute("aria-checked"));
        } catch (Error e) {
            try {
                assertEquals("false", element.getAttribute("ng-reflect-checked"));
            } catch (Error retry) {
                assertFalse(element.getAttribute("class").contains("mat-mdc-checkbox-checked"));
            }
        }
    }

    public static void assertEnabled(WebElement element) {
        try {
            assertEquals("false", element.getAttribute("ng-reflect-disabled"));
        } catch (Error | Exception rerun) {
            assertEquals(true, element.isEnabled());
        }
    }

    public static void assertDisabled(WebElement element) {
        waitFor(ofMillis(500L));
        if ("mat-checkbox".equals(element.getTagName())) {
            try {
                WebElement inputCheckbox = element.findElement(By.tagName("input"));
                assertNotNull(inputCheckbox.getAttribute("disabled"));
            } catch (NoSuchElementException e) {
                assertEquals("true", element.getAttribute("ng-reflect-disabled"));
            }
            return;
        }

        try {
            assertEquals("true", element.getAttribute("ng-reflect-disabled"));
        } catch (Error | Exception rerun) {
            try {
                assertEquals("true", element.getAttribute("aria-disabled"));
            } catch (Error | Exception rerun2) {
                try {
                    assertEquals("true", element.getAttribute("disabled"));
                } catch (Error | Exception e) {
                    try {
                        assertTrue(element.getAttribute("class").contains("mat-mdc-checkbox-disabled"));
                    } catch (Error rerun3) {
                        try {
                            assertTrue(!element.isEnabled());
                        } catch (Error rerun4) {
                            element.sendKeys("abc");
                            waitFor(ofMillis(300L));
                            String fieldNameVal = element.getAttribute("value");
                            assertTrue(!"abc".equals(fieldNameVal));
                        }
                    }
                }
            }
        }
    }
}

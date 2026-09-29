package org.oagi.score.e2e;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;

import java.util.function.Supplier;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.impl.PageHelper.waitFor;

public class AssertionHelper {

    private static final int STALE_ELEMENT_RETRY_COUNT = 5;

    private AssertionHelper() {
    }

    public static void assertChecked(WebElement element) {
        if ("mat-checkbox".equals(element.getTagName())) {
            try {
                WebElement inputCheckbox = element.findElement(By.tagName("input"));
                if (inputCheckbox.isSelected() || "true".equals(inputCheckbox.getAttribute("aria-checked")) || "true".equals(inputCheckbox.getAttribute("checked"))) {
                    return;
                }
            } catch (NoSuchElementException ignored) {
            }
            String cls = element.getAttribute("class");
            if (cls != null && (cls.contains("mat-mdc-checkbox-checked") || cls.contains("mat-checkbox-checked") || cls.contains("mdc-checkbox--selected"))) {
                return;
            }
            if ("true".equals(element.getAttribute("aria-checked")) || "true".equals(element.getAttribute("ng-reflect-checked"))) {
                return;
            }
            try {
                WebElement inputCheckbox = element.findElement(By.tagName("input"));
                assertTrue(inputCheckbox.isSelected(), "Expected mat-checkbox to be checked");
            } catch (Exception e) {
                fail("Expected mat-checkbox to be checked");
            }
            return;
        }

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
        if ("mat-checkbox".equals(element.getTagName())) {
            try {
                WebElement inputCheckbox = element.findElement(By.tagName("input"));
                if (!inputCheckbox.isSelected() && !"true".equals(inputCheckbox.getAttribute("aria-checked"))) {
                    return;
                }
            } catch (NoSuchElementException ignored) {
            }
            String cls = element.getAttribute("class");
            if (cls != null && !cls.contains("mat-mdc-checkbox-checked") && !cls.contains("mat-checkbox-checked") && !cls.contains("mdc-checkbox--selected")) {
                return;
            }
            if ("false".equals(element.getAttribute("aria-checked")) || "false".equals(element.getAttribute("ng-reflect-checked"))) {
                return;
            }
            try {
                WebElement inputCheckbox = element.findElement(By.tagName("input"));
                assertFalse(inputCheckbox.isSelected(), "Expected mat-checkbox to be unchecked");
            } catch (Exception e) {
                fail("Expected mat-checkbox to be unchecked");
            }
            return;
        }

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
        assertFalse(isReadOnly(element), "Expected element to be editable");
        try {
            assertEquals("false", element.getAttribute("ng-reflect-disabled"));
        } catch (Error | Exception rerun) {
            assertEquals(true, element.isEnabled());
        }
    }

    public static void assertDisabled(WebElement element) {
        waitFor(ofMillis(500L));
        assertDisabledNow(element);
    }

    public static void assertDisabled(Supplier<WebElement> elementSupplier) {
        waitFor(ofMillis(500L));
        // The element supplier re-locates the element on every attempt, so a re-render of the
        // surrounding Angular panel between lookup and inspection does not fail the assertion.
        for (int attempt = 1; ; attempt++) {
            try {
                assertDisabledNow(elementSupplier.get());
                return;
            } catch (StaleElementReferenceException e) {
                if (attempt >= STALE_ELEMENT_RETRY_COUNT) {
                    throw e;
                }
                waitFor(ofMillis(300L));
            }
        }
    }

    private static void assertDisabledNow(WebElement element) {
        if (isReadOnly(element)) {
            return;
        }
        if ("mat-checkbox".equals(element.getTagName())) {
            try {
                WebElement inputCheckbox = element.findElement(By.tagName("input"));
                String disabledAttr = inputCheckbox.getAttribute("disabled");
                if (disabledAttr != null) {
                    return;
                }
                String ariaDisabled = inputCheckbox.getAttribute("aria-disabled");
                if ("true".equals(ariaDisabled)) {
                    return;
                }
                if (!inputCheckbox.isEnabled()) {
                    return;
                }
            } catch (NoSuchElementException ignored) {
            }
            String cls = element.getAttribute("class");
            if (cls != null && (cls.contains("mat-mdc-checkbox-disabled") || cls.contains("mat-checkbox-disabled"))) {
                return;
            }
            if ("true".equals(element.getAttribute("aria-disabled")) || "true".equals(element.getAttribute("ng-reflect-disabled"))) {
                return;
            }
            assertFalse(element.isEnabled());
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
                        // MDC radio buttons expose disabled state on the host class while
                        // Selenium still reports the custom element itself as enabled.
                        String classes = element.getAttribute("class");
                        assertTrue(classes.contains("mat-mdc-checkbox-disabled")
                                || classes.contains("mat-mdc-radio-disabled")
                                || classes.contains("mat-radio-disabled"));
                    } catch (Error rerun3) {
                        assertEquals(false, element.isEnabled());
                    }
                }
            }
        }
    }

    /**
     * Textareas used for definition fields are intentionally rendered as readonly rather than disabled
     * so their content remains selectable and copyable. Treat both states as non-editable in assertions.
     */
    private static boolean isReadOnly(WebElement element) {
        return element.getAttribute("readonly") != null;
    }
}

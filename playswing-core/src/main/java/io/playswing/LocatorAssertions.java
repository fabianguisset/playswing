package io.playswing;

import java.awt.Component;
import java.util.List;

/**
 * Provides Playwright-style assertions for a {@link Locator}, following the
 * {@code expect(locator).toBeVisible()} convention.
 *
 * <p>Assertion failures throw {@link AssertionError} with a descriptive message.
 */
public class LocatorAssertions {

    private final Locator locator;

    LocatorAssertions(Locator locator) {
        this.locator = locator;
    }

    /**
     * Asserts that the matched component is visible (shown on screen).
     */
    public LocatorAssertions toBeVisible() {
        if (!locator.isVisible()) {
            throw new AssertionError("Expected component to be visible, but it was not: " + locator);
        }
        return this;
    }

    /**
     * Asserts that the matched component is hidden (not visible or not showing).
     */
    public LocatorAssertions toBeHidden() {
        if (locator.isVisible()) {
            throw new AssertionError("Expected component to be hidden, but it was visible: " + locator);
        }
        return this;
    }

    /**
     * Asserts that the matched component is enabled.
     */
    public LocatorAssertions toBeEnabled() {
        if (!locator.isEnabled()) {
            throw new AssertionError("Expected component to be enabled, but it was disabled: " + locator);
        }
        return this;
    }

    /**
     * Asserts that the matched component is disabled.
     */
    public LocatorAssertions toBeDisabled() {
        if (locator.isEnabled()) {
            throw new AssertionError("Expected component to be disabled, but it was enabled: " + locator);
        }
        return this;
    }

    /**
     * Asserts that the toggle button (checkbox/radio) is checked.
     */
    public LocatorAssertions toBeChecked() {
        if (!locator.isChecked()) {
            throw new AssertionError("Expected component to be checked, but it was not: " + locator);
        }
        return this;
    }

    /**
     * Asserts that the toggle button (checkbox/radio) is unchecked.
     */
    public LocatorAssertions toBeUnchecked() {
        if (locator.isChecked()) {
            throw new AssertionError("Expected component to be unchecked, but it was checked: " + locator);
        }
        return this;
    }

    /**
     * Asserts that the matched component has the given visible text.
     */
    public LocatorAssertions toHaveText(String expected) {
        String actual = locator.getText();
        if (!expected.equals(actual)) {
            throw new AssertionError("Expected text <" + expected + "> but was <" + actual + "> for: " + locator);
        }
        return this;
    }

    /**
     * Asserts that the matched component text contains the given substring.
     */
    public LocatorAssertions toContainText(String substring) {
        String actual = locator.getText();
        if (actual == null || !actual.contains(substring)) {
            throw new AssertionError(
                    "Expected text to contain <" + substring + "> but was <" + actual + "> for: " + locator);
        }
        return this;
    }

    /**
     * Asserts that the matched text component has the given input value.
     */
    public LocatorAssertions toHaveValue(String expected) {
        String actual = locator.getValue();
        if (!expected.equals(actual)) {
            throw new AssertionError("Expected value <" + expected + "> but was <" + actual + "> for: " + locator);
        }
        return this;
    }

    /**
     * Asserts that the number of matched components equals {@code count}.
     */
    public LocatorAssertions toHaveCount(int count) {
        List<Component> all = locator.resolveAll();
        if (all.size() != count) {
            throw new AssertionError(
                    "Expected " + count + " component(s) but found " + all.size() + " for: " + locator);
        }
        return this;
    }
}


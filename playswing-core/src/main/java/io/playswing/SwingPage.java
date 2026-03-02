package io.playswing;

import io.playswing.internal.ComponentFinder;
import io.playswing.internal.ScreenshotCapture;

import java.awt.*;
import java.util.List;
import java.util.Objects;
import javax.swing.*;

/**
 * Represents a single Swing window (JFrame, JDialog, or other Window), analogous to
 * Playwright's {@code Page}.
 *
 * <p>Obtain instances via {@link SwingApplication#getPage()} or
 * {@link SwingApplication#getPages()}.
 */
public class SwingPage {

    private final Window window;
    private final ComponentFinder finder = new ComponentFinder();
    private final ScreenshotCapture screenshotCapture = new ScreenshotCapture();

    SwingPage(Window window) {
        this.window = Objects.requireNonNull(window, "window must not be null");
    }

    // ---- Locators ----

    /**
     * Returns a {@link Locator} for the given CSS-like selector.
     *
     * <p>Examples:
     * <ul>
     *   <li>{@code "button"} – all JButton components</li>
     *   <li>{@code "button[text=OK]"} – JButton with text "OK"</li>
     *   <li>{@code "#submitBtn"} – component with name "submitBtn"</li>
     *   <li>{@code "[text=Hello]"} – any component with text "Hello"</li>
     * </ul>
     */
    public Locator locator(String selector) {
        return new Locator(this, selector, null);
    }

    /**
     * Returns a {@link Locator} for components with the given visible text.
     */
    public Locator getByText(String text) {
        return new TextLocator(this, text, false);
    }

    /**
     * Returns a {@link Locator} for components whose text contains the given substring.
     */
    public Locator getByPartialText(String text) {
        return new TextLocator(this, text, true);
    }

    /**
     * Returns a {@link Locator} for components with the given accessible role.
     */
    public Locator getByRole(AriaRole role) {
        return locator(role.name().toLowerCase());
    }

    /**
     * Returns a {@link Locator} for the input component labeled by the given text.
     */
    public Locator getByLabel(String labelText) {
        return new LabelLocator(this, labelText);
    }

    /**
     * Returns a {@link Locator} for components with the given component name.
     */
    public Locator getByName(String name) {
        return locator("#" + name);
    }

    /**
     * Returns a {@link Locator} for text fields with the given placeholder.
     */
    public Locator getByPlaceholder(String placeholder) {
        return new PlaceholderLocator(this, placeholder);
    }

    // ---- Screenshot ----

    /**
     * Takes a screenshot of this window and returns the PNG bytes.
     */
    public byte[] screenshot() {
        try {
            return screenshotCapture.captureWindow(window);
        } catch (Exception e) {
            throw new PlaySwingException("Screenshot failed", e);
        }
    }

    // ---- Window info ----

    /**
     * Returns the window title.
     */
    public String getTitle() {
        if (window instanceof Frame) return ((Frame) window).getTitle();
        if (window instanceof Dialog) return ((Dialog) window).getTitle();
        return "";
    }

    /**
     * Returns true if the window is currently visible.
     */
    public boolean isVisible() {
        return window.isVisible();
    }

    /**
     * Closes this window.
     */
    public void close() {
        SwingUtilities.invokeLater(window::dispose);
    }

    /**
     * Waits until the window is fully loaded (EDT is idle).
     */
    public void waitForLoadState() {
        try {
            SwingUtilities.invokeAndWait(() -> { /* flush EDT */ });
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }
    }

    // ---- Package-private ----

    Window getWindow() {
        return window;
    }

    // ---- Specialised locator subclasses ----

    private static class TextLocator extends Locator {
        private final String text;
        private final boolean partial;
        private final ComponentFinder finder = new ComponentFinder();

        TextLocator(SwingPage page, String text, boolean partial) {
            super(page, (partial ? "~" : "=") + text, null);
            this.text = text;
            this.partial = partial;
        }

        @Override
        public List<Component> resolveAll() {
            Container root = page.getWindow();
            return partial ? finder.findByPartialText(root, text) : finder.findByText(root, text);
        }
    }

    private static class LabelLocator extends Locator {
        private final String labelText;
        private final ComponentFinder finder = new ComponentFinder();

        LabelLocator(SwingPage swingPage, String labelText) {
            super(swingPage, "label:" + labelText, null);
            this.labelText = labelText;
        }

        @Override
        public List<Component> resolveAll() {
            Container root = page.getWindow();
            return finder.findByLabel(root, labelText);
        }
    }

    private static class PlaceholderLocator extends Locator {
        private final String placeholder;
        private final ComponentFinder finder = new ComponentFinder();

        PlaceholderLocator(SwingPage swingPage, String placeholder) {
            super(swingPage, "placeholder:" + placeholder, null);
            this.placeholder = placeholder;
        }

        @Override
        public List<Component> resolveAll() {
            Container root = page.getWindow();
            return finder.findByPlaceholder(root, placeholder);
        }
    }
}

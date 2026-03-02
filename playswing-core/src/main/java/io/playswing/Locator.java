package io.playswing;

import io.playswing.internal.ComponentFinder;
import io.playswing.internal.ScreenshotCapture;

import java.awt.*;
import java.awt.event.InputEvent;
import java.util.List;
import javax.swing.*;
import javax.swing.text.JTextComponent;

/**
 * Represents a reference to one or more Swing components, analogous to Playwright's {@code Locator}.
 *
 * <p>Locators are lazy: the component is resolved only when an action or query is performed.
 * Chaining is supported: {@code page.locator("panel").locator("button[text=OK]")}.
 */
public class Locator {

    final SwingPage page;
    private final String selector;
    /** Optional parent context for chained locators. */
    private final Container context;

    private final ComponentFinder finder = new ComponentFinder();
    private final ScreenshotCapture screenshotCapture = new ScreenshotCapture();

    Locator(SwingPage page, String selector, Container context) {
        this.page = page;
        this.selector = selector;
        this.context = context;
    }

    // ---- Chaining ----

    /**
     * Returns a new locator scoped to the first component matched by this locator.
     */
    public Locator locator(String childSelector) {
        Component parent = resolveFirst();
        if (!(parent instanceof Container)) {
            throw new PlaySwingException("Component for '" + selector + "' is not a container");
        }
        return new Locator(page, childSelector, (Container) parent);
    }

    // ---- Actions ----

    /**
     * Clicks the matched component.
     */
    public void click() {
        Component c = resolveFirst();
        if (c instanceof AbstractButton) {
            invokeAndWait(() -> ((AbstractButton) c).doClick());
        } else {
            clickWithRobot(c);
        }
    }

    /**
     * Fills the text component with the given value, replacing any existing content.
     */
    public void fill(String text) {
        Component c = resolveFirst();
        if (c instanceof JTextComponent) {
            invokeAndWait(() -> {
                JTextComponent tc = (JTextComponent) c;
                tc.requestFocusInWindow();
                tc.setText(text);
            });
        } else {
            throw new PlaySwingException("Component for '" + selector + "' is not a text component");
        }
    }

    /**
     * Clears the content of a text component.
     */
    public void clear() {
        fill("");
    }

    /**
     * Presses a key, e.g. {@code "Enter"}, {@code "Tab"}, {@code "ArrowDown"}.
     *
     * <p>Single-character strings are typed directly. Named keys use the Playwright key name
     * convention.
     */
    public void press(String key) {
        Component c = resolveFirst();
        invokeAndWait(c::requestFocusInWindow);
        pressKey(key);
    }

    /**
     * Selects an option in a {@link JComboBox} or {@link JList} by visible text.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void selectOption(String value) {
        Component c = resolveFirst();
        if (c instanceof JComboBox) {
            JComboBox cb = (JComboBox) c;
            invokeAndWait(() -> {
                for (int i = 0; i < cb.getItemCount(); i++) {
                    Object item = cb.getItemAt(i);
                    if (value.equals(item == null ? null : item.toString())) {
                        cb.setSelectedIndex(i);
                        return;
                    }
                }
                throw new PlaySwingException("Option '" + value + "' not found in combobox");
            });
        } else if (c instanceof JList) {
            JList list = (JList) c;
            invokeAndWait(() -> {
                ListModel model = list.getModel();
                for (int i = 0; i < model.getSize(); i++) {
                    Object item = model.getElementAt(i);
                    if (value.equals(item == null ? null : item.toString())) {
                        list.setSelectedIndex(i);
                        return;
                    }
                }
                throw new PlaySwingException("Option '" + value + "' not found in list");
            });
        } else {
            throw new PlaySwingException("Component for '" + selector + "' is not a JComboBox or JList");
        }
    }

    /**
     * Checks a {@link JCheckBox} or {@link JRadioButton}.
     */
    public void check() {
        Component c = resolveFirst();
        if (c instanceof JToggleButton) {
            invokeAndWait(() -> {
                JToggleButton btn = (JToggleButton) c;
                if (!btn.isSelected()) {
                    btn.doClick();
                }
            });
        } else {
            throw new PlaySwingException("Component for '" + selector + "' is not a toggle button");
        }
    }

    /**
     * Unchecks a {@link JCheckBox} or {@link JRadioButton}.
     */
    public void uncheck() {
        Component c = resolveFirst();
        if (c instanceof JToggleButton) {
            invokeAndWait(() -> {
                JToggleButton btn = (JToggleButton) c;
                if (btn.isSelected()) {
                    btn.doClick();
                }
            });
        } else {
            throw new PlaySwingException("Component for '" + selector + "' is not a toggle button");
        }
    }

    // ---- Queries ----

    /**
     * Returns the visible text of the matched component.
     */
    public String getText() {
        Component c = resolveFirst();
        return finder.getComponentText(c);
    }

    /**
     * Returns the value of the matched component (e.g. text field content).
     */
    public String getValue() {
        Component c = resolveFirst();
        return finder.getComponentValue(c);
    }

    /**
     * Returns true if the matched component is currently visible on screen.
     */
    public boolean isVisible() {
        try {
            Component c = resolveFirst();
            return c.isVisible() && c.isShowing();
        } catch (PlaySwingException e) {
            return false;
        }
    }

    /**
     * Returns true if the matched component is enabled.
     */
    public boolean isEnabled() {
        return resolveFirst().isEnabled();
    }

    /**
     * Returns true if the matched toggle button (checkbox/radio) is selected.
     */
    public boolean isChecked() {
        Component c = resolveFirst();
        if (c instanceof JToggleButton) {
            return ((JToggleButton) c).isSelected();
        }
        throw new PlaySwingException("Component for '" + selector + "' is not a toggle button");
    }

    /**
     * Returns the number of components matching this locator.
     */
    public int count() {
        return resolveAll().size();
    }

    /**
     * Returns a locator targeting only the n-th match (0-based).
     */
    public Locator nth(int index) {
        List<Component> all = resolveAll();
        if (index < 0 || index >= all.size()) {
            throw new PlaySwingException("Index " + index + " out of range, found " + all.size());
        }
        return new ResolvedLocator(page, selector, all.get(index));
    }

    /**
     * Returns a locator targeting the first match.
     */
    public Locator first() {
        return nth(0);
    }

    /**
     * Returns a locator targeting the last match.
     */
    public Locator last() {
        List<Component> all = resolveAll();
        return nth(all.size() - 1);
    }

    /**
     * Returns assertions for this locator, following Playwright's {@code expect(locator)} style.
     */
    public LocatorAssertions expect() {
        return new LocatorAssertions(this);
    }

    // ---- Package-private resolution ----

    /** Resolves this locator to the first matching component. */
    Component resolveFirst() {
        List<Component> all = resolveAll();
        if (all.isEmpty()) {
            throw new PlaySwingException("No component found for selector: '" + selector + "'");
        }
        return all.get(0);
    }

    /** Resolves this locator to all matching components. */
    public List<Component> resolveAll() {
        Container root = context != null ? context : page.getWindow();
        if (root == null) {
            throw new PlaySwingException("No window available on page");
        }
        return finder.findBySelector(root, selector);
    }

    // ---- Private helpers ----

    private void invokeAndWait(Runnable r) {
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(r);
            } catch (Exception e) {
                if (e.getCause() instanceof PlaySwingException) {
                    throw (PlaySwingException) e.getCause();
                }
                throw new PlaySwingException("Action failed on EDT", e);
            }
        }
    }

    private void clickWithRobot(Component c) {
        try {
            Point loc = c.getLocationOnScreen();
            int x = loc.x + c.getWidth() / 2;
            int y = loc.y + c.getHeight() / 2;
            Robot robot = new Robot();
            robot.mouseMove(x, y);
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
            robot.waitForIdle();
        } catch (AWTException e) {
            throw new PlaySwingException("Robot click failed", e);
        }
    }

    private void pressKey(String key) {
        try {
            Robot robot = new Robot();
            Integer keyCode = ComponentFinder.KEY_MAP.get(key);
            if (keyCode != null) {
                robot.keyPress(keyCode);
                robot.keyRelease(keyCode);
            } else if (key.length() == 1) {
                char ch = key.charAt(0);
                robot.keyPress(Character.toUpperCase(ch));
                robot.keyRelease(Character.toUpperCase(ch));
            } else {
                throw new PlaySwingException("Unknown key: '" + key + "'");
            }
            robot.waitForIdle();
        } catch (AWTException e) {
            throw new PlaySwingException("Robot key press failed", e);
        }
    }

    // ---- Inner class for resolved (already-known) component ----

    /**
     * A Locator that wraps an already-resolved component, used by nth/first/last.
     */
    private static class ResolvedLocator extends Locator {
        private final Component resolved;

        ResolvedLocator(SwingPage page, String selector, Component resolved) {
            super(page, selector, null);
            this.resolved = resolved;
        }

        @Override
        Component resolveFirst() {
            return resolved;
        }

        @Override
        public List<Component> resolveAll() {
            return List.of(resolved);
        }
    }
}

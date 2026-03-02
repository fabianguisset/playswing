package io.playswing.internal;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.List;
import java.util.function.Predicate;
import javax.swing.*;
import javax.swing.text.JTextComponent;

/**
 * Internal helper for finding Swing components by various criteria.
 */
public class ComponentFinder {

    /** Maps role names (Playwright-style) to Swing component classes. */
    private static final Map<String, Class<? extends Component>> ROLE_MAP = new LinkedHashMap<>();

    static {
        ROLE_MAP.put("button", JButton.class);
        ROLE_MAP.put("checkbox", JCheckBox.class);
        ROLE_MAP.put("radiobutton", JRadioButton.class);
        ROLE_MAP.put("combobox", JComboBox.class);
        ROLE_MAP.put("list", JList.class);
        ROLE_MAP.put("table", JTable.class);
        ROLE_MAP.put("tree", JTree.class);
        ROLE_MAP.put("textbox", JTextComponent.class);
        ROLE_MAP.put("textfield", JTextField.class);
        ROLE_MAP.put("textarea", JTextArea.class);
        ROLE_MAP.put("passwordfield", JPasswordField.class);
        ROLE_MAP.put("label", JLabel.class);
        ROLE_MAP.put("slider", JSlider.class);
        ROLE_MAP.put("spinner", JSpinner.class);
        ROLE_MAP.put("menuitem", JMenuItem.class);
        ROLE_MAP.put("menu", JMenu.class);
        ROLE_MAP.put("tab", JTabbedPane.class);
        ROLE_MAP.put("dialog", JDialog.class);
        ROLE_MAP.put("panel", JPanel.class);
    }

    /** Key mappings from Playwright-style names to Java KeyEvent codes. */
    public static final Map<String, Integer> KEY_MAP = new LinkedHashMap<>();

    static {
        KEY_MAP.put("Enter", KeyEvent.VK_ENTER);
        KEY_MAP.put("Tab", KeyEvent.VK_TAB);
        KEY_MAP.put("Escape", KeyEvent.VK_ESCAPE);
        KEY_MAP.put("Backspace", KeyEvent.VK_BACK_SPACE);
        KEY_MAP.put("Delete", KeyEvent.VK_DELETE);
        KEY_MAP.put("ArrowUp", KeyEvent.VK_UP);
        KEY_MAP.put("ArrowDown", KeyEvent.VK_DOWN);
        KEY_MAP.put("ArrowLeft", KeyEvent.VK_LEFT);
        KEY_MAP.put("ArrowRight", KeyEvent.VK_RIGHT);
        KEY_MAP.put("Home", KeyEvent.VK_HOME);
        KEY_MAP.put("End", KeyEvent.VK_END);
        KEY_MAP.put("PageUp", KeyEvent.VK_PAGE_UP);
        KEY_MAP.put("PageDown", KeyEvent.VK_PAGE_DOWN);
        KEY_MAP.put("F1", KeyEvent.VK_F1);
        KEY_MAP.put("F2", KeyEvent.VK_F2);
        KEY_MAP.put("F3", KeyEvent.VK_F3);
        KEY_MAP.put("F4", KeyEvent.VK_F4);
        KEY_MAP.put("F5", KeyEvent.VK_F5);
        KEY_MAP.put("F6", KeyEvent.VK_F6);
        KEY_MAP.put("F7", KeyEvent.VK_F7);
        KEY_MAP.put("F8", KeyEvent.VK_F8);
        KEY_MAP.put("F9", KeyEvent.VK_F9);
        KEY_MAP.put("F10", KeyEvent.VK_F10);
        KEY_MAP.put("F11", KeyEvent.VK_F11);
        KEY_MAP.put("F12", KeyEvent.VK_F12);
    }

    /**
     * Finds all components within {@code root} matching the given selector.
     *
     * <p>Selector syntax:
     * <ul>
     *   <li>{@code button} – by role name (maps to JButton)</li>
     *   <li>{@code JButton} – by exact class name</li>
     *   <li>{@code #name} – by component name</li>
     *   <li>{@code [text=value]} – by text/label content</li>
     *   <li>{@code [name=value]} – by component name</li>
     *   <li>{@code button[text=OK]} – role + attribute</li>
     * </ul>
     */
    public List<Component> findBySelector(Container root, String selector) {
        selector = selector.trim();

        // Descendant combinator: "a > b" or "a b"
        if (selector.contains(" > ")) {
            String[] parts = selector.split(" > ", 2);
            List<Component> parents = findBySelector(root, parts[0].trim());
            List<Component> result = new ArrayList<>();
            for (Component parent : parents) {
                if (parent instanceof Container) {
                    result.addAll(findBySelector((Container) parent, parts[1].trim()));
                }
            }
            return result;
        }

        // Parse type and attributes: e.g. button[text=OK][name=btn1]
        String type = null;
        Map<String, String> attrs = new LinkedHashMap<>();

        int bracketIdx = selector.indexOf('[');
        if (bracketIdx >= 0) {
            type = selector.substring(0, bracketIdx).trim();
            String attrString = selector.substring(bracketIdx);
            // parse all [key=value] pairs
            int i = 0;
            while (i < attrString.length()) {
                if (attrString.charAt(i) == '[') {
                    int end = attrString.indexOf(']', i);
                    if (end < 0) break;
                    String pair = attrString.substring(i + 1, end);
                    int eq = pair.indexOf('=');
                    if (eq >= 0) {
                        String key = pair.substring(0, eq).trim();
                        String val = pair.substring(eq + 1).trim();
                        // strip quotes
                        if ((val.startsWith("\"") && val.endsWith("\""))
                                || (val.startsWith("'") && val.endsWith("'"))) {
                            val = val.substring(1, val.length() - 1);
                        }
                        attrs.put(key, val);
                    }
                    i = end + 1;
                } else {
                    i++;
                }
            }
        } else if (selector.startsWith("#")) {
            // #name shorthand
            attrs.put("name", selector.substring(1));
        } else {
            type = selector;
        }

        final String finalType = type;
        final Map<String, String> finalAttrs = attrs;

        Predicate<Component> predicate = c -> matchesType(c, finalType) && matchesAttrs(c, finalAttrs);
        return findAll(root, predicate);
    }

    /** Finds components by visible text content (label, button text, etc.). */
    public List<Component> findByText(Container root, String text) {
        return findAll(root, c -> text.equals(getComponentText(c)));
    }

    /** Finds components by partial text content. */
    public List<Component> findByPartialText(Container root, String text) {
        return findAll(root, c -> {
            String t = getComponentText(c);
            return t != null && t.contains(text);
        });
    }

    /** Finds components by accessible role. */
    public List<Component> findByRole(Container root, String role) {
        return findAll(root, c -> matchesRole(c, role));
    }

    /** Finds components by component name. */
    public List<Component> findByName(Container root, String name) {
        return findAll(root, c -> name.equals(c.getName()));
    }

    /**
     * Finds the input component that is labeled by a JLabel with the given text.
     * The labeled component is either the label's labelFor target or the next focusable
     * sibling in the parent container.
     */
    public List<Component> findByLabel(Container root, String labelText) {
        List<Component> labels = findAll(root,
                c -> c instanceof JLabel && labelText.equals(((JLabel) c).getText()));
        List<Component> result = new ArrayList<>();
        for (Component lc : labels) {
            JLabel label = (JLabel) lc;
            if (label.getLabelFor() != null) {
                result.add(label.getLabelFor());
            } else {
                // Find next focusable sibling
                Container parent = label.getParent();
                if (parent != null) {
                    Component[] siblings = parent.getComponents();
                    boolean found = false;
                    for (Component sibling : siblings) {
                        if (found && sibling.isFocusable() && !(sibling instanceof JLabel)) {
                            result.add(sibling);
                            break;
                        }
                        if (sibling == label) {
                            found = true;
                        }
                    }
                }
            }
        }
        return result;
    }

    /** Finds text components by placeholder text. */
    public List<Component> findByPlaceholder(Container root, String placeholder) {
        return findAll(root, c -> {
            if (c instanceof JTextField) {
                Object ph = ((JTextField) c).getClientProperty("placeholder");
                return placeholder.equals(ph);
            }
            return false;
        });
    }

    /** Returns the visible text of a component, or null if none. */
    public String getComponentText(Component c) {
        if (c instanceof AbstractButton) {
            return ((AbstractButton) c).getText();
        }
        if (c instanceof JLabel) {
            return ((JLabel) c).getText();
        }
        if (c instanceof JTextComponent) {
            return ((JTextComponent) c).getText();
        }
        if (c instanceof JComboBox) {
            Object sel = ((JComboBox<?>) c).getSelectedItem();
            return sel != null ? sel.toString() : null;
        }
        return null;
    }

    /** Returns the value of a text component. */
    public String getComponentValue(Component c) {
        if (c instanceof JTextComponent) {
            return ((JTextComponent) c).getText();
        }
        if (c instanceof JComboBox) {
            Object sel = ((JComboBox<?>) c).getSelectedItem();
            return sel != null ? sel.toString() : null;
        }
        if (c instanceof JSpinner) {
            Object val = ((JSpinner) c).getValue();
            return val != null ? val.toString() : null;
        }
        return null;
    }

    /** BFS traversal of the component tree, returning all matches. */
    public List<Component> findAll(Container root, Predicate<Component> predicate) {
        List<Component> result = new ArrayList<>();
        Queue<Component> queue = new ArrayDeque<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            Component current = queue.poll();
            if (predicate.test(current)) {
                result.add(current);
            }
            if (current instanceof Container) {
                Container container = (Container) current;
                for (Component child : container.getComponents()) {
                    queue.add(child);
                }
                // Also traverse JMenuBar menus
                if (current instanceof JFrame) {
                    JMenuBar mb = ((JFrame) current).getJMenuBar();
                    if (mb != null) queue.add(mb);
                }
            }
        }
        return result;
    }

    // ---- Private helpers ----

    private boolean matchesType(Component c, String type) {
        if (type == null || type.isEmpty()) return true;
        // Try role name first
        if (matchesRole(c, type)) return true;
        // Try exact class name
        String simpleName = c.getClass().getSimpleName();
        return simpleName.equals(type) || c.getClass().getName().equals(type);
    }

    private boolean matchesRole(Component c, String role) {
        if (role == null || role.isEmpty()) return true;
        Class<? extends Component> expected = ROLE_MAP.get(role.toLowerCase());
        if (expected == null) return false;
        return expected.isInstance(c);
    }

    private boolean matchesAttrs(Component c, Map<String, String> attrs) {
        for (Map.Entry<String, String> entry : attrs.entrySet()) {
            if (!matchesAttr(c, entry.getKey(), entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    private boolean matchesAttr(Component c, String attr, String value) {
        switch (attr.toLowerCase()) {
            case "text":
                return value.equals(getComponentText(c));
            case "name":
                return value.equals(c.getName());
            case "enabled":
                return Boolean.parseBoolean(value) == c.isEnabled();
            case "visible":
                return Boolean.parseBoolean(value) == c.isVisible();
            default:
                return false;
        }
    }
}

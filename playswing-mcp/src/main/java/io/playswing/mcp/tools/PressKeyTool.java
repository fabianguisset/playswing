package io.playswing.mcp.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.playswing.SwingPage;
import io.playswing.mcp.ApplicationContext;
import io.playswing.mcp.Tool;
import io.playswing.mcp.ToolResult;

/**
 * Presses a keyboard key on the focused component.
 *
 * <p>Arguments:
 * <ul>
 *   <li>{@code selector} – selector for the component to focus before pressing</li>
 *   <li>{@code key} – key name (e.g. {@code "Enter"}, {@code "Tab"}, {@code "Escape"}) or
 *       a single character</li>
 *   <li>{@code title} – optional window title</li>
 * </ul>
 */
public class PressKeyTool implements Tool {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public String getName() { return "press_key"; }

    @Override
    public String getDescription() {
        return "Press a keyboard key on a focused Swing component. " +
                "Key names: Enter, Tab, Escape, Backspace, Delete, ArrowUp, ArrowDown, " +
                "ArrowLeft, ArrowRight, F1-F12, or a single character.";
    }

    @Override
    public ObjectNode getInputSchema() {
        ObjectNode schema = MAPPER.createObjectNode();
        schema.put("type", "object");
        ObjectNode props = schema.putObject("properties");
        props.putObject("selector").put("type", "string")
                .put("description", "Selector for the component to focus");
        props.putObject("key").put("type", "string")
                .put("description", "Key name or character to press");
        props.putObject("title").put("type", "string")
                .put("description", "Optional window title");
        schema.putArray("required").add("selector").add("key");
        return schema;
    }

    @Override
    public ToolResult execute(JsonNode arguments, ApplicationContext ctx) {
        try {
            String selector = arguments.get("selector").asText();
            String key = arguments.get("key").asText();
            SwingPage page = getPage(arguments, ctx);
            page.locator(selector).press(key);
            return ToolResult.text("Pressed key \"" + key + "\" on: " + selector);
        } catch (Exception e) {
            return ToolResult.error("Press key failed: " + e.getMessage());
        }
    }

    private SwingPage getPage(JsonNode args, ApplicationContext ctx) {
        if (args.has("title") && !args.get("title").asText().isBlank()) {
            return ctx.getApplication().getPage(args.get("title").asText());
        }
        return ctx.getApplication().getPage();
    }
}

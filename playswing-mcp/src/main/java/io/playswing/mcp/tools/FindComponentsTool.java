package io.playswing.mcp.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.playswing.Locator;
import io.playswing.SwingPage;
import io.playswing.internal.ComponentFinder;
import io.playswing.mcp.ApplicationContext;
import io.playswing.mcp.Tool;
import io.playswing.mcp.ToolResult;

import java.awt.Component;
import java.util.List;

/**
 * Lists all components matching a selector in the current window.
 *
 * <p>Arguments:
 * <ul>
 *   <li>{@code selector} – selector to match (required)</li>
 *   <li>{@code title} – optional window title</li>
 * </ul>
 */
public class FindComponentsTool implements Tool {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final ComponentFinder FINDER = new ComponentFinder();

    @Override
    public String getName() { return "find_components"; }

    @Override
    public String getDescription() {
        return "Find all Swing components matching a selector and return their details " +
                "(type, name, text, enabled, visible).";
    }

    @Override
    public ObjectNode getInputSchema() {
        ObjectNode schema = MAPPER.createObjectNode();
        schema.put("type", "object");
        ObjectNode props = schema.putObject("properties");
        props.putObject("selector").put("type", "string")
                .put("description", "Selector to match (e.g. \"button\", \"#name\", \"[text=OK]\")");
        props.putObject("title").put("type", "string")
                .put("description", "Optional window title");
        schema.putArray("required").add("selector");
        return schema;
    }

    @Override
    public ToolResult execute(JsonNode arguments, ApplicationContext ctx) {
        try {
            String selector = arguments.get("selector").asText();
            SwingPage page = getPage(arguments, ctx);
            Locator locator = page.locator(selector);
            List<Component> components = locator.resolveAll();

            ArrayNode result = MAPPER.createArrayNode();
            for (Component c : components) {
                ObjectNode item = MAPPER.createObjectNode();
                item.put("type", c.getClass().getSimpleName());
                item.put("name", c.getName() != null ? c.getName() : "");
                String text = FINDER.getComponentText(c);
                item.put("text", text != null ? text : "");
                item.put("enabled", c.isEnabled());
                item.put("visible", c.isVisible() && c.isShowing());
                result.add(item);
            }
            return ToolResult.text(result.toString());
        } catch (Exception e) {
            return ToolResult.error("find_components failed: " + e.getMessage());
        }
    }

    private SwingPage getPage(JsonNode args, ApplicationContext ctx) {
        if (args.has("title") && !args.get("title").asText().isBlank()) {
            return ctx.getApplication().getPage(args.get("title").asText());
        }
        return ctx.getApplication().getPage();
    }
}

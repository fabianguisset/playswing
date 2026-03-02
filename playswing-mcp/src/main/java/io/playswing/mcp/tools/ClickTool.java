package io.playswing.mcp.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.playswing.SwingPage;
import io.playswing.mcp.ApplicationContext;
import io.playswing.mcp.Tool;
import io.playswing.mcp.ToolResult;

/**
 * Clicks a component identified by a selector.
 *
 * <p>Arguments:
 * <ul>
 *   <li>{@code selector} – CSS-like selector (e.g. {@code "button[text=OK]"})</li>
 *   <li>{@code title} – optional window title</li>
 * </ul>
 */
public class ClickTool implements Tool {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public String getName() { return "click"; }

    @Override
    public String getDescription() {
        return "Click a Swing component identified by a selector. " +
                "Selector examples: \"button[text=OK]\", \"#submitBtn\", \"[text=Cancel]\".";
    }

    @Override
    public ObjectNode getInputSchema() {
        ObjectNode schema = MAPPER.createObjectNode();
        schema.put("type", "object");
        ObjectNode props = schema.putObject("properties");
        props.putObject("selector").put("type", "string")
                .put("description", "CSS-like selector for the component");
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
            page.locator(selector).click();
            return ToolResult.text("Clicked: " + selector);
        } catch (Exception e) {
            return ToolResult.error("Click failed: " + e.getMessage());
        }
    }

    private SwingPage getPage(JsonNode args, ApplicationContext ctx) {
        if (args.has("title") && !args.get("title").asText().isBlank()) {
            return ctx.getApplication().getPage(args.get("title").asText());
        }
        return ctx.getApplication().getPage();
    }
}

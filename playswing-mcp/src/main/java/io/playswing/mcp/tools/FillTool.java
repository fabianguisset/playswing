package io.playswing.mcp.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.playswing.SwingPage;
import io.playswing.mcp.ApplicationContext;
import io.playswing.mcp.Tool;
import io.playswing.mcp.ToolResult;

/**
 * Fills a text component with a value, replacing any existing content.
 *
 * <p>Arguments:
 * <ul>
 *   <li>{@code selector} – selector for the text field</li>
 *   <li>{@code value} – text to enter</li>
 *   <li>{@code title} – optional window title</li>
 * </ul>
 */
public class FillTool implements Tool {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public String getName() { return "fill"; }

    @Override
    public String getDescription() {
        return "Fill a text component (JTextField, JTextArea) with a value, " +
                "replacing any existing content.";
    }

    @Override
    public ObjectNode getInputSchema() {
        ObjectNode schema = MAPPER.createObjectNode();
        schema.put("type", "object");
        ObjectNode props = schema.putObject("properties");
        props.putObject("selector").put("type", "string")
                .put("description", "Selector for the text component");
        props.putObject("value").put("type", "string")
                .put("description", "Text to enter");
        props.putObject("title").put("type", "string")
                .put("description", "Optional window title");
        schema.putArray("required").add("selector").add("value");
        return schema;
    }

    @Override
    public ToolResult execute(JsonNode arguments, ApplicationContext ctx) {
        try {
            String selector = arguments.get("selector").asText();
            String value = arguments.get("value").asText();
            SwingPage page = getPage(arguments, ctx);
            page.locator(selector).fill(value);
            return ToolResult.text("Filled \"" + selector + "\" with: " + value);
        } catch (Exception e) {
            return ToolResult.error("Fill failed: " + e.getMessage());
        }
    }

    private SwingPage getPage(JsonNode args, ApplicationContext ctx) {
        if (args.has("title") && !args.get("title").asText().isBlank()) {
            return ctx.getApplication().getPage(args.get("title").asText());
        }
        return ctx.getApplication().getPage();
    }
}

package io.playswing.mcp.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.playswing.SwingPage;
import io.playswing.mcp.ApplicationContext;
import io.playswing.mcp.Tool;
import io.playswing.mcp.ToolResult;

/**
 * Gets the visible text or input value of a component.
 *
 * <p>Arguments:
 * <ul>
 *   <li>{@code selector} – selector for the component</li>
 *   <li>{@code attribute} – {@code "text"} (default) or {@code "value"}</li>
 *   <li>{@code title} – optional window title</li>
 * </ul>
 */
public class GetTextTool implements Tool {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public String getName() { return "get_text"; }

    @Override
    public String getDescription() {
        return "Get the text or value of a Swing component identified by a selector.";
    }

    @Override
    public ObjectNode getInputSchema() {
        ObjectNode schema = MAPPER.createObjectNode();
        schema.put("type", "object");
        ObjectNode props = schema.putObject("properties");
        props.putObject("selector").put("type", "string")
                .put("description", "Selector for the component");
        props.putObject("attribute").put("type", "string")
                .put("description", "\"text\" (label/button text) or \"value\" (input value). Default: \"text\"");
        props.putObject("title").put("type", "string")
                .put("description", "Optional window title");
        schema.putArray("required").add("selector");
        return schema;
    }

    @Override
    public ToolResult execute(JsonNode arguments, ApplicationContext ctx) {
        try {
            String selector = arguments.get("selector").asText();
            String attribute = arguments.has("attribute") ? arguments.get("attribute").asText() : "text";
            SwingPage page = getPage(arguments, ctx);

            String result;
            if ("value".equalsIgnoreCase(attribute)) {
                result = page.locator(selector).getValue();
            } else {
                result = page.locator(selector).getText();
            }
            return ToolResult.text(result != null ? result : "");
        } catch (Exception e) {
            return ToolResult.error("get_text failed: " + e.getMessage());
        }
    }

    private SwingPage getPage(JsonNode args, ApplicationContext ctx) {
        if (args.has("title") && !args.get("title").asText().isBlank()) {
            return ctx.getApplication().getPage(args.get("title").asText());
        }
        return ctx.getApplication().getPage();
    }
}

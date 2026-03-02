package io.playswing.mcp.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.playswing.SwingPage;
import io.playswing.mcp.ApplicationContext;
import io.playswing.mcp.Tool;
import io.playswing.mcp.ToolResult;

/**
 * Checks or unchecks a JCheckBox or JRadioButton.
 *
 * <p>Arguments:
 * <ul>
 *   <li>{@code selector} – selector for the toggle button</li>
 *   <li>{@code checked} – {@code true} to check, {@code false} to uncheck (default: true)</li>
 *   <li>{@code title} – optional window title</li>
 * </ul>
 */
public class CheckTool implements Tool {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public String getName() { return "check"; }

    @Override
    public String getDescription() {
        return "Check or uncheck a JCheckBox or JRadioButton.";
    }

    @Override
    public ObjectNode getInputSchema() {
        ObjectNode schema = MAPPER.createObjectNode();
        schema.put("type", "object");
        ObjectNode props = schema.putObject("properties");
        props.putObject("selector").put("type", "string")
                .put("description", "Selector for the checkbox or radio button");
        props.putObject("checked").put("type", "boolean")
                .put("description", "true to check, false to uncheck (default: true)");
        props.putObject("title").put("type", "string")
                .put("description", "Optional window title");
        schema.putArray("required").add("selector");
        return schema;
    }

    @Override
    public ToolResult execute(JsonNode arguments, ApplicationContext ctx) {
        try {
            String selector = arguments.get("selector").asText();
            boolean checked = !arguments.has("checked") || arguments.get("checked").asBoolean(true);
            SwingPage page = getPage(arguments, ctx);

            if (checked) {
                page.locator(selector).check();
            } else {
                page.locator(selector).uncheck();
            }
            return ToolResult.text((checked ? "Checked" : "Unchecked") + ": " + selector);
        } catch (Exception e) {
            return ToolResult.error("check failed: " + e.getMessage());
        }
    }

    private SwingPage getPage(JsonNode args, ApplicationContext ctx) {
        if (args.has("title") && !args.get("title").asText().isBlank()) {
            return ctx.getApplication().getPage(args.get("title").asText());
        }
        return ctx.getApplication().getPage();
    }
}

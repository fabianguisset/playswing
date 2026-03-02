package io.playswing.mcp.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.playswing.SwingPage;
import io.playswing.mcp.ApplicationContext;
import io.playswing.mcp.Tool;
import io.playswing.mcp.ToolResult;

/**
 * Selects an option in a JComboBox or JList by visible text.
 *
 * <p>Arguments:
 * <ul>
 *   <li>{@code selector} – selector for the combo box or list</li>
 *   <li>{@code value} – visible text of the option to select</li>
 *   <li>{@code title} – optional window title</li>
 * </ul>
 */
public class SelectOptionTool implements Tool {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public String getName() { return "select_option"; }

    @Override
    public String getDescription() {
        return "Select an option in a JComboBox or JList by its visible text.";
    }

    @Override
    public ObjectNode getInputSchema() {
        ObjectNode schema = MAPPER.createObjectNode();
        schema.put("type", "object");
        ObjectNode props = schema.putObject("properties");
        props.putObject("selector").put("type", "string")
                .put("description", "Selector for the JComboBox or JList");
        props.putObject("value").put("type", "string")
                .put("description", "Visible text of the option to select");
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
            page.locator(selector).selectOption(value);
            return ToolResult.text("Selected \"" + value + "\" in: " + selector);
        } catch (Exception e) {
            return ToolResult.error("select_option failed: " + e.getMessage());
        }
    }

    private SwingPage getPage(JsonNode args, ApplicationContext ctx) {
        if (args.has("title") && !args.get("title").asText().isBlank()) {
            return ctx.getApplication().getPage(args.get("title").asText());
        }
        return ctx.getApplication().getPage();
    }
}

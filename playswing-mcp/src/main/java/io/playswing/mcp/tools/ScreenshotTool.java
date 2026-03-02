package io.playswing.mcp.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.playswing.SwingPage;
import io.playswing.mcp.ApplicationContext;
import io.playswing.mcp.Tool;
import io.playswing.mcp.ToolResult;

/**
 * Takes a screenshot of the application window.
 *
 * <p>Arguments:
 * <ul>
 *   <li>{@code title} – optional window title; uses the first window if omitted</li>
 * </ul>
 */
public class ScreenshotTool implements Tool {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public String getName() { return "screenshot"; }

    @Override
    public String getDescription() {
        return "Take a screenshot of the current Swing application window. " +
                "Returns the image as base64-encoded PNG.";
    }

    @Override
    public ObjectNode getInputSchema() {
        ObjectNode schema = MAPPER.createObjectNode();
        schema.put("type", "object");
        ObjectNode props = schema.putObject("properties");
        ObjectNode title = props.putObject("title");
        title.put("type", "string");
        title.put("description", "Optional window title. Uses the first window if omitted.");
        return schema;
    }

    @Override
    public ToolResult execute(JsonNode arguments, ApplicationContext ctx) {
        try {
            SwingPage page = getPage(arguments, ctx);
            byte[] png = page.screenshot();
            return ToolResult.image(png);
        } catch (Exception e) {
            return ToolResult.error("Screenshot failed: " + e.getMessage());
        }
    }

    private SwingPage getPage(JsonNode args, ApplicationContext ctx) {
        if (args != null && args.has("title") && !args.get("title").asText().isBlank()) {
            return ctx.getApplication().getPage(args.get("title").asText());
        }
        return ctx.getApplication().getPage();
    }
}

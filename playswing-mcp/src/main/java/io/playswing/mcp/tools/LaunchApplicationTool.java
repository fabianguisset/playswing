package io.playswing.mcp.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.playswing.PlaySwing;
import io.playswing.SwingApplication;
import io.playswing.mcp.ApplicationContext;
import io.playswing.mcp.Tool;
import io.playswing.mcp.ToolResult;

/**
 * Launches a Java Swing application from a JAR file or by class name.
 *
 * <p>Arguments:
 * <ul>
 *   <li>{@code jar} – path to an executable JAR (uses manifest Main-Class)</li>
 *   <li>{@code args} – optional array of string arguments</li>
 * </ul>
 */
public class LaunchApplicationTool implements Tool {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public String getName() { return "launch_application"; }

    @Override
    public String getDescription() {
        return "Launch a Java Swing application from an executable JAR file. " +
                "Returns when the first window is visible.";
    }

    @Override
    public ObjectNode getInputSchema() {
        ObjectNode schema = MAPPER.createObjectNode();
        schema.put("type", "object");
        ObjectNode props = schema.putObject("properties");

        ObjectNode jar = props.putObject("jar");
        jar.put("type", "string");
        jar.put("description", "Path to the executable JAR file");

        ObjectNode args = props.putObject("args");
        args.put("type", "array");
        args.putObject("items").put("type", "string");
        args.put("description", "Optional command-line arguments");

        schema.putArray("required").add("jar");
        return schema;
    }

    @Override
    public ToolResult execute(JsonNode arguments, ApplicationContext ctx) {
        try {
            String jarPath = arguments.get("jar").asText();
            String[] args = new String[0];
            if (arguments.has("args") && arguments.get("args").isArray()) {
                JsonNode argsNode = arguments.get("args");
                args = new String[argsNode.size()];
                for (int i = 0; i < argsNode.size(); i++) {
                    args[i] = argsNode.get(i).asText();
                }
            }

            PlaySwing playSwing = new PlaySwing();
            SwingApplication application = playSwing.launchJar(jarPath, args);
            ctx.setApplication(application);

            String title = application.getPage().getTitle();
            return ToolResult.text("Application launched. Main window title: " + title);
        } catch (Exception e) {
            return ToolResult.error("Failed to launch application: " + e.getMessage());
        }
    }
}

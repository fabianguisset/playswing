package io.playswing.mcp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.playswing.mcp.tools.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Model Context Protocol (MCP) server for Swing automation.
 *
 * <p>Implements the JSON-RPC 2.0 stdio transport used by the MCP specification.
 * Start this server and connect it to an MCP client (e.g. Claude Desktop) to enable
 * AI-driven automation of any Java Swing application.
 *
 * <p>Supported MCP methods:
 * <ul>
 *   <li>{@code initialize} – capability handshake</li>
 *   <li>{@code tools/list} – returns available tools</li>
 *   <li>{@code tools/call} – executes a tool</li>
 *   <li>{@code notifications/initialized} – client notification (acknowledged)</li>
 * </ul>
 *
 * <p>Available tools: launch_application, screenshot, click, fill, press_key,
 * get_text, find_components, select_option, check, close_application.
 */
public class McpServer {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String PROTOCOL_VERSION = "2024-11-05";
    private static final String SERVER_NAME = "playswing";
    private static final String SERVER_VERSION = "0.1.0";

    private final Map<String, Tool> tools = new LinkedHashMap<>();
    private final ApplicationContext appContext = new ApplicationContext();

    public McpServer() {
        register(new LaunchApplicationTool());
        register(new ScreenshotTool());
        register(new ClickTool());
        register(new FillTool());
        register(new PressKeyTool());
        register(new GetTextTool());
        register(new FindComponentsTool());
        register(new SelectOptionTool());
        register(new CheckTool());
        register(new CloseApplicationTool());
    }

    private void register(Tool tool) {
        tools.put(tool.getName(), tool);
    }

    /**
     * Starts the MCP server, reading JSON-RPC messages from stdin and writing responses to
     * stdout. Runs until stdin is closed.
     */
    public void run() throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter writer = new PrintWriter(System.out, true);

        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) continue;
            try {
                JsonNode request = MAPPER.readTree(line);
                String response = handleRequest(request);
                if (response != null) {
                    writer.println(response);
                }
            } catch (Exception e) {
                String error = buildError(null, -32700, "Parse error: " + e.getMessage());
                writer.println(error);
            }
        }
    }

    private String handleRequest(JsonNode request) throws Exception {
        JsonNode idNode = request.get("id");
        Object id = idNode == null ? null : (idNode.isNumber() ? idNode.numberValue() : idNode.asText());

        String method = request.has("method") ? request.get("method").asText() : "";

        // Notifications (no id) – no response required
        if (idNode == null) {
            return null;
        }

        return switch (method) {
            case "initialize" -> handleInitialize(id, request);
            case "tools/list" -> handleToolsList(id);
            case "tools/call" -> handleToolsCall(id, request);
            default -> buildError(id, -32601, "Method not found: " + method);
        };
    }

    private String handleInitialize(Object id, JsonNode request) throws Exception {
        ObjectNode result = MAPPER.createObjectNode();
        result.put("protocolVersion", PROTOCOL_VERSION);

        ObjectNode capabilities = result.putObject("capabilities");
        capabilities.putObject("tools");

        ObjectNode serverInfo = result.putObject("serverInfo");
        serverInfo.put("name", SERVER_NAME);
        serverInfo.put("version", SERVER_VERSION);

        return buildResult(id, result);
    }

    private String handleToolsList(Object id) throws Exception {
        ArrayNode toolsArray = MAPPER.createArrayNode();
        for (Tool tool : tools.values()) {
            ObjectNode entry = MAPPER.createObjectNode();
            entry.put("name", tool.getName());
            entry.put("description", tool.getDescription());
            entry.set("inputSchema", tool.getInputSchema());
            toolsArray.add(entry);
        }
        ObjectNode result = MAPPER.createObjectNode();
        result.set("tools", toolsArray);
        return buildResult(id, result);
    }

    private String handleToolsCall(Object id, JsonNode request) throws Exception {
        JsonNode params = request.get("params");
        if (params == null) {
            return buildError(id, -32602, "Missing params");
        }
        String toolName = params.has("name") ? params.get("name").asText() : "";
        JsonNode arguments = params.get("arguments");

        Tool tool = tools.get(toolName);
        if (tool == null) {
            return buildError(id, -32602, "Unknown tool: " + toolName);
        }

        ToolResult toolResult = tool.execute(arguments != null ? arguments : MAPPER.createObjectNode(), appContext);

        ArrayNode content = MAPPER.createArrayNode();
        if (toolResult.isImage()) {
            ObjectNode imageContent = MAPPER.createObjectNode();
            imageContent.put("type", "image");
            imageContent.put("data", toolResult.getImageBase64());
            imageContent.put("mimeType", "image/png");
            content.add(imageContent);
        } else {
            ObjectNode textContent = MAPPER.createObjectNode();
            textContent.put("type", "text");
            textContent.put("text", toolResult.getText() != null ? toolResult.getText() : "");
            content.add(textContent);
        }

        ObjectNode result = MAPPER.createObjectNode();
        result.set("content", content);
        result.put("isError", toolResult.isError());
        return buildResult(id, result);
    }

    private String buildResult(Object id, JsonNode result) throws Exception {
        ObjectNode response = MAPPER.createObjectNode();
        response.put("jsonrpc", "2.0");
        if (id instanceof Number) {
            response.put("id", ((Number) id).longValue());
        } else if (id != null) {
            response.put("id", id.toString());
        } else {
            response.putNull("id");
        }
        response.set("result", result);
        return MAPPER.writeValueAsString(response);
    }

    private String buildError(Object id, int code, String message) {
        try {
            ObjectNode response = MAPPER.createObjectNode();
            response.put("jsonrpc", "2.0");
            if (id instanceof Number) {
                response.put("id", ((Number) id).longValue());
            } else if (id != null) {
                response.put("id", id.toString());
            } else {
                response.putNull("id");
            }
            ObjectNode error = response.putObject("error");
            error.put("code", code);
            error.put("message", message);
            return MAPPER.writeValueAsString(response);
        } catch (Exception e) {
            return "{\"jsonrpc\":\"2.0\",\"id\":null,\"error\":{\"code\":-32603,\"message\":\"Internal error\"}}";
        }
    }

    /**
     * Main entry point. Starts the MCP server on stdio.
     */
    public static void main(String[] args) throws Exception {
        new McpServer().run();
    }
}

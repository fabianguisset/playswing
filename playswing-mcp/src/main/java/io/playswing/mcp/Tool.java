package io.playswing.mcp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * A tool exposed via the MCP {@code tools/call} endpoint.
 */
public interface Tool {

    /**
     * Returns the tool name as registered in the MCP tools list.
     */
    String getName();

    /**
     * Returns a human-readable description.
     */
    String getDescription();

    /**
     * Returns a JSON Schema object describing the tool's input parameters.
     */
    ObjectNode getInputSchema();

    /**
     * Executes the tool with the given arguments and returns a {@link ToolResult}.
     *
     * @param args  the {@code arguments} object from the {@code tools/call} request
     * @param ctx   the shared application context
     */
    ToolResult execute(JsonNode args, ApplicationContext ctx);
}

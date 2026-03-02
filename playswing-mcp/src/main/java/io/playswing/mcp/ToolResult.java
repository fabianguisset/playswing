package io.playswing.mcp;

/**
 * The result of a tool call, corresponding to the MCP {@code content} array.
 */
public class ToolResult {

    private final String text;
    private final String imageBase64;
    private final boolean isError;

    private ToolResult(String text, String imageBase64, boolean isError) {
        this.text = text;
        this.imageBase64 = imageBase64;
        this.isError = isError;
    }

    /** Creates a successful text result. */
    public static ToolResult text(String text) {
        return new ToolResult(text, null, false);
    }

    /** Creates a successful image result (PNG bytes encoded as Base64). */
    public static ToolResult image(byte[] pngBytes) {
        String b64 = java.util.Base64.getEncoder().encodeToString(pngBytes);
        return new ToolResult(null, b64, false);
    }

    /** Creates an error result. */
    public static ToolResult error(String message) {
        return new ToolResult(message, null, true);
    }

    public String getText() { return text; }
    public String getImageBase64() { return imageBase64; }
    public boolean isError() { return isError; }
    public boolean isImage() { return imageBase64 != null; }
}

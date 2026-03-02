# playswing
Playwright equivalent for Java Swing

PlaySwing is a Java automation framework for Swing applications with a
[Model Context Protocol (MCP)](https://modelcontextprotocol.io) server, enabling AI coding
agents to automate and test any Java Swing application.

---

## Overview

PlaySwing follows the same API philosophy as [Playwright](https://playwright.dev):

| Playwright (web)                        | PlaySwing (Swing)                                |
|-----------------------------------------|--------------------------------------------------|
| `playwright.chromium().launch()`        | `new PlaySwing().launch(MyApp.class, args)`      |
| `page.locator("button")`                | `page.locator("button")`                         |
| `page.getByText("Submit")`              | `page.getByText("Submit")`                       |
| `locator.click()`                       | `locator.click()`                                |
| `locator.fill("text")`                  | `locator.fill("text")`                           |
| `expect(locator).toBeVisible()`         | `locator.expect().toBeVisible()`                 |
| `page.screenshot()`                     | `page.screenshot()`                              |

---

## Modules

| Module           | Description                                      |
|------------------|--------------------------------------------------|
| `playswing-core` | Java automation library for Swing applications   |
| `playswing-mcp`  | MCP server exposing Swing automation as AI tools |

---

## Building

Requirements: Java 17+, Maven 3.8+

```bash
mvn package
```

The fat JAR for the MCP server is produced at:
```
playswing-mcp/target/playswing-mcp-0.1.0-SNAPSHOT-jar-with-dependencies.jar
```

---

## Core Library Usage

Add `playswing-core` as a dependency and automate Swing applications directly from Java:

```java
try (PlaySwing playSwing = new PlaySwing()) {

    // Launch the application in the same JVM
    SwingApplication app = playSwing.launch(MyApp.class, new String[]{});
    SwingPage page = app.getPage();

    // Find components using CSS-like selectors
    page.locator("button[text=Submit]").click();
    page.locator("#username").fill("alice");
    page.locator("#cityCombo").selectOption("Berlin");
    page.locator("#agreeCheck").check();

    // Use getBy* helpers (like Playwright)
    page.getByText("Submit").click();
    page.getByLabel("Name:").fill("Bob");
    page.getByRole(AriaRole.BUTTON).first().click();

    // Assertions
    page.locator("button[text=Submit]").expect()
        .toBeVisible()
        .toBeEnabled()
        .toHaveText("Submit");

    // Screenshots (PNG bytes)
    byte[] png = page.screenshot();

    app.close();
}
```

### Selectors

| Selector              | Matches                                       |
|-----------------------|-----------------------------------------------|
| `button`              | All `JButton` components                      |
| `button[text=OK]`     | `JButton` with text "OK"                      |
| `#myName`             | Component with `component.getName() = myName` |
| `[text=Hello]`        | Any component with visible text "Hello"       |
| `[name=username]`     | Component with name "username"                |
| `textfield`           | All `JTextComponent` instances                |
| `checkbox[text=Agree]`| `JCheckBox` with text "Agree"                 |
| `combobox`            | All `JComboBox` components                    |

### Role names

`button`, `checkbox`, `radiobutton`, `combobox`, `list`, `table`, `tree`,
`textbox`, `textfield`, `textarea`, `label`, `slider`, `spinner`,
`menuitem`, `menu`, `tab`, `dialog`, `panel`

---

## MCP Server

The MCP server allows AI agents (Claude, Copilot, etc.) to automate any Swing application
without writing Java code.

### Running the server

```bash
java -jar playswing-mcp/target/playswing-mcp-0.1.0-SNAPSHOT-jar-with-dependencies.jar
```

The server communicates over **stdio** using the JSON-RPC 2.0 transport defined by the
[MCP specification](https://spec.modelcontextprotocol.io).

### Claude Desktop integration

Add the following to your `claude_desktop_config.json`:

```json
{
  "mcpServers": {
    "playswing": {
      "command": "java",
      "args": [
        "-jar",
        "/path/to/playswing-mcp-0.1.0-SNAPSHOT-jar-with-dependencies.jar"
      ]
    }
  }
}
```

### Available MCP tools

| Tool                  | Description                                                      |
|-----------------------|------------------------------------------------------------------|
| `launch_application`  | Launch a Swing app from an executable JAR                        |
| `screenshot`          | Take a screenshot of the current window                          |
| `click`               | Click a component by selector                                    |
| `fill`                | Fill a text field with a value                                   |
| `press_key`           | Press a keyboard key (Enter, Tab, ArrowDown, …)                  |
| `get_text`            | Get text or value of a component                                 |
| `find_components`     | List all components matching a selector                          |
| `select_option`       | Select an option in a JComboBox or JList                         |
| `check`               | Check or uncheck a JCheckBox / JRadioButton                      |
| `close_application`   | Close the running application                                    |

### Example MCP conversation

```
User: Launch myapp.jar, fill the username field with "alice", and click Submit.

Agent calls:
  launch_application(jar="/path/to/myapp.jar")
  fill(selector="#username", value="alice")
  click(selector="button[text=Submit]")
  screenshot()
```

---

## Running tests

A display is required (set `DISPLAY` or start Xvfb):

```bash
Xvfb :99 -screen 0 1280x1024x24 &
export DISPLAY=:99
mvn test
```

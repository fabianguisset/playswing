package io.playswing;

import org.junit.jupiter.api.*;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Integration tests for the PlaySwing core library.
 *
 * <p>These tests launch a real Swing application in the same JVM and exercise
 * the Locator API. A display is required (set {@code DISPLAY} or start Xvfb).
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PlaySwingTest {

    private static PlaySwing playSwing;
    private static SwingApplication app;
    private static SwingPage page;

    @BeforeAll
    static void setUp() {
        assumeTrue(
                !Boolean.getBoolean("java.awt.headless"),
                "Skipping UI tests in headless mode");

        playSwing = new PlaySwing();
        app = playSwing.launch(TestApp.class, new String[]{});
        page = app.getPage();
    }

    @AfterAll
    static void tearDown() {
        if (app != null) app.close();
        if (playSwing != null) playSwing.close();
    }

    // ---- Basic page info ----

    @Test
    @Order(1)
    void testPageTitle() {
        assertThat(page.getTitle()).isEqualTo("PlaySwing Test App");
    }

    @Test
    @Order(2)
    void testPageIsVisible() {
        assertThat(page.isVisible()).isTrue();
    }

    // ---- Locator by selector ----

    @Test
    @Order(3)
    void testLocatorByRole() {
        Locator submitBtn = page.locator("button[text=Submit]");
        assertThat(submitBtn.isVisible()).isTrue();
        assertThat(submitBtn.isEnabled()).isTrue();
        assertThat(submitBtn.getText()).isEqualTo("Submit");
    }

    @Test
    @Order(4)
    void testLocatorByName() {
        Locator nameField = page.locator("#nameField");
        assertThat(nameField.isVisible()).isTrue();
        nameField.expect().toBeEnabled();
    }

    // ---- getBy* helpers ----

    @Test
    @Order(5)
    void testGetByText() {
        Locator submitBtn = page.getByText("Submit");
        assertThat(submitBtn.getText()).isEqualTo("Submit");
    }

    @Test
    @Order(6)
    void testGetByRole() {
        Locator btn = page.getByRole(AriaRole.BUTTON);
        assertThat(btn.count()).isGreaterThan(0);
    }

    @Test
    @Order(7)
    void testGetByLabel() {
        Locator nameField = page.getByLabel("Name:");
        assertThat(nameField.isVisible()).isTrue();
    }

    @Test
    @Order(8)
    void testGetByName() {
        Locator combo = page.getByName("cityCombo");
        assertThat(combo.isVisible()).isTrue();
    }

    // ---- Actions ----

    @Test
    @Order(9)
    void testFillAndSubmit() throws Exception {
        Locator nameField = page.locator("#nameField");
        nameField.fill("Alice");

        // Wait for EDT to process
        javax.swing.SwingUtilities.invokeAndWait(() -> {});

        assertThat(nameField.getValue()).isEqualTo("Alice");
    }

    @Test
    @Order(10)
    void testSelectOption() throws Exception {
        Locator cityCombo = page.locator("#cityCombo");
        cityCombo.selectOption("London");

        javax.swing.SwingUtilities.invokeAndWait(() -> {});

        assertThat(cityCombo.getText()).isEqualTo("London");
    }

    @Test
    @Order(11)
    void testCheck() throws Exception {
        Locator agreeCheck = page.locator("#agreeCheck");
        agreeCheck.check();

        javax.swing.SwingUtilities.invokeAndWait(() -> {});

        assertThat(agreeCheck.isChecked()).isTrue();
        agreeCheck.expect().toBeChecked();
    }

    @Test
    @Order(12)
    void testClickSubmitAndVerifyResult() throws Exception {
        // Ensure known state
        page.locator("#nameField").fill("Bob");
        page.locator("#cityCombo").selectOption("Paris");
        page.locator("#agreeCheck").check();
        page.locator("button[text=Submit]").click();

        javax.swing.SwingUtilities.invokeAndWait(() -> {});

        Locator result = page.locator("#resultLabel");
        result.expect().toContainText("Bob");
        result.expect().toContainText("Paris");
        result.expect().toContainText("agreed");
    }

    // ---- Assertions ----

    @Test
    @Order(13)
    void testLocatorAssertions() {
        page.locator("button[text=Submit]").expect()
                .toBeVisible()
                .toBeEnabled()
                .toHaveText("Submit");
    }

    @Test
    @Order(14)
    void testCountLocator() {
        // There is exactly one Submit button
        page.locator("button[text=Submit]").expect().toHaveCount(1);
    }

    @Test
    @Order(15)
    void testNthLocator() {
        // The first button should be visible
        Locator firstBtn = page.locator("button").first();
        assertThat(firstBtn.isVisible()).isTrue();
    }

    // ---- Screenshot ----

    @Test
    @Order(16)
    void testScreenshot() {
        byte[] png = page.screenshot();
        assertThat(png).isNotNull().isNotEmpty();
        // PNG magic bytes
        assertThat(png[0]).isEqualTo((byte) 0x89);
        assertThat(png[1]).isEqualTo((byte) 0x50); // 'P'
        assertThat(png[2]).isEqualTo((byte) 0x4E); // 'N'
        assertThat(png[3]).isEqualTo((byte) 0x47); // 'G'
    }
}

package io.playswing.internal;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Captures screenshots of AWT windows or the entire screen.
 */
public class ScreenshotCapture {

    /**
     * Captures a screenshot of the given window and returns it as a PNG byte array.
     */
    public byte[] captureWindow(Window window) throws AWTException, IOException {
        Point location = window.getLocationOnScreen();
        Dimension size = window.getSize();
        Rectangle bounds = new Rectangle(location, size);
        Robot robot = new Robot();
        BufferedImage image = robot.createScreenCapture(bounds);
        return toPng(image);
    }

    /**
     * Captures a screenshot of the given component and returns it as a PNG byte array.
     */
    public byte[] captureComponent(Component component) throws AWTException, IOException {
        Point location = component.getLocationOnScreen();
        Dimension size = component.getSize();
        Rectangle bounds = new Rectangle(location, size);
        Robot robot = new Robot();
        BufferedImage image = robot.createScreenCapture(bounds);
        return toPng(image);
    }

    /**
     * Captures the entire screen and returns it as a PNG byte array.
     */
    public byte[] captureScreen() throws AWTException, IOException {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Rectangle bounds = new Rectangle(screenSize);
        Robot robot = new Robot();
        BufferedImage image = robot.createScreenCapture(bounds);
        return toPng(image);
    }

    private byte[] toPng(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return baos.toByteArray();
    }
}

package io.playswing;

import java.awt.Window;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.swing.*;

/**
 * Represents a launched Java Swing application.
 *
 * <p>Obtain instances via {@link PlaySwing#launch(String, String[])} or
 * {@link PlaySwing#launch(Class, String[])}.
 */
public class SwingApplication implements AutoCloseable {

    private final Thread appThread;
    private final List<Window> initialWindows;
    private final int defaultTimeoutMs;

    SwingApplication(Thread appThread, List<Window> initialWindows, int defaultTimeoutMs) {
        this.appThread = appThread;
        this.initialWindows = Collections.unmodifiableList(new ArrayList<>(initialWindows));
        this.defaultTimeoutMs = defaultTimeoutMs;
    }

    // ---- Pages ----

    /**
     * Returns the first visible top-level window belonging to this application as a
     * {@link SwingPage}.
     *
     * @throws PlaySwingException if no window is available
     */
    public SwingPage getPage() {
        List<SwingPage> pages = getPages();
        if (pages.isEmpty()) {
            throw new PlaySwingException("No visible window found for application");
        }
        return pages.get(0);
    }

    /**
     * Returns the first visible window with the given title.
     *
     * @throws PlaySwingException if no matching window is found
     */
    public SwingPage getPage(String title) {
        return getPages().stream()
                .filter(p -> title.equals(p.getTitle()))
                .findFirst()
                .orElseThrow(() -> new PlaySwingException("No window with title: " + title));
    }

    /**
     * Returns all visible top-level windows belonging to this application.
     */
    public List<SwingPage> getPages() {
        return Arrays.stream(Window.getWindows())
                .filter(w -> w.isVisible() && !initialWindows.contains(w))
                .map(SwingPage::new)
                .collect(Collectors.toList());
    }

    // ---- Screenshot ----

    /**
     * Takes a screenshot of the main application window.
     */
    public byte[] screenshot() {
        return getPage().screenshot();
    }

    // ---- Lifecycle ----

    /**
     * Waits up to the default timeout for at least one application window to appear.
     *
     * @return this instance
     */
    public SwingApplication waitForWindow() {
        return waitForWindow(defaultTimeoutMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Waits up to the given duration for at least one application window to appear.
     *
     * @return this instance
     */
    public SwingApplication waitForWindow(long timeout, TimeUnit unit) {
        long deadlineMs = System.currentTimeMillis() + unit.toMillis(timeout);
        while (System.currentTimeMillis() < deadlineMs) {
            if (!getPages().isEmpty()) {
                return this;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        throw new PlaySwingException("No application window appeared within timeout");
    }

    /**
     * Closes all windows belonging to this application and interrupts the application thread.
     */
    @Override
    public void close() {
        try {
            SwingUtilities.invokeAndWait(() -> {
                for (SwingPage page : getPages()) {
                    page.close();
                }
            });
        } catch (Exception e) {
            // Ignore
        }
        if (appThread != null && appThread.isAlive()) {
            appThread.interrupt();
        }
    }

    /**
     * Waits for the application to finish, up to the given timeout.
     *
     * @return {@code true} if the application has exited, {@code false} if still running
     */
    public boolean waitForExit(long timeout, TimeUnit unit) throws InterruptedException {
        if (appThread == null) return true;
        appThread.join(unit.toMillis(timeout));
        return !appThread.isAlive();
    }
}

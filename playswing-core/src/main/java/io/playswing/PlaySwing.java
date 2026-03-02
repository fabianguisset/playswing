package io.playswing;

import java.awt.Window;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * Entry point for the PlaySwing automation framework, analogous to Playwright's
 * {@code playwright} object.
 *
 * <p>Usage:
 * <pre>{@code
 * try (PlaySwing playSwing = new PlaySwing()) {
 *     SwingApplication app = playSwing.launch(MyApp.class, new String[]{});
 *     SwingPage page = app.getPage();
 *     page.locator("button[text=Submit]").click();
 *     app.close();
 * }
 * }</pre>
 */
public class PlaySwing implements AutoCloseable {

    private static final int DEFAULT_TIMEOUT_MS = 30_000;

    // ---- Launch from Class ----

    /**
     * Launches a Swing application by calling the {@code main(String[])} method of the given
     * class in a daemon thread.
     *
     * @param mainClass the class containing the {@code main} method
     * @param args      command-line arguments passed to {@code main}
     * @return a {@link SwingApplication} whose windows become available after launch
     */
    public SwingApplication launch(Class<?> mainClass, String[] args) {
        return launchInThread(() -> {
            try {
                Method main = mainClass.getMethod("main", String[].class);
                main.invoke(null, (Object) args);
            } catch (InvocationTargetException e) {
                // Ignore – the app may call System.exit or throw on normal exit
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    // ---- Launch from JAR ----

    /**
     * Launches a Swing application from an executable JAR file.
     * The main class is read from the JAR's {@code Main-Class} manifest attribute.
     *
     * @param jarPath path to the JAR file
     * @param args    command-line arguments
     * @return a {@link SwingApplication}
     */
    public SwingApplication launchJar(String jarPath, String[] args) {
        try {
            File jar = new File(jarPath);
            String mainClass;
            try (JarFile jf = new JarFile(jar)) {
                Manifest mf = jf.getManifest();
                mainClass = mf.getMainAttributes().getValue("Main-Class");
            }
            if (mainClass == null || mainClass.isBlank()) {
                throw new PlaySwingException("No Main-Class attribute in " + jarPath);
            }
            URL[] urls = {jar.toURI().toURL()};
            URLClassLoader loader = new URLClassLoader(urls, getClass().getClassLoader());
            Class<?> cls = loader.loadClass(mainClass);
            return launch(cls, args);
        } catch (PlaySwingException e) {
            throw e;
        } catch (Exception e) {
            throw new PlaySwingException("Failed to launch JAR: " + jarPath, e);
        }
    }

    // ---- Launch from class name + classloader ----

    /**
     * Launches a Swing application by class name, using the supplied {@link ClassLoader}.
     *
     * @param className   fully-qualified class name
     * @param args        command-line arguments
     * @param classLoader class loader used to load the main class
     * @return a {@link SwingApplication}
     */
    public SwingApplication launch(String className, String[] args, ClassLoader classLoader) {
        try {
            Class<?> cls = classLoader.loadClass(className);
            return launch(cls, args);
        } catch (ClassNotFoundException e) {
            throw new PlaySwingException("Class not found: " + className, e);
        }
    }

    @Override
    public void close() {
        // No resources to release on the factory itself
    }

    // ---- Private helpers ----

    private SwingApplication launchInThread(Runnable appRunner) {
        // Snapshot windows that already exist before the app starts
        List<Window> existingWindows = Arrays.asList(Window.getWindows());

        Thread thread = new Thread(appRunner, "playswing-app");
        thread.setDaemon(true);
        thread.start();

        SwingApplication application = new SwingApplication(thread, existingWindows, DEFAULT_TIMEOUT_MS);
        application.waitForWindow(DEFAULT_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        return application;
    }
}

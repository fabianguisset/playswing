package io.playswing.mcp;

import io.playswing.SwingApplication;

/**
 * Holds the single active application instance for the lifetime of the MCP server session.
 */
public class ApplicationContext {

    private SwingApplication application;

    public synchronized SwingApplication getApplication() {
        if (application == null) {
            throw new IllegalStateException("No application is currently running. " +
                    "Call launch_application first.");
        }
        return application;
    }

    public synchronized void setApplication(SwingApplication application) {
        if (this.application != null) {
            this.application.close();
        }
        this.application = application;
    }

    public synchronized boolean hasApplication() {
        return application != null;
    }

    public synchronized void clear() {
        if (application != null) {
            application.close();
            application = null;
        }
    }
}

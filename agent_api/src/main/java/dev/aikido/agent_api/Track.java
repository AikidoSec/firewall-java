package dev.aikido.agent_api;

import dev.aikido.agent_api.background.cloud.api.events.CustomEvent;
import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.helpers.logging.LogManager;
import dev.aikido.agent_api.helpers.logging.Logger;
import dev.aikido.agent_api.storage.AttackQueue;

public final class Track {
    private Track() {}
    private static final Logger logger = LogManager.getLogger(Track.class);
    private static boolean loggedWarningTrackCalledWithoutContext = false;

    /**
     * External function for applications to track a custom event, e.g. a
     * failed login or a signup. Only works inside an HTTP request.
     */
    public static void track(String eventName) {
        if (eventName == null || eventName.isEmpty()) {
            logger.info("track(...) expects a non-empty string as event name.");
            return;
        }

        ContextObject currentContext = Context.get();
        if (currentContext == null) {
            logWarningTrackCalledWithoutContext();
            return;
        }

        AttackQueue.add(CustomEvent.createAPIEvent(eventName, currentContext));
    }

    private static void logWarningTrackCalledWithoutContext() {
        if (loggedWarningTrackCalledWithoutContext) {
            return;
        }
        logger.warn(
            "track(...) was called without a context. The event will not be tracked. " +
            "Make sure to call track(...) within an HTTP request."
        );
        loggedWarningTrackCalledWithoutContext = true;
    }

    /**
     * Resets internal warning state. Only intended for use in tests.
     */
    public static void reset() {
        loggedWarningTrackCalledWithoutContext = false;
    }
}

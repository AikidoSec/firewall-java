package dev.aikido.agent_api.storage;

/**
 * Carries the request context and the inbound block decision across a thread hop (e.g. Micronaut
 * offloading a controller to a virtual thread), keyed by the request object itself. Unlike stashing
 * on the framework's own request-attribute map, this stays invisible to the application and cannot
 * collide with its keys. Values are typed as Object to keep this free of framework and collector
 * dependencies; callers cast back.
 */
public final class RequestLocalStorage {
    private RequestLocalStorage() {}

    private static final WeakConcurrentIdentityMap<Object> contextMap = new WeakConcurrentIdentityMap<>();
    private static final WeakConcurrentIdentityMap<Object> blockMap = new WeakConcurrentIdentityMap<>();

    public static void setContext(Object request, Object context) {
        contextMap.put(request, context);
    }

    public static Object getContext(Object request) {
        return contextMap.get(request);
    }

    public static void setBlock(Object request, Object block) {
        blockMap.put(request, block);
    }

    public static Object getBlock(Object request) {
        return blockMap.get(request);
    }

    public static void remove(Object request) {
        contextMap.remove(request);
        blockMap.remove(request);
    }
}

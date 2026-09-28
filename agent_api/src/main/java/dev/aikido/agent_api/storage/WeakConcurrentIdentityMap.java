package dev.aikido.agent_api.storage;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Associates state with an object by identity, without touching that object. Keys are held weakly
 * (identity-based), so an entry disappears once its key is garbage collected; stale entries are
 * also expunged on every access via a reference queue. Mirrors the map-backed storage OpenTelemetry
 * falls back to when it cannot inject a field into the key's class.
 */
public final class WeakConcurrentIdentityMap<V> {
    private final ConcurrentHashMap<WeakKey, V> map = new ConcurrentHashMap<>();
    private final ReferenceQueue<Object> queue = new ReferenceQueue<>();

    public void put(Object key, V value) {
        expungeStaleEntries();
        map.put(new WeakKey(key, queue), value);
    }

    public V get(Object key) {
        expungeStaleEntries();
        return map.get(new WeakKey(key, null));
    }

    public void remove(Object key) {
        expungeStaleEntries();
        map.remove(new WeakKey(key, null));
    }

    public int size() {
        expungeStaleEntries();
        return map.size();
    }

    private void expungeStaleEntries() {
        Reference<?> ref;
        while ((ref = queue.poll()) != null) {
            map.remove(ref);
        }
    }

    private static final class WeakKey extends WeakReference<Object> {
        private final int hash;

        WeakKey(Object referent, ReferenceQueue<Object> queue) {
            super(referent, queue);
            this.hash = System.identityHashCode(referent);
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof WeakKey)) {
                return false;
            }
            Object self = get();
            return self != null && self == ((WeakKey) other).get();
        }
    }
}

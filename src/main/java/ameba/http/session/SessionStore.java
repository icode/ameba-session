package ameba.http.session;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author icode
 */
class SessionStore implements Serializable {
    private transient boolean change = false;
    private long lastAccessTime;
    private Map<Object, Object> attributes = new HashMap<Object, Object>() {

        @Override
        public Object put(Object key, Object value) {
            change = true;
            return super.put(key, value);
        }

        @Override
        public Object remove(Object key) {
            return super.remove(key);
        }

        @Override
        public void clear() {
            change = true;
            super.clear();
        }

        @Override
        public void putAll(Map<?, ?> m) {
            if (m != null && !m.isEmpty()) {
                change = true;
            }
            super.putAll(m);
        }
    };
    private long timestamp;
    private long timeout;

    public SessionStore(long timeout) {
        this.timestamp = System.currentTimeMillis();
        this.timeout = timeout;
        this.change = true;
        this.lastAccessTime = timestamp;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.change = true;
        this.timeout = timeout;
    }

    public boolean isChange() {
        return change;
    }

    public void unchange() {
        change = false;
    }

    public Map<Object, Object> getAttributes() {
        return attributes;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getLastAccessTime() {
        return lastAccessTime;
    }

    public void updateLastAccessTime() {
        this.lastAccessTime = System.currentTimeMillis();
    }
}
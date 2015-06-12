package ameba.http.session;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author icode
 */
class SessionStore implements Serializable {

    private Attributes attributes = new Attributes();
    private long timestamp;
    private long timeout;

    public SessionStore(long timeout) {
        this.timestamp = System.currentTimeMillis();
        this.timeout = timeout;
        this.attributes.change = true;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.attributes.change = true;
        this.timeout = timeout;
    }

    public boolean isChange() {
        return this.attributes.change;
    }

    public void unchange() {
        this.attributes.change = false;
    }

    public Map<Object, Object> getAttributes() {
        return attributes;
    }

    public long getTimestamp() {
        return timestamp;
    }

    private static final class Attributes extends HashMap<Object, Object> {
        private transient boolean change = false;

        @Override
        public Object put(Object key, Object value) {
            this.change = true;
            return super.put(key, value);
        }

        @Override
        public Object remove(Object key) {
            this.change = true;
            return super.remove(key);
        }

        @Override
        public void clear() {
            this.change = true;
            super.clear();
        }

        @Override
        public void putAll(Map<?, ?> m) {
            if (m != null && !m.isEmpty()) {
                this.change = true;
            }
            super.putAll(m);
        }
    }

}
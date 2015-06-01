package ameba.http.session;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author icode
 */
class SessionStore implements Serializable {
    boolean isChange = false;
    Map<String, Object> attributes = new HashMap<String, Object>() {

        @Override
        public Object put(String key, Object value) {
            isChange = true;
            return super.put(key, value);
        }

        @Override
        public Object remove(Object key) {
            return super.remove(key);
        }

        @Override
        public void clear() {
            isChange = true;
            super.clear();
        }

        @Override
        public void putAll(Map<? extends String, ?> m) {
            if (m != null && !m.isEmpty()) {
                isChange = true;
            }
            super.putAll(m);
        }
    };
    long timestamp;
}
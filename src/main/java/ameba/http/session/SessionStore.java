package ameba.http.session;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author icode
 */
class SessionStore implements Serializable {
    Map<String, Object> attributes = new HashMap<String, Object>();
    long timestamp;
}
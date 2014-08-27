package ameba.http.session;

import ameba.exceptions.AmebaException;

/**
 * @author icode
 */
public class SessionExcption extends AmebaException {
    public SessionExcption() {
    }

    public SessionExcption(Throwable cause) {
        super(cause);
    }

    public SessionExcption(String message) {
        super(message);
    }

    public SessionExcption(String message, Throwable cause) {
        super(message, cause);
    }
}

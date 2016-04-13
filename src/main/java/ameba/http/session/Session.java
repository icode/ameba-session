package ameba.http.session;

import ameba.core.Requests;
import ameba.util.Times;
import com.google.common.collect.Maps;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;
import java.lang.invoke.MethodHandle;
import java.util.Map;

/**
 * @author icode
 */
public class Session {

    public static final String REQ_SESSION_KEY = Session.class.getName() + ".SESSION_VALUE";
    public static final String SET_COOKIE_KEY = SessionFilter.class.getName() + ".__SET_SESSION_COOKIE__";
    static MethodHandle GET_SESSION_METHOD_HANDLE;
    static MethodHandle NEW_SESSION_ID_METHOD_HANDLE;
    static MethodHandle SESSION_CONSTRUCTOR_HANDLE;
    static long SESSION_TIMEOUT = Times.parseDuration("2h");
    static int COOKIE_MAX_AGE = NewCookie.DEFAULT_MAX_AGE;
    static String SESSION_ID_COOKIE_KEY = "s";

    public static AbstractSession get() {
        return get(true);
    }

    public static AbstractSession get(boolean create) {
        AbstractSession session = (AbstractSession) Requests.getProperty(REQ_SESSION_KEY);
        if (session == null && create && Requests.getRequest() != null) {

            session = create();
            NewCookie cookie = new NewCookie(
                    SESSION_ID_COOKIE_KEY,
                    session.getId(),
                    "/",
                    null,
                    Cookie.DEFAULT_VERSION,
                    null,
                    COOKIE_MAX_AGE,
                    null,
                    Requests.getSecurityContext().isSecure(),
                    true);
            Requests.setProperty(SET_COOKIE_KEY, cookie);
            Requests.setProperty(REQ_SESSION_KEY, session);
        }
        return session;
    }

    public static AbstractSession get(String id) {
        try {
            return (AbstractSession) GET_SESSION_METHOD_HANDLE.invoke(id);
        } catch (Throwable throwable) {
            throw new SessionExcption(throwable);
        }
    }

    public static String generateId() {
        try {
            return (String) NEW_SESSION_ID_METHOD_HANDLE.invoke();
        } catch (Throwable throwable) {
            throw new SessionExcption(throwable);
        }
    }

    public static AbstractSession create() {
        return create(generateId(), Requests.getRemoteRealAddr(), true);
    }

    public static AbstractSession create(String host) {
        return create(generateId(), host, true);
    }

    public static AbstractSession create(String host, boolean isNew) {
        return create(generateId(), host, isNew);
    }

    public static AbstractSession create(boolean isNew) {
        return create(generateId(), Requests.getRemoteRealAddr(), isNew);
    }

    public static AbstractSession create(String sessionId, String host, boolean isNew) {
        AbstractSession session;
        if (SESSION_CONSTRUCTOR_HANDLE != null) {
            try {
                session = (AbstractSession) SESSION_CONSTRUCTOR_HANDLE.invoke(sessionId, host, SESSION_TIMEOUT, isNew);
            } catch (Throwable throwable) {
                throw new SessionExcption("new session instance error");
            }
        } else {
            session = new CacheSession(sessionId, host, SESSION_TIMEOUT, isNew);
        }
        return session;
    }

    /**
     * Add an attribute to this session.
     *
     * @param key   key
     * @param value value
     */
    public static void setAttribute(Object key, Object value) {
        get().setAttribute(key, value);
    }

    /**
     * Return an attribute.
     *
     * @param key key
     * @return an attribute
     */
    public static <V> V getAttribute(Object key) {
        if (get(false) != null)
            return get().getAttribute(key);
        else return null;
    }

    /**
     * Return attributes.
     *
     * @return attributes
     */
    public static Map<Object, Object> getAttributes() {
        if (get(false) != null)
            return get().getAttributes();
        else return Maps.newLinkedHashMap();
    }


    /**
     * Remove an attribute.
     *
     * @param key key
     * @return true if successful.
     */
    public static <V> V removeAttribute(Object key) {
        if (get(false) != null)
            return get().removeAttribute(key);
        else return null;
    }

    /**
     * Returns a string containing the unique identifier assigned
     * to this session. The identifier is assigned
     * by the servlet container and is implementation dependent.
     *
     * @return a string specifying the identifier
     * assigned to this session
     */
    public static String getId() {
        return get().getId();
    }

    /**
     * Return a long representing the maximum idle time (in milliseconds) a session can be.
     *
     * @return a long representing the maximum idle time (in milliseconds) a session can be.
     */
    public static long getTimeout() {
        return get(false) != null ? get().getTimeout() : -1;
    }

    public static void setTimeout(long timeout) {
        get().setTimeout(timeout);
    }

    /**
     * Invalidates this session then unbinds any objects bound
     * to it.
     *
     * @throws IllegalStateException if this method is called on an
     *                               already invalidated session
     */
    public static void invalidate() {
        if (get(false) != null)
            get().invalidate();
    }


    public static boolean isInvalid() {
        return get(false) == null || get().isInvalid();
    }

    /**
     * @return the timestamp when this session has been created.
     */
    public static long getTimestamp() {
        return get(false) != null ? get().getTimestamp() : -1;
    }

    public static void flush() {
        if (get(false) != null)
            get().flush();
    }

    public static void refresh() {
        if (get(false) != null)
            get().refresh();
    }

    public static void touch() {
        if (get(false) != null)
            get().touch();
    }

    public static String getHost() {
        return get(false) != null ? get().getHost() : null;
    }
}

package ameba.http.session;

import java.lang.invoke.MethodHandle;
import java.util.Map;

/**
 * @author icode
 */
public class Session {

    static final ThreadLocal<AbstractSession> sessionThreadLocal = new ThreadLocal<>();
    static MethodHandle GET_SESSION_METHOD_HANDLE;

    public static AbstractSession get() {
        return sessionThreadLocal.get();
    }

    public static AbstractSession get(String id) {
        try {
            return (AbstractSession) GET_SESSION_METHOD_HANDLE.invoke(id);
        } catch (Throwable throwable) {
            return null;
        }
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
        return get().getAttribute(key);
    }

    /**
     * Return attributes.
     *
     * @return attributes
     */
    public static Map<Object, Object> getAttributes() {
        return get().getAttributes();
    }


    /**
     * Remove an attribute.
     *
     * @param key key
     * @return true if successful.
     */
    public static <V> V removeAttribute(Object key) {
        return get().removeAttribute(key);
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
        return get().getTimeout();
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
        get().invalidate();
    }

    /**
     * @return the timestamp when this session has been created.
     */
    public static long getTimestamp() {
        return get().getTimestamp();
    }

    public static void flush() {
        get().flush();
    }

    public static void refresh() {
        get().refresh();
    }

    public static void touch() {
        get().touch();
    }

    public static long getLastAccessTime() {
        return get().getLastAccessTime();
    }

    public static String getHost() {
        return get().getHost();
    }
}

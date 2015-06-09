package ameba.http.session;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * @author icode
 */
public abstract class AbstractSession {

    protected long defaultTimeout;
    protected boolean isNew;
    protected String host;
    protected String id;

    protected AbstractSession(String id, String host, long defaultTimeout, boolean isNew) {
        if (StringUtils.isBlank(id)) throw new SessionExcption("session id is invalid");
        setId(id);
        this.host = host;
        this.defaultTimeout = defaultTimeout;
        this.isNew = isNew;
    }

    public static AbstractSession get(String id) {
        return null;
    }

    /**
     * Add an attribute to this session.
     *
     * @param key   key
     * @param value value
     */
    public abstract void setAttribute(Object key, Object value);

    /**
     * Return an attribute.
     *
     * @param key key
     * @return an attribute
     */
    public abstract <V> V getAttribute(Object key);

    /**
     * Return all attributes.
     *
     * @return an attribute
     */
    public abstract Map<Object, Object> getAttributes();


    /**
     * Remove an attribute.
     *
     * @param key key
     * @return true if successful.
     */
    public abstract <V> V removeAttribute(Object key);

    /**
     * Returns a string containing the unique identifier assigned
     * to this session. The identifier is assigned
     * by the servlet container and is implementation dependent.
     *
     * @return a string specifying the identifier
     * assigned to this session
     */
    public String getId() {
        return id;
    }

    protected void setId(String id) {
        this.id = id;
    }

    /**
     * Return a long representing the maximum idle time (in milliseconds) a session can be.
     *
     * @return a long representing the maximum idle time (in milliseconds) a session can be.
     */
    public long getTimeout() {
        return defaultTimeout;
    }

    public abstract void setTimeout(long maxIdleTimeInMillis);

    /**
     * Invalidates this session then unbinds any objects bound
     * to it.
     */
    public abstract void invalidate();

    /**
     * get this session is invalid
     */
    public abstract boolean isInvalid();

    /**
     * @return the timestamp when this session has been created.
     */
    public abstract long getTimestamp();

    /**
     * flush all attributes to session
     */
    public abstract void flush();

    /**
     * fetch attribute form session
     */
    public abstract void refresh();

    /**
     * touch session
     */
    public abstract void touch();

    public boolean isNew() {
        return isNew;
    }

    public String getHost() {
        return host;
    }

    public abstract long getLastAccessTime();
}

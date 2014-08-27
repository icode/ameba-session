package ameba.http.session;

import ameba.cache.Cache;

import java.io.Serializable;
import java.util.Map;

/**
 * @author icode
 */
public class CacheSession extends AbstractSession {
    private static final String SESSION_PRE_KEY = CacheSession.class.getName() + ".__SESSION__.";

    private String id;
    private Session session;
    private long timeout;

    protected CacheSession(String id, long timeout) {
        super(id, timeout);
        this.timeout = timeout;
    }

    @Override
    public void setAttribute(String key, Object value) {
        Session session = getSession();
        session.attributes.put(key, value);
        flush();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V> V getAttribute(String key) {
        return (V) getSession().attributes.get(key);
    }

    @Override
    public Map<String, Object> getAttributes() {
        return getSession().attributes;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V> V removeAttribute(String key) {
        Session session = getSession();
        V value = (V) session.attributes.remove(key);
        if (value != null)
            flush();
        return value;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    protected void setId(String id) {
        this.id = id;
    }

    @Override
    public long getSessionTimeout() {
        return timeout;
    }

    @Override
    public void invalidate() {
        Cache.syncDelete(getKey());
    }

    @Override
    public long getTimestamp() {
        return getSession().timestamp;
    }

    @Override
    public void flush() {
        if (session != null)
            setSession(session);
    }

    @Override
    public void refresh() {
        session = Cache.gat(getKey(), (int) (timeout * 1000));
    }

    private Session getSession() {
        if (session == null) {
            synchronized (this) {
                if (session == null) {
                    refresh();
                }
                if (session == null) {
                    session = new Session();
                    session.timestamp = System.currentTimeMillis();
                    flush();
                }
            }
        }
        return session;
    }

    private void setSession(Session session) {
        Cache.syncSet(getKey(), session, (int) (timeout * 1000));
    }

    private String getKey() {
        return SESSION_PRE_KEY + getId();
    }

    private class Session implements Serializable {
        Map<String, Object> attributes;
        long timestamp;
    }
}
package ameba.http.session;

import ameba.cache.Cache;

import java.util.Map;

/**
 * @author icode
 */
public class CacheSession extends AbstractSession {
    private static final String SESSION_PRE_KEY = CacheSession.class.getName() + ".__SESSION__.";

    private SessionStore session;

    protected CacheSession(String id, long timeout, boolean isNew) {
        super(id, timeout, isNew);
    }

    @Override
    public void setAttribute(String key, Object value) {
        SessionStore session = getSession();
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
        SessionStore session = getSession();
        V value = (V) session.attributes.remove(key);
        if (value != null)
            flush();
        return value;
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
    public boolean isInvalid() {
        refresh();
        return session == null;
    }

    @Override
    public long getTimestamp() {
        return getSession().timestamp;
    }

    @Override
    public void flush() {
        setSession(getSession());
    }

    @Override
    public void refresh() {
        session = Cache.gat(getKey(), (int) (timeout * 1000));
    }

    private SessionStore getSession() {
        if (session == null) {
            synchronized (this) {
                if (session == null) {
                    refresh();
                }
                if (session == null) {
                    session = new SessionStore();
                    session.timestamp = System.currentTimeMillis();
                }
            }
        }
        return session;
    }

    private void setSession(SessionStore session) {
        Cache.syncSet(getKey(), session, (int) (timeout * 1000));
    }

    private String getKey() {
        return SESSION_PRE_KEY + getId();
    }

}
package ameba.http.session;

import ameba.cache.Cache;

import java.util.Map;

/**
 * @author icode
 */
public class CacheSession extends AbstractSession {
    private static final String SESSION_PRE_KEY = CacheSession.class.getName() + ".__SESSION__.";

    private SessionStore store;
    private boolean fetched = false;

    protected CacheSession(String id, long timeout, boolean isNew) {
        super(id, timeout, isNew);
    }

    public static AbstractSession get(String id) {
        return Cache.get(getKey(id));
    }

    private static String getKey(String id) {
        return SESSION_PRE_KEY + id;
    }

    @Override
    public void setAttribute(String key, Object value) {
        getStore().attributes.put(key, value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V> V getAttribute(String key) {
        return (V) getStore().attributes.get(key);
    }

    @Override
    public Map<String, Object> getAttributes() {
        return getStore().attributes;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V> V removeAttribute(String key) {
        return (V) getStore().attributes.remove(key);
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
        if (isNew) {
            return false;
        }
        if (store == null) {
            refresh(false);
        }
        return store == null;
    }

    @Override
    public long getTimestamp() {
        return getStore().timestamp;
    }

    @Override
    public void flush() {
        if (store != null && store.isChange)
            Cache.syncSet(getKey(), store, (int) (timeout * 1000));
    }

    @Override
    public void refresh() {
        refresh(true);
    }

    public void refresh(boolean force) {
        if (!fetched || force) {
            store = Cache.gat(getKey(), (int) (timeout * 1000));
            fetched = true;
        }
    }

    private SessionStore getStore() {
        if (store == null) {
            synchronized (this) {
                if (store == null) {
                    refresh(false);
                }
                if (store == null) {
                    store = new SessionStore();
                    store.timestamp = System.currentTimeMillis();
                }
            }
        }
        return store;
    }

    private String getKey() {
        return getKey(getId());
    }

}
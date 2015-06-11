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
    private boolean isDelete = false;
    private boolean isTouch = false;

    protected CacheSession(String id, String host, long defaultTimeout, boolean isNew) {
        super(id, host, defaultTimeout, isNew);
    }

    public static AbstractSession get(String id) {
        return Cache.get(getKey(id));
    }

    private static String getKey(String id) {
        return SESSION_PRE_KEY + id;
    }

    @Override
    public void setAttribute(Object key, Object value) {
        getStore().getAttributes().put(key, value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V> V getAttribute(Object key) {
        return (V) getStore().getAttributes().get(key);
    }

    @Override
    public Map<Object, Object> getAttributes() {
        return getStore().getAttributes();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V> V removeAttribute(Object key) {
        return (V) getStore().getAttributes().remove(key);
    }

    @Override
    public long getTimeout() {
        return getStore().getTimeout();
    }

    @Override
    public void setTimeout(long maxIdleTimeInMillis) {
        getStore().setTimeout(maxIdleTimeInMillis);
    }

    @Override
    public void invalidate() {
        Cache.syncDelete(getKey());
        isDelete = true;
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
        return getStore().getTimeout();
    }

    @Override
    public void flush() {
        if (!isDelete) {
            if (store == null)
                getStore();
            if (store != null) {
                if (store.isChange()) {
                    store.unchange();
                    Cache.syncSet(getKey(), store, (int) store.getTimeout());
                } else if (isTouch) {
                    isTouch = false;
                    Cache.touch(getKey(), (int) getStore().getTimeout());
                }
            }
        }
    }

    @Override
    public void refresh() {
        refresh(true);
    }

    @Override
    public void touch() {
        isTouch = true;
    }

    public void refresh(boolean force) {
        if (!fetched || force) {
            store = Cache.get(getKey());
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
                    store = new SessionStore(defaultTimeout);
                }
            }
        }
        return store;
    }

    private String getKey() {
        return getKey(getId());
    }


    @Override
    protected void setId(String id) {
        if (id != null && !getId().equals(id)) {
            super.setId(id);
            fetched = false;
            isDelete = false;
            isTouch = false;
        }
    }
}
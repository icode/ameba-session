package ameba.http.session;

import ameba.cache.CacheException;
import ameba.util.ClassUtils;
import ameba.util.Times;
import com.google.common.primitives.Ints;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.internal.inject.InjectionManager;

import javax.inject.Inject;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.NewCookie;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * @author icode
 */
public class SessionFeature implements Feature {
    public static final String SET_COOKIE_KEY = SessionFilter.class.getName() + ".__SET_SESSION_COOKIE__";
    static int CLIENT_MAX_AGE = NewCookie.DEFAULT_MAX_AGE;
    static String SESSION_ID_KEY = "s";

    @Inject
    private InjectionManager injection;

    public static int getClientMaxAge() {
        return CLIENT_MAX_AGE;
    }

    public static String getSessionIdKey() {
        return SESSION_ID_KEY;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean configure(FeatureContext context) {
        Configuration configuration = context.getConfiguration();
        if (!context.getConfiguration().isRegistered(SessionFilter.class)) {
            String key = (String) configuration.getProperty("http.session.id.key");
            if (StringUtils.isNotBlank(key)) {
                SESSION_ID_KEY = key;
            }
            String sessionTimeout = (String) configuration.getProperty("http.session.timeout");
            Integer timeout;
            try {
                timeout = parseTime(sessionTimeout);
                if (timeout != null) {
                    Session.SESSION_TIMEOUT = timeout;
                }
            } catch (Exception e) {
                throw new CacheException("http.session.timeout config error for [" + sessionTimeout + "] value.", e);
            }

            String clientMaxAge = (String) configuration.getProperty("http.session.client.maxAge");
            try {
                Integer maxAge = parseTime(clientMaxAge);
                if (maxAge != null) {
                    CLIENT_MAX_AGE = maxAge;
                } else if (timeout != null) {
                    CLIENT_MAX_AGE = timeout;
                }
            } catch (Exception e) {
                throw new CacheException("http.session.client.maxAge config error for [" + clientMaxAge + "] value.", e);
            }

            String sessionClassStr = (String) configuration.getProperty("http.session.class");
            Class sessionClass = CacheSession.class;
            if (StringUtils.isNotBlank(sessionClassStr)) {
                try {
                    sessionClass = ClassUtils.getClass(sessionClassStr);
                } catch (ClassNotFoundException e) {
                    throw new SessionExcption("new session instance error");
                }
            }
            String storeClassStr = (String) configuration.getProperty("http.session.client.store.class");
            Class storeClass = SessionClientCookieStore.class;
            if (StringUtils.isNotBlank(storeClassStr)) {
                try {
                    storeClass = ClassUtils.getClass(storeClassStr);
                } catch (ClassNotFoundException e) {
                    throw new SessionExcption("new session instance error");
                }
            }
            Session.CLIENT_STORE = (SessionClientStore) injection.createAndInitialize(storeClass);
            setSessionConstructorHandle(sessionClass);
            setGetSessionHandle(sessionClass);
            setNewSessionIdHandle(sessionClass);
            context.register(SessionFilter.class);
        }
        return true;
    }

    private Integer parseTime(String time) {
        if (StringUtils.isNotBlank(time)) {
            time = StringUtils.deleteWhitespace(time);
            if (time.matches("^([0-9]+)(d|h|(mi?n)|s)$")) {
                return Times.parseDuration(time);
            } else {
                return Ints.tryParse(time);
            }
        }
        return null;
    }

    private void setGetSessionHandle(Class clazz) {
        try {
            Session.GET_SESSION_METHOD_HANDLE = MethodHandles.publicLookup()
                    .findStatic(clazz, "get", MethodType.methodType(AbstractSession.class, String.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new SessionExcption(e);
        }
    }

    private void setNewSessionIdHandle(Class sessionClass) {
        try {
            Session.NEW_SESSION_ID_METHOD_HANDLE = MethodHandles.publicLookup()
                    .findStatic(sessionClass, "newSessionId", MethodType.methodType(String.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new SessionExcption(e);
        }
    }

    private void setSessionConstructorHandle(Class sessionClass) {
        try {
            Session.SESSION_CONSTRUCTOR_HANDLE = MethodHandles.lookup().findConstructor(sessionClass,
                    MethodType.methodType(void.class, String.class, String.class, long.class, boolean.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new SessionExcption(e);
        }
    }
}

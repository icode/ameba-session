package ameba.http.session;

import ameba.cache.CacheException;
import ameba.util.ClassUtils;
import ameba.util.Times;
import com.google.common.primitives.Ints;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * @author icode
 */
public class SessionFeature implements Feature {
    @Override
    @SuppressWarnings("unchecked")
    public boolean configure(FeatureContext context) {
        Configuration configuration = context.getConfiguration();
        if (!context.getConfiguration().isRegistered(SessionFilter.class)) {
            String key = (String) configuration.getProperty("http.session.cookie.key");
            if (StringUtils.isNotBlank(key)) {
                SessionFilter.DEFAULT_SESSION_ID_COOKIE_KEY = key;
            }
            String sessionTimeout = (String) configuration.getProperty("http.session.timeout");
            try {
                Integer timeout = parseTime(sessionTimeout);
                if (timeout != null) {
                    SessionFilter.SESSION_TIMEOUT = timeout;
                }
            } catch (Exception e) {
                throw new CacheException("http.session.timeout config error for [" + sessionTimeout + "] value.", e);
            }

            String cookieMaxAge = (String) configuration.getProperty("http.session.cookie.maxAge");
            try {
                Integer maxAge = parseTime(cookieMaxAge);
                if (maxAge != null) {
                    SessionFilter.COOKIE_MAX_AGE = maxAge;
                }
            } catch (Exception e) {
                throw new CacheException("http.session.cookie.maxAge config error for [" + cookieMaxAge + "] value.", e);
            }

            String sessionClassStr = (String) configuration.getProperty("http.session.class");
            if (StringUtils.isNotBlank(sessionClassStr)) {
                try {
                    Class sessionClass = ClassUtils.getClass(sessionClassStr);
                    SessionFilter.METHOD_HANDLE = MethodHandles.lookup().findConstructor(sessionClass,
                            MethodType.methodType(void.class, String.class, long.class, boolean.class));
                    setGetSessionHandle(sessionClass);
                } catch (NoSuchMethodException | ClassNotFoundException | IllegalAccessException e) {
                    throw new SessionExcption("new session instance error");
                }
            } else {
                setGetSessionHandle(CacheSession.class);
            }
            context.register(SessionFilter.class);
        }
        return true;
    }

    private Integer parseTime(String time) {
        if (StringUtils.isNotBlank(time)) {
            time = StringUtils.deleteWhitespace(time);
            if (time.matches("^([0-9]+)(d|h|(mi?n)|s)$")) {
                return Times.parseDuration(time) * 1000;
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
}

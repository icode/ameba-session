package ameba.http.session;

import ameba.cache.CacheException;
import ameba.util.ClassUtils;
import ameba.util.Times;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

/**
 * @author icode
 */
public class SessionFeature implements Feature {
    @Override
    @SuppressWarnings("unchecked")
    public boolean configure(FeatureContext context) {
        Configuration configuration = context.getConfiguration();
        if (!context.getConfiguration().isRegistered(SessionFilter.class)) {
            SessionFilter.DEFAULT_SESSION_ID_COOKIE_KEY = (String) configuration.getProperty("http.session.cookie.key");
            String sessionTimeout = (String) configuration.getProperty("http.session.timeout");
            if (StringUtils.isNotBlank(sessionTimeout)) {
                sessionTimeout = StringUtils.deleteWhitespace(sessionTimeout);
                try {
                    if (sessionTimeout.matches("^([0-9]+)(d|h|(mi?n)|s)$")) {
                        SessionFilter.SESSION_TIMEOUT = Times.parseDuration(sessionTimeout) * 1000;
                    } else {
                        SessionFilter.SESSION_TIMEOUT = Long.parseLong(sessionTimeout);
                    }
                } catch (Exception e) {
                    throw new CacheException("http.session.timeout config error for [" + sessionTimeout + "] value.", e);
                }
            }

            String sessionClassStr = (String) configuration.getProperty("http.session.class");
            if (StringUtils.isNotBlank(sessionClassStr)) {
                try {
                    Class sessionClass = ClassUtils.getClass(sessionClassStr);
                    SessionFilter.SESSION_IMPL_CONSTRUCTOR = sessionClass.getDeclaredConstructor(String.class, long.class, boolean.class);
                } catch (NoSuchMethodException e) {
                    throw new SessionExcption("new session instance error");
                } catch (ClassNotFoundException e) {
                    throw new SessionExcption("new session instance error");
                }
            }
            context.register(SessionFilter.class);
        }
        return true;
    }
}

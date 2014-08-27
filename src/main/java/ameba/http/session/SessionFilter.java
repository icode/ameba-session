package ameba.http.session;

import ameba.util.Times;
import org.apache.commons.codec.digest.DigestUtils;

import javax.annotation.Priority;
import javax.inject.Singleton;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.ext.Provider;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

/**
 * @author icode
 */
@Provider
@Priority(Priorities.AUTHENTICATION - 1)
@Singleton
public class SessionFilter implements ContainerRequestFilter, ContainerResponseFilter {
    static String DEFAULT_SESSION_ID_COOKIE_KEY = "SID";
    static long SESSION_TIMEOUT = Times.parseDuration("1h") * 1000;
    static Constructor<AbstractSession> SESSION_IMPL_CONSTRUCTOR;
    private static final String SET_COOKIE_KEY = SessionFilter.class.getName() + ".__SET_SESSION_COOKIE__";

    @Override
    @SuppressWarnings("unchecked")
    public void filter(ContainerRequestContext requestContext) {
        Cookie cookie = requestContext.getCookies().get(DEFAULT_SESSION_ID_COOKIE_KEY);
        boolean isNew = false;
        if (cookie == null) {
            isNew = true;
            cookie = newCookie(requestContext);
        }
        AbstractSession session;
        if (SESSION_IMPL_CONSTRUCTOR != null) {
            try {
                session = SESSION_IMPL_CONSTRUCTOR.newInstance(cookie.getValue(), SESSION_TIMEOUT, isNew);
            } catch (InvocationTargetException e) {
                throw new SessionExcption("new session instance error");
            } catch (InstantiationException e) {
                throw new SessionExcption("new session instance error");
            } catch (IllegalAccessException e) {
                throw new SessionExcption("new session instance error");
            }
        } else {
            session = new CacheSession(cookie.getValue(), SESSION_TIMEOUT, isNew);
            if (isNew) {
                session.flush();
            }
        }

        if (!session.isNew() && session.isInvalid()) {
            cookie = newCookie(requestContext);
            session.setId(cookie.getValue());
            session.flush();
        }

        Session.sessionThreadLocal.set(session);
    }

    private String newSessionId() {
        return DigestUtils.sha1Hex(UUID.randomUUID().toString() + Math.random() + this.hashCode() + System.nanoTime());
    }

    private NewCookie newCookie(ContainerRequestContext requestContext) {
        NewCookie cookie = new NewCookie(
                DEFAULT_SESSION_ID_COOKIE_KEY,
                newSessionId(),
                requestContext.getUriInfo().getBaseUri().getPath(),
                null,
                Cookie.DEFAULT_VERSION, null,
                NewCookie.DEFAULT_MAX_AGE,
                null, false, true);


        requestContext.setProperty(SET_COOKIE_KEY, cookie);
        return cookie;
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        NewCookie cookie = (NewCookie) requestContext.getProperty(SET_COOKIE_KEY);

        if (cookie != null)
            responseContext.getHeaders().add("Set-Cookie", cookie.toString());
    }
}

package ameba.http.session;

import ameba.util.Times;
import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;

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
import java.lang.invoke.MethodHandle;
import java.net.URI;
import java.util.UUID;

/**
 * @author icode
 */
@Provider
@Priority(Priorities.AUTHENTICATION - 1)
@Singleton
public class SessionFilter implements ContainerRequestFilter, ContainerResponseFilter {
    private static final String SET_COOKIE_KEY = SessionFilter.class.getName() + ".__SET_SESSION_COOKIE__";
    static String DEFAULT_SESSION_ID_COOKIE_KEY = "s";
    static long SESSION_TIMEOUT = Times.parseDuration("2h") * 1000;
    static int COOKIE_MAX_AGE = NewCookie.DEFAULT_MAX_AGE;
    static MethodHandle METHOD_HANDLE;

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
        if (METHOD_HANDLE != null) {
            try {
                session = (AbstractSession) METHOD_HANDLE.invoke(cookie.getValue(), SESSION_TIMEOUT, isNew);
            } catch (Throwable throwable) {
                throw new SessionExcption("new session instance error");
            }
        } else {
            session = new CacheSession(cookie.getValue(), SESSION_TIMEOUT, isNew);
        }

        if (!session.isNew() && session.isInvalid()) {
            cookie = newCookie(requestContext);
            session.setId(cookie.getValue());
        }

        Session.sessionThreadLocal.set(session);
    }

    protected String newSessionId() {
        return Hashing.sha1()
                .hashString(
                        UUID.randomUUID().toString() + Math.random() + this.hashCode() + System.nanoTime(),
                        Charsets.UTF_8
                )
                .toString();
    }

    private NewCookie newCookie(ContainerRequestContext requestContext) {
        URI uri = requestContext.getUriInfo().getBaseUri();
        NewCookie cookie = new NewCookie(
                DEFAULT_SESSION_ID_COOKIE_KEY,
                newSessionId(),
                uri.getPath(),
                uri.getHost(),
                Cookie.DEFAULT_VERSION,
                null,
                COOKIE_MAX_AGE,
                null,
                requestContext.getSecurityContext().isSecure(),
                true);


        requestContext.setProperty(SET_COOKIE_KEY, cookie);
        return cookie;
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {

        Session.flush();

        NewCookie cookie = (NewCookie) requestContext.getProperty(SET_COOKIE_KEY);

        if (cookie != null)
            responseContext.getHeaders().add("Set-Cookie", cookie.toString());
    }
}

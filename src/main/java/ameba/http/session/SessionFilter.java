package ameba.http.session;

import ameba.core.Requests;
import ameba.mvc.assets.AssetsResource;
import ameba.util.Cookies;
import ameba.util.Times;
import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.inject.Singleton;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.*;
import javax.ws.rs.core.*;
import java.lang.invoke.MethodHandle;
import java.util.List;
import java.util.UUID;

/**
 * @author icode
 */
@PreMatching
@Priority(Priorities.AUTHENTICATION - 500)
@Singleton
public class SessionFilter implements ContainerRequestFilter, ContainerResponseFilter {
    private static final Logger logger = LoggerFactory.getLogger(SessionFilter.class);
    private static final String SET_COOKIE_KEY = SessionFilter.class.getName() + ".__SET_SESSION_COOKIE__";
    static String SESSION_ID_COOKIE_KEY = "s";
    static long SESSION_TIMEOUT = Times.parseDuration("2h");
    static int COOKIE_MAX_AGE = NewCookie.DEFAULT_MAX_AGE;
    static MethodHandle METHOD_HANDLE;

    @Context
    private UriInfo uriInfo;

    private boolean isIgnore() {
        List<Object> resources = uriInfo.getMatchedResources();
        return resources.size() != 0 && AssetsResource.class.isAssignableFrom(resources.get(0).getClass());
    }

    @Override
    @SuppressWarnings("unchecked")
    public void filter(ContainerRequestContext requestContext) {
        if (isIgnore()) {
            return;
        }
        Cookie cookie = requestContext.getCookies().get(SESSION_ID_COOKIE_KEY);
        boolean isNew = false;
        if (cookie == null) {
            isNew = true;
            cookie = newCookie(requestContext);
        }
        AbstractSession session;
        String host = Requests.getRemoteRealAddr();
        if (host == null || host.equals("unknown")) {
            host = Requests.getRemoteAddr();
        }
        String sessionId = cookie.getValue();
        if (METHOD_HANDLE != null) {
            try {
                session = (AbstractSession) METHOD_HANDLE.invoke(sessionId, host, SESSION_TIMEOUT, isNew);
            } catch (Throwable throwable) {
                throw new SessionExcption("new session instance error");
            }
        } else {
            session = new CacheSession(sessionId, host, SESSION_TIMEOUT, isNew);
        }

        if (!session.isNew()) {
            try {
                checkSession(session, requestContext);
            } catch (Exception e) {
                logger.warn("get session error", e);
            }
        }

        Session.sessionThreadLocal.set(session);
    }

    private void checkSession(AbstractSession session, ContainerRequestContext requestContext) {
        if (session.isInvalid()) {
            Cookie cookie = newCookie(requestContext);
            session.setId(cookie.getValue());
        } else {
            session.touch();
            session.flush();
        }
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
//        URI uri = requestContext.getUriInfo().getBaseUri();
//        String domain = uri.getHost();
//        // localhost domain must be null
//        if (domain.equalsIgnoreCase("localhost")) {
//            domain = null;
//        }

        NewCookie cookie = new NewCookie(
                SESSION_ID_COOKIE_KEY,
                newSessionId(),
                "/",
                null,
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
        if (isIgnore()) {
            return;
        }

        try {
            Session.flush();
        } catch (Exception e) {
            logger.warn("flush session error", e);
        }

        NewCookie cookie = (NewCookie) requestContext.getProperty(SET_COOKIE_KEY);

        if (cookie == null && !Session.isInvalid()) {
            cookie = Cookies.newDeletedCookie(SESSION_ID_COOKIE_KEY);
        }

        if (cookie != null)
            responseContext.getHeaders().add(HttpHeaders.SET_COOKIE, cookie);
        Session.sessionThreadLocal.remove();
    }
}

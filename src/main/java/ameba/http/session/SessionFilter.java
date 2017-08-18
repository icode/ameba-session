package ameba.http.session;

import ameba.core.Requests;
import ameba.mvc.assets.AssetsResource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.inject.Singleton;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.UriInfo;
import java.util.List;

import static ameba.http.session.SessionFeature.SET_COOKIE_KEY;

/**
 * @author icode
 */
@PreMatching
@Priority(Priorities.AUTHENTICATION - 500)
@Singleton
public class SessionFilter implements ContainerRequestFilter, ContainerResponseFilter {
    private static final Logger logger = LoggerFactory.getLogger(SessionFilter.class);
    private static final String EXECED_KEY = SessionFilter.class.getName() + ".EXECED";

    @Context
    private UriInfo uriInfo;

    protected boolean isIgnore() {
        List<Object> resources = uriInfo.getMatchedResources();
        return resources.size() != 0 && AssetsResource.class.isAssignableFrom(resources.get(0).getClass());
    }

    @Override
    @SuppressWarnings("unchecked")
    public void filter(ContainerRequestContext requestContext) {
        if (isIgnore()) {
            return;
        }
        Boolean execed = (Boolean) requestContext.getProperty(EXECED_KEY);
        if (execed != null) {
            return;
        }
        requestContext.setProperty(EXECED_KEY, false);
        String sessionId = Session.CLIENT_STORE.getToken();
        if (StringUtils.isNotBlank(sessionId)) {
            String host = Requests.getRemoteRealAddr();
            AbstractSession session = Session.create(sessionId, host, false);
            try {
                checkSession(session, requestContext);
            } catch (Exception e) {
                logger.warn("get session error", e);
            }
        }
    }

    private void checkSession(AbstractSession session, ContainerRequestContext requestContext) {
        if (!session.isInvalid()) {
            session.touch();
            session.flush();
            requestContext.setProperty(Session.REQ_SESSION_KEY, session);
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        if (isIgnore()) {
            return;
        }
        Boolean execed = (Boolean) requestContext.getProperty(EXECED_KEY);
        if (execed == null || execed) {
            return;
        }
        requestContext.setProperty(EXECED_KEY, true);
        try {
            Session.flush();
        } catch (Exception e) {
            logger.warn("flush session error", e);
        }


        NewCookie cookie = Requests.getProperty(SET_COOKIE_KEY);

        if (cookie == null && Session.isInvalid() && Session.get(false) != null) {
            Session.CLIENT_STORE.removeToken();
            cookie = Requests.getProperty(SET_COOKIE_KEY);
        }

        if (cookie != null)
            responseContext.getHeaders().add(HttpHeaders.SET_COOKIE, cookie);
    }
}

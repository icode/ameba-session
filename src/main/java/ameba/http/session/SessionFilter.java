package ameba.http.session;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

/**
 * @author icode
 */
@Provider
@Priority(Priorities.AUTHENTICATION - 1)
public class SessionFilter implements ContainerRequestFilter {
    public static final String AUTHENTICATION_COOKIE_KEY = "SID";
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        Cookie cookie = requestContext.getCookies().get(AUTHENTICATION_COOKIE_KEY);

    }
}

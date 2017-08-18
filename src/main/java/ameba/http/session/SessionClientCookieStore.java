package ameba.http.session;

import ameba.core.Requests;
import ameba.util.Cookies;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;

import static ameba.http.session.SessionFeature.SESSION_ID_KEY;
import static ameba.http.session.SessionFeature.SET_COOKIE_KEY;

/**
 * @author icode
 */
public class SessionClientCookieStore implements SessionClientStore {
    @Override
    public String getToken() {
        Cookie cookie = Requests.getCookies().get(SESSION_ID_KEY);
        if (cookie != null && !Cookies.DELETED_COOKIE_VALUE.equals(cookie.getValue())) {
            return cookie.getValue();
        }
        return null;
    }

    @Override
    public void removeToken() {
        Requests.setProperty(SET_COOKIE_KEY, Cookies.newDeletedCookie(SESSION_ID_KEY));
    }

    @Override
    public void createToken(String token) {
        NewCookie cookie = new NewCookie(
                SESSION_ID_KEY,
                token,
                "/",
                null,
                Cookie.DEFAULT_VERSION,
                null,
                SessionFeature.COOKIE_MAX_AGE,
                null,
                Requests.getSecurityContext().isSecure(),
                true);
        Requests.setProperty(SET_COOKIE_KEY, cookie);
    }
}

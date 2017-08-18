package ameba.http.session;

/**
 * Session client store
 *
 * @author icode
 */
public interface SessionClientStore {
    String getToken();

    void removeToken();

    void createToken(String token);
}

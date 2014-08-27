package ameba.http.session;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

/**
 * @author icode
 */
public class SessionFeature implements Feature {
    @Override
    public boolean configure(FeatureContext context) {
        if (!context.getConfiguration().isRegistered(SessionFilter.class))
            context.register(SessionFilter.class);
        return true;
    }
}

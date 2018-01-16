package si.uni_lj.fri.rso.ir_recommendation.cdi;

import com.kumuluz.ee.configuration.utils.ConfigurationUtil;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import java.util.logging.Logger;

@ApplicationScoped
public class ConfigEventHandler {
    private static final Logger log = Logger.getLogger(ConfigEventHandler.class.getName());

    public void init(@Observes @Initialized(ApplicationScoped.class) Object init) {
        final String watchedKey = "rest-config.endpoint-enabled";

        ConfigurationUtil.getInstance().subscribe(watchedKey, (String key, String value) -> {
            if (watchedKey.equals(key)) {
                if ("true".equals(value.toLowerCase())) {
                    log.info("Endpoint enabled.");
                } else {
                    log.info("Endpoint disabled.");
                }
            }
        });
    }
}

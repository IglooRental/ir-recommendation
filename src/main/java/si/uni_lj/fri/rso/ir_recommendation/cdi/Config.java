package si.uni_lj.fri.rso.ir_recommendation.cdi;

import com.kumuluz.ee.configuration.cdi.ConfigBundle;
import com.kumuluz.ee.configuration.cdi.ConfigValue;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
@ConfigBundle("rest-config")
public class Config {

    @ConfigValue(watch = true)
    private Boolean endpointEnabled;

    @ConfigValue(watch = true)
    private boolean healthy;

    @ConfigValue(watch = true)
    private int loadIntensity;

    public Boolean getEndpointEnabled() {
        return endpointEnabled;
    }

    public void setEndpointEnabled(Boolean endpointEnabled) {
        this.endpointEnabled = endpointEnabled;
    }

    public boolean getHealthy() {
        return healthy;
    }

    public void setHealthy(boolean healthy) {
        this.healthy = healthy;
    }

    public int getLoadIntensity() {
        return loadIntensity;
    }

    public void setLoadIntensity(int loadIntensity) {
        this.loadIntensity = loadIntensity;
    }
}

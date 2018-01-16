package si.uni_lj.fri.rso.ir_recommendation.cdi;

import org.eclipse.microprofile.health.Health;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@Health
@ApplicationScoped
public class RecommendationConfigModifiableHealthCheck implements HealthCheck {
    @Inject
    private Config config;

    @Override
    public HealthCheckResponse call() {
        if (config.getHealthy()) {
            return HealthCheckResponse.named(RecommendationConfigModifiableHealthCheck.class.getSimpleName()).up().build();
        } else {
            return HealthCheckResponse.named(RecommendationConfigModifiableHealthCheck.class.getSimpleName()).down().build();
        }
    }
}

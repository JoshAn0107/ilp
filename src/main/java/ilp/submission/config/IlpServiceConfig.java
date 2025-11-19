package ilp.submission.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration for ILP service connection.
 */
@Configuration
public class IlpServiceConfig {

    @Value("${ilp.endpoint:#{systemEnvironment['ILP_ENDPOINT'] ?: 'https://ilp-rest-2025-bvh6e9hschfagrgy.ukwest-01.azurewebsites.net/'}}")
    private String ilpEndpoint;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public String ilpEndpoint() {
        // Also check environment variable directly as fallback
        String envEndpoint = System.getenv("ILP_ENDPOINT");
        if (envEndpoint != null && !envEndpoint.isEmpty()) {
            return envEndpoint.endsWith("/") ? envEndpoint : envEndpoint + "/";
        }
        return ilpEndpoint.endsWith("/") ? ilpEndpoint : ilpEndpoint + "/";
    }
}

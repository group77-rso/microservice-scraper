package com.scraper.api.v1.health;

import com.scraper.services.config.MicroserviceLocations;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Readiness
@ApplicationScoped
public class ScraperHealth implements HealthCheck {

    @Inject
    private MicroserviceLocations microserviceLocations;

    @Override
    public HealthCheckResponse call() {
        String urlMerchants = microserviceLocations.getMerchants() + "/v1/merchants/ping/";
        if (pingDependency(urlMerchants))
        {
            return HealthCheckResponse.up(ScraperHealth.class.getSimpleName());
        }
        return HealthCheckResponse.down(ScraperHealth.class.getSimpleName());
    }

    private boolean pingDependency(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            int code = HttpClient.newBuilder()
                    .build()
                    .send(request, HttpResponse.BodyHandlers.ofString())
                    .statusCode();
            return code < 300;
        }
        catch (IOException | InterruptedException e) {
            return false;
        }
    }

}

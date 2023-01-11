package com.scraper.api.v1.health;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

import javax.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

@Readiness
@ApplicationScoped
public class SparWebPageAvailableCheck implements HealthCheck {

    @Override
    public HealthCheckResponse call() {

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL("https://www.spar.si/online/").openConnection();
            connection.setRequestMethod("HEAD");
            return HealthCheckResponse.up(SparWebPageAvailableCheck.class.getSimpleName());
        } catch (IOException e) {
            return HealthCheckResponse.down(SparWebPageAvailableCheck.class.getSimpleName());
        }
    }


}

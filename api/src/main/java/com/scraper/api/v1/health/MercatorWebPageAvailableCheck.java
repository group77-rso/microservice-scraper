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
public class MercatorWebPageAvailableCheck implements HealthCheck {

    @Override
    public HealthCheckResponse call() {

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL("https://trgovina.mercator.si/market").openConnection();
            connection.setRequestMethod("HEAD");
            return HealthCheckResponse.up(MercatorWebPageAvailableCheck.class.getSimpleName());
        } catch (IOException e) {
            return HealthCheckResponse.down(MercatorWebPageAvailableCheck.class.getSimpleName());
        }
    }


}

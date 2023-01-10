//package com.scraper.api.v1.health;
//
//import com.scraper.services.config.MicroserviceLocations;
//import org.eclipse.microprofile.health.HealthCheck;
//import org.eclipse.microprofile.health.HealthCheckResponse;
//import org.eclipse.microprofile.health.Readiness;
//
//import javax.enterprise.context.ApplicationScoped;
//import javax.inject.Inject;
//import java.io.IOException;
//import java.net.HttpURLConnection;
//import java.net.URL;
//
//@Readiness
//@ApplicationScoped
//public class ScraperHealth implements HealthCheck {
//
//    @Inject
//    private MicroserviceLocations microserviceLocations;
//
//    @Override
//    public HealthCheckResponse call() {
//        String urlMerchants = microserviceLocations.getMerchants() + "/v1/merchants/ping/";
//        if (pingDependency(urlMerchants))
//        {
//            return HealthCheckResponse.up(ScraperHealth.class.getSimpleName());
//        }
//        return HealthCheckResponse.down(ScraperHealth.class.getSimpleName());
//    }
//
//    private boolean pingDependency(String url) {
//        try {
//            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
//            connection.setRequestMethod("HEAD");
//            return connection.getResponseCode() == 200;
//        }
//        catch (IOException e) {
//            return false;
//        }
//    }
//
//}

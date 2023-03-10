package com.scraper.api.v1.resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kumuluz.ee.logs.cdi.Log;
import com.scraper.api.v1.scraper.JagerScraper;
import com.scraper.api.v1.scraper.MercatorScraper;
import com.scraper.api.v1.scraper.Scraper;
import com.scraper.api.v1.scraper.SparScraper;
import com.scraper.lib.Price;
import com.scraper.services.config.MicroserviceLocations;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.io.IOException;

@Log
@ApplicationScoped
@Path("/scraper")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ScraperResource {

    private final Logger log = Logger.getLogger(ScraperResource.class.getName());

    @Inject
    private MicroserviceLocations microserviceLocations;
    private static HttpURLConnection conn;

    @GET
    @Operation(description = "Scrapes websites to obtain new prices of products.", summary = "Scrape websites with products")
    @APIResponses({
            @APIResponse(responseCode = "200",
                    description = "Scraping done",
                    headers = {@Header(name = "requestId", description = "Unique request id.")})})
    @Path("scrape")
    public Response scrapePrices(@Parameter(hidden = true) @HeaderParam("requestId") String requestId) {

        requestId = requestId != null ? requestId : UUID.randomUUID().toString();

        // najprej poiscemo vse cene
        String content = fetchPrices();

        if (content == null || content.length() == 0) {
            return null;
        }

        // Map objects that we got
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writerWithDefaultPrettyPrinter();

        ArrayList<Price> prices = new ArrayList<>();
        try {
            prices = objectMapper.readValue(
                    content,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Price.class));
        } catch (JsonProcessingException e) {
            log.log(Level.SEVERE, String.format("%s : Can not parse json to java object.", this.getClass().getName()) + " - requestId: " + requestId);
        }

        for (Price price : prices) {
            float newPrice;
            try {
                switch (price.getMerchant()) {
                    case "Mercator" -> newPrice = MercatorScraper.scrape(price, requestId);
                    case "Spar" -> newPrice = SparScraper.scrape(price, requestId);
                    case "Jager" -> newPrice = JagerScraper.scrape(price, requestId);
                    default -> newPrice = Scraper.scrape(price, requestId);
                }
                updatePrices(price, newPrice, requestId);
            } catch (IOException e) {
                log.log(Level.SEVERE, String.format("Could not scrape new price for product %s on link %s. - requestId: %s", price.getProductId(), price.getProductLink(), requestId));
                updatePrices(price, 0, requestId);
            }
        }
        sendSuccessEmail(microserviceLocations.getEmails() + "/v1/emails/send", "requestId");
        return Response.status(Response.Status.OK).header("requestId", requestId).entity(prices).build();
    }

    @Timeout(value = 2, unit = ChronoUnit.SECONDS)
    @Retry(maxRetries = 4)
    private String fetchPrices() {
        return callRestGet(microserviceLocations.getMerchants() + "/v1/prices/");
    }

    /**
     * Makes request to API at given url.
     *
     * @param url       url of the service we are calling
     * @param requestId passed on for tracking requests
     */
    private void sendSuccessEmail(String url, String requestId) {
        log.log(Level.INFO, String.format("Sending success email after successfully finishing scraping. - requestId: %s", requestId));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMinutes(2))
                .header("Content-Type", "application/json")
                .header("requestId", requestId)
                .GET()
                .build();

        HttpClient.newBuilder()
                .build()
                .sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Updates price
     *
     * @param price     Object for which we want to update price
     * @param newPrice  new price value
     * @param requestId passed on for tracking requests
     */
    private void updatePrices(Price price, float newPrice, String requestId) {
        String urlString = microserviceLocations.getMerchants() + "/v1/products/" + price.getProductId();
        String requestBody = String.format("""
                {
                    "price": %.2f
                }""", newPrice);
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlString))
                    .timeout(Duration.ofMinutes(2))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpClient.newBuilder()
                    .build()
                    .sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenAccept(s ->
                            log.log(Level.INFO,
                                    String.format("Price for product %d in %s was updated.",
                                            price.getProductId(),
                                            price.getMerchant() + " - requestId: " + requestId)));
        } catch (Exception e) {
            log.log(Level.SEVERE, String.format("Post was unsuccessful because of %s.", e) + " - requestId: " + requestId);
        }
    }


    /**
     * Functions that rends GET request and returns responses string
     *
     * @param urlString url that we want to call
     * @return response content as a string
     */
    private String callRestGet(String urlString) {
        StringBuilder content = new StringBuilder();
        try {
            URL url = new URL(urlString);
            conn = (HttpURLConnection) url.openConnection();

            // Request setup
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);// 5000 milliseconds = 5 seconds
            conn.setReadTimeout(5000);
            conn.setRequestProperty("Content-Type", "application/json");

            int status = conn.getResponseCode();
            if (status > 300) {
                log.log(Level.SEVERE, String.format("Reaching %s failed with status %d.", url, status));
                conn.disconnect();
                return null;
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
        } catch (IOException e) {
            log.log(Level.SEVERE, String.format("Connecting %s was unsuccessful. Error %s occured.", urlString, e.getMessage()));
        } finally {
            conn.disconnect();
        }
        return content.toString();
    }

}
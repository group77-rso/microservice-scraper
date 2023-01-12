package com.scraper.api.v1.scraper;

import com.scraper.lib.Price;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ApplicationScoped
public class JagerScraper extends Scraper {

    private final static Logger log = Logger.getLogger(JagerScraper.class.getName());

    @Timeout(value = 2, unit = ChronoUnit.SECONDS)
    @Fallback(fallbackMethod = "scraperPriceFallback")
    public static float scrape(Price price, String requestId) throws IOException {

        Document doc = reachWebpage(price.getProductLink());

        // Select all the elements with the class "class-name"
        Elements elements = doc.select(".regular-price");

        // extract price
        Pattern pattern = Pattern.compile("[0-9]+?,[0-9][0-9]", Pattern.CASE_INSENSITIVE);

        for (Element element : elements) {
            Matcher matcher = pattern.matcher(element.text());
            boolean match = matcher.find();
            if (match) {
                String officialPrice = matcher.group();
                log.log(Level.INFO, String.format("New price for product %d: %s. (before %s) - requestId: %s", price.getProductId(), officialPrice, price.getPrice(), requestId));
                officialPrice = officialPrice.replace(",", ".");
                return Float.parseFloat(officialPrice);
            }
        }
        return 0;
    }

    @Timeout(value = 2, unit = ChronoUnit.SECONDS)
    @Retry(maxRetries = 2, delay = 500)
    private static Document reachWebpage(String url) throws IOException {
        Document doc = Jsoup.connect(url).get();
        doc.outputSettings().charset("UTF-8");
        return doc;
    }

    private float scraperPriceFallback(Price price, String requestId) {
        log.log(Level.SEVERE, String.format("Could not scrape new price for product %s. Connection to url %s was unsuccessful. - requestId: %s", price.getProductId(), price.getProductLink(), requestId));
        return 0;
    }

}

package com.scraper.api.v1.scraper;

import com.scraper.lib.Price;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MercatorScraper extends Scraper {

    private final static Logger log = Logger.getLogger(MercatorScraper.class.getName());

    public static float scrape(Price price) {

        // stran, ki jo scrapamo
        String url = price.getProductLink();

        try {
            Document doc;
            // Fetch the webpage HTML
            doc = Jsoup.connect(url).get();
            // Set the output encoding to UTF-8
            doc.outputSettings().charset("UTF-8");



            // Select all the elements with the class "class-name"
            Elements elements = doc.select(".price-unit");

            // extract price
            Pattern pattern = Pattern.compile("[0-9]+?,[0-9][0-9]", Pattern.CASE_INSENSITIVE);

            for (Element element : elements) {
                Matcher matcher = pattern.matcher(element.text());
                boolean match = matcher.find();
                if (match) {
                    String officialPrice = matcher.group();
                    log.log(Level.INFO, "--> new price: " + officialPrice);
                    officialPrice = officialPrice.replace(",", ".");
                    return Float.parseFloat(officialPrice);
                }
            }
        } catch (IOException e) {
            log.log(Level.SEVERE, "Could not create the connection.");
        }
        return 0;
    }



}

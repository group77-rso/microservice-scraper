package com.scraper.api.v1.scraper;

import com.scraper.api.v1.resources.ScraperResource;
import com.scraper.lib.Price;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SparScraper extends Scraper {

    private final static Logger log = Logger.getLogger(ScraperResource.class.getName());

    public static float scrape(Price price) {

        // dokler testiramo, sem shranim html, da ne nalagam za vsako znova
        String fileName = "page.html";

        // stran, ki jo scrapamo
        String url = price.getProductLink();

        try {
            File file = new File(fileName);
            Document doc;
            if (file.exists()) {
                FileReader reader = new FileReader(file);
                char[] chars = new char[(int) file.length()];
                reader.read(chars);
                reader.close();

                // Parse the contents of the file as HTML
                String html = new String(chars);
                doc = Jsoup.parse(html);
            } else {
                // Fetch the webpage HTML
                doc = Jsoup.connect(url).get();
                // Set the output encoding to UTF-8
                doc.outputSettings().charset("UTF-8");
                // Get the HTML as a string
                String html = doc.html();
                // Write the HTML to a file
                FileWriter writer = new FileWriter(file);
                writer.write(html);
                writer.close();
            }
            // Select all the elements with the class "class-name"
            Elements elements = doc.select(".productDetailsPricePerUnit ");

            // extract price
            Pattern pattern = Pattern.compile("[0-9]+?,[0-9][0-9]", Pattern.CASE_INSENSITIVE);

            for (Element element : elements) {
                Matcher matcher = pattern.matcher(element.text());
                boolean match = matcher.find();
                if (match) {
                    String officialPrice = matcher.group();
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

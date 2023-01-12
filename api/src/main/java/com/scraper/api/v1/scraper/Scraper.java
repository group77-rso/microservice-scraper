package com.scraper.api.v1.scraper;

import com.scraper.lib.Price;

import java.io.IOException;

abstract public class Scraper {

    public static float scrape(Price price, String requestId) throws IOException {
        float random = (float)  Math.random();
        return price.getPrice() * random;
    }

}

package com.scraper.api.v1.scraper;

import com.scraper.lib.Price;

abstract public class Scraper {

    public static float scrape(Price price) {
        float random = (float)  Math.random();
        return price.getPrice() * random;
    }

}

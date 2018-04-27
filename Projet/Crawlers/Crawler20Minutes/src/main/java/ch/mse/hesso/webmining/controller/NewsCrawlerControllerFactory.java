package ch.mse.hesso.webmining.controller;

import ch.mse.hesso.webmining.crawling.NewsCrawler;
import ch.mse.hesso.webmining.crawling.NewsParserFunction;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.WebCrawler;

public class NewsCrawlerControllerFactory implements CrawlController.WebCrawlerFactory {

    private NewsParserFunction newsParserFunction;
    private String newsDomain;

    public NewsCrawlerControllerFactory(NewsParserFunction newsParserFunction, String newsDomain) {
        this.newsParserFunction = newsParserFunction;
        this.newsDomain = newsDomain;
    }


    public WebCrawler newInstance() {
        return new NewsCrawler(this.newsParserFunction, this.newsDomain);
    }
}
package ch.mse.hesso.webmining.controller;

import ch.mse.hesso.webmining.crawlers.LocalChParserFunction;
import ch.mse.hesso.webmining.crawlers.LocalChWebCrawler;
import ch.mse.hesso.webmining.solrbridge.Indexer;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.WebCrawler;

public class LocalChCrawlerControllerFactory implements CrawlController.WebCrawlerFactory {

    private Indexer indexer;
    private LocalChParserFunction localChParserFunction;

    public LocalChCrawlerControllerFactory(Indexer indexer, LocalChParserFunction localChParserFunction){
        this.indexer = indexer;
        this.localChParserFunction = localChParserFunction;
    }


    public WebCrawler newInstance() {
        return new LocalChWebCrawler(this.indexer, this.localChParserFunction);
    }
}

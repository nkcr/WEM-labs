package ch.mse.hesso.webmining.crawlers;

import ch.mse.hesso.webmining.solrbridge.Indexer;
import edu.uci.ics.crawler4j.crawler.Page;

@FunctionalInterface
public interface LocalChParserFunction {
    void parse(Page page, Indexer indexer);
}

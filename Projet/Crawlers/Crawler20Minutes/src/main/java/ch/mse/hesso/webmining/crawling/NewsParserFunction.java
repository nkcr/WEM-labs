package ch.mse.hesso.webmining.crawling;

import edu.uci.ics.crawler4j.crawler.Page;

@FunctionalInterface
public interface NewsParserFunction {
    void parse(Page page);
}

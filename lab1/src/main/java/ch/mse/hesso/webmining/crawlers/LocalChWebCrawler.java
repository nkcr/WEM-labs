package ch.mse.hesso.webmining.crawlers;

import ch.mse.hesso.webmining.solrbridge.Indexer;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.url.WebURL;
import java.util.regex.Pattern;

public class LocalChWebCrawler extends WebCrawler {

    private Indexer indexer;
    private LocalChParserFunction localChParserFunction;

    public LocalChWebCrawler(Indexer indexer, LocalChParserFunction localChParserFunction){
        super();
        this.indexer = indexer;
        this.localChParserFunction = localChParserFunction;
    }

    private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|gif|jpg"
            + "|png|mp3|mp4|zip|gz))$");

    private final static String DOMAIN = "https://tel.local.ch/fr/q/sommentier?page=";


    /**
     * This method receives two parameters. The first parameter is the page
     * in which we have discovered this new url and the second parameter is
     * the new url. You should implement this function to specify whether
     * the given url should be crawled or not (based on your crawling logic).
     * In this example, we are instructing the crawler to ignore urls that
     * have css, js, git, ... extensions and to only accept urls that start
     * with "http://www.ics.uci.edu/". In this case, we didn't need the
     * referringPage parameter to make the decision.
     */
    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        String href = url.getURL().toLowerCase();
        return !FILTERS.matcher(href).matches()
                && href.startsWith(DOMAIN);
    }

    /**
     * This function is called when a page is fetched and ready
     * to be processed by your program.
     */
    @Override
    public void visit(Page page) {
        this.localChParserFunction.parse(page,indexer);
    }
}

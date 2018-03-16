package ch.mse.hesso.webmining;

import ch.mse.hesso.webmining.controller.LocalChCrawlerControllerFactory;
import ch.mse.hesso.webmining.crawlers.LocalChParserFunction;
import ch.mse.hesso.webmining.solrbridge.Indexer;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.MapSolrParams;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class App {

    /*
    * Dummy parser which is only indexing raw HTML data
     */
    private static LocalChParserFunction ex1 = (page, indexer) -> {
        if (page.getParseData() instanceof HtmlParseData) {
            String url = page.getWebURL().getURL();
            HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
            String html = htmlParseData.getHtml();
            Document doc = Jsoup.parse(html);
            Element el = doc.select("div.result-list").first();
            SolrInputDocument solrInputDocument = new SolrInputDocument();
            solrInputDocument.addField("rawContent",el.html());
            solrInputDocument.addField("pageUrl",url);
            indexer.index(solrInputDocument);
        }
    };


    /*
        Parser which extracts name, address and phone from each local.ch page entries
     */
    private static LocalChParserFunction ex2 = (page, indexer) -> {
        if (page.getParseData() instanceof  HtmlParseData){
            String url = page.getWebURL().getURL();
            HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
            String html = htmlParseData.getHtml();
            Document doc = Jsoup.parse(html);

            List<SolrInputDocument> solrInputDocumentList = doc.select("div.container.clearfix").stream().map(element -> {

                String name = element.select("a.details-entry-title-link").first().text();
                String address = element.select("span.address").first().text();
                String phone = element.select("a.number").first().text();

                SolrInputDocument solrInputDocument = new SolrInputDocument();
                if (name != null)
                    solrInputDocument.addField("name", name);
                if (address != null)
                    solrInputDocument.addField("address", address);
                if (phone != null)
                    solrInputDocument.addField("phone", phone);

                solrInputDocument.addField("pageUrl", url);
                return solrInputDocument;

            }).collect(Collectors.toList());

            //index results
            indexer.index(solrInputDocumentList);
        }
    };


    public static void main(String[] args) {
        String crawlStorageFolder = "./crawl/root";
        int numberOfCrawlers = 3;

        CrawlConfig config = new CrawlConfig();
        config.setIncludeHttpsPages(true);
        config.setCrawlStorageFolder(crawlStorageFolder);

        //Instantiate solr client
        SolrClient solr = initSolorClient(CONNECTION.SORL_BASE_URL+CONNECTION.SORL_CORE);

        /*
         * Instantiate the controller for this crawl.
         */
        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        try {
            CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);
            /*
             * For each crawl, you need to add some seed urls. These are the first
             * URLs that are fetched and then the crawler starts following links
             * which are found in these pages
             */
            controller.addSeed("https://tel.local.ch/fr/q/sommentier?page=1&what=");

            /*
             * Start the crawl. This is a blocking operation, meaning that your code
             * will reach the line after this only when crawling is finished.
             */
            CrawlController.WebCrawlerFactory wbf = new LocalChCrawlerControllerFactory(new Indexer(solr), ex2);
            controller.start(wbf, numberOfCrawlers);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

    }


    private static SolrClient initSolorClient(String urlString) {
        return new HttpSolrClient.Builder(urlString).build();
    }

}

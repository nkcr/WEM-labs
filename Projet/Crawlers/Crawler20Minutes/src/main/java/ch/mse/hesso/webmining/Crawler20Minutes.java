package ch.mse.hesso.webmining;

import ch.mse.hesso.webmining.controller.NewsCrawlerControllerFactory;
import ch.mse.hesso.webmining.crawling.NewsItem;
import ch.mse.hesso.webmining.crawling.NewsParserFunction;
import ch.mse.hesso.webmining.data.MongoService;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Crawler20Minutes {

    private static String SEED = "http://www.20min.ch/ro/";
    private static String DOMAIN = "http://www.20min.ch/ro/";
    private static String SOURCE = "20 Minutes";

    private static MongoService mongoService = new MongoService();

    /**
     * extract the date that is present in a string (first one)
     * @param texteContainingDate that might contain the date
     * @return string date or null
     */
    private static String getDate(String texteContainingDate) {
        String match = null;
        Matcher m = Pattern.compile("(\\d{1,2}.\\d{1,2}.\\d{4}) \\d{2}:\\d{2}").matcher(texteContainingDate);
        if (m.find()) {
            match = m.group();
        }
        return match;
    }

    /**
     * parse the current page and retrieve the date, the title, the html content and the date of an article
     * Source: 20 Minutes (romandie)
     */
    private static NewsParserFunction parser20Minutes = (page) -> {
        if (page.getParseData() instanceof HtmlParseData) {
            String url = page.getWebURL().getURL();
            HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
            String html = htmlParseData.getHtml();
            Document doc = Jsoup.parse(html);
            System.out.println(url);

            try {
                Element content = doc.getElementById("story_content");

                // retrieve the date
                String newsDateStr = getDate(doc.select("div.published.clearfix > p > span").text());
                DateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm");
                Long newsDateLong = format.parse(newsDateStr).getTime();

                // retrieve the title
                String newsTitle = doc.select("div.story_titles > h1 > span").text();

                // retrieve the html
                String newsHTML = content.select("div.story_text").html();

                // create the MongoDB object that will be stored in the database
                DBObject news = NewsItem.builder()
                        .id(url)
                        .url(url)
                        .html(newsHTML)
                        .tags(new ArrayList<String>())
                        .title(newsTitle)
                        .source(SOURCE)
                        .articleDate(newsDateLong)
                        .crawlDate(System.currentTimeMillis())
                        .build().toDBObject();
                try {
                    mongoService.store(news);
                }catch (MongoException e) {
                    e.printStackTrace();
                    System.out.println(news.toString());
                }

            } catch (NullPointerException exp) {
                // page is not an article --> ignore it
                return;
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    };


    public static void main(String[] args) {
        String crawlStorageFolder = "./crawl/root";
        int numberOfCrawlers = 8;

        /*************************************************************/
        /*************** Set Config for crawler **********************/
        /*************************************************************/
        CrawlConfig config = new CrawlConfig();
        config.setIncludeHttpsPages(true);
        config.setCrawlStorageFolder(crawlStorageFolder);


        /*************************************************************/
        /********  Instantiate the controller for this crawl. ********/
        /*************************************************************/

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
            controller.addSeed(SEED);

            /*
             * Start the crawl. This is a blocking operation, meaning that your code
             * will reach the line after this only when crawling is finished.
             */
            CrawlController.WebCrawlerFactory wbf = new NewsCrawlerControllerFactory(parser20Minutes, DOMAIN);
            controller.start(wbf, numberOfCrawlers);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

    }
}

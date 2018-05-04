package ch.wem.scrapper;

import ch.wem.scrapper.data.MongoService;
import com.mongodb.DBObject;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

public class LeTempsCrawler extends WebCrawler {

    private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|gif|jpg"
            + "|png|mp3|mp4|zip|gz))$");

    private final static Pattern OKAY = Pattern.compile("^(https)://www.letemps.ch/(|suisse|economie|monde|opinions|culture|sciences|sport|societe|lifestyle)/.*$");

    private MongoService mongoService = new MongoService();

    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        String href = url.getURL().toLowerCase();
        //System.out.println("Should visit URL: " + href);

        return !FILTERS.matcher(href).matches()
                && OKAY.matcher(href).matches();
    }

    @Override
    public void visit(Page page) {
        String url = page.getWebURL().getURL();
        System.out.println("URL: " + url);

        if (page.getParseData() instanceof HtmlParseData) {
            HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
            String html = htmlParseData.getHtml();
            Document doc = Jsoup.parse(html);
            Element title = doc.selectFirst("#block-letemps-content > article > div.container > div > div.col-sm-9.col-md-6 > div.article-content.article-content-inset.gallery.main-content > h1 > span");
            Element article = doc.selectFirst("#block-letemps-content > article > div.container > div > div.col-sm-9.col-md-6 > div.article-content.article-content-inset.gallery.main-content > div.article_body");
            List<String> tags = doc.select("#block-letemps-content > article > div.container > div > div.col-sm-3.col-md-2 > section > p.tags").select("b").eachText();

            Element date = doc.select("meta[itemprop='datePublished']").first();
            Long dateUnix = date != null ? parseDate(date.attr("content")) : 0l;

            DBObject news = NewsItem.builder()
                    .title(title.text())
                    .crawlDate(dateUnix)
                    .articleDate(dateUnix)
                    .html(article.html())
                    .source("LE TEMPS")
                    .url(url)
                    .id(""+title.hashCode())
                    .tags(tags)
                    .build().toDBObject();

            try { mongoService.store(news); }
            catch (Exception e){
                e.printStackTrace();
                System.out.println(news.toString());
            }

        }
    }

    public LeTempsCrawler() {
    }

    private long parseDate(String str){
        if (str == null)
            return 0L;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        try {
            Date date = df.parse(str);
            return date.getTime();
        } catch (ParseException e) {
            return 0L;
        }
    }


}

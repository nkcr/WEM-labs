package ch.mse.hesso.webmining.crawling;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.util.List;


@Getter
@Setter
@Builder
public class NewsItem {
    String id;
    String url;
    String html;
    List<String> tags;
    String title;
    String source;
    long crawlDate;
    long articleDate;

    public DBObject toDBObject() {
        return BasicDBObjectBuilder.start("_id", id)
                .add("url", url)
                .add("html", html)
                .add("tags", tags)
                .add("source", source)
                .add("crawlDate", crawlDate)
                .add("articleDate", articleDate)
                .add("title", title)
                .get();
    }
}
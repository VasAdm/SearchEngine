package searchengine.services.parsing;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import searchengine.model.page.Page;
import searchengine.model.site.SiteEntity;

import java.io.IOException;
import java.util.Objects;

public class PageParser {
    private final String url;
    private final SiteEntity site;

    private final Logger logger = LoggerFactory.getLogger(PageParser.class);


    public PageParser(String url, SiteEntity site) {
        this.url = url;
        this.site = site;
    }

    public Page parsePage() {
        Page page = new Page();

        long delay = (long) (Math.random() * ((1000 - 100) + 1)) + 100;
        try {
            Thread.sleep(delay);

            Connection.Response response = Jsoup.connect(url)
                    .maxBodySize(0)
                    .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:101.0) Gecko/20100101 Firefox/101.0")
                    .referrer("https://www.google.com")
//                    .timeout(5000)
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true)
                    .followRedirects(true)
                    .execute();
            String contentType = response.contentType();

            assert contentType != null;
            if (contentType.contains("text/")) {
                int statusCode = response.statusCode();

                Document document = response.parse();

                page.setContent(document);
                String path = url.substring(site.getUrl().length());
                page.setPath(Objects.equals(path, "") ? "/" : path);
                page.setCode(statusCode);
                page.setSite(site);
            }

        } catch (InterruptedException | IOException ex) {
            logger.warn(ex.getLocalizedMessage());
        }

        return page;
    }
}

package searchengine.services.parsing;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;

import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Slf4j
public class HtmlParser {
    private final String url;
    private final SiteEntity site;
    private Document document;

    public HtmlParser(String url, SiteEntity site) {
        this.url = url;
        this.site = site;
    }

    public PageEntity getPage() {
        long delay = (long) (Math.random() * ((2000 - 500) + 1)) + 500;
        try {
            Thread.sleep(delay);

            Connection.Response response = Jsoup.connect(url)
                    .maxBodySize(0)
                    .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:101.0) Gecko/20100101 Firefox/101.0")
                    .referrer("https://www.google.com")
                    .timeout(11000)
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true)
                    .followRedirects(true)
                    .execute();
            String contentType = response.contentType();

            assert contentType != null;
            if (!contentType.contains("text/")) {
                return null;
            }
            PageEntity pageEntity = new PageEntity();
            int statusCode = response.statusCode();
            document = response.parse();

            pageEntity.setContent(document.toString());
            String path = url.substring(site.getUrl().length());
            pageEntity.setPath(Objects.equals(path, "") ? "/" : path);
            pageEntity.setCode(statusCode);
            pageEntity.setSite(site);

            return pageEntity;
        } catch (InterruptedException | IOException ex) {
            Thread.currentThread().interrupt();
        }
        return null;
    }

    public Set<String> getPaths() {
        if (document == null) {
            return null;
        }
        Set<String> result = new HashSet<>();
        Elements elements = document.select("a[href^=/]");

        for (Element el : elements) {
            String absPath = el.attr("abs:href");
            String relPath = el.attr("href");

            boolean isContainRoot = absPath.contains(site.getUrl());
            boolean isAnchor = relPath.contains("#");
            boolean isFit = isContainRoot && !isAnchor;

            if (isFit) {
                result.add(absPath);
            }
        }

        return result;
    }
}
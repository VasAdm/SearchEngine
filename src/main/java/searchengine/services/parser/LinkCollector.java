package searchengine.services.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import searchengine.model.page.PageEntity;
import searchengine.model.site.SiteEntity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LinkCollector {
    public static Map<String, SiteEntity> collectLinks(PageEntity page, Document document) {
        Map<String, SiteEntity> result = new HashMap<>();

        if (page.getContent() != null) {
            Elements elements = document.select("a[href^=/]");

            for (Element el : elements) {
                String absPath = el.attr("abs:href");
                String relPath = el.attr("href");

                boolean isFit = absPath.contains(page.getSite().getUrl()) &&
                        LinkRepo.addLink(absPath) &&
                        !relPath.contains("#");

                if (isFit) {
                    result.put(relPath, page.getSite());
                }
            }
        }
        return result;
    }
}

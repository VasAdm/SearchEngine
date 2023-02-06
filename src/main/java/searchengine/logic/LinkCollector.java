package searchengine.logic;

import lombok.RequiredArgsConstructor;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import searchengine.model.page.PageEntity;
import searchengine.model.page.PageEntityWithDoc;
import searchengine.model.site.SiteEntity;
import searchengine.services.LinkHolder;
import searchengine.services.RepoHolder;
import searchengine.services.page.PageService;
import searchengine.services.site.SiteService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class LinkCollector {
    private final SiteService siteService;
    private final PageService pageService;

    public LinkCollector() {
        siteService = RepoHolder.getSiteService();
        pageService = RepoHolder.getPageService();
    }

    public Map<PageEntity, SiteEntity> collectLinks(PageEntityWithDoc pageEntityWithDoc) {
        Map<PageEntity, SiteEntity> result = Collections.synchronizedMap(new HashMap<>());
        PageEntity page = pageEntityWithDoc.getPage();
        Document document = pageEntityWithDoc.getDocument();

        if (document != null) {
            Elements elements = document.select("a[href^=/]");

            for (Element el : elements) {
                String absPath = el.attr("abs:href");
                String relPath = el.attr("href");


                boolean isContainRoot = absPath.contains(page.getSite().getUrl());
                boolean isAlreadyAdded = LinkHolder.addLink(absPath);
                boolean isAnchor = relPath.contains("#");

                boolean isFit =
                        isContainRoot &&
                                !isAlreadyAdded &&
                                !isAnchor;

                if (isFit) {
                    PageEntityWithDoc pageWithDoc = PageParser.parsePage(absPath, page.getSite());
                    if (pageWithDoc.getPage() != null) result.put(pageWithDoc.getPage(), page.getSite());
                }
            }
        }

        pageService.saveAll(result.keySet().stream().toList());
        SiteEntity site = page.getSite();
        site.setStatusTime(LocalDateTime.now());
        siteService.save(site);

        return result;
    }
}

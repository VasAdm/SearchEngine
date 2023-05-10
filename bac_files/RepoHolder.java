package searchengine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

@Component
public class RepoHolder {

    private static PageRepository pageService;
    private static SiteRepository siteService;

    @Autowired
    public RepoHolder(SiteService siteService, PageService pageService) {
        RepoHolder.siteService = siteService;
        RepoHolder.pageService = pageService;
    }

    public static PageService getPageService() {
        return pageService;
    }

    public static SiteService getSiteService() {
        return siteService;
    }
}

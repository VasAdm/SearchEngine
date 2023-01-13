package searchengine.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import searchengine.services.page.PageService;
import searchengine.services.site.SiteService;

@Component
public class RepoHolder {

    private static PageService pageService;
    private static SiteService siteService;

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

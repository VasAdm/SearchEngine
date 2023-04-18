package searchengine.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import searchengine.repository.RedisRepository;
import searchengine.repository.PageService;
import searchengine.repository.SiteService;

@Component
public class RepoHolder {

    private static PageService pageService;
    private static SiteService siteService;
    private static RedisRepository redisRepository;

    @Autowired
    public RepoHolder(SiteService siteService, PageService pageService, RedisRepository redisRepository) {
        RepoHolder.siteService = siteService;
        RepoHolder.pageService = pageService;
        RepoHolder.redisRepository = redisRepository;
    }

    public static PageService getPageService() {
        return pageService;
    }

    public static SiteService getSiteService() {
        return siteService;
    }

    public static RedisRepository getRedisRepository() {
        return redisRepository;
    }
}

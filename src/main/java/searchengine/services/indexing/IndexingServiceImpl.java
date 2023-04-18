package searchengine.services.indexing;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import searchengine.model.page.PageEntity;
import searchengine.model.site.Site;
import searchengine.model.site.SitesList;
import searchengine.dto.indexing.IndexingStatusResponse;
import searchengine.dto.indexing.IndexingStatusResponseError;
import searchengine.services.parsing.PageParser;
import searchengine.services.parsing.TaskRunner;
import searchengine.model.site.SiteEntity;
import searchengine.repository.RedisService;
import searchengine.model.site.StatusType;
import searchengine.repository.PageService;
import searchengine.repository.SiteService;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {
    private final SitesList sites;
    private final SiteService siteService;
    private final PageService pageService;
    private final RedisService redisService;

    @Override
    public IndexingStatusResponse startIndexing() {
        Set<SiteEntity> siteEntities = new HashSet<>();

        sites.getSites().forEach(s -> {
            SiteEntity site = siteService.getSiteByUrl(s.getUrl());
            if (site != null) siteEntities.add(site);
        });

        if (siteEntities.stream().map(SiteEntity::getStatus).anyMatch(Predicate.isEqual(StatusType.INDEXING))) {
            return new IndexingStatusResponseError(false, "Индексаци уже запущена");
        } else {

            siteService.deleteAll();
            pageService.deleteAll();
            redisService.deleteAll(sites.getSites().stream().map(Site::getUrl).toList());
            siteEntities.clear();

            sites.getSites().forEach(site -> {
                SiteEntity siteEntity = new SiteEntity();
                siteEntity.setName(site.getName());
                siteEntity.setUrl(site.getUrl());
                siteEntity.setStatus(StatusType.INDEXING);
                siteEntity.setStatusTime(LocalDateTime.now());

                siteEntities.add(siteService.save(siteEntity));
            });

            TaskRunner taskRunner = new TaskRunner(siteEntities);
            Thread indexingThread = new Thread(taskRunner);
            IndexingThreadHolder.setThread(taskRunner);
            IndexingThreadHolder.setSiteSet(siteEntities);
            indexingThread.start();

            return new IndexingStatusResponse(true);
        }
    }

    @Override
    public IndexingStatusResponse stopIndexing() {
        TaskRunner taskRunner = IndexingThreadHolder.getTaskRunner();
        if (!taskRunner.isAlive()) {
            return new IndexingStatusResponseError(false, "Индексаци не запущена");
        } else {
            Set<SiteEntity> siteEntities = IndexingThreadHolder.getSiteSet();
            taskRunner.stop();
            siteEntities.stream()
                    .filter(siteEntity -> siteEntity.getStatus().equals(StatusType.INDEXING))
                    .forEach(siteEntity -> {
                        siteEntity.setStatusTime(LocalDateTime.now());
                        siteEntity.setStatus(StatusType.FAILED);
                        siteEntity.setLastError("Индексация остановлена пользователем");
                        siteService.save(siteEntity);
                    });
            return new IndexingStatusResponse(true);
        }
    }

    @Override
    public IndexingStatusResponse indexPage(String url) {
        String regex = "^https?:\\/\\/[a-zA-Zа-яА-Я\\._-]*\\.[\\w]{2,3}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);
        String subString = "";
        while (matcher.find()) {
            subString = url.substring(matcher.start(), matcher.end());
        }

        if (subString.isEmpty()) {
            return new IndexingStatusResponseError(false, "Переданная строка не является ссылкой");
        }
        SiteEntity site = siteService.getSiteByUrl(subString);
        if (site == null) {
            return new IndexingStatusResponseError(false, "Данная страница находится за пределами сайтов, указанных в конфигурационном файле");
        }
        PageEntity page = pageService.getPageByPathAndSite(url.substring(subString.length(),url.length()), site);
        PageEntity newPage = new PageParser(url, site).parsePage().getPageEntity();
        if (page == null) {
            page = newPage;
        } else {
            page.setContent(newPage.getContent());
            page.setCode(newPage.getCode());
            page.setSite(newPage.getSite());
            page.setPath(newPage.getPath());
            page.setIndex(newPage.getIndex());
        }
        pageService.save(page);
        return new IndexingStatusResponse(true);
    }
}

package searchengine.services.indexing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import searchengine.dto.indexing.IndexingStatusResponse;
import searchengine.dto.indexing.IndexingStatusResponseError;
import searchengine.model.page.PageEntity;
import searchengine.model.site.SiteEntity;
import searchengine.model.site.SitesList;
import searchengine.model.site.StatusType;
import searchengine.repository.PageService;
import searchengine.repository.SiteService;
import searchengine.services.parsing.PageParser;
import searchengine.services.parsing.TaskRunner;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class IndexingServiceImpl implements IndexingService {
    private final SitesList sites;
    private final SiteService siteService;
    private final PageService pageService;

    private final int coreCount = Runtime.getRuntime().availableProcessors();
    private final ExecutorService executorService = Executors.newWorkStealingPool(coreCount);

    private final List<TaskRunner> taskList = new ArrayList<>();
    Logger logger = LoggerFactory.getLogger(IndexingServiceImpl.class);


    @Autowired
    public IndexingServiceImpl(SitesList sites, SiteService siteService, PageService pageService) {
        this.sites = sites;
        this.siteService = siteService;
        this.pageService = pageService;
    }

    @Override
    public ResponseEntity<IndexingStatusResponse> startIndexing() {
        Set<SiteEntity> siteEntities = new HashSet<>();

        sites.getSites().forEach(s -> {
            SiteEntity site = siteService.getSiteByUrl(s.getUrl());
            if (site != null) siteEntities.add(site);
        });

        if (siteEntities.stream().map(SiteEntity::getStatus).anyMatch(Predicate.isEqual(StatusType.INDEXING))) {
            return ResponseEntity.badRequest().body(new IndexingStatusResponseError(false, "Индексаци уже запущена"));
        } else {

            siteService.deleteAll();
            siteEntities.clear();

            sites.getSites().forEach(site -> {
                SiteEntity siteEntity = new SiteEntity();
                siteEntity.setName(site.getName());
                siteEntity.setUrl(site.getUrl());
                siteEntity.setStatus(StatusType.INDEXING);
                siteEntity.setStatusTime(LocalDateTime.now());

                TaskRunner task = new TaskRunner(siteService.save(siteEntity));
                executorService.execute(task);
                taskList.add(task);

                logger.info("Parsing site - " + siteEntity.getName() + ": started");
            });

            executorService.shutdown();

            return ResponseEntity.ok(new IndexingStatusResponse(true));
        }
    }

    @Override
    public ResponseEntity<IndexingStatusResponse> stopIndexing() {
        if (taskList.isEmpty()) {
            return ResponseEntity.badRequest().body(new IndexingStatusResponseError(false, "Индексаци не запущена"));
        } else {
            Set<SiteEntity> siteEntities = taskList.stream().map(TaskRunner::getSiteEntity).collect(Collectors.toSet());
            taskList.forEach(taskRunner -> taskRunner.getTask().shutdownNow());
            taskList.removeIf(taskRunner ->
                    taskRunner.getTask().isQuiescent() ||
                    taskRunner.getTask().isTerminated() ||
                    taskRunner.getTask().isShutdown());

            siteEntities.stream()
                    .filter(siteEntity -> siteEntity.getStatus().equals(StatusType.INDEXING))
                    .forEach(siteEntity -> {
                        siteEntity.setStatusTime(LocalDateTime.now());
                        siteEntity.setStatus(StatusType.FAILED);
                        siteEntity.setLastError("Индексация остановлена пользователем");
                        siteService.save(siteEntity);
                        logger.info("Parsing site - " + siteEntity.getName() + ": stopped");
                    });
            return ResponseEntity.ok(new IndexingStatusResponse(true));
        }
    }

    @Override
    public ResponseEntity<IndexingStatusResponse> indexPage(String url) {
        String regex = "^https?://[a-zA-Zа-яА-Я._-]*.\\w{2,3}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);
        String subString = "";
        while (matcher.find()) {
            subString = url.substring(matcher.start(), matcher.end());
        }

        if (subString.isEmpty()) {
            return ResponseEntity.badRequest().body(new IndexingStatusResponseError(false, "Переданная строка не является ссылкой"));
        }
        SiteEntity site = siteService.getSiteByUrl(subString);
        if (site == null) {
            return ResponseEntity.badRequest().body(new IndexingStatusResponseError(false, "Данная страница находится за пределами сайтов, указанных в конфигурационном файле"));
        }
        PageEntity page = pageService.getPageByPathAndSite(url.substring(subString.length()), site);
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
        logger.info("Parsing page - " + page.getSite().getUrl() + page.getPath() + ": completed");
        return ResponseEntity.ok(new IndexingStatusResponse(true));
    }
}

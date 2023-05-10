package searchengine.services.indexing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import searchengine.config.SitesList;
import searchengine.dto.indexing.IndexingStatusResponse;
import searchengine.dto.indexing.IndexingStatusResponseError;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.model.StatusType;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;
import searchengine.services.parsing.HtmlParser;
import searchengine.services.parsing.TaskRunner;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class IndexingServiceImpl implements IndexingService {
    private final SitesList sites;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final int coreCount = Runtime.getRuntime().availableProcessors();
    private final ExecutorService executorService = Executors.newWorkStealingPool(coreCount);
    private final List<TaskRunner> taskList = new ArrayList<>();


    @Autowired
    public IndexingServiceImpl(SitesList sites, SiteRepository siteRepository, PageRepository pageRepository) {
        this.sites = sites;
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
    }

    @Override
    public ResponseEntity<IndexingStatusResponse> startIndexing() {
        Set<SiteEntity> siteEntities = new HashSet<>();

        sites.getSites().forEach(s -> {
            SiteEntity site = siteRepository.getByUrl(s.getUrl());
            if (site != null) siteEntities.add(site);
        });

        if (siteEntities.stream().map(SiteEntity::getStatus).anyMatch(Predicate.isEqual(StatusType.INDEXING))) {
            return ResponseEntity.badRequest().body(new IndexingStatusResponseError(false, "Индексация уже запущена"));
        } else {

            siteRepository.deleteAll();
            siteEntities.clear();

            sites.getSites().forEach(site -> {
                SiteEntity siteEntity = new SiteEntity();
                siteEntity.setName(site.getName());
                siteEntity.setUrl(site.getUrl());
                siteEntity.setStatus(StatusType.INDEXING);
                siteEntity.setStatusTime(LocalDateTime.now());

                TaskRunner task = new TaskRunner(siteRepository.save(siteEntity), siteRepository, pageRepository);
                executorService.submit(task);
                taskList.add(task);

                log.info("Parsing site - " + siteEntity.getName() + ": started");
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
                        siteRepository.save(siteEntity);
                        log.info("Parsing site - " + siteEntity.getName() + ": stopped");
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
        SiteEntity site = siteRepository.getByUrl(subString);
        if (site == null) {
            return ResponseEntity.badRequest().body(new IndexingStatusResponseError(false, "Данная страница находится за пределами сайтов, указанных в конфигурационном файле"));
        }
        Optional<PageEntity> optionalPageEntity = pageRepository.findAllByPathAndSite_Id(url.substring(subString.length()), site.getId());
        PageEntity page = new HtmlParser(url, site).getPage();
        optionalPageEntity.ifPresent(pageEntity -> page.setId(pageEntity.getId()));

        pageRepository.save(page);

        log.info("Parsing page - " + page.getSite().getUrl() + page.getPath() + ": completed");
        return ResponseEntity.ok(new IndexingStatusResponse(true));
    }
}

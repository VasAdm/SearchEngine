package searchengine.services.indexing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.indexing.IndexingStatusResponse;
import searchengine.dto.indexing.IndexingStatusResponseError;
import searchengine.model.*;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;
import searchengine.services.lemmasIndexesScraper.LemmasIndexesCollector;
import searchengine.services.parsing.HtmlParser;
import searchengine.services.parsing.TaskRunner;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class IndexingServiceImpl implements IndexingService {
    private final SitesList sites;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final Map<SiteEntity, RunnableFuture<Integer>> taskList = Collections.synchronizedMap(new HashMap<>());
    private Thread secondaryThread = null;

    @Autowired
    public IndexingServiceImpl(SitesList sites, SiteRepository siteRepository, PageRepository pageRepository,
                               LemmaRepository lemmaRepository, IndexRepository indexRepository) {
        this.sites = sites;
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
        this.lemmaRepository = lemmaRepository;
        this.indexRepository = indexRepository;
    }

    @Override
    public ResponseEntity<IndexingStatusResponse> startIndexing() {
        Set<SiteEntity> siteEntities = new HashSet<>();
        int coreCount = (Runtime.getRuntime().availableProcessors() - 1) / sites.getSites().size();
        ExecutorService executorService = Executors.newWorkStealingPool(coreCount);

        sites.getSites().forEach(s -> {
            SiteEntity site = siteRepository.getByUrl(s.getUrl());
            if (site != null) siteEntities.add(site);
        });

        if (siteEntities.stream().map(SiteEntity::getStatus).anyMatch(Predicate.isEqual(StatusType.INDEXING)) || secondaryThread != null) {
            return ResponseEntity.badRequest()
                    .body(new IndexingStatusResponseError(false, "Индексация уже запущена"));
        } else {
            secondaryThread = new Thread(() -> {
                LocalDateTime start = LocalDateTime.now();
//                clearTables();
                siteRepository.deleteAll(siteEntities);
                System.out.println(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) - start.toEpochSecond(ZoneOffset.UTC));

                sites.getSites().forEach(site -> {
                    SiteEntity siteEntity = createSite(site);
                    RunnableFuture<Integer> task = new FutureTask<>(new TaskRunner(siteEntity, siteRepository,
                            pageRepository, lemmaRepository, indexRepository), siteEntity.getId());
                    taskList.put(siteEntity, task);
                });

                taskList.values().forEach(executorService::execute);

                ResultChecker resultChecker = new ResultChecker(taskList, siteRepository);

                executorService.execute(resultChecker);

                executorService.shutdown();
            });
            secondaryThread.start();

            return ResponseEntity.ok(new IndexingStatusResponse(true));
        }
    }

    @Override
    public ResponseEntity<IndexingStatusResponse> stopIndexing() {
        if (taskList.isEmpty()) {
            return ResponseEntity.badRequest().body(new IndexingStatusResponseError(false, "Индексаци не запущена"));
        } else {
            taskList.values().forEach(task -> task.cancel(true));

            return ResponseEntity.ok(new IndexingStatusResponse(true));
        }
    }

    @Override
    public ResponseEntity<IndexingStatusResponse> indexPage(String url) {
        String siteUrl = getSiteUrl(url);

        if (siteUrl == null) {
            return ResponseEntity.badRequest().body(new IndexingStatusResponseError(false, "Переданная строка не является ссылкой"));
        }
        SiteEntity siteEntity = getSite(siteUrl);

        if (siteEntity == null) {
            return ResponseEntity.badRequest().body(new IndexingStatusResponseError(false, "Данная страница находится за пределами сайтов, указанных в конфигурационном файле"));
        }
        Optional<PageEntity> optionalPageEntity = pageRepository.findAllByPathAndSite_Id(url.substring(siteUrl.length()), siteEntity.getId());
        PageEntity page = new HtmlParser(url, siteEntity).getPage();
        PageEntity finalPage = page;
        optionalPageEntity.ifPresent(pageEntity -> finalPage.setId(pageEntity.getId()));

        if (finalPage.getId() != 0) {

            List<IndexEntity> indexList = indexRepository.getByPage(finalPage);
            List<String> lemmaList = indexList.stream()
                    .map(IndexEntity::getLemma)
                    .map(LemmaEntity::getLemma)
                    .toList();
            lemmaRepository.updateFrequencyByLemmaIn(lemmaList);
            indexRepository.deleteAll(indexList);
        }
        page = pageRepository.save(finalPage);
        LemmasIndexesCollector collector = new LemmasIndexesCollector(siteEntity, finalPage, lemmaRepository, indexRepository);
        collector.collect();
        siteEntity.setStatus(StatusType.INDEXED);
        siteRepository.save(siteEntity);

        log.info("Parsing page - " + page.getSite().getUrl() + page.getPath() + ": completed");
        return ResponseEntity.ok(new IndexingStatusResponse(true));
    }

    private SiteEntity getSite(String url) {
        SiteEntity resultSite = siteRepository.getByUrl(url);
        if (resultSite == null) {
            for (Site s : sites.getSites()) {
                if (s.getUrl().equals(url)) {
                    resultSite = createSite(s);
                }
            }
        }
        return resultSite;
    }

    private SiteEntity createSite(Site site) {
        SiteEntity siteEntity = new SiteEntity();
        siteEntity.setName(site.getName());
        siteEntity.setUrl(site.getUrl());
        siteEntity.setStatus(StatusType.INDEXING);
        siteEntity.setStatusTime(LocalDateTime.now());
        siteEntity = siteRepository.save(siteEntity);
        return siteEntity;
    }

    private String getSiteUrl(String url) {
        String regex = "^https?://[a-zA-Zа-яА-Я._-]*\\.\\w{2,3}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);
        String result = null;
        while (matcher.find()) {
            result = url.substring(matcher.start(), matcher.end());
        }
        return result;
    }

    private void clearTables() {
        indexRepository.deleteAllInBatch();
        lemmaRepository.deleteAllInBatch();
        pageRepository.deleteAllInBatch();
        siteRepository.deleteAllInBatch();
    }
}

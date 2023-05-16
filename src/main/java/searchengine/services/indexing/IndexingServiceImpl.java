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
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;
import searchengine.services.parsing.HtmlParser;
import searchengine.services.parsing.TaskRunner;

import java.time.LocalDateTime;
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
    private final Map<SiteEntity, RunnableFuture<Integer>> taskList = Collections.synchronizedMap(new HashMap<>());


    @Autowired
    public IndexingServiceImpl(SitesList sites, SiteRepository siteRepository, PageRepository pageRepository, LemmaRepository lemmaRepository) {
        this.sites = sites;
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
        this.lemmaRepository = lemmaRepository;
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

        if (siteEntities.stream().map(SiteEntity::getStatus).anyMatch(Predicate.isEqual(StatusType.INDEXING))) {
            return ResponseEntity.badRequest().body(new IndexingStatusResponseError(false, "Индексация уже запущена"));
        } else {

            siteRepository.deleteAll(siteEntities);

            sites.getSites().forEach(site -> {
                SiteEntity siteEntity = new SiteEntity();
                siteEntity.setName(site.getName());
                siteEntity.setUrl(site.getUrl());
                siteEntity.setStatus(StatusType.INDEXING);
                siteEntity.setStatusTime(LocalDateTime.now());
                siteEntity = siteRepository.save(siteEntity);

                RunnableFuture<Integer> task = new FutureTask<>(new TaskRunner(siteEntity, siteRepository, pageRepository, lemmaRepository, coreCount), siteEntity.getId());
                taskList.put(siteEntity, task);
            });

            taskList.values().forEach(executorService::execute);

            ResultChecker resultChecker = new ResultChecker(taskList, siteRepository);

            executorService.execute(resultChecker);

            executorService.shutdown();

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

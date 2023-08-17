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
import searchengine.services.parsing.WebParser;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
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
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final Map<SiteEntity, WebParser> parserMap = Collections.synchronizedMap(new HashMap<>());

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

        Set<SiteEntity> siteEntities = sites.getSites()
                .stream()
                .map(s -> siteRepository.getByUrl(s.getUrl()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (siteEntities.stream().map(SiteEntity::getStatus).anyMatch(Predicate.isEqual(StatusType.INDEXING))) {
            return ResponseEntity.badRequest()
                    .body(new IndexingStatusResponseError(false, "Индексация уже запущена"));
        } else {
            Thread secondaryThread = new Thread(() -> {

                siteRepository.deleteAllInBatch(siteEntities);
                sites.getSites().forEach(site -> {
                    SiteEntity siteEntity = createSite(site);
                    WebParser.clearPageSet();
                    WebParser siteParser = new WebParser(siteEntity, siteEntity.getUrl(), siteRepository,
                            pageRepository, lemmaRepository, indexRepository, true);
                    parserMap.put(siteEntity, siteParser);
                });

                try (ForkJoinPool task = new ForkJoinPool()) {
                    parserMap.values().forEach(task::execute);

                    while (!parserMap.isEmpty()) {
                        Iterator<Map.Entry<SiteEntity, WebParser>> iterator = parserMap.entrySet().iterator();
                        while (iterator.hasNext()) {
                            Map.Entry<SiteEntity, WebParser> entry = iterator.next();

                            if (entry.getValue().state().equals(Future.State.CANCELLED)) {
                                entry.getKey().setStatus(StatusType.FAILED);
                                entry.getKey().setLastError("Индексация остановлена пользователем");
                                log.info("Парсинг сайта: " + entry.getKey().getName() + " отменён пользователем");
                                entry.getKey().setStatusTime(LocalDateTime.now());
                                siteRepository.saveAndFlush(entry.getKey());
                                parserMap.remove(entry.getKey());
                                iterator.remove();
                            } else if (entry.getValue().state().equals(Future.State.SUCCESS)) {
                                log.info("Парсинг сайта: " + entry.getKey().getName() + " завершён.");
                                entry.getKey().setStatus(StatusType.INDEXED);
                                entry.getKey().setStatusTime(LocalDateTime.now());
                                siteRepository.saveAndFlush(entry.getKey());
                                parserMap.remove(entry.getKey());
                                iterator.remove();
                            }
                        }
                    }
                }
            });
            secondaryThread.start();

            return ResponseEntity.ok(new IndexingStatusResponse(true));
        }
    }

    @Override
    public ResponseEntity<IndexingStatusResponse> stopIndexing() {
        if (parserMap.isEmpty()) {
            return ResponseEntity.badRequest().body(new IndexingStatusResponseError(false, "Индексаци не запущена"));
        } else {
            parserMap.values().forEach(parser -> System.out.println(parser.cancel(false)));

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
}

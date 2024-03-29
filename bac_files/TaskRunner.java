package searchengine.services.parsing;

import lombok.extern.slf4j.Slf4j;
import searchengine.model.SiteEntity;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;

@Slf4j
public class TaskRunner implements Runnable {
    private final Set<String> pageSet = Collections.synchronizedSet(new HashSet<>());
    private final SiteEntity siteEntity;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;

    public TaskRunner(SiteEntity siteEntity, SiteRepository siteRepository, PageRepository pageRepository, LemmaRepository lemmaRepository, IndexRepository indexRepository) {
        this.siteEntity = siteEntity;
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
        this.lemmaRepository = lemmaRepository;
        this.indexRepository = indexRepository;
    }

    @Override
    public void run() {
            WebParser webParser = new WebParser(siteEntity, "/", siteRepository, pageRepository, lemmaRepository, indexRepository, pageSet, true);
            log.info("Запущен парсинг сайта: " + siteEntity.getName());
            task.execute(webParser);
    }
}
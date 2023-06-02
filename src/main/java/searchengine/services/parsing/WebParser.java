package searchengine.services.parsing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.model.StatusType;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;
import searchengine.services.lemmasIndexesScraper.LemmasIndexesCollector;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class WebParser extends RecursiveAction {
    private final SiteEntity siteEntity;
    private final String path;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final Set<String> pageSet;
    private final boolean root;

//    public WebParser(SiteEntity siteEntity, String path, SiteRepository siteRepository, PageRepository pageRepository, LemmaRepository lemmaRepository, IndexRepository indexRepository, Set<String> pageSet, boolean root) {
//        this.siteEntity = siteEntity;
//        this.path = path;
//        this.siteRepository = siteRepository;
//        this.pageRepository = pageRepository;
//        this.lemmaRepository = lemmaRepository;
//        this.indexRepository = indexRepository;
//        this.pageSet = pageSet;
//        this.root = root;
//    }

    @Override
    protected void compute() {
        String url = isRoot() ? siteEntity.getUrl() + "/" : path;
        HtmlParser htmlParser = new HtmlParser(url, siteEntity);
        if (isNotFailed() && isNotVisited(url)) {
            PageEntity pageEntity = htmlParser.getPage();

            if (pageEntity != null) {
                savePage(pageEntity);
                log.info("Парсинг страницы: " + siteEntity.getUrl() + pageEntity.getPath());

                LemmasIndexesCollector collector = new LemmasIndexesCollector(siteEntity, pageEntity, lemmaRepository, indexRepository);
                collector.collect();

                updateStatusTime();
            }

            Set<ForkJoinTask<Void>> tasks = htmlParser.getPaths().stream()
                    .map(childPath -> new WebParser(siteEntity, childPath, siteRepository, pageRepository,
                            lemmaRepository, indexRepository, pageSet, false).fork())
                    .collect(Collectors.toSet());
            tasks.forEach(ForkJoinTask::join);
        }
    }

    protected boolean isNotFailed() {
        return !siteEntity.getStatus().equals(StatusType.FAILED);
    }

    protected boolean isNotVisited(String url) {
        return pageSet.add(url);
    }

    protected void updateStatusTime() {
        siteRepository.updateTime(LocalDateTime.now(), siteEntity.getId());
    }

    protected boolean isRoot() {
        return root;
    }

    protected void savePage(PageEntity page) {
        pageRepository.save(page);
    }
}
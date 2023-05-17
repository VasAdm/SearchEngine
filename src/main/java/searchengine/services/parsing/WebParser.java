package searchengine.services.parsing;

import lombok.extern.slf4j.Slf4j;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.model.StatusType;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;
import searchengine.services.lemmasScraper.LemmasScraper;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.stream.Collectors;

@Slf4j
public class WebParser extends RecursiveAction {
    private final SiteEntity siteEntity;
    private final String path;
    private final Set<String> pageSet;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final boolean root;
    private String url;
    private HtmlParser htmlParser;
    private PageEntity pageEntity = null;

    public WebParser(SiteEntity siteEntity, String path, SiteRepository siteRepository, PageRepository pageRepository, LemmaRepository lemmaRepository, Set<String> pageSet, boolean root) {
        this.siteEntity = siteEntity;
        this.path = path;
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
        this.lemmaRepository = lemmaRepository;
        this.pageSet = pageSet;
        this.root = root;
    }

    @Override
    protected void compute() {
        url = isRoot() ? siteEntity.getUrl() + "/" : path;
        htmlParser = new HtmlParser(url, siteEntity);
        if (isNotFailed() && isNotVisited()) {
            pageEntity = savePage();
            boolean saved = pageEntity != null;
            LemmasScraper lemmasScraper;
            try {
                lemmasScraper = LemmasScraper.getInstance();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Set<String> lemmasSet = lemmasScraper.getLemmaSet(pageEntity.getContent());
            saveLemmas(lemmasSet);

            updateStatusTime();
            if (saved) {
                Set<ForkJoinTask<Void>> tasks = htmlParser.getPaths().stream()
                        .map(childPath -> new WebParser(
                                siteEntity,
                                childPath,
                                siteRepository,
                                pageRepository,
                                lemmaRepository,
                                pageSet,
                                false)
                                .fork())
                        .collect(Collectors.toSet());
                tasks.forEach(ForkJoinTask::join);
            }
        }
    }

    protected boolean isNotFailed() {
        return !siteEntity.getStatus().equals(StatusType.FAILED);
    }

    protected boolean isNotVisited() {
        return pageSet.add(url);
    }

    protected void updateStatusTime() {
        siteRepository.updateTime(LocalDateTime.now(), siteEntity.getId());
    }

    protected boolean isRoot() {
        return root;
    }

    protected PageEntity savePage() {
        return pageRepository.save(htmlParser.getPage());
    }

    protected void saveLemmas(Set<String> lemmas) {
        lemmas.forEach(s -> {
            LemmaEntity lemma = new LemmaEntity();
            lemma.setLemma(s);
            lemma.setSite(siteEntity);
            lemma.setFrequency(1);
            lemmaRepository.saveOrUpdate(lemma);
        });
    }
}
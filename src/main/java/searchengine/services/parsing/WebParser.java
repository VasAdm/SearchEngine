package searchengine.services.parsing;

import lombok.extern.slf4j.Slf4j;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.model.StatusType;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

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
    private final boolean root;
    private String url;
    private HtmlParser htmlParser;

    public WebParser(SiteEntity siteEntity, String path, SiteRepository siteRepository, PageRepository pageRepository, Set<String> pageSet, boolean root) {
        this.siteEntity = siteEntity;
        this.path = path;
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
        this.pageSet = pageSet;
        this.root = root;
    }

    @Override
    protected void compute() {
        url = isRoot() ? siteEntity.getUrl() + "/" : path;
        htmlParser = new HtmlParser(url, siteEntity);
        if (isNotFailed() && isNotVisited()) {
            boolean saved = savePage() != null;
            updateStatusTime();
            if (saved) {
                Set<ForkJoinTask<Void>> tasks = htmlParser.getPaths().stream()
                        .map(childPath -> new WebParser(
                                siteEntity,
                                childPath,
                                siteRepository,
                                pageRepository,
                                pageSet,
                                false)
                                .fork())
                        .collect(Collectors.toSet());
                tasks.forEach(ForkJoinTask::join);

//                if (isRoot() && isNotFailed()) {
//                    indexed();
//                }
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
        siteEntity.setStatusTime(LocalDateTime.now());
        siteRepository.save(siteEntity);
    }

    protected PageEntity savePage() {
        return pageRepository.save(htmlParser.getPage());
    }

    protected boolean isRoot() {
        return root;
    }

//    protected void indexed() {
//        siteEntity.setStatusTime(LocalDateTime.now());
//        siteEntity.setStatus(StatusType.INDEXED);
//        siteRepository.save(siteEntity);
//    }
}
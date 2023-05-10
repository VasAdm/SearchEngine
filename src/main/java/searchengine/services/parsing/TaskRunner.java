package searchengine.services.parsing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import searchengine.model.SiteEntity;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;

@Slf4j
@RequiredArgsConstructor
public class TaskRunner implements Runnable {
    private final Set<String> pageSet = Collections.synchronizedSet(new HashSet<>());
    private final SiteEntity siteEntity;
    private final ForkJoinPool task = new ForkJoinPool();
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;

    @Override
    public void run() {
        try (task) {
            WebParser webParser = new WebParser(siteEntity, "/", siteRepository, pageRepository, pageSet, true);
            task.execute(webParser);
        } catch (Exception ex) {
            log.error(ex.getLocalizedMessage());
        }
    }

    public SiteEntity getSiteEntity() {
        return siteEntity;
    }

    public ForkJoinPool getTask() {
        return task;
    }
}

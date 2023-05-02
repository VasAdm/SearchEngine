package searchengine.services.parsing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import searchengine.model.site.SiteEntity;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;

public class TaskRunner implements Runnable {
    private final SiteEntity siteEntity;
    private final Set<String> pageSet = Collections.synchronizedSet(new HashSet<>());

    private final ForkJoinPool task;

    private final Logger logger = LoggerFactory.getLogger(TaskRunner.class);



    public TaskRunner(SiteEntity siteEntity) {
        this.siteEntity = siteEntity;
        this.task = new ForkJoinPool();
    }

    @Override
    public void run() {
        try (task) {
            task.execute(new WebParser(siteEntity.getUrl(), siteEntity, pageSet));
        } catch (Exception ex) {
            logger.error(ex.getLocalizedMessage());
        }
    }

    public SiteEntity getSiteEntity() {
        return siteEntity;
    }

    public ForkJoinPool getTask() {
        return task;
    }
}

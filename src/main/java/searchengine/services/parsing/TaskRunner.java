package searchengine.services.parsing;

import searchengine.model.site.SiteEntity;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;

public class TaskRunner implements Runnable {
    private final SiteEntity siteEntity;
    private final Set<String> pageSet = Collections.synchronizedSet(new HashSet<>());

    public TaskRunner(SiteEntity siteEntity) {
        this.siteEntity = siteEntity;
    }

    @Override
    public void run() {
        try (ForkJoinPool task = new ForkJoinPool()) {
            task.execute(new WebParser(siteEntity.getUrl(), siteEntity, pageSet));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public SiteEntity getSiteEntity() {
        return siteEntity;
    }

}

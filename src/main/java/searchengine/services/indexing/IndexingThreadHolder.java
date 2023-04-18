package searchengine.services.indexing;

import searchengine.services.parsing.TaskRunner;
import searchengine.model.site.SiteEntity;

import java.util.Set;

public class IndexingThreadHolder {
    private static TaskRunner taskRunner;
    private static Set<SiteEntity> siteSet;

    public static TaskRunner getTaskRunner() {
        return taskRunner;
    }

    public static void setThread(TaskRunner taskRunner) {
        IndexingThreadHolder.taskRunner = taskRunner;
    }

    public static Set<SiteEntity> getSiteSet() {
        return siteSet;
    }

    public static void setSiteSet(Set<SiteEntity> siteSet) {
        IndexingThreadHolder.siteSet = siteSet;
    }
}

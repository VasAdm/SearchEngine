package searchengine.logic;

import searchengine.model.site.SiteEntity;
import searchengine.services.RepoHolder;
import searchengine.services.StatusType;
import searchengine.services.site.SiteService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;

public class TaskRunner {
    private static List<SiteEntity> siteEntities = new ArrayList<>();
    private static final Map<SiteEntity, ForkJoinPool> taskList = new HashMap<>();

    private static boolean started = false;


    public static void start() {
        started = true;
        SiteService siteService = RepoHolder.getSiteService();

        for (SiteEntity site : siteEntities) {
            ForkJoinPool task = new ForkJoinPool();
            task.execute(new WebParser(site.getUrl(), site));
            taskList.put(site, task);
        }

        while (!taskList.isEmpty()) {
            for (Map.Entry<SiteEntity, ForkJoinPool> entry: taskList.entrySet()) {
                if (entry.getValue().isTerminated()) {
                    SiteEntity site = entry.getKey();
                    taskList.remove(site);
                    site.setStatusTime(LocalDateTime.now());
                    site.setStatus(StatusType.INDEXED);
                    siteService.save(site);
                }
            }
        }
        started = false;


//        try {
//            new ForkJoinPool().invoke(new WebParser(siteEntity.getUrl(), siteEntity));
//            siteEntity.setStatus(StatusType.INDEXED);
//        } catch (Exception ex) {
//            siteEntity.setStatus(StatusType.FAILED);
//            siteEntity.setLastError(Arrays.toString(ex.getStackTrace()));
//        } finally {
//            siteEntity.setStatusTime(LocalDateTime.now());
//            siteService.save(siteEntity);
//        }
    }

    public static void setSiteEntities(List<SiteEntity> siteList) {
        siteEntities = siteList;
    }

    public static Map<SiteEntity, ForkJoinPool> getTaskList() {
        return taskList;
    }

    public static boolean isStarted() {
        return started;
    }
}

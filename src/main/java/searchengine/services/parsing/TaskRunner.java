package searchengine.services.parsing;

import searchengine.model.site.SiteEntity;
import searchengine.services.RepoHolder;
import searchengine.model.site.StatusType;
import searchengine.repository.SiteService;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;

public class TaskRunner implements Runnable {
    private final Set<SiteEntity> siteEntities;
    private final Map<SiteEntity, ForkJoinPool> taskList = new ConcurrentHashMap<>();

    public TaskRunner(Set<SiteEntity> siteEntities) {
        this.siteEntities = siteEntities;
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        Thread.currentThread().setName("Waiting thread");
        SiteService siteService = RepoHolder.getSiteService();

        for (SiteEntity site : siteEntities) {
            ForkJoinPool task = new ForkJoinPool(4);
            task.execute(new WebParser(site.getUrl(), site));
            taskList.put(site, task);
        }

        while (!taskList.isEmpty()) {
            try {
                Thread.sleep(10000);
//				System.out.println(Thread.currentThread().getName() + " - started check");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for (Map.Entry<SiteEntity, ForkJoinPool> entry : taskList.entrySet()) {
//				System.out.println(
//						entry.getKey().getName() + System.lineSeparator() +
//								"\tis terminated - " + entry.getValue().isTerminated() + System.lineSeparator() +
//								"\tis quiescent - " + entry.getValue().isQuiescent() + System.lineSeparator() +
//								"\tis terminating - " + entry.getValue().isTerminating() + System.lineSeparator() +
//								"\tis shutdown - " + entry.getValue().isShutdown() + System.lineSeparator()
//				);
                if (entry.getValue().isQuiescent() || entry.getValue().isTerminated()) {
                    SiteEntity site = entry.getKey();
                    System.out.println("Парсинг сайта: " + site.getName() + " завершился за - " + ((System.currentTimeMillis() - startTime) / 1000) + " секунд");
                    taskList.remove(site);
                    site.setStatusTime(LocalDateTime.now());
                    if (site.getStatus() != StatusType.FAILED) {
                        site.setStatus(StatusType.INDEXED);
						siteService.save(site);
                    }
                }
            }
        }
        System.out.println((System.currentTimeMillis() - startTime) / 1000);
    }

    public void stop() {
        taskList.values().forEach(ForkJoinPool::shutdownNow);

    }

    public boolean isAlive() {
        return Thread.currentThread().isAlive();
    }
}

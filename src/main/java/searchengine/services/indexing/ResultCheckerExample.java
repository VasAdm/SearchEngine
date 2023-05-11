package searchengine.services.indexing;

import lombok.extern.slf4j.Slf4j;
import searchengine.model.SiteEntity;
import searchengine.model.StatusType;
import searchengine.repository.SiteRepository;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.RunnableFuture;

@Slf4j
public class ResultCheckerExample implements Runnable {

    private final Map<SiteEntity, RunnableFuture<Integer>> runnableFutureList;
    private final SiteRepository siteRepository;

    public ResultCheckerExample(Map<SiteEntity, RunnableFuture<Integer>> runnableFutureList, SiteRepository siteRepository) {
        this.runnableFutureList = runnableFutureList;
        this.siteRepository = siteRepository;
    }

    @Override
    public void run() {
        while (!runnableFutureList.isEmpty()) {
            for (Map.Entry<SiteEntity, RunnableFuture<Integer>> entry : runnableFutureList.entrySet()) {
                if (entry.getValue().isCancelled()) {
                    entry.getKey().setStatus(StatusType.FAILED);
                    entry.getKey().setLastError("Индексация остановлена пользователем");
                    entry.getKey().setStatusTime(LocalDateTime.now());
                    siteRepository.save(entry.getKey());
                    runnableFutureList.remove(entry.getKey());
                } else if (entry.getValue().isDone()) {
                    entry.getKey().setStatus(StatusType.INDEXED);
                    entry.getKey().setStatusTime(LocalDateTime.now());
                    siteRepository.save(entry.getKey());
                    runnableFutureList.remove(entry.getKey());
                }


//                RunnableFuture<Integer> future = futureIterator.next();
//                if (future.isDone() || future.isCancelled()) {
//                    futureIterator.remove();
//                    runnableFutureList.remove(future);
//                }
            }
        }
    }
}

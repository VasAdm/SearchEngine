package searchengine.services.indexing;

import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.RunnableFuture;

@Slf4j
public class ResultCheckerExample implements Runnable {

    private final List<RunnableFuture<Integer>> runnableFutureList;

    public ResultCheckerExample(List<RunnableFuture<Integer>> runnableFutureList) {
        this.runnableFutureList = runnableFutureList;
    }

    @Override
    public void run() {
        while (!runnableFutureList.isEmpty()) {
            for (Iterator<RunnableFuture<Integer>> futureIterator = runnableFutureList.iterator(); futureIterator.hasNext(); ) {
                RunnableFuture<Integer> future = futureIterator.next();
                if (future.isDone() || future.isCancelled()) {
                    futureIterator.remove();
                }
            }
        }
    }
}

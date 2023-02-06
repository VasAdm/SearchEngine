package searchengine.logic;

import searchengine.model.site.SiteEntity;
import searchengine.services.RepoHolder;
import searchengine.services.StatusType;
import searchengine.services.site.SiteService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;

public class TaskRunner implements Runnable {
	private final Set<SiteEntity> siteEntities;
	private final Map<SiteEntity, ForkJoinPool> taskList = new HashMap<>();

	public TaskRunner(Set<SiteEntity> siteEntities) {
		this.siteEntities = siteEntities;
	}

	@Override
	public void run() {
		SiteService siteService = RepoHolder.getSiteService();

		for (SiteEntity site : siteEntities) {
			ForkJoinPool task = new ForkJoinPool();
			task.execute(new WebParser(site.getUrl(), site));
			taskList.put(site, task);
		}

		while (!taskList.isEmpty()) {
			for (Map.Entry<SiteEntity, ForkJoinPool> entry : taskList.entrySet()) {
				if (entry.getValue().isTerminated()) {
					SiteEntity site = entry.getKey();
					taskList.remove(site);
					site.setStatusTime(LocalDateTime.now());
					site.setStatus(StatusType.INDEXED);
					siteService.save(site);
				}
			}
		}
	}
}

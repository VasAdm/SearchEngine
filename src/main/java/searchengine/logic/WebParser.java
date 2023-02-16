package searchengine.logic;

import lombok.RequiredArgsConstructor;
import searchengine.model.page.PageEntity;
import searchengine.model.site.SiteEntity;

import java.util.Map;
import java.util.concurrent.RecursiveAction;

@RequiredArgsConstructor
public class WebParser extends RecursiveAction {
	private final String url;
	private final SiteEntity siteEntity;

	@Override
	protected void compute() {
		LinkCollector linkCollector = new LinkCollector(url, siteEntity);

		Map<String, SiteEntity> child = linkCollector.collectLinks();

		for (Map.Entry<String, SiteEntity> entity : child.entrySet()) {
			WebParser task = new WebParser(entity.getKey(), entity.getValue());
			task.fork();
		}
	}
}
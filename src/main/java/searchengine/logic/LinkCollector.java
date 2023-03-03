package searchengine.logic;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import searchengine.model.page.Page;
import searchengine.model.site.SiteEntity;
import searchengine.services.LinkHolder;
import searchengine.services.RepoHolder;
import searchengine.services.page.PageService;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class LinkCollector {

	private final String url;
	private final SiteEntity site;
	private final PageService pageService;

	public LinkCollector(String url, SiteEntity site) {
		this.url = url;
		this.site = site;
		this.pageService = RepoHolder.getPageService();
	}

	public Map<String, SiteEntity> collectLinks() {
		Map<String, SiteEntity> result = new HashMap<>();
		Page page = new PageParser(url, site).parsePage();

		Elements elements = page.getDocument().select("a[href^=/]");

		for (Element el : elements) {
			String absPath = el.attr("abs:href");
			String relPath = el.attr("href");


			boolean isContainRoot = absPath.contains(page.getSite().getUrl());
			boolean isAlreadyAdded = pageService.isAlreadyExist(relPath, site);
			boolean isAnchor = relPath.contains("#");

			boolean isFit =
					isContainRoot &&
							!isAlreadyAdded &&
							!isAnchor;

			if (isFit) {
				System.out.println(absPath + " - " + page.getSite().getUrl());
				result.put(absPath, page.getSite());
			}
		}

		return result;
	}
}

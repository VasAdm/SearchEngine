package searchengine.services.parsing;

import lombok.Getter;
import lombok.Setter;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import searchengine.RepoHolder;
import searchengine.model.page.Page;
import searchengine.model.page.PageEntity;
import searchengine.model.site.SiteEntity;
import searchengine.repository.PageService;
import searchengine.repository.SiteService;

import java.time.LocalDateTime;
import java.util.*;

@Getter
@Setter
public class LinkCollector {

	private final String url;
	private final SiteEntity site;
	private final Set<String> pageSet;
	private final PageService pageService;
	private final SiteService siteService;

	private final Logger logger = LoggerFactory.getLogger(LinkCollector.class);


	public LinkCollector(String url, SiteEntity site, Set<String> pageSet) {
		this.url = url;
		this.site = site;
		this.pageSet = pageSet;
		this.pageService = RepoHolder.getPageService();
		this.siteService = RepoHolder.getSiteService();
	}

	public Map<String, SiteEntity> collectLinks() {
		Map<String, SiteEntity> result = new HashMap<>();
		List<PageEntity> entityList = new ArrayList<>();
		Page page = new PageParser(url, site).parsePage();

		if (pageSet.add(site.getUrl() + page.getPath())) {
			entityList.add(page.getPageEntity());
		}

		Elements elements = page.getContent().select("a[href^=/]");

		for (Element el : elements) {
			String absPath = el.attr("abs:href");
			String relPath = el.attr("href");


			boolean isContainRoot = absPath.contains(page.getSite().getUrl());
			boolean isAlreadyAdded = !pageSet.add(site.getUrl() + relPath);
			boolean isAnchor = relPath.contains("#");

			boolean isFit =
					isContainRoot &&
							!isAlreadyAdded &&
							!isAnchor;

			if (isFit) {
				Page tmpPage = new PageParser(absPath, site).parsePage();
				PageEntity newPage = tmpPage.getPageEntity();

				if (!Objects.equals(newPage.getContent(), "")) {
					result.put(absPath, site);
					entityList.add(newPage);
				}
			}
		}

		pageService.saveAll(entityList);
		entityList.clear();
		site.setStatusTime(LocalDateTime.now());
		siteService.save(site);

		return result;
	}
}

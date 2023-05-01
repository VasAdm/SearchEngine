package searchengine.services.parsing;

import lombok.Getter;
import lombok.Setter;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import searchengine.model.page.Page;
import searchengine.model.page.PageEntity;
import searchengine.model.site.SiteEntity;
import searchengine.repository.RedisRepository;
import searchengine.RepoHolder;
import searchengine.repository.PageService;
import searchengine.repository.SiteService;

import java.time.LocalDateTime;
import java.util.*;

@Getter
@Setter
public class LinkCollector {

	private final String url;
	private final SiteEntity site;
	private final RedisRepository redisRepository;
	private final PageService pageService;
	private final SiteService siteService;
//	private final List<Page> pageList;


	public LinkCollector(String url, SiteEntity site) {
		this.url = url;
		this.site = site;
		this.redisRepository = RepoHolder.getRedisRepository();
		this.pageService = RepoHolder.getPageService();
		this.siteService = RepoHolder.getSiteService();
//		this.pageList = new ArrayList<>();
	}

	public Map<String, SiteEntity> collectLinks() {
		List<PageEntity> entityList = new ArrayList<>();
		Map<String, SiteEntity> result = new HashMap<>();
		Page page = new PageParser(url, site).parsePage();

		if (redisRepository.add(site.getUrl(), page.getPath()) == 1) {
			entityList.add(page.getPageEntity());
		}

		Elements elements = page.getContent().select("a[href^=/]");

		for (Element el : elements) {
			String absPath = el.attr("abs:href");
			String relPath = el.attr("href");


			boolean isContainRoot = absPath.contains(page.getSite().getUrl());
			boolean isAlreadyAdded = redisRepository.add(site.getUrl(), relPath) == 0;
			boolean isAnchor = relPath.contains("#");

			boolean isFit =
					isContainRoot &&
							!isAlreadyAdded &&
							!isAnchor;

			if (isFit) {
				Page tmpPage = new PageParser(absPath, site).parsePage();
//				pageList.add(tmpPage);
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

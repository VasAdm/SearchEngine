package searchengine.services.parsing;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import searchengine.Page;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.time.LocalDateTime;
import java.util.*;

@Getter
@Setter
@RequiredArgsConstructor
public class LinkCollector {

	private final String url;
	private final SiteEntity site;
	private final Set<String> pageSet;
	private final PageRepository pageRepository;
	private final SiteRepository siteRepository;

	private final Logger logger = LoggerFactory.getLogger(LinkCollector.class);


	public Map<String, SiteEntity> collectLinks() {
		Map<String, SiteEntity> result = new HashMap<>();
		List<PageEntity> entityList = new ArrayList<>();
		Page page = new HtmlParser(url, site).parsePage();

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
				Page tmpPage = new HtmlParser(absPath, site).parsePage();
				PageEntity newPage = tmpPage.getPageEntity();

				if (!Objects.equals(newPage.getContent(), "")) {
					result.put(absPath, site);
					entityList.add(newPage);
				}
			}
		}

		pageRepository.saveAll(entityList);
		entityList.clear();
		site.setStatusTime(LocalDateTime.now());
		siteRepository.save(site);

		return result;
	}
}

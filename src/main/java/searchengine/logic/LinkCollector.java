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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Getter
@Setter
public class LinkCollector {

	private final String url;
	private final SiteEntity site;

	public Map<String, SiteEntity> collectLinks() {
		Map<String, SiteEntity> result = Collections.synchronizedMap(new HashMap<>());
		Page page = new PageParser(url, site).parsePage();


		Elements elements = page.getDocument().select("a[href^=/]");

		for (Element el : elements) {
			String absPath = el.attr("abs:href");
			String relPath = el.attr("href");


			boolean isContainRoot = absPath.contains(page.getSite().getUrl());
			boolean isNotAlreadyAdded = LinkHolder.addLink(site, absPath);
			boolean isAnchor = relPath.contains("#");

			boolean isFit =
					isContainRoot &&
							isNotAlreadyAdded &&
							!isAnchor;

			if (isFit) {
				result.put(absPath, page.getSite());
			}
		}

		return result;
	}
}

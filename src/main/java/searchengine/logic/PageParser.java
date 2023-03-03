package searchengine.logic;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.internal.StringUtil;
import org.jsoup.nodes.Document;
import searchengine.model.page.Page;
import searchengine.model.page.PageEntity;
import searchengine.model.site.SiteEntity;
import searchengine.services.RepoHolder;
import searchengine.services.page.PageService;
import searchengine.services.site.SiteService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Objects;

public class PageParser {
	private String url;
	private final SiteEntity site;
	private final PageService pageService;
	private final SiteService siteService;

	public PageParser(String url, SiteEntity site) {
		this.url = url;
		this.site = site;
		this.pageService = RepoHolder.getPageService();
		this.siteService = RepoHolder.getSiteService();
	}

	public Page parsePage() {
		PageEntity pageEntity = new PageEntity();
		Page page = new Page();

//		int delay = (int) (Math.random() * ((5000 - 3000) + 1)) + 3000;
		try {
//			Thread.sleep(delay);

			Connection.Response response = Jsoup.connect(url).maxBodySize(0).userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6").referrer("https://www.google.com").ignoreContentType(true).execute();

			String contentType = response.contentType();

			assert contentType != null;
			if (contentType.contains("text/")) {
				int statusCode = response.statusCode();

				Document document = response.parse();

				page.setSite(site);
				page.setDocument(document);

				pageEntity.setContent(document.toString());
				String path = response.url().getPath();
				pageEntity.setPath(Objects.equals(path, "") ? "/" : path);
				pageEntity.setCode(statusCode);
				pageEntity.setSite(site);


				if (pageService.save(pageEntity)) {
					site.setStatusTime(LocalDateTime.now());
					siteService.save(site);
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return page;
	}
}

package searchengine.services.page;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.model.page.PageEntity;
import searchengine.model.site.SiteEntity;
import searchengine.repository.PageRepository;

import java.util.List;

@Service
public class PageService {
	private final PageRepository pageRepository;

	@Autowired
	public PageService(PageRepository pageRepository) {
		this.pageRepository = pageRepository;
	}

	public boolean save(PageEntity page) {
		System.out.println(page.getPath());
		if (pageRepository.isPageAlreadyInDB(page.getPath(), page.getSite()) != null) {
			System.out.println("-");
			return false;
		} else {
			pageRepository.save(page);
			System.out.println("+");
			return true;
		}
	}

	public Iterable<PageEntity> saveAll(List<PageEntity> pagesList) {
		return pageRepository.saveAll(pagesList);
	}

	public void deleteAll() {
		pageRepository.deleteAll();
	}

	public boolean isAlreadyExist(String path, SiteEntity site) {
		return pageRepository.isPageAlreadyInDB(path, site) != null;
	}
}

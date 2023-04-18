package searchengine.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.model.page.PageEntity;
import searchengine.model.site.SiteEntity;
import searchengine.repository.PageRepository;

import java.util.List;
import java.util.stream.Stream;

@Service
public class PageService {
    private final PageRepository pageRepository;

    @Autowired
    public PageService(PageRepository pageRepository) {
        this.pageRepository = pageRepository;
    }

    public PageEntity save(PageEntity page) {
        return pageRepository.save(page);
    }

    public Iterable<PageEntity> saveAll(List<PageEntity> pagesList) {
        return pageRepository.saveAll(pagesList);
    }

    public List<PageEntity> saveAllAndFlush(List<PageEntity> pagesList) {
        return pageRepository.saveAllAndFlush(pagesList);
    }

    public void deleteAll() {
        pageRepository.deleteAll();
    }

    public boolean isAlreadyExist(String path, SiteEntity site) {
        return pageRepository.isPageAlreadyInDB(path, site) != null;
    }

    public PageEntity getPageByPathAndSite(String path, SiteEntity site) {
        return pageRepository.findAllByPathAndSite(path, site);
    }

}

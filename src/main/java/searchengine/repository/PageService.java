package searchengine.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;

import java.util.List;

@Service
public class PageService {
    private final PageRepository pageRepository;

    @Autowired
    public PageService(PageRepository pageRepository) {
        this.pageRepository = pageRepository;
    }

    public void save(PageEntity page) {
        pageRepository.save(page);
    }

    public void saveAll(List<PageEntity> pagesList) {
        pageRepository.saveAll(pagesList);
    }

    public PageEntity getPageByPathAndSite(String path, SiteEntity site) {
        return pageRepository.findAllByPathAndSite(path, site);
    }

}

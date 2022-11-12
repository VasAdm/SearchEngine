package searchengine.model.page;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.model.site.SiteEntity;

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

    public boolean isPageExist(String path, SiteEntity site) {return pageRepository.existsByPathAndSite(path, site);}
}

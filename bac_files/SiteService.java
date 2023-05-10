package searchengine.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.model.SiteEntity;

@Service
public class SiteService {

    private final SiteRepository siteRepository;

    @Autowired
    public SiteService(SiteRepository siteRepository) {
        this.siteRepository = siteRepository;
    }

    public SiteEntity getSiteByUrl(String url) {
        return siteRepository.getByUrl(url);
    }

    public SiteEntity save(SiteEntity site) {
        return siteRepository.save(site);
    }

    public void deleteAll() {
        siteRepository.deleteAll();
    }


}

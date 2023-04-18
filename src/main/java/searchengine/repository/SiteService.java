package searchengine.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.model.site.SiteEntity;
import searchengine.repository.SiteRepository;

import java.util.List;

@Service
public class SiteService {

    private final SiteRepository siteRepository;

    @Autowired
    public SiteService(SiteRepository siteRepository) {
        this.siteRepository = siteRepository;
    }

    public Iterable<SiteEntity> getSitesData() {
        return siteRepository.findAll();
    }

    public SiteEntity getSiteByUrl(String url) {
        return siteRepository.getByUrl(url);
    }

    public void deleteAllById(Integer id) {
        siteRepository.deleteById(id);
    }

    public SiteEntity save(SiteEntity site) {
        return siteRepository.save(site);
    }

    public void deleteAll() {siteRepository.deleteAll();}


}

package searchengine.model.site;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SiteService {

    private final SiteRepository siteRepository;

    @Autowired
    public SiteService(SiteRepository siteRepository) {
        this.siteRepository = siteRepository;
    }

    public List<SiteEntity> getSitesData() {
        return siteRepository.findAll();
    }

    public SiteEntity getSiteByUrl(String url) {
        return siteRepository.getByUrl(url);
    }

    public Integer deleteAllByUrl(String url) {
        return siteRepository.deleteAllByUrl(url);
    }

    public SiteEntity save(SiteEntity site) {
        return siteRepository.save(site);
    }
}

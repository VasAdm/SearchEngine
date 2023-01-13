package searchengine.repository;

import org.springframework.data.repository.CrudRepository;
import searchengine.model.site.SiteEntity;

public interface SiteRepository extends CrudRepository<SiteEntity, Integer> {
    SiteEntity getByUrl(String url);
}

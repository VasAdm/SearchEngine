package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import searchengine.model.SiteEntity;

@Repository
public interface SiteRepository extends CrudRepository<SiteEntity, Integer> {
    SiteEntity getByUrl(String url);
}

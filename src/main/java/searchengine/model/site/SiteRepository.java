package searchengine.model.site;

import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;

public interface SiteRepository extends JpaRepository<SiteEntity, Integer> {

    SiteEntity getByUrl(String url);
    @Transactional
    Integer deleteAllByUrl(String url);

}

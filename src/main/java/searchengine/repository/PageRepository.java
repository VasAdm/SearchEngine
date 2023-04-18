package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import searchengine.model.page.PageEntity;
import searchengine.model.site.SiteEntity;

public interface PageRepository extends JpaRepository<PageEntity, Integer> {
    @Query("from PageEntity p where p.path = ?1 and p.site = ?2")
    PageEntity isPageAlreadyInDB(String path, SiteEntity site);

    PageEntity findAllByPathAndSite(String path, SiteEntity site);
}

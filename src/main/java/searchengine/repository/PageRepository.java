package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.PageEntity;

import java.util.Optional;

@Repository
public interface PageRepository extends CrudRepository<PageEntity, Integer> {
//    @Query("from PageEntity p where p.path = ?1 and p.site = ?2")
//    PageEntity isPageAlreadyInDB(String path, SiteEntity site);
//

//    Optional<PageEntity> findAllByPathAndSite(String path, SiteEntity site);
    Optional<PageEntity> findAllByPathAndSite_Id(String path, int siteId);

}

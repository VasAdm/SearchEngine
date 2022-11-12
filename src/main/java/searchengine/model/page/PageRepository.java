package searchengine.model.page;

import org.springframework.data.jpa.repository.JpaRepository;
import searchengine.model.site.SiteEntity;

import java.util.List;

public interface PageRepository extends JpaRepository<PageEntity, Integer> {

    boolean existsByPathAndSite(String path, SiteEntity site);
}

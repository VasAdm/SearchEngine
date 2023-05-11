package searchengine.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.SiteEntity;

import java.util.Optional;

@Repository
public interface SiteRepository extends CrudRepository<SiteEntity, Integer> {
    @Query("select s from SiteEntity s where s.id = ?1")
    Optional<SiteEntity> find(int id);
    SiteEntity getByUrl(String url);
}

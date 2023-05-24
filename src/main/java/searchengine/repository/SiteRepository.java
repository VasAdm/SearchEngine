package searchengine.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.SiteEntity;

import java.time.LocalDateTime;

@Repository
public interface SiteRepository extends CrudRepository<SiteEntity, Integer> {
    SiteEntity getByUrl(String url);

    @Transactional
    @Modifying
    @Query("update SiteEntity s set s.statusTime = ?1 where s.id = ?2")
    void updateTime(LocalDateTime statusTime, int id);


}
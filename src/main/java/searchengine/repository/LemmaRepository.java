package searchengine.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.LemmaEntity;

import java.util.Optional;

@Repository
public interface LemmaRepository extends CrudRepository<LemmaEntity, Integer> {
    Optional<LemmaEntity> findBySiteIdAndLemma(Integer siteId, String lemma);

    @Modifying
    @Query(value = "insert into lemmas (site_id, lemma, frequency) " +
            "  values (:#{#lemma.site.id}, :#{#lemma.lemma}, :#{#lemma.frequency}) " +
            "  on conflict(site_id, lemma) do update set frequency = lemmas.frequency + 1", nativeQuery = true)
    int saveOrUpdate(LemmaEntity lemma);
    }

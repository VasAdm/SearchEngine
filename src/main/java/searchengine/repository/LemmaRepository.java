package searchengine.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.LemmaEntity;

import java.util.Collection;

@Repository
public interface LemmaRepository extends CrudRepository<LemmaEntity, Integer> {
    @Transactional
    @Modifying
    @Query("update LemmaEntity l set l.frequency = l.frequency-1 where l.lemma in ?1")
    void updateFrequencyByLemmaIn(Collection<String> lemmas);

    @Query(value = "insert into lemmas (site_id, lemma, frequency) " +
            "  values (:#{#lemma.site.id}, :#{#lemma.lemma}, :#{#lemma.frequency}) " +
            "  on conflict(site_id, lemma) do update set frequency = case " +
            "when lemmas.frequency = 1 then 1 else lemmas.frequency - 1 end returning *", nativeQuery = true)
    LemmaEntity saveOrUpdate(LemmaEntity lemma);
}


package searchengine.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.IndexEntity;
import searchengine.model.PageEntity;

import java.util.List;

@Repository
public interface IndexRepository extends CrudRepository<IndexEntity, Integer> {
    List<IndexEntity> deleteByPage(PageEntity page);

}

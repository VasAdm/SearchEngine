package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.IndexEntity;
import searchengine.model.PageEntity;

import java.util.List;

@Repository
public interface IndexRepository extends JpaRepository<IndexEntity, Integer> {
    List<IndexEntity> getByPage(PageEntity page);
}

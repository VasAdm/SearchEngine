package searchengine.services.lemmasIndexesScraper;

import lombok.RequiredArgsConstructor;
import searchengine.model.IndexEntity;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class LemmasIndexesCollector {
    private final SiteEntity siteEntity;
    private final PageEntity pageEntity;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;

    public void collect() {

        LemmasScraper lemmasScraper;
        try {
            lemmasScraper = LemmasScraper.getInstance();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (pageEntity.getCode() < 400) {
            Map<String, Integer> lemmas = lemmasScraper.collectLemmas(pageEntity.getContent());
            Map<LemmaEntity, Integer> lemmasMap = saveLemmas(lemmas);
            saveIndexes(lemmasMap, pageEntity);
        }
    }

    protected Map<LemmaEntity, Integer> saveLemmas(Map<String, Integer> lemmasMap) {
        Map<LemmaEntity, Integer> result = new HashMap<>();
        lemmasMap.forEach((k, v) -> {
            LemmaEntity lemma = new LemmaEntity(siteEntity, k, 1);
            result.put(lemmaRepository.saveOrUpdate(lemma), v);
        });
        return result;
    }

    protected void saveIndexes(Map<LemmaEntity, Integer> lemmasMap, PageEntity pageEntity) {
        lemmasMap.forEach((k, v) -> {
            IndexEntity indexEntity = new IndexEntity(v, pageEntity, k);
            indexRepository.save(indexEntity);
        });
    }
}

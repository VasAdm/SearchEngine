package searchengine.services.indexing;

import org.springframework.http.ResponseEntity;
import searchengine.dto.indexing.IndexingStatusResponse;

import java.util.concurrent.ExecutionException;

public interface IndexingService {
    ResponseEntity<IndexingStatusResponse> startIndexing();
    ResponseEntity<IndexingStatusResponse> stopIndexing();
    ResponseEntity<IndexingStatusResponse> indexPage(String url);
}

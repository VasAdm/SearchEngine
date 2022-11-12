package searchengine.services.indexing;

import searchengine.dto.indexing.IndexingStatusResponse;

public interface IndexingService {
    IndexingStatusResponse getIndexingStatus();
}

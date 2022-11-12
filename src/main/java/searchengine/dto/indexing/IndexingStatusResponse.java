package searchengine.dto.indexing;

import lombok.Data;

@Data
public class IndexingStatusResponse {
    private boolean result;

    public IndexingStatusResponse(boolean result) {
        this.result = result;
    }
}

package searchengine.dto.indexing;

import lombok.Data;

@Data
public class IndexingStatusResponseError extends IndexingStatusResponse{
    private String error;

    public IndexingStatusResponseError(boolean result, String error) {
        super(result);
        this.error = error;
    }
}

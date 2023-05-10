package searchengine.dto.indexing;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IndexingStatusResponseError extends IndexingStatusResponse{
    private String error;

    public IndexingStatusResponseError(boolean result, String error) {
        super(result);
        this.error = error;
    }
}

package searchengine.model.page;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jsoup.nodes.Document;

@NoArgsConstructor
@Getter
@Setter
public class PageEntityWithDoc {
    private PageEntity page;
    private Document document;
}

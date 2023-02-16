package searchengine.model.page;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jsoup.nodes.Document;
import searchengine.model.site.SiteEntity;

@NoArgsConstructor
@Getter
@Setter
public class Page {
    private SiteEntity site;
    private Document document;
}

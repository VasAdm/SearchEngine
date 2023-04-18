package searchengine.model.page;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.jsoup.nodes.Document;
import searchengine.model.site.SiteEntity;

import javax.persistence.Column;
import java.util.Objects;

@NoArgsConstructor
@Getter
@Setter
public class Page {
    private SiteEntity site;
    private String path;
    private int code;
    private Document content;

    public PageEntity getPageEntity() {
        PageEntity pageEntity = new PageEntity();

        if (content != null) {
            pageEntity.setContent(content.toString());
        } else {
            pageEntity.setContent("");
        }
        pageEntity.setPath(path);
        pageEntity.setCode(code);
        pageEntity.setSite(site);

        return pageEntity;
    }
}

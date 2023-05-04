package searchengine.services.parsing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import searchengine.model.SiteEntity;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.RecursiveAction;

public class WebParser extends RecursiveAction {
    private final String url;
    private final SiteEntity siteEntity;
    private final Set<String> pageSet;

    private final Logger logger = LoggerFactory.getLogger(WebParser.class);

    public WebParser(String url, SiteEntity siteEntity, Set<String> pageSet) {
        this.url = url;
        this.siteEntity = siteEntity;
        this.pageSet = pageSet;
    }

    @Override
    protected void compute() {
        LinkCollector linkCollector = new LinkCollector(url, siteEntity, pageSet);

        try {
            Map<String, SiteEntity> child = linkCollector.collectLinks();

            if (child.size() != 0) {

                for (Map.Entry<String, SiteEntity> entity : child.entrySet()) {
                    WebParser task = new WebParser(entity.getKey(), entity.getValue(), pageSet);
                    task.fork();
                }
            }
        } catch (Exception ex) {
            logger.warn(ex.getMessage());
        }
    }
}
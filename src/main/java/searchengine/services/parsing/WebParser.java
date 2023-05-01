package searchengine.services.parsing;

import lombok.RequiredArgsConstructor;
import searchengine.model.site.SiteEntity;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.RecursiveAction;

@RequiredArgsConstructor
public class WebParser extends RecursiveAction {
    private final String url;
    private final SiteEntity siteEntity;
    private final Set<String> pageSet;

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
            ex.printStackTrace();
        }
    }
}
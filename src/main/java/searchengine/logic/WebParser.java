package searchengine.logic;

import lombok.RequiredArgsConstructor;
import searchengine.model.page.PageEntity;
import searchengine.model.page.PageEntityWithDoc;
import searchengine.model.site.SiteEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RecursiveAction;

@RequiredArgsConstructor
public class WebParser extends RecursiveAction {
    private final String url;
    private final SiteEntity siteEntity;

    @Override
    protected void compute() {

        List<WebParser> taskList = Collections.synchronizedList(new ArrayList<>());

        PageEntityWithDoc pageWithDoc = PageParser.parsePage(url, siteEntity);

        LinkCollector linkCollector = new LinkCollector();

        Map<PageEntity, SiteEntity> child = linkCollector.collectLinks(pageWithDoc);

        for (Map.Entry<PageEntity, SiteEntity> entity : child.entrySet()) {
            String newUrl = entity.getValue().getUrl() + entity.getKey().getPath();
            WebParser task = new WebParser(newUrl, entity.getValue());
            task.fork();
            taskList.add(task);
        }

        for (WebParser task : taskList) {
            task.join();
        }
    }
}
package searchengine.services.parser;

import lombok.RequiredArgsConstructor;
import searchengine.model.page.PageEntity;
import searchengine.model.page.PageEntityWithDoc;
import searchengine.model.page.PageService;
import searchengine.model.site.SiteEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

@RequiredArgsConstructor
public class WebParser extends RecursiveAction {

    private final PageService pageService;
    private final String url;
    private final SiteEntity site;

    @Override
    protected void compute() {
        List<WebParser> taskList = new ArrayList<>();

        PageEntityWithDoc pageWithDoc = PageParser.parsePage(url, site);
        PageEntity page = pageWithDoc.getPage();
        pageService.save(page);

        Map<String, SiteEntity> child = LinkCollector.collectLinks(page, pageWithDoc.getDocument());

        for (Map.Entry<String, SiteEntity> entity : child.entrySet()) {
            WebParser task = new WebParser(pageService, entity.getKey(), entity.getValue());
            task.fork();
            taskList.add(task);
        }

        taskList.forEach(ForkJoinTask::join);
    }

    @Override
    public String toString() {
        return "WebParser{" +
                "pageService=" + pageService +
                ", url='" + url + '\'' +
                ", site=" + site +
                '}';
    }
}

package searchengine.services.indexing;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import searchengine.config.SitesList;
import searchengine.dto.indexing.IndexingStatusResponse;
import searchengine.dto.indexing.IndexingStatusResponseError;
import searchengine.logic.TaskRunner;
import searchengine.model.site.SiteEntity;
import searchengine.services.StatusType;
import searchengine.services.page.PageService;
import searchengine.services.site.SiteService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

@Component
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {
    private final SitesList sites;
    private final SiteService siteService;
    private final PageService pageService;

    @Override
    public IndexingStatusResponse getIndexingStatus() {
        Set<SiteEntity> siteEntities = new HashSet<>();

        sites.getSites().forEach(s -> {
            SiteEntity site = siteService.getSiteByUrl(s.getUrl());
            if (site != null) siteEntities.add(site);
        });

        if (siteEntities.stream().map(SiteEntity::getStatus).anyMatch(Predicate.isEqual(StatusType.INDEXING))) {
            return new IndexingStatusResponseError(false, "Индексаци уже запущена");
        } else {

            siteService.deleteAll();
            pageService.deleteAll();
            siteEntities.clear();

            sites.getSites().forEach(site -> {
                SiteEntity siteEntity = new SiteEntity();
                siteEntity.setName(site.getName());
                siteEntity.setUrl(site.getUrl());
                siteEntity.setStatus(StatusType.INDEXING);
                siteEntity.setStatusTime(LocalDateTime.now());

                siteEntities.add(siteService.save(siteEntity));
            });

            new Thread(new TaskRunner(siteEntities)).start();

            return new IndexingStatusResponse(true);
        }
    }

}

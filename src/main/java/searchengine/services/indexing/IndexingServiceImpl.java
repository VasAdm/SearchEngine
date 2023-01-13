package searchengine.services.indexing;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import searchengine.config.SitesList;
import searchengine.dto.indexing.IndexingStatusResponse;
import searchengine.dto.indexing.IndexingStatusResponseError;
import searchengine.logic.TaskRunner;
import searchengine.model.site.SiteEntity;
import searchengine.services.StatusType;
import searchengine.services.site.SiteService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@Component
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {
    private final SitesList sites;
    private final SiteService siteService;

    @Override
    public IndexingStatusResponse getIndexingStatus() {
        List<SiteEntity> siteList = new ArrayList<>();

        sites.getSites().forEach(s -> {
            SiteEntity site = siteService.getSiteByUrl(s.getUrl());
            if (site != null) siteList.add(site);
        });

        if (siteList.stream().map(SiteEntity::getStatus).anyMatch(Predicate.isEqual(StatusType.INDEXING))) {
            return new IndexingStatusResponseError(false, "Индексаци уже запущена");
        } else {

            siteList.forEach(s -> {
                siteService.deleteAllById(s.getId());
            });

            sites.getSites().forEach(site -> {
                SiteEntity siteEntity = new SiteEntity();
                siteEntity.setName(site.getName());
                siteEntity.setUrl(site.getUrl());
                siteEntity.setStatus(StatusType.INDEXING);
                siteEntity.setStatusTime(LocalDateTime.now());

                siteService.save(siteEntity);
            });

            TaskRunner.setSiteEntities(siteList);
            new Thread(TaskRunner::start).start();

            return new IndexingStatusResponse(true);
        }
    }

}

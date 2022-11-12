package searchengine.services.indexing;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.indexing.IndexingStatusResponse;
import searchengine.dto.indexing.IndexingStatusResponseError;
import searchengine.model.StatusType;
import searchengine.model.page.PageService;
import searchengine.model.site.SiteEntity;
import searchengine.model.site.SiteService;
import searchengine.services.parser.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Predicate;

@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {

    private final SitesList sites;

    private final SiteService siteService;

    private final PageService pageService;

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

            sites.getSites().forEach(s -> siteService.deleteAllByUrl(s.getUrl()));

            for (Site s : sites.getSites()) {

                SiteEntity siteEntity = new SiteEntity();
                siteEntity.setName(s.getName());
                siteEntity.setStatus(StatusType.INDEXING);
                siteEntity.setStatusTime(LocalDateTime.now());
                siteEntity.setUrl(s.getUrl());

                siteService.save(siteEntity);

                new ForkJoinPool().execute(new WebParser(pageService, siteEntity.getUrl(), siteEntity));
            }

            return new IndexingStatusResponse(true);
        }
    }


}

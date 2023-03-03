package searchengine.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.config.SpringfoxConfig;
import searchengine.dto.indexing.IndexingStatusResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.model.page.PageEntity;
import searchengine.model.site.SiteEntity;
import searchengine.services.indexing.IndexingService;
import searchengine.services.page.PageService;
import searchengine.services.site.SiteService;
import searchengine.services.statistic.StatisticsService;

@RestController
@RequestMapping("/api")
@Api(tags = {SpringfoxConfig.SEARCHENGINE_TAG})
public class ApiController {
    private final PageService pageService;
    private final SiteService siteService;
    private final StatisticsService statisticsService;
    private final IndexingService indexingService;

    @Autowired
    public ApiController(StatisticsService statisticsService, IndexingService indexingService, PageService pageService, SiteService siteService) {
        this.statisticsService = statisticsService;
        this.indexingService = indexingService;
        this.pageService = pageService;
        this.siteService = siteService;
    }

    @GetMapping("/statistics")
    @ApiOperation("operation to get statistic of websites")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    @ApiOperation("operation for starting indexing")
    public ResponseEntity<IndexingStatusResponse> startIndexing() {
        return ResponseEntity.ok(indexingService.getIndexingStatus());
    }
    @GetMapping("/getPage")
    public ResponseEntity<Boolean> getPage(@RequestParam(name = "path") String path) {
        return ResponseEntity.ok(pageService.isAlreadyExist(path, siteService.getSiteByUrl("https://www.playback.ru")));
    }
}

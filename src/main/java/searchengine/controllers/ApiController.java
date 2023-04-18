package searchengine.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import searchengine.config.SpringfoxConfig;
import searchengine.dto.indexing.IndexingStatusResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.indexing.IndexingService;
import searchengine.repository.PageService;
import searchengine.repository.SiteService;
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
    @ApiOperation("operation for start indexing")
    public ResponseEntity<IndexingStatusResponse> startIndexing() {
        return ResponseEntity.ok(indexingService.startIndexing());
    }

    @GetMapping("/stopIndexing")
    @ApiOperation("operation for stop indexing")
    public ResponseEntity<IndexingStatusResponse> stopIndexing() {
        return ResponseEntity.ok(indexingService.stopIndexing());
    }

    @PostMapping(value = "/indexPage")
    @ApiOperation("operation for indexing only one web page")
    public ResponseEntity<IndexingStatusResponse> indexPage(@RequestBody String url) {
        return ResponseEntity.ok(indexingService.indexPage(url));
    }
}

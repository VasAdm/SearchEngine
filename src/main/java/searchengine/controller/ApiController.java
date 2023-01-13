package searchengine.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import searchengine.config.SpringfoxConfig;
import searchengine.dto.indexing.IndexingStatusResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.indexing.IndexingService;
import searchengine.services.statistic.StatisticsService;

@RestController
@RequestMapping("/api")
@Api(tags = {SpringfoxConfig.SEARCHENGINE_TAG})
public class ApiController {

    private final StatisticsService statisticsService;
    private final IndexingService indexingService;

    public ApiController(StatisticsService statisticsService, IndexingService indexingService) {
        this.statisticsService = statisticsService;
        this.indexingService = indexingService;
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
}

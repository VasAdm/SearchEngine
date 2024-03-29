package searchengine.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.config.SpringfoxConfig;
import searchengine.dto.indexing.IndexingStatusResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.indexing.IndexingService;
import searchengine.services.statistic.StatisticsService;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api")
@Slf4j
@Api(tags = {SpringfoxConfig.SEARCHENGINE_TAG})
public class ApiController {
    private final StatisticsService statisticsService;
    private final IndexingService indexingService;

    @Autowired
    public ApiController(StatisticsService statisticsService, IndexingService indexingService) {
        this.statisticsService = statisticsService;
        this.indexingService = indexingService;
    }

    @GetMapping("/statistics")
    @ApiOperation("operation to get statistic of websites")
    public ResponseEntity<StatisticsResponse> statistics() {
        log.info("A request to get statistics has been received");
        return statisticsService.getStatistics();
    }

    @GetMapping("/startIndexing")
    @ApiOperation("operation to start indexing")
    public ResponseEntity<IndexingStatusResponse> startIndexing() {
        log.info("A request to start indexing has been received");
        return indexingService.startIndexing();
    }

    @GetMapping("/stopIndexing")
    @ApiOperation("operation to stop indexing")
    public ResponseEntity<IndexingStatusResponse> stopIndexing() {
        log.info("A request to stop indexing has been received");
        return indexingService.stopIndexing();
    }

    @PostMapping(value = "/indexPage")
    @ApiOperation("operation to indexing a web page")
    public ResponseEntity<IndexingStatusResponse> indexPage(@RequestBody String url) {
        log.info("A request to index page - " + url + ", has been received");
        return indexingService.indexPage(url);
    }
}

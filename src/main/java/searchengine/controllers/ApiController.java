package searchengine.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.StatisticsService;
import searchengine.services.index.StartIndexing;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final StatisticsService statisticsService;
    private final StartIndexing startIndexing;




    public ApiController(StatisticsService statisticsService, StartIndexing startIndexing) {
        this.statisticsService = statisticsService;
        this.startIndexing = startIndexing;
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public void startIndexing() {
        startIndexing.startIndexing();
    }

    @GetMapping("/stopIndexing")
    public void stopIndexing() {
        startIndexing.stopIndexing();
    }
}

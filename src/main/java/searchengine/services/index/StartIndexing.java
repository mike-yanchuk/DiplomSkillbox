package searchengine.services.index;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.services.index.site.SiteIndexing;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StartIndexing {
    private final SitesList sitesList;

    public void startIndexing() {
        List<Site> sites = sitesList.getSites();
        int core = Runtime.getRuntime().availableProcessors();
        ExecutorService service = Executors.newFixedThreadPool(core);
        for (Site site : sites) {
            String name = site.getName();
            String url = site.getUrl();
            service.execute(() -> new SiteIndexing(url, name).indexing());
        }
    }
}

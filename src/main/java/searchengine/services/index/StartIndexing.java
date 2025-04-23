package searchengine.services.index;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.model.SiteModel;
import searchengine.model.Status;
import searchengine.repository.RepositoryPage;
import searchengine.repository.RepositorySite;
import searchengine.services.index.site.SiteIndexing;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor

public class StartIndexing {
    private final SitesList sitesList;
    private final RepositorySite repositorySite;

    @Transactional
    public void startIndexing() {
        List<Site> sites = sitesList.getSites();
        int core = Runtime.getRuntime().availableProcessors();
        ExecutorService service = Executors.newFixedThreadPool(core);
        Path path = Path.of("fileDocument/");
        if (!Files.exists(path)) {
            try {
                Files.createDirectory(path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        for (Site site : sites) {
            String name = site.getName();
            String url = site.getUrl();
            SiteModel siteModel = saveDB(name, url);
            service.execute(() -> new SiteIndexing(url, name, siteModel, repositorySite).indexing());
        }
    }

    public void stopIndexing() {

    }


    public SiteModel saveDB(String name, String url) {
        if (repositorySite.findByUrl(url) != null) {
            log.info("delete site {}", url);
            SiteModel siteModel = repositorySite.findByUrl(url);
            repositorySite.delete(siteModel);
        }
        SiteModel siteModel = new SiteModel();
        siteModel.setStatus(Status.INDEXING);
        siteModel.setStatusTime(Instant.now());
        siteModel.setUrl(url);
        siteModel.setName(name);
        return repositorySite.save(siteModel);
    }
}

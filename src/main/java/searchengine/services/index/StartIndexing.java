package searchengine.services.index;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.model.PageModel;
import searchengine.model.SiteModel;
import searchengine.model.Status;
import searchengine.repository.RepositoryPage;
import searchengine.repository.RepositorySite;
import searchengine.services.index.site.SiteIndexing;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@RequiredArgsConstructor
public class StartIndexing {
    private final SitesList sitesList;
    private final RepositorySite repositorySite;
    private final RepositoryPage repositoryPage;
    public boolean startYes = false;
    public static int core = Runtime.getRuntime().availableProcessors();
    private ExecutorService service = Executors.newFixedThreadPool(core);
    private Map<String, AtomicBoolean> stopFlags = new ConcurrentHashMap<>();

    @Transactional
    public void startIndexing() {
        startYes = true;
        log.info("Starting indexing");
        if (service.isShutdown()) {
            service = Executors.newFixedThreadPool(core);
        }
        List<Site> sites = sitesList.getSites();

        Path pathDocument = Path.of("fileDocument/");
        if (!Files.exists(pathDocument)) {
            try {
                Files.createDirectory(pathDocument);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        Path pathSiteMap = Path.of("fileSiteMap/");
        if (!Files.exists(pathSiteMap)) {
            try {
                Files.createDirectory(pathSiteMap);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        for (Site site : sites) {
            String name = site.getName();
            String url = site.getUrl();
            SiteModel siteModel = saveDB(name, url);
            AtomicBoolean stopFlag = new AtomicBoolean(false);
            stopFlags.put(url, stopFlag);
            SiteIndexing siteIndexing = new SiteIndexing(url, name, siteModel, repositorySite, repositoryPage, stopFlag);
            service.execute(() -> {
                try {
                    siteIndexing.indexing();
                } catch (Exception e) {
                    log.error("Error indexing site {}", url, e);
                    siteModel.setStatus(Status.FAILED);
                    siteModel.setLastError(e.getMessage());
                    repositorySite.save(siteModel);
                }
            });
        }
    }

    public void stopIndexing() {
        log.info("Trying to stop indexing");
        if (!startYes) {
            log.info("Indexing was not started");
        } else {
            for (AtomicBoolean flag : stopFlags.values()) {
                flag.set(true);
            }
            service.shutdownNow();
            startYes = false;
        }
    }

    public SiteModel saveDB(String name, String url) {
        if (repositorySite.findByUrl(url) != null) {
            log.info("Deleting site {}", url);
            SiteModel siteModel = repositorySite.findByUrl(url);
            List<PageModel> pageModel = repositoryPage.findBySiteId(siteModel.getId());
            repositoryPage.deleteAll(pageModel);
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
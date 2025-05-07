package searchengine.services.index.site;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import searchengine.model.SiteModel;
import searchengine.model.Status;
import searchengine.repository.RepositoryPage;
import searchengine.repository.RepositorySite;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.System.currentTimeMillis;
import static searchengine.services.index.StartIndexing.core;

@RequiredArgsConstructor
@Slf4j
public class SiteIndexing {

    private final String url;
    private final String name;
    private final SiteModel siteModel;
    private final RepositorySite repositorySite;
    private final RepositoryPage repositoryPage;
    private final AtomicBoolean stopFlag;
    private ForkJoinPool forkJoinPool = new ForkJoinPool(core);
    private final CopyOnWriteArrayList<String> visitedUrls = new CopyOnWriteArrayList<>();

    public void indexing() throws InterruptedException {
        AtomicLong startOfTime = new AtomicLong();
        String pathFile = "fileSiteMap/";
        String nameFile = name.replaceAll("\\.", "_");
        log.info("Indexing site: {} - {}", name, url);
        MapSite recursiveTask = new MapSite(url, siteModel, repositoryPage, stopFlag, visitedUrls);
        startOfTime.set(System.currentTimeMillis());

        try {
            String fullURL = forkJoinPool.invoke(recursiveTask);
            if (stopFlag.get()) {
                siteModel.setStatus(Status.FAILED);
                siteModel.setLastError("Indexing stopped");
                repositorySite.save(siteModel);
                return;
            }
            fileWriter(fullURL, pathFile, nameFile, startOfTime);
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            siteModel.setStatus(Status.FAILED);
            siteModel.setLastError("Indexing interrupted");
            repositorySite.save(siteModel);
            throw e;
        }
    }

    public void fileWriter(String string, String pathFile, String nameFile, AtomicLong startOfTime) {
        log.info("Writing file for {}", name);
        String pathName = pathFile.concat(nameFile).concat(".txt");
        File file = new File(pathName);
        try (PrintWriter writer = new PrintWriter(file)) {
            writer.write(string);
            writer.flush();
            siteModel.setStatus(Status.INDEXED);
            siteModel.setStatusTime(Instant.now());
            repositorySite.save(siteModel);
            long allTimeStop = (currentTimeMillis() - startOfTime.get()) / 1_000;
            log.info("Completed writing site structure for {} in {} seconds: {}", url, allTimeStop, file.getName());
        } catch (FileNotFoundException e) {
            log.error("Error writing sitemap for {}", name, e);
            siteModel.setStatus(Status.FAILED);
            siteModel.setLastError("Error writing sitemap: " + e.getMessage());
            repositorySite.save(siteModel);
        }
    }
}
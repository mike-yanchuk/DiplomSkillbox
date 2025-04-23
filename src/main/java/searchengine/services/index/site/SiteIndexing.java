package searchengine.services.index.site;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import searchengine.model.SiteModel;
import searchengine.model.Status;
import searchengine.repository.RepositorySite;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.System.currentTimeMillis;


@RequiredArgsConstructor
@Slf4j
public class SiteIndexing  {

    private final String url;
    private final String name;
    private final SiteModel siteModel;
    private final RepositorySite repositorySite;

    public void indexing() {
        AtomicLong startOfTime = new AtomicLong();
        String pathFile = "fileSiteMap/";
        String nameFile = name.replaceAll("\\.", "_");
        log.info("Indexing site: {} - {}", name, url);
        MapSite recursiveTask = new MapSite(url, siteModel.getId());
        int core = Runtime.getRuntime().availableProcessors();
        startOfTime.set(System.currentTimeMillis());
        String fullURL = new ForkJoinPool(core).invoke(recursiveTask);
        System.out.println();
        fileWriter(fullURL, pathFile, nameFile, startOfTime);
    }

    @SneakyThrows
    public void fileWriter(String string, String pathFile, String nameFile, AtomicLong startOfTime) {
        log.info("Запись файла {}", name);
        siteModel.setStatus(Status.INDEXED);
        siteModel.setStatusTime(Instant.now());
        repositorySite.save(siteModel);
        String pathName = pathFile.concat(nameFile).concat(".txt");
        File file = new File(pathName);

        PrintWriter writer = null;
        try {
            writer = new PrintWriter(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        writer.write(string);
        writer.flush();

        long allTimeStop = (currentTimeMillis() - startOfTime.get()) / 1_000;

        log.info("Выполнена запись структуры сайта {} за {} секунд: {}", url, allTimeStop, file.getName());
    }

}





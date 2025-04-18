package searchengine.services.index;

import lombok.SneakyThrows;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.System.currentTimeMillis;

public class SiteIndexing {
    public SiteIndexing(String name, String url) {
        this.url = url;
        this.name = name;
    }


    public static String name;
    public static String url;
    public static String nameFile = "skillbox_ru";
    public static final StringBuilder stringBuilder = new StringBuilder();
    public static final String pathFile = "filesSiteMap/";
    public static AtomicLong startOfTime = new AtomicLong();


    @lombok.SneakyThrows
    public static void main(String[] args) {
        MapSite recursiveTask = new MapSite(url);
        int core = Runtime.getRuntime().availableProcessors();
        startOfTime.set(System.currentTimeMillis());
        String fullURL = new ForkJoinPool(20).invoke(recursiveTask);
        System.out.println();

        fileWriter(fullURL);
    }

    @SneakyThrows
    public static void fileWriter(String string) {
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

        System.out.printf("Выполнена запись структуры сайта %s за %d секунд: %s%n", url, allTimeStop, file.getName());
    }

}

package searchengine.services.index.site;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.RecursiveTask;

@Slf4j
public class MapSite extends RecursiveTask<String> {
    private String path;
    private static final CopyOnWriteArrayList<String> WRITE_ARRAY_LIST = new CopyOnWriteArrayList<>();
    private static final String CSS_QUERY = "a[href]";
    private static final String ATTRIBUTE_KEY = "href";
    private static final String pathDocument = "fileDocument/";


    public MapSite(String path) {
        this.path = path.trim();
    }


    @Override
    protected String compute() {

        String stringUtils = StringUtils.repeat('\t',
                path.lastIndexOf("/") != path.length() - 1
                        ? StringUtils.countMatches(path, "/") - 2
                        : StringUtils.countMatches(path, "/") - 3);

        StringBuffer sb = new StringBuffer(String.format("%s%s%s", stringUtils, path, System.lineSeparator()));
        List<MapSite> mapSites = new CopyOnWriteArrayList<>();
        Document doc;
        Elements elements;

        try {
            Thread.sleep(150);
            doc = Jsoup.connect(path).ignoreContentType(true).userAgent("Mozilla/5.0").get();
            elements = doc.select(CSS_QUERY);
            for (Element element : elements) {
                String attributeUrl = element.absUrl(ATTRIBUTE_KEY);
                if (!attributeUrl.isEmpty() && attributeUrl.startsWith(this.path)
                        && !WRITE_ARRAY_LIST.contains(attributeUrl)
                        && !attributeUrl.contains("#")) {
                    MapSite linkExecutor = new MapSite(attributeUrl);
                    writeDocument(doc);
                    linkExecutor.fork();
                    mapSites.add(linkExecutor);
                    WRITE_ARRAY_LIST.add(attributeUrl);

                }

            }
        } catch (InterruptedException | IOException e) {
            Thread.currentThread().interrupt();
        }

        mapSites.sort(Comparator.comparing((MapSite o) -> o.path));
        int allTasksSize = mapSites.size();
        for (int i = 0; i < allTasksSize; i++) {
            MapSite link = mapSites.get(i);
            sb.append(link.join());
        }
        return sb.toString();
    }

    public void writeDocument(Document doc) {

        String information = doc.outerHtml();
        String name = path.substring(path.indexOf("//") + 2)
                .replace("\\", "")  // Удаляем двоеточия
                .replace("/", "_")
                .replace("<", "")
                .replace(">", "")
                .replace("*", "")
                .replace("|", "")
                .replace(" ", "_")
                .replace("\"", "")
                .replace(":", "")
                .replace("?", "")
                .replace(".", "_")
                // Заменяем пробелы на подчёркивания
                + ".txt";
        if (!Files.exists(Path.of(pathDocument))) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        File file = new File(pathDocument + name);
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(file);
        } catch (FileNotFoundException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }

        writer.write(information);
        writer.flush();

    }
}

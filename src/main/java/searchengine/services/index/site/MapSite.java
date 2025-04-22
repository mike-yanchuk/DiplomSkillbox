package searchengine.services.index.site;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
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



    public MapSite(String path) {
        this.path = path.trim();
    }


    @Override
    protected String compute() {
        log.info("Compute MapSite..." + path);
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
}

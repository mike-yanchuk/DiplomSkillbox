package searchengine.services.index.site;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import searchengine.model.PageModel;
import searchengine.model.SiteModel;
import searchengine.repository.RepositoryPage;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class MapSite extends RecursiveTask<String> {
    private String path;
    private SiteModel siteModel;
    private RepositoryPage repositoryPage;
    private AtomicBoolean stopFlag;
    private CopyOnWriteArrayList<String> visitedUrls;

    private static final String CSS_QUERY = "a[href]";
    private static final String ATTRIBUTE_KEY = "href";

    public MapSite(String path, SiteModel siteModel, RepositoryPage repositoryPage, AtomicBoolean stopFlag, CopyOnWriteArrayList<String> visitedUrls) {
        this.path = path.trim();
        this.siteModel = siteModel;
        this.repositoryPage = repositoryPage;
        this.stopFlag = stopFlag;
        this.visitedUrls = visitedUrls;
    }

    @Override
    protected String compute() {
        if (stopFlag.get()) {
            return "";
        }

        String stringUtils = StringUtils.repeat('\t',
                path.lastIndexOf("/") != path.length() - 1
                        ? StringUtils.countMatches(path, "/") - 2
                        : StringUtils.countMatches(path, "/") - 3);

        StringBuffer sb = new StringBuffer(String.format("%s%s%s", stringUtils, path, System.lineSeparator()));
        List<MapSite> mapSites = new CopyOnWriteArrayList<>();
        Document doc;
        Elements elements;

        try {
            if (stopFlag.get()) {
                return "";
            }
            Thread.sleep(150);
            if (stopFlag.get()) {
                return "";
            }
            doc = Jsoup.connect(path).ignoreContentType(true)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .referrer("http://www.google.com")
                    .get();
            elements = doc.select(CSS_QUERY);
            for (Element element : elements) {
                if (stopFlag.get()) {
                    break;
                }
                String attributeUrl = element.absUrl(ATTRIBUTE_KEY);
                if (!attributeUrl.isEmpty() && attributeUrl.startsWith(this.path)
                        && !visitedUrls.contains(attributeUrl)
                        && !attributeUrl.contains("#")) {
                    MapSite linkExecutor = new MapSite(attributeUrl, siteModel, repositoryPage, stopFlag, visitedUrls);
                    writeDocument(doc, path, siteModel);
                    log.info("site parse " + path);
                    linkExecutor.fork();
                    mapSites.add(linkExecutor);
                    visitedUrls.add(attributeUrl);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "";
        } catch (IOException e) {
            log.error("IOException while parsing " + path, e);
        }

        mapSites.sort(Comparator.comparing((MapSite o) -> o.path));
        int allTasksSize = mapSites.size();
        for (int i = 0; i < allTasksSize; i++) {
            MapSite link = mapSites.get(i);
            sb.append(link.join());
        }
        return sb.toString();
    }

    public void writeDocument(Document doc, String path, SiteModel siteModel) {
        String information = doc.outerHtml();
        PageModel pageModel = new PageModel();
        pageModel.setPath(path);
        pageModel.setContent(information);
        pageModel.setCode(200);
        pageModel.setSite(siteModel);

        repositoryPage.save(pageModel);
    }
}
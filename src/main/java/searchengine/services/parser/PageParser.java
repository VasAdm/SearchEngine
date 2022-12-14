package searchengine.services.parser;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import searchengine.model.page.PageEntity;
import searchengine.model.page.PageEntityWithDoc;
import searchengine.model.site.SiteEntity;

import java.io.IOException;

public class PageParser {

    public static PageEntityWithDoc parsePage(String url, SiteEntity site) {
        PageEntityWithDoc pageWithDoc = new PageEntityWithDoc();
        PageEntity page = new PageEntity();
        Document document = null;

        int delay = (int) (Math.random() * ((2000 - 500) + 1)) + 500;
        try {
            Thread.sleep(delay);

            Connection.Response response = Jsoup.connect(url).maxBodySize(0).userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6").referrer("https://www.google.com").ignoreHttpErrors(true).ignoreContentType(true).execute();

            String contentType = response.contentType();

            assert contentType != null;
            if (contentType.contains("text/")) {
                int statusCode = response.statusCode();

                document = response.parse();

                page.setContent(document.toString());
                if (url.equals(site.getUrl())) {
                    page.setPath("/");
                } else {
                    page.setPath(url.substring(site.getUrl().length() - 1));
                }

                page.setCode(statusCode);
                page.setSite(site);
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        pageWithDoc.setPage(page);
        pageWithDoc.setDocument(document);

        return pageWithDoc;
    }
}

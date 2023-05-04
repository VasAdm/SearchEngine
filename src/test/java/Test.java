import searchengine.services.lemmasScraper.LemmasScraper;

import java.io.IOException;
import java.util.Map;

public class Test {
    public static void main(String[] args) throws IOException {
        LemmasScraper lemmasScraper = LemmasScraper.getInstance();
        String text = "Повторное появление леопарда в Осетии позволяет предположить,\n" +
                "что леопард постоянно обитает в некоторых районах Северного\n" +
                "Кавказа.";
        Map<String, Integer> result = lemmasScraper.collectLemmas(text);
        result.forEach((k, v) -> System.out.println(k + " - " + v));
    }
}

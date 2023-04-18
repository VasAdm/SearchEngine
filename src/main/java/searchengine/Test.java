package searchengine;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import searchengine.dto.indexing.IndexingStatusResponseError;
import searchengine.services.lemmasScraper.LemmasScraper;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Service;
import searchengine.repository.SiteRepository;

import java.io.IOException;

@EnableJpaRepositories("searchengine.repository")
public class Test {
    public static void main(String[] args) throws IOException {
//        LemmasScraper lemmasScraper = LemmasScraper.getInstance();
//        String text = "Повторное появление леопарда в Осетии позволяет предположить,\n" +
//                "что леопард постоянно обитает в некоторых районах Северного\n" +
//                "Кавказа.";
//        Map<String, Integer> result = lemmasScraper.collectLemmas(text);
//        result.forEach((k, v) -> System.out.println(k + " - " + v));

        Repo repo = new Repo();
        repo.print();

    }

    @Service
    @EnableJpaRepositories("searchengine.repository")
    public static class Repo {
        @Autowired
        private SiteRepository siteRepository;

        public void print() {
            siteRepository.findAll().forEach(System.out::println);
        }
    }

}

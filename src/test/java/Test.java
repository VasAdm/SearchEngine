import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Service;
import searchengine.repository.SiteRepository;
import searchengine.services.parsing.HtmlParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
//        repo.print();
//        LemmasScraper lemmasScraper = LemmasScraper.getInstance();


        Path path = Paths.get("./index.html");

        try {

            List<String> contents = Files.readAllLines(path);
            StringBuilder stringBuilder = new StringBuilder();

            //Read from the stream
            for (String content : contents) {//for each line of content in contents
                stringBuilder.append(content).append(System.lineSeparator());
//                System.out.println(content);// print the line
            }
            String res = stringBuilder.toString().replaceAll("<[^>]*>", "");
            res = res.replaceAll("\\s+", " ");


//            System.out.println(stringBuilder);
            System.out.println(res);



        } catch (IOException ex) {
            ex.printStackTrace();//handle exception here
        }


    }

    public static String cleaner(List<String> textList) {
        List<String> preResult =  textList.stream()
                .map(s -> s.split("<?\\S\\w+>"))
                .flatMap(Arrays::stream)
                .filter(s -> !s.isEmpty())
//                .map(String::trim)
                .toList();

        StringBuilder builder = new StringBuilder();
        preResult.forEach(s -> {
            builder.append(s).append(System.lineSeparator());
        });
        return builder.toString();
    }


    @Service
    @EnableJpaRepositories("searchengine.repository")
    public static class Repo {
        @Autowired
        private SiteRepository siteRepository;

        public void print() {
            siteRepository.findAll().forEach(System.out::println);
        }

        public SiteRepository getSiteRepository() {
            return siteRepository;
        }
    }




}

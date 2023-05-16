import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.io.IOException;

@EnableJpaRepositories("searchengine.repository")
public class Test {
    public static void main(String[] args) {
//        LemmasScraper lemmasScraper = LemmasScraper.getInstance();
//        String text = "Повторное появление леопарда в Осетии позволяет предположить,\n" +
//                "что леопард постоянно обитает в некоторых районах Северного\n" +
//                "Кавказа.";
//        Map<String, Integer> result = lemmasScraper.collectLemmas(text);
//        result.forEach((k, v) -> System.out.println(k + " - " + v));

//        Repo repo = new Repo();
//        repo.print();
//        LemmasScraper lemmasScraper = LemmasScraper.getInstance();


    }

//    public static String cleaner(List<String> textList) {
//        List<String> preResult =  textList.stream()
//                .map(s -> s.split("<?\\S\\w+>"))
//                .flatMap(Arrays::stream)
//                .filter(s -> !s.isEmpty())
////                .map(String::trim)
//                .toList();
//
//        StringBuilder builder = new StringBuilder();
//        preResult.forEach(s -> {
//            builder.append(s).append(System.lineSeparator());
//        });
//        return builder.toString();
//    }
//
//
//    @Service
//    @EnableJpaRepositories("searchengine.repository")
//    public static class Repo {
//        @Autowired
//        private SiteRepository siteRepository;
//
//        public void print() {
//            siteRepository.findAll().forEach(System.out::println);
//        }
//
//        public SiteRepository getSiteRepository() {
//            return siteRepository;
//        }
//    }


}

package searchengine.config;

import io.swagger.annotations.Api;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Tag;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.ArrayList;

@Configuration
public class SpringfoxConfig {

    public static final String SEARCHENGINE_TAG = "API";

    @Bean
    public Docket docket() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.withClassAnnotation(Api.class))
                .paths(PathSelectors.ant("/api/*"))
                .build()
                .tags(new Tag(SEARCHENGINE_TAG, "Api for search engine"))
                .apiInfo(apiInfo());
    }

    public ApiInfo apiInfo() {
        return new ApiInfo(
                "SearchEngine API",
                "API for SearchEngine",
                "1.0",
                "http://www.termsofservice.org",
                new Contact("Vasiliy Ershov", "http://www.owner.com", "ershovvasiliy174@gmail.com"),
                "api_license",
                "http://www.license.edu.org",
                new ArrayList<>()
        );
    }
}

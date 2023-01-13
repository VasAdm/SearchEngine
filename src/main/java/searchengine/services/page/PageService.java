package searchengine.services.page;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.model.page.PageEntity;
import searchengine.repository.PageRepository;

import java.util.List;

@Service
public class PageService {
    private final PageRepository pageRepository;

    @Autowired
    public PageService(PageRepository pageRepository) {
        this.pageRepository = pageRepository;
    }

    public PageEntity save(PageEntity page) {
        return pageRepository.save(page);
    }

    public Iterable<PageEntity> saveAll(List<PageEntity> pagesList) {
        return pageRepository.saveAll(pagesList);
    }

    public void deleteAll() {
        pageRepository.deleteAll();
    }
}

package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.PageModel;
import searchengine.model.SiteModel;

import java.util.List;

public interface RepositoryPage extends JpaRepository<PageModel, Integer> {

    @Transactional
    List<PageModel> findBySiteId(Integer siteId);

}

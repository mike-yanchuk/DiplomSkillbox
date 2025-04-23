package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.SiteModel;

public interface RepositorySite extends JpaRepository<SiteModel, Integer> {

    @Transactional
    SiteModel findByUrl(String url);
}

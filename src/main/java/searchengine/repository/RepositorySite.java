package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.SiteModel;
import searchengine.model.Status;
import java.util.List;

@Repository
public interface RepositorySite extends JpaRepository<SiteModel, Integer> {
    SiteModel findByUrl(String url);
    List<SiteModel> findByStatus(Status status);
}
package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import searchengine.model.PageModel;

public interface RepositoryPage extends JpaRepository<PageModel, Integer> {


}

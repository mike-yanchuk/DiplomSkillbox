package searchengine.model;


import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class Index {

    @Id
    private int id;

    @Column(name = "page_id")
    private int pageId;

    @Column(name = "lemma_id")
    private int lemmaId;

    private float rank;

//id INT NOT NULL AUTO_INCREMENT;
//page_id INT NOT NULL — идентификатор страницы;
//lemma_id INT NOT NULL — идентификатор леммы;
//rank FLOAT NOT NULL — количество данной леммы для данной страницы.


}

package searchengine.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;


@Entity
@Getter
@Setter
public class Lemma {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private int site_id;

    private String lemma;

    private int frequency;


//    id INT NOT NULL AUTO_INCREMENT;
//    site_id INT NOT NULL — ID веб-сайта из таблицы site;
//    lemma VARCHAR(255) NOT NULL — нормальная форма слова (лемма);
//    frequency INT NOT NULL — количество страниц, на которых слово встречается хотя бы один раз.
//    Максимальное значение не может превышать общее количество слов на сайте.

}

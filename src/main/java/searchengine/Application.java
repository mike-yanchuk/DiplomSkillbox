package searchengine;


import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import searchengine.services.lemmacheck.Lemma;
import searchengine.services.lemmacheck.LemmaFinder;

import java.io.IOException;
import java.util.List;

@EntityScan(basePackages = "searchengine.model")
@SpringBootApplication
public class Application {
    public static void main(String[] args) throws IOException {
        LemmaFinder lemmaFinder = new LemmaFinder(new RussianLuceneMorphology());
        String line = "              Повторное появление леопарда в Осетии позволяет предположить," +
                "что леопард постоянно обитает в некоторых районах Северного " +
                "Кавказа.                     ";

        Lemma lemma = new Lemma();
        lemma.takeLemma(line);


    }

}

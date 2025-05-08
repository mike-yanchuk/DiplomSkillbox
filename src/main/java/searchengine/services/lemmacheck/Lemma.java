package searchengine.services.lemmacheck;

import jdk.swing.interop.SwingInterOpUtils;
import lombok.SneakyThrows;
import org.apache.lucene.morphology.Heuristic;
import org.apache.lucene.morphology.LetterDecoderEncoder;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lemma {
    LuceneMorphology luceneMorph;
    private static final String[] particlesNames = new String[]{"МЕЖД", "ПРЕДЛ", "СОЮЗ"};

    {
        try {
            luceneMorph = new RussianLuceneMorphology();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String[] listWord(String line) {
        line = line.trim().toLowerCase().replaceAll("[,.]|\\s+", " ");
        System.out.println(line);

        String[] words = line.split("\\s+");

        return words;
    }


    @SneakyThrows
    public void takeLemma(String line) {

        if (!line.contains(" ")) {
            return;
        }

        Map<String, Integer> wordsCount = new HashMap<>();
        String[] words = listWord(line);

        for (String word : words) {
            boolean accses = false;
            if (word.length() < 5) {
                if (getMorphInfo(word)) {
                    accses = true;
                }
            }
            if (!accses) {
                String parse = parseLemma(word);
                if (wordsCount.containsKey(parse)) {

                    wordsCount.put(parse, wordsCount.getOrDefault(parse, 1) + 1);
                } else {
                    wordsCount.put(parse, wordsCount.getOrDefault(parse, 1));
                }
            }

        }
        for (Map.Entry<String, Integer> wordCount : wordsCount.entrySet()) {
            System.out.println(wordCount.getKey() + " - " + wordCount.getValue());
        }

    }

    public String parseLemma(String line) {
        List<String> wordBaseForms =
                luceneMorph.getNormalForms(line);
        return wordBaseForms.get(0);
    }

    private boolean hasParticleProperty(String wordBase) {
        for (String property : particlesNames) {
            if (wordBase.toUpperCase().contains(property)) {
                return true;
            }
        }
        return false;
    }


    public boolean getMorphInfo(String s) {
        List<String> wordMorphForm = luceneMorph.getMorphInfo(s);
        return wordMorphForm.stream().anyMatch(this::hasParticleProperty);
    }

}

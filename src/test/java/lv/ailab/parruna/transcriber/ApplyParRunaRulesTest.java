package lv.ailab.parruna.transcriber;

import lv.semti.morphology.analyzer.Analyzer;
import lv.semti.morphology.analyzer.Wordform;
import org.junit.Test;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ApplyParRunaRulesTest {

    public List<Wordform> generateWordforms(String word, int paradigm) throws Exception {
        Analyzer a = new Analyzer();
        return a.generateInflectionsFromParadigm(word, paradigm);
    }


    @Test
    public void csvReadTest() throws IOException {
        ApplyParRunaRules a = new ApplyParRunaRules();
    }


    @Test
    public void rulesApplicationTest() throws IOException {
        ApplyParRunaRules a = new ApplyParRunaRules();
        a.applyRules("slazds");
        a.applyRules("mežs");
        a.applyRules("kooperatīvs");
//        assertEquals("slasc", a.applyRules("slazds"));
    }


    @Test
    public void rootPronunciationTest() throws Exception {
        ApplyParRunaRules a = new ApplyParRunaRules();
        String lemma = "slazds";
        String root = "slazd";
        String pronunciation = "slasc";

        assertEquals("slazd", a.generateRootPronunciation(lemma, root, pronunciation));
    }



}
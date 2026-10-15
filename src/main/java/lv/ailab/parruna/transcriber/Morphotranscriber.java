package lv.ailab.parruna.transcriber;

import java.io.IOException;
import java.util.List;
import lv.semti.morphology.analyzer.Wordform;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Morphotranscriber {
    public Morphotranscriber() {

    }

    public void transcribe(List<Wordform> wfs, Wordform lemma, String lemma_pronunciation) throws IOException {
        // TODO: Iterate over wordform and get each wordforms pronunciation
        //  understand how best to structure the functions in the java project
        //  probably a separate the rules based pronunciation generation in a separate function
        // This should just call functions from elsewhere and return the wfs w/ appended pronunciations
        // Actually no the initialization should just set up the object and then call a a
        // different function defined in this file that does the call and return logic

        ApplyParRunaRules rules = new ApplyParRunaRules();
        for (Wordform wf: wfs) {
            String wf_pron = rules.generateWordformPronunciation(wf, lemma, lemma_pronunciation);
        }
    }
}
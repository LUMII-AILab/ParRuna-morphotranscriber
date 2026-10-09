package lv.ailab.parruna.transcriber;

import java.util.List;
import lv.semti.morphology.analyzer.Wordform;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Morphotranscriber {
    private final List<Wordform> wfs;
    private final String lemma;
    private final String lemma_pronunciation;

    public Morphotranscriber(List<Wordform> wfs, String lemma, String lemma_pronunciation) {
        // TODO: Iterate over wordform and get each wordforms pronunciation
        //  understand how best to structure the functions in the java project
        //  probably a separate the rules based pronunciation generation in a separate function
        // This should just call functions from elsewhere and return the wfs w/ appended pronunciations
        // Actually no the initialization should just set up the object and then call a a
        // different function defined in this file that does the call and return logic
        this.wfs = wfs;
        this.lemma = lemma;
        this.lemma_pronunciation = lemma_pronunciation;
    }

    public void morphotranscribe() {
        for (Wordform wf: this.wfs) {

        }
    }
}
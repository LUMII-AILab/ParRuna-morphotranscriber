package lv.ailab.parruna.transcriber;

import lv.semti.morphology.analyzer.Wordform;

public class InflectWordform {
    private final Wordform wf;
    private final String lemma;
    private final String lemma_pronunciation;

    public InflectWordform(Wordform wf, String lemma, String lemma_pronunciation) {
        this.wf = wf;
        this.lemma = lemma;
        this.lemma_pronunciation = lemma_pronunciation;
    }

    private String generateInflection() {
        // Atšķirībā no ParRunas likumbāzētā transkribētāja, kur izrunas speciāli norādīja kvadrātiekavās
        // šeit izruna ir dota precīzi visai saknei
        // Attiecīgi jāizgūst jebkādas modifikācijas saknes izrunai un tad

        // Example:
        //  vārds: cerēdams
        //  lemma_pron: cerē=t

        // Verbiem piemēram nav pieejama vārda sakne




        return "test";
    }
}

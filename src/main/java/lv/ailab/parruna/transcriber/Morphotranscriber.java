package lv.ailab.parruna.transcriber;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lv.semti.morphology.analyzer.Wordform;
import lv.semti.morphology.lexicon.Paradigm;
import lv.semti.morphology.lexicon.StemType;


public class Morphotranscriber {
    ApplyParRunaRules rules;
    NotationConverter notations;


    public Morphotranscriber() throws IOException
    {
        rules = new ApplyParRunaRules();
        notations = new NotationConverter();
    }

    /**
     * TODO: add more description?
     * Returns cloned list of wordforms with added LVMFA pronunciation in
     * `AttributeNames.i_Pronunciation` attribute or null, if the paradigm is
     * not unsupported yet.
     */
    public List<Wordform> transcribe(
            List<Wordform> wfs, String lemma, String lemma_pronunciation, Paradigm paradigm) {
        // Beware: sometimes null stems can happen, sometimes either stem2 or
        // stem3 can be null, but not both. /Lauma
        if (wfs == null)
            return null;
        if (paradigm.name.endsWith("-ltg"))
            return null; // Latgalian not supported as of now.
        // TODO: Iterate over wordform and get each wordforms pronunciation
        //  understand how best to structure the functions in the java project
        //  probably a separate the rules based pronunciation generation in a separate function
        // This should just call functions from elsewhere and return the wfs w/ appended pronunciations
        // Actually no the initialization should just set up the object and then call a a
        // different function defined in this file that does the call and return logic

        if (lemma_pronunciation == null) {
            lemma_pronunciation = rules.applyRules(lemma);
        }

        List<Wordform> result = new ArrayList<>();
        String lemma_root_pron = rules.generateRootPronunciation(lemma, wfs.get(0).lexeme.getStem(StemType.STEM1), lemma_pronunciation);
        for (Wordform wf: wfs) {
            String wf_pron = rules.generateWordformPronunciation(wf, lemma_root_pron);
            // I'm not completely sure, what is better – to clone or not, but
            // this seems a smidge safer for various future uses. /Lauma
            Wordform edited = (Wordform) wf.clone();
            edited.addAttribute(AttributeNames.i_Pronunciation, wf_pron);
            result.add(edited);
        }
        return result;
    }

    // TODO: Add toggle for intonation data in external formats (i.e. LFA, IPA)
    /**
     * Converts internally used LVMFA type transcription to IPA.
     */
    public String lvmfaToIpa(String lvmfa)
    {
        return notations.lvmfaToIPA(lvmfa);
    }

    /**
     * Converts internally used LVMFA type transcription to LFA.
     */
    public String lvmfaToLfa(String lvmfa)
    {
        return notations.lvmfaToLFA(lvmfa);
    }
}
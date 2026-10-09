package lv.ailab.parruna.transcriber;

import lv.semti.morphology.analyzer.Wordform;
import lv.semti.morphology.attributes.AttributeNames;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ApplyParRunaRules {
    private static String rules_file = "/parruna/rules.csv";
    private static String metas_file = "/parruna/metas.csv";
    private final List<ParrunaRules> rules;
    private final List<ParrunaMetas> metas;

    public ApplyParRunaRules() throws IOException {
        this.metas = loadMetas(metas_file);
        this.rules = loadRules(rules_file);
    }


    private String generateWordformPronunciation(Wordform wf, String lemma_pronunciation) {
        // FIXME: Figure out how to limit the application of rules only onto
        //  non-root and modified root text.
        // TODO: First apply the special rules


        // TODO: Second apply basic rules


        return wf.getToken();
    }


    private List<ParrunaRules> loadRules(String filename) {
        List<ParrunaRules> rules = new ArrayList<>();

        try {
            InputStream input = ApplyParRunaRules.class.getResourceAsStream(filename);

            if (input == null) {
                throw new IOException("Could not find data.csv in resources");
            }

            CSVReader csv = new CSVReader(new InputStreamReader(input));
            csv.setTextQualifier('"');
            csv.setSeparator(',');

            csv.nextLine();
            String[] titles = csv.getTitles();

            String[] dataRow;
            String pattern_base = "(?<=(%s))(%s)(?=(%s))";

            while((dataRow = csv.nextLine()) != null){
                // TODO: Filter out the rules with tag/lemma conditions
                if (!Objects.equals(dataRow[4], "") || !Objects.equals(dataRow[5], "")) continue;

                String lookBehind = applyMetas(dataRow[0]);
                String coreMatch = dataRow[1];
                String lookAhead = applyMetas(dataRow[2]);
                String coreReplace = dataRow[3];

                String pattern = String.format(pattern_base, lookBehind, coreMatch, lookAhead);
                Pattern p = Pattern.compile(pattern);
                ParrunaRules r = new ParrunaRules(p, coreReplace, coreMatch.length());

                rules.add(r);
            }
            csv.close();

        } catch (IOException e) {
            System.out.println("Error reading file" + e);
        }

        rules.sort(Comparator.comparingInt(ParrunaRules::getReplace_len).reversed());

        return rules;
    }


    private List<ParrunaMetas> loadMetas(String filename) {
        List<ParrunaMetas> metas = new ArrayList<>();

        try {
            InputStream input = ApplyParRunaRules.class.getResourceAsStream(filename);

            if (input == null) {
                throw new IOException("Could not find data.csv in resources");
            }

            CSVReader csv = new CSVReader(new InputStreamReader(input));
            csv.setTextQualifier('"');
            csv.setSeparator(',');

            csv.nextLine();
            String[] titles = csv.getTitles();

            String[] dataRow;

            while((dataRow = csv.nextLine()) != null){
                ParrunaMetas m = new ParrunaMetas(dataRow[1], dataRow[2]);
                metas.add(m);
            }

            csv.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return metas;
    }


    public String applyRules(String base) {
        // FIXME: The reason why this doesn't work is because the (<klase>)
        //  defined custom classes are not being converted to their Regex expressions
        StringBuilder sb = new StringBuilder(base);

        int pos = sb.length() - 1;
        while (pos >= 0) {
            boolean replaced = false;

            for (ParrunaRules rule : this.rules) {
                Matcher matcher = rule.getPattern().matcher(sb);

                int matchStart = -1;
                int matchEnd = -1;
                // Find matches that end at or before pos + 1
                while (matcher.find() && matcher.end() <= pos + 1) {
                    if (matcher.end() == pos + 1) {
                        matchStart = matcher.start();
                        matchEnd = matcher.end();
                    }
                }

                if (matchStart >= 0) {
//                    System.out.println(sb.substring(matchStart, matchEnd)+" -> "+rule.getReplace_value());
                    sb.replace(matchStart, matchEnd, rule.getReplace_value());
                    pos = matchStart - 1;
                    replaced = true;
                    break;
                }
            }
            if (!replaced) {
                pos--;
            }
        }
        System.out.println(sb);
        return sb.toString();
    }


    private String applyMetas(String base) {
        StringBuilder sb = new StringBuilder(base);

        for (ParrunaMetas meta: this.metas) {
            Pattern pattern = Pattern.compile("<"+meta.getMetaName()+">");
            Matcher matcher = pattern.matcher(sb);

            int matchStart = -1;
            int matchEnd = -1;

            while (matcher.find()) {
                matchStart = matcher.start();
                matchEnd = matcher.end();
            }

            if (matchStart >= 0) {
//                System.out.println(sb.substring(matchStart, matchEnd)+" -> "+meta.getMetaPattern());
                sb.replace(matchStart, matchEnd, meta.getMetaPattern());
                break;
            }
        }
        return sb.toString();
    }

    private String generateRootPronunciation() {
        // TODO: List all variants, when base ending modifies root pronunciation.
        //  then take off ending
        // TODO: Use the resources.parruna.rules.csv in the opposite direction to get the base root pronunciation.
        //  i.e. if taking the ending off would not trigger a rule,
        //  then apply the rule in reverse to the lemma_ponunciation

        // List of changes that the ending may do to the root:
        // 1) Voicing, devoicing - must be extra careful as the changes here cascade back
        // 2) Partial vocalization - i^, u^
        // 3) Assimilation - zs, žs, ss, šs, ds, džs, cs, ts, čs

        return "test";
    }


    private String applySpecialRules(Wordform wf, String pronunciation) {
        // TODO: Šeit nevajag likumus, kas attiecas uz saknes izrunu, ja tā nemainās viscaur vārdformām
        // Uzsvara likums vispārākās pakāpes īpašības vārdiem
        if (wf.isMatchingStrong(AttributeNames.i_PartOfSpeech, AttributeNames.v_Adjective) &&
            wf.isMatchingStrong(AttributeNames.i_Degree, AttributeNames.v_Superlative)) {

        }
        // Uzsvara likums vispārākās pakāpes apstākļa vārdiem
        if (wf.isMatchingStrong(AttributeNames.i_PartOfSpeech, AttributeNames.v_Adverb) &&
                wf.isMatchingStrong(AttributeNames.i_Degree, AttributeNames.v_Superlative)) {
            // vis... -> %vis"...
        }
        // nestu, vestu, celtu, meklētu, smeltu, šķeltu, šķeltos u.c. --> darbības vārdu vēlējuma izteiksmes formas
        // mīlētu, vēlētu, vēlētos, zīmētu, zīmētos, sēdētu u.c. --> darbības vārdu vēlējuma izteiksmes formas
        if (wf.isMatchingStrong(AttributeNames.i_PartOfSpeech, AttributeNames.v_Verb) &&
                wf.isMatchingStrong(AttributeNames.i_Mood, AttributeNames.v_Conditional)) {
            // Šeit notiek /e/ mija uz /E/, un /ē/ mija uz /Ē/

        }
        // It kā šie likumi neattiecas uz {"lemt", "vemt", "liet", "riet", "smiet"}
        // Bet man šķiet, ka tas nav gluži pareizi
        // TODO: Lokāmais darāmās kārtas pagātnes divdabis ar galotni -is, izskaņu -usi
        // TODO: Lokāmais ciešamās kārtas tagadnes divdabis ar izskaņu -ams, -ama
        // TODO: Lokāmais ciešamās kārtas pagātnes divdabis ar izskaņu -ts, -ta
        // TODO: Daļēji lokāms divdabis ar izskaņu -dams, -dama

        return "test";
    }

}

package lv.ailab.parruna.transcriber;

import lv.semti.morphology.analyzer.Wordform;
import lv.semti.morphology.attributes.AttributeNames;
import lv.semti.morphology.lexicon.StemType;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ApplyParRunaRules {
    private static String rules_file = "/parruna/rules.csv";
    private static String metas_file = "/parruna/metas.csv";
    private final List<ParrunaRules> rules;
    private final List<ParrunaMetas> metas;

    public ApplyParRunaRules() throws IOException {
        this.metas = loadMetas(metas_file);
        this.rules = loadRules(rules_file);
    }


    public String generateWordformPronunciation(Wordform wf, String lemma, String lemma_pronunciation) {
        // FIXME: Figure out how to limit the application of rules only onto
        //  non-root and modified root text.
        // TODO: First apply the special rules
        String lemma_root_pronunciation = applySpecialRules(wf, lemma_pronunciation);

        // FIXME: Figure out how to get lemma root here - lemma.getValue("Pamatformas celms") might work?
        lemma_root_pronunciation = generateRootPronunciation(wf.getToken(), wf.lexeme.getStem(StemType.STEM1), lemma_pronunciation);

        // TODO: Figure out how to apply ParRuna rules partially to the word
        //  Wordform changes can affect the root pronunciation as well
        // FIXME: This assumes root doesn't get changed
        String a = lemma_root_pronunciation + applyRules(wf.getEnding().getEnding());


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

                ParrunaRules r = new ParrunaRules(lookBehind, coreMatch, lookAhead, coreReplace);
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

                if (matchStart >= 0) {
//                System.out.println(sb.substring(matchStart, matchEnd)+" -> "+meta.getMetaPattern());
                    sb.replace(matchStart, matchEnd, meta.getMetaPattern());
//                break;
                }
            }
        }
        return sb.toString();
    }


    private String getNaiveOrthography(String base) {
        StringBuilder sb = new StringBuilder(base);
        return sb.toString();
    }


    public String applyRulesToEnding(String ending) {
        // FIXME: Quick hacky way to ensure that string doesn't get interpreted as
        //  being at the start of a word for rules application purposes.
        String sample = applyRules("hhh"+ending);
        sample = sample.replace("h", "");
        return sample;
    }

    public String generateRootPronunciation(String lemma, String lemma_root, String lemma_pronunciation) {
        // List of changes that the ending may do to the root:
        // 1) Voicing, devoicing - must be extra careful as the changes here cascade back
        // 2) Partial vocalization - i^, u^
        // 3) Assimilation - zs, žs, ss, šs, ds, džs, cs, ts, čs

        StringBuilder lexeme_root_pronunciation;
         // Ja nav nekādas galotnes, tad lemmas izruna ir arī saknes izruna
        if (lemma.length() == lemma_root.length()) {
            return lemma_pronunciation;
        }

        String lexeme_ending = lemma.substring(lemma_root.length());
        if (lemma_pronunciation.endsWith(lexeme_ending)) {
            // Ja galotnes izruna jau ir tieši tāda pati fonētiskā pierakstā, tad vnk noņemam to
            // -s, -š, -t
            lexeme_root_pronunciation = new StringBuilder(lemma_pronunciation.substring(0, lemma_pronunciation.length() - lexeme_ending.length()));
        } else {
            // TODO: Pārbaudīt vai strādā visa veida galotnēm ar specsimboliem
            //  -u_ot, -ē=t, -ax
            String sample = applyRulesToEnding(lexeme_ending);

            String regex = sample.chars()
                    .mapToObj(c -> Pattern.quote(String.valueOf((char) c)))
                    .collect(Collectors.joining("[qx_=\\\\]*")) + "$";

            lexeme_root_pronunciation = new StringBuilder(lemma_pronunciation.replaceAll(regex, ""));
        }

        // 3) Assimilation - zs, žs, ss, šs, ds, džs, cs, ts, čs
        Matcher ts_ds_matcher = Pattern.compile(".*([td])s").matcher(lemma);
        if (lemma.matches(".*(dz|dž|c|č|z|ž|s|š)s")) {
            if (lexeme_root_pronunciation.toString().matches(".*[:s\\\\]")) {
                lexeme_root_pronunciation.setLength(lexeme_root_pronunciation.length() - 1);
            }
        } else if (ts_ds_matcher.matches()) {
            if (ts_ds_matcher.group(1).equals("t")) {
                lexeme_root_pronunciation.setLength(lexeme_root_pronunciation.length() - 1);
                lexeme_root_pronunciation.append('t');
            } else if (ts_ds_matcher.group(1).equals("d")) {
                lexeme_root_pronunciation.setLength(lexeme_root_pronunciation.length() - 1);
                lexeme_root_pronunciation.append('d');
            }
        }

        // FIXME: It kā risinājums ar _ pielikšanu metas.csv failā strādā, bet tas nav īsti glīti
        //   vajadzētu tā kā nodalīt atsevišķi ortogrāfiski pierakstītās skaņas un LVMLA formātā.
        // 1) Voicing, devoicing - must be extra careful as the changes here cascade back
        List<String> voiced_c = List.of(applyMetas("<balsīgi_līdzsk>").replace("?", "").split("\\|"));
        List<String> unvoiced_c = List.of(applyMetas("<nebalsīgi_līdzsk>").split("\\|"));

        if (lemma.matches(applyMetas(".*(<nebalsīgi_līdzsk>)$"))) {
            if (lemma_root.matches(applyMetas(".*(<balsīgi_līdzsk>)$"))) {
                Matcher unvoiced_matcher = Pattern.compile(applyMetas("(<nebalsīgi_līdzsk>)+(<balsīgi_līdzsk>)?$")).matcher(lexeme_root_pronunciation);
                if (unvoiced_matcher.find()) {
                    int start = unvoiced_matcher.start();
                    for (int i = start; i < lexeme_root_pronunciation.length(); i++) {
                        String ch = String.valueOf(lexeme_root_pronunciation.charAt(i));
                        int pos = unvoiced_c.indexOf(ch);
                        if (pos != -1 && !ch.equals("f")) {
                            lexeme_root_pronunciation.replace(i, i + 1, voiced_c.get(pos));
                        }
                    }
                }
                System.out.println("Voicing mismatch - b,nb");
            }
        } else if (lemma.matches(applyMetas(".*(<balsīgi_līdzsk>)$"))) {
            if (lemma_root.matches(applyMetas(".*(<nebalsīgi_līdzsk>)$"))) {
                // TODO: Nezinu, vai šādi gadījumi maz ir?
                System.out.println("Voicing mismatch - nb,b");
            }
        }

        // 2) Partial vocalization - i^, u^
        if (lemma.matches(applyMetas(".*(j|v)(<līdzskaņi>)$"))) {
            if (lemma_root.matches(".*([jv])$")) {
                Matcher i_u_matcher = Pattern.compile("([iu])\\^$").matcher(lexeme_root_pronunciation);
                if (i_u_matcher.find()) {
                    int start = i_u_matcher.start();

                    if (i_u_matcher.group(1).equals("i")) {
                        lexeme_root_pronunciation.replace(start, lexeme_root_pronunciation.length(), "j");
                    } else if (i_u_matcher.group(1).equals("u")) {
                        lexeme_root_pronunciation.replace(start, lexeme_root_pronunciation.length(), "v");
                    }
                }
                System.out.println("Possible vocalization in lemma");
            }
        }

        System.out.println(">>"+lexeme_root_pronunciation);
        return lexeme_root_pronunciation.toString();
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

        return pronunciation;
    }

}

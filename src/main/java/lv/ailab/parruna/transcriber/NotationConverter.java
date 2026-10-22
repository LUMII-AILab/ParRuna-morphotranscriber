package lv.ailab.parruna.transcriber;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NotationConverter {
    private static String notation_file = "/parruna/fonetiskais_alfabets.csv";
    private final List<List<String>> notations;

    public NotationConverter() {
        this.notations = loadNotations(notation_file);
    }

    private List<List<String>> loadNotations(String filename) {
        List<List<String>> notation_lines = new ArrayList<>();
        try {
            InputStream input = ApplyParRunaRules.class.getResourceAsStream(filename);
            if (input == null) {
                throw new IOException("Could not find fonetiskais_alfabets.csv in resources");
            }

            CSVReader csv = new CSVReader(new InputStreamReader(input));
            csv.setTextQualifier('"');
            csv.setSeparator(',');

            csv.nextLine();
            String[] titles = csv.getTitles();

            String[] dataRow;
            while((dataRow = csv.nextLine()) != null){
                List<String> phoneme = Arrays.stream(dataRow).toList();
                notation_lines.add(phoneme);
            }
            csv.close();


        } catch (Exception e) {
            System.out.println("Error reading file" + e);
        }
        return notation_lines;
    }

    // FIXME: Maybe this should be reworked, with some kind of HashMap for better lookup
    //  Or passing multiple of the already tokenized phoneme strings to avoid looping repeatedly
    //  for more optimal execution time.
    private String getPhonemeInNotation(String lvmfa, int notation) {
        for (List<String> phoneme: notations) {
            if (phoneme.get(0).equals(lvmfa)) {
                return phoneme.get(notation);
            }
        }
        // This shouldn't normally trigger
        return lvmfa;
    }

    // TODO: Rework this so that the notation positions in csv get loaded
    //  using titles row data in loadNotations instead of relying on 'magic numbers'
    private String getIPA(String lvmfa) {
        return getPhonemeInNotation(lvmfa, 1);
    }

    private String getLFA(String lvmfa) {
        return getPhonemeInNotation(lvmfa, 2);
    }


    /**
     * Tokenize LVMFA notation by phoneme
     */
    private List<String> tokenizeLVMFA(String lvmfa) {
        List<String> phonemes = new ArrayList<String>();
        Pattern p = Pattern.compile("(?:._.|.)[x:\\\\^]*");
        Matcher m = p.matcher(lvmfa);
        while (m.find()) {
            phonemes.add(m.group());
        }
        return phonemes;
    }

    public String lvmfaToIPA(String lvmfa) {
        StringBuilder sb = new StringBuilder();
        List<String> phonemes = tokenizeLVMFA(lvmfa);
        for (String phoneme: phonemes) {
            sb.append(getIPA(phoneme));
        }
        return sb.toString();
    }

    public String lvmfaToLFA(String lvmfa) {
        StringBuilder sb = new StringBuilder();
        List<String> phonemes = tokenizeLVMFA(lvmfa);
        for (String phoneme: phonemes) {
            sb.append(getLFA(phoneme));
        }
        return sb.toString();
    }
}

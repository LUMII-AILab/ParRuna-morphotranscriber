package lv.ailab.parruna.transcriber;

import java.util.regex.Pattern;

public class ParrunaRules {
    private final Pattern rule;
    private final Pattern inverse_rule;

    private final String look_behind;
    private final String look_ahead;
    private final String core_match;
    private final String replace_value;

    private final int replace_len;


    public ParrunaRules(String lookBehind, String coreMatch, String lookAhead, String replaceValue) {
        this.look_behind = lookBehind;
        this.look_ahead = lookAhead;
        this.core_match = coreMatch;
        this.replace_value = replaceValue;

        this.replace_len = core_match.length();

        this.rule = createMainRule();
        this.inverse_rule = createInverseRule();
    }

    private Pattern createMainRule() {
        String pattern_base = "(?<=(%s))(%s)(?=(%s))";
        String pattern = String.format(pattern_base, look_behind, core_match, look_ahead);
        return Pattern.compile(pattern);
    }

    private Pattern createInverseRule() {
        String pattern_base = "(?<=(%s))(%s)(?=(%s))";
        String pattern = String.format(pattern_base, look_behind, replace_value, look_ahead);
        return Pattern.compile(Pattern.quote(pattern));
    }

    public Pattern getPattern() {
        return rule;
    }

    public String getCore_match() {
        return core_match;
    }

    public String getReplace_value() {
        return replace_value;
    }

    public int getReplace_len() {
        return replace_len;
    }
}

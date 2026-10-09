package lv.ailab.parruna.transcriber;

import java.util.regex.Pattern;

public class ParrunaRules {
    private final Pattern pattern;
    private final String replace_value;
    private final int replace_len;

    public ParrunaRules(Pattern pattern, String replaceValue, int replace_len) {
        this.pattern = pattern;
        this.replace_value = replaceValue;
        this.replace_len = replace_len;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public String getReplace_value() {
        return replace_value;
    }

    public int getReplace_len() {
        return replace_len;
    }
}

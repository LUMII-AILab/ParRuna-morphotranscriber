package lv.ailab.parruna.transcriber;

public class ParrunaMetas {
    private final String metaName;
    private final String metaPattern;

    public ParrunaMetas(String metaName, String metaPattern) {
        this.metaName = metaName;
        this.metaPattern = metaPattern;
    }

    public String getMetaName() {
        return metaName;
    }

    public String getMetaPattern() {
        return metaPattern;
    }
}

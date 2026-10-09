package lv.ailab.parruna.transcriber;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;


public class CSVReader {

    private BufferedReader reader;
    private String line;
    private boolean titlesInFirstRow;
    private String[] titles;
    private int currentRow;
    private char separator;
    private char textQualifier;
    private StringBuilder sb;

    public CSVReader(Reader reader) {
        sb = new StringBuilder();
        if (reader instanceof BufferedReader)
            this.reader = (BufferedReader) reader;
        else
            this.reader = new BufferedReader(reader);
    }

    public void setTextQualifier(char textQualifier) {
        this.textQualifier = textQualifier;
    }

    public char getTextQualifier() {
        return textQualifier;
    }

    public void setSeparator(char separator) {
        this.separator = separator;
    }

    public char getSeparator() {
        return separator;
    }

    public int getCurrentRow() {
        return currentRow;
    }

    public String[] getTitles() {
        return titles;
    }

    public String getRawLine() {
        return line;
    }

    public String[] nextLine() throws IOException {
        line = reader.readLine();
        if (line == null)
            return null;

        currentRow++;
        sb.setLength(0);

        String[] values;
        if (separator == ',' && textQualifier == '"')
            values = parseCommaAndQuoteRow(line);
        else
            values = parseGeneric(line);

        if (titlesInFirstRow && titles == null)
            titles = values;

        return values;
    }

    private String[] parseGeneric(String line) {
        ArrayList<String> values = new ArrayList<String>();
        int prevIndex = 0, index = 0;
        while ((index = line.indexOf(separator, prevIndex)) >= 0) {
            String token = line.substring(prevIndex, index);
            values.add(token);
            prevIndex = index + 1;
        }

        if (prevIndex < line.length()) {
            String token = line.substring(prevIndex);
            values.add(token);
        }

        return values.toArray(new String[values.size()]);
    }

    private String[] parseCommaAndQuoteRow(String line) {
        ArrayList<String> values = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == textQualifier) {
                if (inQuotes && i + 1 < line.length()
                        && line.charAt(i + 1) == textQualifier) {
                    // Escaped double quote: ""
                    field.append(textQualifier);
                    i++;
                } else {
                    // Start or end of a quoted field
                    inQuotes = !inQuotes;
                }
            } else if (c == separator && !inQuotes) {
                // End of the current field
                values.add(field.toString());
                field.setLength(0);
            } else {
                field.append(c);
            }
        }

        // Always add the final field, even if it's empty
        values.add(field.toString());

        return values.toArray(new String[0]);
    }

    public void close() throws IOException {
        reader.close();
    }
}


package gradeflow.processor;

import gradeflow.model.ComparisonMethod;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class OutputComparator {

    public CompareResult compare(String actual, Path expectedFile, ComparisonMethod method)
            throws IOException {
        String expected = Files.readString(expectedFile);
        return compare(actual, expected, method);
    }

    public CompareResult compare(String actual, String expected, ComparisonMethod method) {
        String a = prepare(actual, method);
        String e = prepare(expected, method);

        if (a.equals(e)) return new CompareResult(true, "Output matches.");


        List<String> aLines = a.lines().toList();
        List<String> eLines = e.lines().toList();
        int max = Math.max(aLines.size(), eLines.size());

        StringBuilder diff = new StringBuilder();
        for (int i = 0; i < max; i++) {
            String al = i < aLines.size() ? aLines.get(i) : "<missing>";
            String el = i < eLines.size() ? eLines.get(i) : "<missing>";
            if (al.equals(el)) {
                diff.append("  ").append(al).append('\n');
            } else {
                diff.append("< ").append(el).append('\n');
                diff.append("> ").append(al).append('\n');
            }
        }
        return new CompareResult(false, diff.toString());
    }

    private String prepare(String text, ComparisonMethod method) {
        text = text.replace("\r\n", "\n").replace("\r", "\n");
        if (method == ComparisonMethod.TRIMMED) {
            text = text.lines()
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .reduce((a, b) -> a + "\n" + b)
                    .orElse("");
        }
        return text;
    }

    public static class CompareResult {
        public final boolean matched;
        public final String  diff;
        public CompareResult(boolean matched, String diff) {
            this.matched = matched;
            this.diff    = diff;
        }
    }
}

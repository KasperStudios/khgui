package kasperstudios.khgui.editor;

import kasperstudios.khgui.api.ITheme;
import kasperstudios.khgui.core.ThemeManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Syntax highlighter with regex-based token coloring.
 * Ported from old ModernTextArea.renderHighlightedLine().
 */
public class SyntaxHighlighter {

    /**
     * A colored text segment for rendering.
     */
    public record ColoredSegment(String text, int color) {
    }

    // Keyword sets
    private static final Set<String> JAVA_KEYWORDS = Set.of(
            "public", "private", "protected", "static", "final", "abstract",
            "class", "interface", "enum", "extends", "implements",
            "void", "int", "long", "double", "float", "boolean", "char", "byte", "short",
            "if", "else", "for", "while", "do", "switch", "case", "default",
            "return", "break", "continue", "new", "this", "super",
            "try", "catch", "finally", "throw", "throws",
            "import", "package", "instanceof", "synchronized", "volatile",
            "transient", "native", "assert", "var", "record", "sealed", "permits",
            "true", "false", "null");

    private static final Set<String> LUA_KEYWORDS = Set.of(
            "and", "break", "do", "else", "elseif", "end",
            "false", "for", "function", "if", "in",
            "local", "nil", "not", "or", "repeat",
            "return", "then", "true", "until", "while",
            "print", "require", "type", "tostring", "tonumber");

    private static final Map<String, Integer> DYNAMIC_KEYWORDS = new java.util.concurrent.ConcurrentHashMap<>();

    // Patterns
    private static final Pattern STRING_PATTERN = Pattern.compile("\"[^\"]*\"|'[^']*'");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\b\\d+(\\.\\d+)?\\b");
    private static final Pattern SINGLE_LINE_COMMENT = Pattern.compile("//.*$|--.*$|#.*$");
    private static final Pattern FUNCTION_CALL = Pattern.compile("\\b([a-zA-Z_]\\w*)\\s*\\(");
    private static final Pattern IDENTIFIER = Pattern.compile("\\b[a-zA-Z_]\\w*\\b|\\$([a-zA-Z_]\\w*)");
    private static final Pattern VARIABLE = Pattern.compile("\\$[a-zA-Z_]\\w*\\b");

    /**
     * Tokenizes a line into colored segments using the current theme.
     */
    public static List<ColoredSegment> highlight(String line) {
        return highlight(line, ThemeManager.getCurrentTheme());
    }

    /**
     * Tokenizes a line into colored segments.
     */
    public static List<ColoredSegment> highlight(String line, ITheme theme) {
        if (line == null || line.isEmpty()) {
            return List.of(new ColoredSegment("", theme.getPrimaryText()));
        }

        // Build color array indexed by character position
        int[] colors = new int[line.length()];
        int defaultColor = theme.getPrimaryText();
        for (int i = 0; i < colors.length; i++) {
            colors[i] = defaultColor;
        }

        // 1. Comments (highest priority - everything after comment marker)
        Matcher commentMatch = SINGLE_LINE_COMMENT.matcher(line);
        if (commentMatch.find()) {
            int commentColor = theme.getSyntaxComment();
            for (int i = commentMatch.start(); i < line.length(); i++) {
                colors[i] = commentColor;
            }
            // Only color non-comment part
            String beforeComment = line.substring(0, commentMatch.start());
            colorNonComment(beforeComment, colors, theme);
        } else {
            colorNonComment(line, colors, theme);
        }

        // Build segments from color array
        return buildSegments(line, colors);
    }

    private static void colorNonComment(String text, int[] colors, ITheme theme) {
        // 2. Strings
        Matcher stringMatch = STRING_PATTERN.matcher(text);
        while (stringMatch.find()) {
            int strColor = theme.getSyntaxString();
            for (int i = stringMatch.start(); i < stringMatch.end(); i++) {
                colors[i] = strColor;
            }
        }

        // 3. Numbers
        Matcher numMatch = NUMBER_PATTERN.matcher(text);
        while (numMatch.find()) {
            // Only color if not inside a string
            if (colors[numMatch.start()] == theme.getPrimaryText()) {
                int numColor = theme.getSyntaxNumber();
                for (int i = numMatch.start(); i < numMatch.end(); i++) {
                    colors[i] = numColor;
                }
            }
        }

        // 4. Function calls
        Matcher funcMatch = FUNCTION_CALL.matcher(text);
        while (funcMatch.find()) {
            int start = funcMatch.start(1);
            int end = funcMatch.end(1);
            // Only color if not already colored
            if (colors[start] == theme.getPrimaryText()) {
                String name = funcMatch.group(1);
                if (!JAVA_KEYWORDS.contains(name) && !LUA_KEYWORDS.contains(name)) {
                    int funcColor = theme.getSyntaxFunction();
                    for (int i = start; i < end; i++) {
                        colors[i] = funcColor;
                    }
                }
            }
        }

        // 5. Keywords and identifiers
        Matcher idMatch = IDENTIFIER.matcher(text);
        while (idMatch.find()) {
            if (colors[idMatch.start()] == theme.getPrimaryText()) {
                String word = idMatch.group();
                
                // Priority 1: Dynamic Keywords (Pointed Highlighting)
                if (DYNAMIC_KEYWORDS.containsKey(word)) {
                    int color = DYNAMIC_KEYWORDS.get(word);
                    for (int i = idMatch.start(); i < idMatch.end(); i++) {
                        colors[i] = color;
                    }
                }
                // Priority 2: Built-in Keywords
                else if (JAVA_KEYWORDS.contains(word) || LUA_KEYWORDS.contains(word)) {
                    int kwColor = theme.getSyntaxKeyword();
                    for (int i = idMatch.start(); i < idMatch.end(); i++) {
                        colors[i] = kwColor;
                    }
                }
                // Priority 3: Variables ($var)
                else if (word.startsWith("$")) {
                    int varColor = theme.getSyntaxVariable();
                    for (int i = idMatch.start(); i < idMatch.end(); i++) {
                        colors[i] = varColor;
                    }
                }
            }
        }
    }

    /**
     * Updates the dynamic keywords map for pointed highlighting.
     */
    public static void setDynamicKeywords(Map<String, Integer> keywords) {
        DYNAMIC_KEYWORDS.clear();
        if (keywords != null) {
            DYNAMIC_KEYWORDS.putAll(keywords);
        }
    }

    private static List<ColoredSegment> buildSegments(String line, int[] colors) {
        List<ColoredSegment> segments = new ArrayList<>();
        if (line.isEmpty())
            return segments;

        int currentColor = colors[0];
        int start = 0;

        for (int i = 1; i < line.length(); i++) {
            if (colors[i] != currentColor) {
                segments.add(new ColoredSegment(line.substring(start, i), currentColor));
                currentColor = colors[i];
                start = i;
            }
        }
        segments.add(new ColoredSegment(line.substring(start), currentColor));

        return segments;
    }

    // Static color getters for backward compatibility
    public static int getKeywordColor() {
        return ThemeManager.getCurrentTheme().getSyntaxKeyword();
    }

    public static int getStringColor() {
        return ThemeManager.getCurrentTheme().getSyntaxString();
    }

    public static int getNumberColor() {
        return ThemeManager.getCurrentTheme().getSyntaxNumber();
    }

    public static int getCommentColor() {
        return ThemeManager.getCurrentTheme().getSyntaxComment();
    }

    public static int getVariableColor() {
        return ThemeManager.getCurrentTheme().getSyntaxVariable();
    }

    public static int getFunctionColor() {
        return ThemeManager.getCurrentTheme().getSyntaxFunction();
    }

    public static int getDefaultColor() {
        return ThemeManager.getCurrentTheme().getPrimaryText();
    }
}

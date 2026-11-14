package com.mycompany.levelup_code.presentation.component;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

/** Lexical highlighting only. Evaluation continues to use the original source. */
final class CodeSyntax {
    private static final Map<String, Pattern> PATTERNS = Map.of(
            "JAVA", pattern("abstract assert boolean break byte case catch char class const continue default do double else enum extends final finally float for if implements import instanceof int interface long native new package private protected public record return short static strictfp super switch synchronized this throw throws transient try var void volatile while yield true false null", false),
            "PYTHON", pattern("and as assert async await break class continue def del elif else except False finally for from global if import in is lambda None nonlocal not or pass raise return True try while with yield match case", true),
            "CPP", pattern("alignas alignof auto bool break case catch char class const constexpr continue decltype default delete do double else enum explicit export extern false float for friend if inline int long mutable namespace new noexcept nullptr operator private protected public register reinterpret_cast return short signed sizeof static static_cast struct switch template this throw true try typedef typename union unsigned using virtual void volatile wchar_t while", false));

    private CodeSyntax() { }

    private static Pattern pattern(String keywords, boolean python) {
        String comment = python ? "#[^\\r\\n]*" : "//[^\\r\\n]*|/\\*[\\s\\S]*?(?:\\*/|$)";
        String string = "\"\"\"[\\s\\S]*?(?:\"\"\"|$)|'''[\\s\\S]*?(?:'''|$)|\"(?:[^\"\\\\\\r\\n]|\\\\.)*(?:\"|$)|'(?:[^'\\\\\\r\\n]|\\\\.)*(?:'|$)";
        return Pattern.compile("(?<COMMENT>" + comment + ")|(?<STRING>" + string + ")"
                + "|(?<KEYWORD>\\b(?:" + keywords.replace(" ", "|") + ")\\b)"
                + "|(?<NUMBER>\\b(?:0[xX][0-9a-fA-F_]+|\\d[\\d_]*(?:\\.\\d[\\d_]*)?(?:[eE][+-]?\\d+)?)[fFdDlL]?\\b)"
                + "|(?<BRACKET>[(){}\\[\\]])");
    }

    static StyleSpans<Collection<String>> highlight(String text, String language) {
        String key = language.toUpperCase(Locale.ROOT);
        if (key.equals("C++")) key = "CPP";
        Matcher matcher = PATTERNS.getOrDefault(key, PATTERNS.get("JAVA")).matcher(text);
        StyleSpansBuilder<Collection<String>> spans = new StyleSpansBuilder<>();
        int end = 0;
        while (matcher.find()) {
            spans.add(List.of(), matcher.start() - end);
            String kind = matcher.group("COMMENT") != null ? "comment"
                    : matcher.group("STRING") != null ? "string"
                    : matcher.group("KEYWORD") != null ? "keyword"
                    : matcher.group("NUMBER") != null ? "number" : "bracket";
            spans.add(List.of("syntax-" + kind), matcher.end() - matcher.start());
            end = matcher.end();
        }
        spans.add(List.of(), text.length() - end);
        return spans.create();
    }
}

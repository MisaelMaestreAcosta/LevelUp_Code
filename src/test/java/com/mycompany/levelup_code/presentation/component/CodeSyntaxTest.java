package com.mycompany.levelup_code.presentation.component;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class CodeSyntaxTest {
    private static String styleAt(String source, String language, String token) {
        int position = source.indexOf(token);
        int offset = 0;
        for (var span : CodeSyntax.highlight(source, language)) {
            offset += span.getLength();
            if (position < offset) return span.getStyle().stream().findFirst().orElse("");
        }
        return "";
    }

    @Test void javaStringsAndCommentsKeepKeywordsAsText() {
        String code = "public class Test { String value = \"if // 42\"; /* return */ int n = 12; }";
        assertEquals("syntax-keyword", styleAt(code, "JAVA", "public"));
        assertEquals("syntax-string", styleAt(code, "JAVA", "if"));
        assertEquals("syntax-comment", styleAt(code, "JAVA", "return"));
        assertEquals("syntax-number", styleAt(code, "JAVA", "12"));
    }

    @Test void pythonTripleQuotedStringsAndHashCommentsRemainSeparate() {
        String code = "def solve(input):\n    text = '''for\n# not a comment'''\n    # return 1\n    return 7";
        assertEquals("syntax-keyword", styleAt(code, "Python", "def"));
        assertEquals("syntax-string", styleAt(code, "Python", "# not"));
        assertEquals("syntax-comment", styleAt(code, "Python", "return 1"));
        assertEquals("syntax-number", styleAt(code, "Python", "7"));
    }

    @Test void cppIncludesAndEmptyOrIncompleteSourceCanBeHighlighted() {
        String code = "#include <string>\nstd::string solve() { return \"value\"; } // done";
        assertEquals("syntax-keyword", styleAt(code, "C++", "return"));
        assertEquals("syntax-string", styleAt(code, "CPP", "value"));
        assertEquals("syntax-comment", styleAt(code, "CPP", "done"));
        assertEquals(0, CodeSyntax.highlight("", "JAVA").length());
        assertEquals("syntax-comment", styleAt("/* unfinished", "JAVA", "unfinished"));
    }
}

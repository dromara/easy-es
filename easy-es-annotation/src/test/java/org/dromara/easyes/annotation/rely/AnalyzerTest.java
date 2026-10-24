package org.dromara.easyes.annotation.rely;

import org.dromara.easyes.annotation.IndexField;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;

/**
 * Regression tests for language analyzer aliases and annotation compatibility.
 */
public class AnalyzerTest {
    @Test
    public void languageAnalyzerNamesMatchElasticsearch() {
        assertEquals("arabic", Analyzer.ARABIC);
        assertEquals("cjk", Analyzer.CJK);
        assertEquals("nori", Analyzer.NORI);
    }

    @Test
    public void existingAnalyzerNamesRemainCompatible() throws Exception {
        String[][] existing = {
            {"NONE", "none"},
            {"STANDARD", "standard"},
            {"SIMPLE", "simple"},
            {"STOP", "stop"},
            {"WHITESPACE", "whitespace"},
            {"KEYWORD", "keyword"},
            {"PATTERN", "pattern"},
            {"LANGUAGE", "language"},
            {"SNOWBALL", "snowball"},
            {"IK_SMART", "ik_smart"},
            {"IK_MAX_WORD", "ik_max_word"},
            {"PINYIN", "pinyin"}
        };
        for (String[] entry : existing) {
            assertEquals(entry[0], entry[1], Analyzer.class.getField(entry[0]).get(null));
        }
    }

    @Test
    public void aliasesAreUsableInRuntimeRetainedAnnotations() throws Exception {
        assertAnalyzers("arabicText", "arabic", "arabic");
        assertAnalyzers("cjkText", "cjk", "cjk");
        assertAnalyzers("koreanText", "nori", "nori");
    }

    @Test
    public void unspecifiedAnalyzersKeepTheirDefaults() throws Exception {
        assertAnalyzers("defaultText", "none", "none");
    }

    private void assertAnalyzers(String fieldName, String analyzer, String searchAnalyzer) throws Exception {
        Field field = Document.class.getDeclaredField(fieldName);
        IndexField annotation = field.getAnnotation(IndexField.class);
        assertEquals(analyzer, annotation.analyzer());
        assertEquals(searchAnalyzer, annotation.searchAnalyzer());
    }

    private static class Document {
        @IndexField(analyzer = Analyzer.ARABIC, searchAnalyzer = Analyzer.ARABIC)
        private String arabicText;

        @IndexField(analyzer = Analyzer.CJK, searchAnalyzer = Analyzer.CJK)
        private String cjkText;

        @IndexField(analyzer = Analyzer.NORI, searchAnalyzer = Analyzer.NORI)
        private String koreanText;

        @IndexField
        private String defaultText;
    }
}

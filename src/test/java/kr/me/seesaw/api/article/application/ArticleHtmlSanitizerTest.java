package kr.me.seesaw.api.article.application;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ArticleHtmlSanitizerTest {

    @Test
    void preservesSafeStylesAndRemovesUnsafeHtmlAndStyles() {
        String result = ArticleHtmlSanitizer.clean("""
                <div style="background-color: #eef8f2; padding: 22px 24px; border-left: 5px solid #50a985; position: fixed; z-index: 999999; margin-top: -100px; background-image: url(https://example.com/x)">
                  <p style="color: #176448; font-weight: 700" onclick="alert(1)">내용</p><script>alert(1)</script>
                </div>
                """);

        assertTrue(result.contains("background-color: #eef8f2"));
        assertTrue(result.contains("padding: 22px 24px"));
        assertTrue(result.contains("border-left: 5px solid #50a985"));
        assertTrue(result.contains("color: #176448"));
        assertFalse(result.contains("position"));
        assertFalse(result.contains("z-index"));
        assertFalse(result.contains("-100px"));
        assertFalse(result.contains("url("));
        assertFalse(result.contains("onclick"));
        assertFalse(result.contains("script"));
    }
}

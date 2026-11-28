package kr.me.seesaw.api.article.application;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;

import java.util.Map;
import java.util.regex.Pattern;

/** 게시글 본문의 HTML과 인라인 스타일을 제한한다. */
final class ArticleHtmlSanitizer {

    private static final String LENGTH = "(?:0|[0-9]{1,3}(?:\\.[0-9]+)?(?:px|pt)|[0-9](?:\\.[0-9]+)?(?:em|rem)|(?:100|[0-9]{1,2})(?:\\.[0-9]+)?%)";
    private static final String COLOR = "(?:#[0-9a-fA-F]{3,8}|[a-zA-Z]{1,20}|rgba?\\([0-9.,%\\s]+\\))";
    private static final String BOX = LENGTH + "(?:\\s+" + LENGTH + "){0,3}";
    private static final Map<String, Pattern> STYLES = Map.ofEntries(
            Map.entry("color", Pattern.compile(COLOR)),
            Map.entry("background-color", Pattern.compile(COLOR)),
            Map.entry("font-size", Pattern.compile(LENGTH)),
            Map.entry("line-height", Pattern.compile("(?:[0-9](?:\\.[0-9]+)?|" + LENGTH + ")")),
            Map.entry("font-weight", Pattern.compile("(?:normal|bold|[1-9]00)")),
            Map.entry("font-style", Pattern.compile("(?:normal|italic)")),
            Map.entry("text-align", Pattern.compile("(?:left|right|center|justify)")),
            Map.entry("text-decoration", Pattern.compile("(?:none|underline|line-through)")),
            Map.entry("margin", Pattern.compile("(?:auto|" + BOX + ")")),
            Map.entry("margin-top", Pattern.compile(LENGTH)),
            Map.entry("margin-bottom", Pattern.compile(LENGTH)),
            Map.entry("padding", Pattern.compile(BOX)),
            Map.entry("padding-bottom", Pattern.compile(LENGTH)),
            Map.entry("border-radius", Pattern.compile(BOX)),
            Map.entry("border", Pattern.compile(LENGTH + "\\s+(?:solid|dashed|dotted)\\s+" + COLOR)),
            Map.entry("border-left", Pattern.compile(LENGTH + "\\s+(?:solid|dashed|dotted)\\s+" + COLOR)),
            Map.entry("border-bottom", Pattern.compile(LENGTH + "\\s+(?:solid|dashed|dotted)\\s+" + COLOR)),
            Map.entry("max-width", Pattern.compile(LENGTH)),
            Map.entry("width", Pattern.compile(LENGTH))
    );

    private ArticleHtmlSanitizer() {
    }

    static String clean(String html) {
        Safelist safelist = Safelist.relaxed().preserveRelativeLinks(true)
                .addTags("video")
                .addAttributes("video", "src", "controls", "aria-label")
                .addAttributes(":all", "style");
        Document document = Jsoup.parseBodyFragment(Jsoup.clean(html, "http://localhost", safelist));
        for (Element element : document.select("[style]")) {
            StringBuilder safeStyle = new StringBuilder();
            for (String declaration : element.attr("style").split(";")) {
                String[] parts = declaration.split(":", 2);
                if (parts.length != 2) continue;
                String property = parts[0].trim().toLowerCase(java.util.Locale.ROOT);
                String value = parts[1].trim();
                Pattern allowed = STYLES.get(property);
                if (allowed != null && allowed.matcher(value).matches()) {
                    if (!safeStyle.isEmpty()) safeStyle.append("; ");
                    safeStyle.append(property).append(": ").append(value);
                }
            }
            if (safeStyle.isEmpty()) element.removeAttr("style");
            else element.attr("style", safeStyle.toString());
        }
        return document.body().html();
    }
}

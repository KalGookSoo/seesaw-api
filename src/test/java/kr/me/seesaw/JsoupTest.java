package kr.me.seesaw;

import kr.me.seesaw.core.jsoup.PatternMatcher;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsoupTest {

    @Test
    @DisplayName("XSS 차단")
    void cleanTest() {
        // Given
        String input = """
                <p>
                <img src="http://localhost:8080/api/attachments/9f65c098-e337-4bbe-aa4d-11017f0ab633" alt="fileOriginName" contenteditable="false">
                <img src="/api/attachments/38cd14f2-f86a-48d6-b094-da9c2d4af772" alt="fileOriginName" contenteditable="false">
                <img src="https://www.naver.com" alt="fileOriginName" contenteditable="false">
                <img src="javascript:alert('이게 실행되면 안된다고')">
                <img src="data:image/svg+xml;base64,<encoded malicious script>">
                <a href="javascript:alert('이게 실행되면 안된다고')">매우 안전한 링크</a>
                <script>alert('이게 실행되면 안된다고')</script>
                <br>
                </p>
                """;
        Safelist safelist = Safelist.relaxed().addAttributes(":all", "data-*");

        // When
        String sanitized = Jsoup.clean(input, safelist);

        // Then
        Assertions.assertFalse(sanitized.contains("javascript"));
        Assertions.assertFalse(sanitized.contains("script"));
        Assertions.assertFalse(sanitized.contains("data:"));
        System.out.println(sanitized);

    }

    @Test
    @DisplayName("src 값에서 첨부파일 식별자 추출")
    void extractTest() {
        // Given
        String input = """
                <p><img src="http://localhost:8080/api/attachments/cd031c7c-e3db-412b-bf15-8b2a50b28699" alt="carousel_3.jpeg"><br></p>
                <p><br></p>
                <p><br></p>
                <p><img src="http://localhost:8080/api/attachments/b65857dc-077f-4db2-83fe-9f3334b9659c" alt="sample1.jpeg"><br></p>
                """;

        // When
        Document document = Jsoup.parse(input);
        Elements images = document.select("img[src]");
        String inlineImagePattern = "/api/attachments/(" + PatternMatcher.UUID + ")$";
        Pattern pattern = Pattern.compile(inlineImagePattern);
        List<String> attachmentIds = new ArrayList<>();
        for (Element image : images) {
            String src = image.attr("src");
            Matcher matcher = pattern.matcher(src);
            if (matcher.find()) {
                String uuidStr = matcher.group(1);
                attachmentIds.add(uuidStr);
            }
        }

        // Then
        Assertions.assertFalse(attachmentIds.isEmpty());
        System.out.println(attachmentIds);

    }

    @Test
    void test() {
        String input = """
                <section class="container-sm carousel-section">
                    <div id="carousel-indicators" class="carousel slide" data-bs-ride="carousel">
                        <div class="carousel-indicators">
                            <button type="button" data-bs-target="#carousel-indicators" data-bs-slide-to="0" class="active" aria-current="true" aria-label="Slide 1"><img alt="캐러셀이미지" class="d-block w-100" src="http://localhost:8080/api/attachments/cb1fc388-0531-44ec-93e1-49ce2df373e1}"></button>
                            <button type="button" data-bs-target="#carousel-indicators" data-bs-slide-to="1" aria-label="Slide 2"><img alt="캐러셀이미지" class="d-block w-100" src="http://localhost:8080/api/attachments/401224b1-410a-4152-a7ab-62a21dcd2a26}"></button>
                            <button type="button" data-bs-target="#carousel-indicators" data-bs-slide-to="2" aria-label="Slide 3"><img alt="캐러셀이미지" class="d-block w-100" src="http://localhost:8080/api/attachments/6d5cb399-c9c7-4939-981f-6e1ae32262ab}"></button>
                        </div>
                        <div class="carousel-inner">
                            <div class="carousel-item active" data-bs-interval="1500"><img alt="캐러셀이미지" class="d-block w-100" src="http://localhost:8080/api/attachments/cb1fc388-0531-44ec-93e1-49ce2df373e1}"></div>
                            <div class="carousel-item" data-bs-interval="1500"><img alt="캐러셀이미지" class="d-block w-100" src="http://localhost:8080/api/attachments/401224b1-410a-4152-a7ab-62a21dcd2a26}"></div>
                            <div class="carousel-item" data-bs-interval="1500"><img alt="캐러셀이미지" class="d-block w-100" src="http://localhost:8080/api/attachments/6d5cb399-c9c7-4939-981f-6e1ae32262ab}"></div>
                        </div>
                        <button class="carousel-control-prev" type="button" data-bs-target="#carousel-indicators" data-bs-slide="prev"><span class="carousel-control-prev-icon" aria-hidden="true"></span><span class="visually-hidden">Previous</span></button>
                        <button class="carousel-control-next" type="button" data-bs-target="#carousel-indicators" data-bs-slide="next"><span class="carousel-control-next-icon" aria-hidden="true"></span><span class="visually-hidden">Next</span></button>
                    </div>
                </section>
                """;


        Safelist safelist = Safelist.relaxed()
                .addAttributes(":all", "*")
                .preserveRelativeLinks(true)
                .addProtocols("img", "src", "http", "https", "data");
        String cleaned = Jsoup.clean(input, safelist);

        System.out.println(cleaned);
    }

    @Test
    void test2() {
        String input = """
                <img src="/api/attachments/38cd14f2-f86a-48d6-b094-da9c2d4af772" alt="fileOriginName" contenteditable="false">
                <img src="http://www.naver.com/api/attachments/38cd14f2-f86a-48d6-b094-da9c2d4af772" alt="fileOriginName" contenteditable="false">
                <p>
                <img src="http://localhost:8080/api/attachments/9f65c098-e337-4bbe-aa4d-11017f0ab633" alt="fileOriginName" contenteditable="false">
                <img src="/api/attachments/38cd14f2-f86a-48d6-b094-da9c2d4af772" alt="fileOriginName" contenteditable="false">
                <img src="https://www.naver.com" alt="fileOriginName" contenteditable="false">
                <img src="javascript:alert('이게 실행되면 안된다고')">
                <img src="data:image/svg+xml;base64,<encoded malicious script>">
                <a href="javascript:alert('이게 실행되면 안된다고')">매우 안전한 링크</a>
                <script>alert('이게 실행되면 안된다고')</script>
                <br>
                </p>
                """;

        Safelist safelist = Safelist.relaxed().preserveRelativeLinks(true);
        String cleaned = Jsoup.clean(input, "http://localhost", safelist);

        System.out.println(cleaned);

        Assertions.assertTrue(cleaned.contains("/api/attachments"));
    }

}

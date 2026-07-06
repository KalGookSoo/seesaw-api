package kr.me.seesaw.api.sitemap;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.Duration;

@Validated
@RequiredArgsConstructor
@RestController
public class SitemapApiController {

    private static final MediaType XML_MEDIA_TYPE = MediaType.APPLICATION_XML;

    private final SitemapService sitemapService;

    @GetMapping(value = "/api/test/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> getSitemap(@RequestParam @NotBlank String origin, @RequestParam @NotBlank String domainName) {
        return ResponseEntity.ok()
                .contentType(XML_MEDIA_TYPE)
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(10)))
                .body(sitemapService.getSitemap(origin, domainName));
    }

    @GetMapping(value = "/api/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> getSitemap() {
        final ServletUriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentContextPath();
        final String origin = builder.toUriString();
        final String host = builder.build().getHost();
        return ResponseEntity.ok()
                .contentType(XML_MEDIA_TYPE)
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(10)))
                .body(sitemapService.getSitemap(origin, host));
    }

}

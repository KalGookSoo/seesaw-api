package kr.me.seesaw.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import kr.me.seesaw.message.CmsMessageSource;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@Tag(name = "MessageApiController", description = "메세지 API 컨트롤러")
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageApiController {

    private final CmsMessageSource messageSource;

    @GetMapping
    public ResponseEntity<Map<String, String>> getAllMessages() {
        ResourceBundle resourceBundle = ResourceBundle.getBundle("messages", LocaleContextHolder.getLocale());
        Map<String, String> messages = resourceBundle.keySet()
                .stream()
                .collect(Collectors.toMap(key -> key, resourceBundle::getString));
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/{code}")
    public ResponseEntity<String> getMessageByCode(@PathVariable String code, @RequestParam(required = false) String[] args) {
        String message = messageSource.getMessage(code, args);
        return ResponseEntity.ok(message);
    }

}

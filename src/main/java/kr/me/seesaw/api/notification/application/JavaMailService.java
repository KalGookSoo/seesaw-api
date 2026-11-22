package kr.me.seesaw.api.notification.application;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import kr.me.seesaw.api.notification.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@Profile("prod")
@Transactional
@RequiredArgsConstructor
@Service
public class JavaMailService implements MailService {

    private final JavaMailSender sender;
    private final MailProperties properties;
    private final SpringTemplateEngine springTemplateEngine;

    @Override
    public void send(String from, String[] to, String title, String viewName, Map<String, String> values) {
        log.info("메일을 전송합니다.\n발신자: {}\n수신자: {}\n제목: {}", from, to, title);
        MimeMessage message = sender.createMimeMessage();
        Context context = new Context();
        values.forEach(context::setVariable);
        String html = springTemplateEngine.process(viewName, context);

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom(properties.getUsername());
            helper.setTo(to);
            helper.setReplyTo(from);
            helper.setSubject(title);
            helper.setText(html, true);
            sender.send(message);
        } catch (MessagingException e) {
            log.error("메시지 생성에 실패했습니다.", e);
        }
    }

}

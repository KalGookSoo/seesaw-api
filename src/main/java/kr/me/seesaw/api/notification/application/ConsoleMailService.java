package kr.me.seesaw.api.notification.application;

import kr.me.seesaw.api.notification.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Profile("!prod")
@Transactional
@RequiredArgsConstructor
@Service
public class ConsoleMailService implements MailService {

    @Override
    public void send(String from, String[] to, String title, String viewName, Map<String, String> values) {
        log.info("메일을 전송합니다.\n발신자: {}\n수신자: {}\n제목: {}", from, to, title);
    }

}

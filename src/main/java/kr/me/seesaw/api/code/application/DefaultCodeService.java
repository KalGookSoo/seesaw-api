package kr.me.seesaw.api.code.application;

import kr.me.seesaw.api.code.dto.CodeResponse;
import kr.me.seesaw.api.service.CodeService;
import kr.me.seesaw.core.domain.code.CodeRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@RequiredArgsConstructor
@Service
public class DefaultCodeService implements CodeService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final CodeRepository codeRepository;

    @Transactional(readOnly = true)
    @Override
    public List<CodeResponse> getAllCodes() {
        logger.debug("모든 코드 조회");
        return codeRepository.findAll(Sort.by(Sort.Direction.ASC, "sequence"))
                .stream()
                .map(CodeResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<CodeResponse> getAllCodesByName(String name) {
        logger.debug("코드 이름으로 조회: name={}", name);
        return codeRepository.findByName(name)
                .stream()
                .map(CodeResponse::new)
                .toList();
    }

}

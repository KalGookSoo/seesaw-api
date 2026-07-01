package kr.me.seesaw.api.user.application;

import kr.me.seesaw.api.user.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @see UserService
 */
@Service
@RequiredArgsConstructor
public class DefaultUserService implements UserService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

}

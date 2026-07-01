package kr.me.seesaw.api.service;

import kr.me.seesaw.api.code.dto.CodeResponse;

import java.util.List;

/**
 * 코드 서비스
 */
public interface CodeService {

    List<CodeResponse> getAllCodes();

    List<CodeResponse> getAllCodesByName(String name);

}

package kr.me.seesaw.service;

import kr.me.seesaw.response.CodeResponse;

import java.util.List;

/**
 * 코드 서비스
 */
public interface CodeService {

    List<CodeResponse> getAllCodes();

    List<CodeResponse> getAllCodesByName(String name);

}

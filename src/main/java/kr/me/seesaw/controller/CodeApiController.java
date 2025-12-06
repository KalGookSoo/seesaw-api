package kr.me.seesaw.controller;

import kr.me.seesaw.core.hierarchy.HierarchicalFactory;
import kr.me.seesaw.model.CodeModel;
import kr.me.seesaw.service.CodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/codes")
public class CodeApiController {

    private final CodeService codeService;

    @GetMapping
    public ResponseEntity<Map<String, List<CodeModel>>> getAllCodes(
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "isNested", defaultValue = "false") boolean isNested
    ) {
        List<CodeModel> codes = name == null ? codeService.getAllCodes() : codeService.getAllCodesByName(name);
        return ResponseEntity.ok(Map.of("codes", isNested ? HierarchicalFactory.build(codes) : codes));
    }

}

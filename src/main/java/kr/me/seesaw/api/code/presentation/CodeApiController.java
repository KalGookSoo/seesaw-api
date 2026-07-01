package kr.me.seesaw.api.code.presentation;

import kr.me.seesaw.core.support.hierarchy.HierarchicalFactory;
import kr.me.seesaw.api.code.dto.CodeResponse;
import kr.me.seesaw.api.service.CodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    public ResponseEntity<Map<String, List<CodeResponse>>> getAllCodes(
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "isNested", defaultValue = "false") boolean isNested
    ) {
        List<CodeResponse> codes = name == null ? codeService.getAllCodes() : codeService.getAllCodesByName(name);
        return ResponseEntity.ok(Map.of("codes", isNested ? HierarchicalFactory.build(codes) : codes));
    }

    @GetMapping("/{name}")
    public ResponseEntity<List<CodeResponse>> getCode(@PathVariable("name") String name) {
        List<CodeResponse> codes = codeService.getAllCodesByName(name);
        return ResponseEntity.ok(HierarchicalFactory.build(codes));
    }

}

package kr.me.seesaw.api.role.presentation;

import kr.me.seesaw.framework.TestDataInitializerConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("역할 API 컨트롤러 테스트")
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@Import(TestDataInitializerConfig.class)
@SpringBootTest
class RoleApiControllerTest {

    private final MockMvc mockMvc;

    public RoleApiControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    @DisplayName("역할 목록 조회 요청 성공 시 200 응답 코드를 반환합니다.")
    void getAllRolesShouldReturnOk() throws Exception {
        // then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/roles"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

}

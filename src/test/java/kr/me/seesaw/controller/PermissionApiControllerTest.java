package kr.me.seesaw.controller;

import jakarta.persistence.EntityManager;
import kr.me.seesaw.config.TestDataInitializerConfig;
import kr.me.seesaw.domain.Category;
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

import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("권한 API 컨트롤러 테스트")
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@Import(TestDataInitializerConfig.class)
@SpringBootTest
class PermissionApiControllerTest {

    private final MockMvc mockMvc;

    private final EntityManager entityManager;

    public PermissionApiControllerTest(MockMvc mockMvc, EntityManager entityManager) {
        this.mockMvc = mockMvc;
        this.entityManager = entityManager;
    }

    @Test
    @DisplayName("권한 목록 조회 요청 성공 시 200 응답 코드를 반환합니다.")
    void getAllPermissionsShouldReturnOk() throws Exception {
        // given
        List<Category> categories = entityManager.createQuery("SELECT c FROM Category c", Category.class).getResultList();

        // when & then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/permissions?targetId=" + categories.get(0).getId()))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

}

package kr.me.seesaw.api.framework.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class SecurityConfigTest {

    private final MockMvc mockMvc;

    public SecurityConfigTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @BeforeEach
    void setup() {
    }

    @Test
    @DisplayName("계정 인증이 필요없는 요청에는 200번대 응답코드를 반환한다")
    void anonymous() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/security-test/anonymous"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
    }

    @Test
    @DisplayName("계정인증이 필요한 요청에는 401 응답코드를 반환한다")
    void authenticatedShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/security-test/authenticated"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @DisplayName("계정인증이 필요한 요청에는 신원이 확인되면 200번대 응답코드를 반환한다")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void authenticatedShouldReturnSuccessful() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/security-test/authenticated"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
    }

    @Test
    @DisplayName("ADMIN 권한이 필요한 요청에는 미인증 시 401 응답코드를 반환한다")
    void adminShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/security-test/admin"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @DisplayName("ADMIN 권한이 필요한 요청에는 ADMIN 신원이 확인되면 2xx 응답코드를 반환한다")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void adminShouldReturnSuccessful() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/security-test/admin"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
    }

    @Test
    @DisplayName("MANAGER 권한이 필요한 요청에는 미인증 시 401 응답코드를 반환한다")
    void managerShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/security-test/manager"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @DisplayName("MANAGER 권한이 필요한 요청에는 MANAGER 신원이 확인되면 2xx 응답코드를 반환한다")
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    void managerShouldReturnSuccessful() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/security-test/manager"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
    }

    @Test
    @DisplayName("USER 권한이 필요한 요청에는 미인증 시 401 응답코드를 반환한다")
    void userShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/security-test/user"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @SuppressWarnings("DefaultAnnotationParam")
    @Test
    @DisplayName("USER 권한이 필요한 요청에는 USER 신원이 확인되면 2xx 응답코드를 반환한다")
    @WithMockUser(username = "user", roles = {"USER"})
    void userShouldReturnSuccessful() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/security-test/user"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
    }

}
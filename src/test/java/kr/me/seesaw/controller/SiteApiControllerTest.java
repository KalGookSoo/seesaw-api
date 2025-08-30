package kr.me.seesaw.controller;

import kr.me.seesaw.core.authentication.PrincipalProvider;
import kr.me.seesaw.domain.Site;
import kr.me.seesaw.service.SiteService;
import kr.me.seesaw.message.CmsMessageSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(SiteApiControllerTest.TestMethodSecurityConfig.class)
@WebMvcTest(controllers = SiteApiController.class)
class SiteApiControllerTest {

    @TestConfiguration
    @EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
    static class TestMethodSecurityConfig {}

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SiteService siteService;

    @MockitoBean
    private PrincipalProvider principalProvider;

    @MockitoBean
    private CmsMessageSource cmsMessageSource;

    @BeforeEach
    void setupMessageSource() {
        when(cmsMessageSource.getMessage(anyString())).thenReturn("오류");
        when(cmsMessageSource.getMessage(anyString(), any())).thenReturn("오류");
    }

    @Test
    @DisplayName("비인증 사용자의 보호 엔드포인트 접근은 401이어야 한다")
    void unauthorizedAccessShouldReturn401() throws Exception {
        // 인증 정보 없는 상태에서 접근 -> 401
        Mockito.when(principalProvider.getAuthentication()).thenThrow(new org.springframework.security.authentication.AuthenticationCredentialsNotFoundException("No auth"));
        mockMvc.perform(get("/api/sites"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("ADMIN 권한으로 본인 사이트 목록을 200으로 받는다")
    void getOwnSitesWithAdminRole() throws Exception {
        Mockito.when(principalProvider.getAuthentication()).thenReturn(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("admin", "N/A", java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN"))));
        List<Site> sites = Collections.singletonList(Site.create("s", "d.com", "desc", "code", true, true, "tags", null, "010", "intro", "content"));
        when(siteService.getOwnSites("admin")).thenReturn(sites);

        mockMvc.perform(get("/api/sites").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("권한 부족 시 403 반환")
    void forbiddenWhenRoleIsNotAllowed() throws Exception {
        Mockito.when(principalProvider.getAuthentication()).thenReturn(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("user", "N/A", java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"))));
        mockMvc.perform(get("/api/sites").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("도메인 기반 공개 조회는 인증 없이 200 반환")
    void getSiteContextPublic() throws Exception {
        Site site = Site.create("s", "example.com", "desc", "code", true, true, "tags", null, "010", "intro", "content");
        when(siteService.getSiteContext("example.com")).thenReturn(site);

        mockMvc.perform(get("/api/sites/by-domain/{domain}", "example.com").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("유효하지 않은 생성 요청은 422와 에러 배열을 반환")
    void createSiteValidationErrorReturns422() throws Exception {
        Mockito.when(principalProvider.getAuthentication()).thenReturn(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("admin", "N/A", java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN"))));
        mockMvc.perform(post("/api/sites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("존재하지 않는 ID 조회 시 404 반환")
    void getSiteByIdNotFoundReturns404() throws Exception {
        Mockito.when(principalProvider.getAuthentication()).thenReturn(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("admin", "N/A", java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN"))));
        when(siteService.getSiteById("notfound")).thenThrow(new java.util.NoSuchElementException());

        mockMvc.perform(get("/api/sites/{id}", "notfound").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
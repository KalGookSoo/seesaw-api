package kr.me.seesaw.controller;

import kr.me.seesaw.domain.Site;
import kr.me.seesaw.service.SiteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class SiteApiControllerTest {

    @BeforeEach
    void setUp() {
        siteService = Mockito.mock(SiteService.class);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SiteService siteService;

//    @Test
//    @DisplayName("사용자의 사이트 목록을 정상적으로 조회할 수 있다")
//    @WithMockUser
//    void getSites_ReturnsListOfSites() throws Exception {
//        // given
//        List<Site> sites = Arrays.asList(
//                createSite("site1", "example1.com", "사이트 1 설명"),
//                createSite("site2", "example2.com", "사이트 2 설명")
//        );
//        when(siteService.getOwnSites()).thenReturn(sites);
//
//        // when & then
//        mockMvc.perform(get("/api/sites")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
//    }

//    @Test
//    @DisplayName("인증되지 않은 사용자는 사이트 목록을 조회할 수 없다")
//    @WithAnonymousUser
//    void getSites_WithoutAuthentication_ReturnsUnauthorized() throws Exception {
//        // when & then
//        mockMvc.perform(get("/api/sites")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isForbidden());
//    }
//
//    @Test
//    @DisplayName("관리자 권한이 있는 사용자는 관리자 정보를 조회할 수 있다")
//    @WithMockUser(roles = "ADMIN")
//    void getAdminInfo_WithAdminRole_ReturnsOk() throws Exception {
//        // when & then
//        mockMvc.perform(get("/api/sites/admin")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(content().string("관리자 권한으로 접근한 정보입니다."));
//    }
//
//    @Test
//    @DisplayName("관리자 권한이 없는 사용자는 관리자 정보를 조회할 수 없다")
//    @WithMockUser(roles = "USER")
//    void getAdminInfo_WithoutAdminRole_ReturnsForbidden() throws Exception {
//        // when & then
//        mockMvc.perform(get("/api/sites/admin")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isForbidden());
//    }
//
//    @Test
//    @DisplayName("인증된 사용자는 인증 정보를 조회할 수 있다")
//    @WithMockUser
//    void getAuthenticatedInfo_WithAuthentication_ReturnsOk() throws Exception {
//        // when & then
//        mockMvc.perform(get("/api/sites/authenticated")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(content().string("인증된 사용자만 접근 가능한 정보입니다."));
//    }
//
//    @Test
//    @DisplayName("인증되지 않은 사용자는 인증 정보를 조회할 수 없다")
//    @WithAnonymousUser
//    void getAuthenticatedInfo_WithoutAuthentication_ReturnsUnauthorized() throws Exception {
//        // when & then
//        mockMvc.perform(get("/api/sites/authenticated")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isForbidden());
//    }
//
//    @Test
//    @DisplayName("관리자 권한이 있는 사용자는 권한 정보를 조회할 수 있다")
//    @WithMockUser(roles = "ADMIN")
//    void getAuthorizedInfo_WithAdminRole_ReturnsOk() throws Exception {
//        // when & then
//        mockMvc.perform(get("/api/sites/authorized")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(content().string("관리자 권한 또는 관리자 권한으로 접근한 정보입니다."));
//    }
//
//    @Test
//    @DisplayName("매니저 권한이 있는 사용자는 권한 정보를 조회할 수 있다")
//    @WithMockUser(roles = "MANAGER")
//    void getAuthorizedInfo_WithManagerRole_ReturnsOk() throws Exception {
//        // when & then
//        mockMvc.perform(get("/api/sites/authorized")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(content().string("관리자 권한 또는 관리자 권한으로 접근한 정보입니다."));
//    }
//
//    @Test
//    @DisplayName("필요한 권한이 없는 사용자는 권한 정보를 조회할 수 없다")
//    @WithMockUser(roles = "USER")
//    void getAuthorizedInfo_WithoutRequiredRoles_ReturnsForbidden() throws Exception {
//        // when & then
//        mockMvc.perform(get("/api/sites/authorized")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isForbidden());
//    }
//
//    // 테스트용 Site 객체 생성 헬퍼 메서드
//    private Site createSite(String name, String domainName, String description) {
//        // 테스트용 Site 객체 생성을 위한 모의 객체 생성
//        Site mockSite = Mockito.mock(Site.class);
//
//        // 필요한 메서드 스텁 설정
//        when(mockSite.getName()).thenReturn(name);
//        when(mockSite.getDomainName()).thenReturn(domainName);
//        when(mockSite.getDescription()).thenReturn(description);
//        when(mockSite.getId()).thenReturn("site-" + domainName);
//
//        return mockSite;
//    }
}
package kr.me.seesaw.controller;

import kr.me.seesaw.command.CreateSiteCommand;
import kr.me.seesaw.core.authentication.PrincipalProvider;
import kr.me.seesaw.domain.vo.RoleName;
import kr.me.seesaw.exception.GlobalExceptionHandler;
import kr.me.seesaw.message.CmsMessageSource;
import kr.me.seesaw.model.SiteModel;
import kr.me.seesaw.service.SiteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;

@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalExceptionHandler.class})
@WebMvcTest(controllers = {SiteApiController.class})
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class SiteApiControllerTest {

    private final MockMvc mockMvc;

    @MockitoBean
    private SiteService siteService;

    @MockitoBean
    private CmsMessageSource messageSource;

    @MockitoBean
    private PrincipalProvider principalProvider;

    public SiteApiControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @BeforeEach
    void setup() {
    }

    @Test
    @DisplayName("권한을 가진 사이트 목록 요청 - 인증 필요, ADMIN/MANAGER 2xx")
    void getOwnSites() throws Exception {
        List<SiteModel> sites = Collections.singletonList(Mockito.mock(SiteModel.class));

        List<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList(RoleName.ROLE_ADMIN.name());
        AnonymousAuthenticationToken token = new AnonymousAuthenticationToken("admin", User.withUsername("admin"), authorities);
        Mockito.when(principalProvider.getAuthentication()).thenReturn(token);
        Mockito.when(siteService.getOwnSites(anyString())).thenReturn(sites);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/sites"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
    }

    @Test
    @DisplayName("사이트 생성 - 유효 요청 본문이면 2xx와 생성된 사이트 반환")
    void createSite() throws Exception {

        MockMultipartFile profileImage = new MockMultipartFile(
                "profileImage",
                "profile.png",
                "image/png",
                "profile image data".getBytes()
        );

        MockMultipartFile backgroundImage = new MockMultipartFile(
                "backgroundImage",
                "background.png",
                "image/png",
                "background image data".getBytes()
        );

        Mockito.when(siteService.createSite(Mockito.any(CreateSiteCommand.class)))
                .thenReturn(Mockito.mock(SiteModel.class));

        mockMvc.perform(
                        MockMvcRequestBuilders.multipart("/api/sites")
                                .file(profileImage)
                                .file(backgroundImage)
                                .param("name", "site-name")
                                .param("domainName", "example.com")
                                .param("description", "desc")
                                .param("distributionCode", "code")
                                .param("searchEngineExposed", "true")
                                .param("imageExposed", "true")
                                .param("tags", "tag1,tag2")
                                .param("address.zipcode", "")
                                .param("address.value", "")
                                .param("contactNumber", "010-0000-0000")
                                .param("intro", "intro")
                                .param("content", "content")
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
    }

    @Test
    @DisplayName("사이트 단건 조회 - 존재하면 2xx와 사이트 반환")
    void getSiteById() throws Exception {
        Mockito.when(siteService.getSiteById("id-1")).thenReturn(Mockito.mock(SiteModel.class));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/sites/{id}", "id-1"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
    }

    @Test
    @DisplayName("도메인으로 컨텍스트 조회 - 공개 API 2xx")
    void getSiteContext() throws Exception {
        Mockito.when(siteService.getSiteByDomainName("example.com")).thenReturn(Mockito.mock(SiteModel.class));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/sites/by-domain/{domain}", "example.com"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
    }

    @Test
    @DisplayName("사이트 수정 - 유효 요청 본문이면 2xx")
    void updateSite() throws Exception {
        MockMultipartFile profileImage = new MockMultipartFile(
                "profileImage",
                "profile.png",
                "image/png",
                "dummy image".getBytes()
        );

        MockMultipartFile backgroundImage = new MockMultipartFile(
                "backgroundImage",
                "background.png",
                "image/png",
                "dummy background".getBytes()
        );

        Mockito.when(siteService.updateSite(Mockito.eq("id-1"), Mockito.any(CreateSiteCommand.class))).thenReturn(Mockito.mock(SiteModel.class));

        mockMvc.perform(
                        MockMvcRequestBuilders.multipart("/api/sites/{id}", "id-1")
                                .file(profileImage)
                                .file(backgroundImage)
                                .param("name", "updated")
                                .param("domainName", "example.com")
                                .param("description", "desc")
                                .param("distributionCode", "code")
                                .param("searchEngineExposed", "true")
                                .param("imageExposed", "true")
                                .param("tags", "tag1")
                                .param("address.zipcode", "12345")
                                .param("address.value", "대전광역시 어딘가")
                                .param("contactNumber", "010-1234-5678")
                                .param("intro", "intro")
                                .param("content", "content")
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
    }

    @Test
    @DisplayName("사이트 삭제 - 204 No Content")
    void deleteSite() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/sites/{id}", "id-1"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNoContent());
        Mockito.verify(siteService).deleteSite("id-1");
    }

}

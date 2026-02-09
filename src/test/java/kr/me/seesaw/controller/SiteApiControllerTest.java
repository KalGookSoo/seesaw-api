package kr.me.seesaw.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import kr.me.seesaw.command.CreateSiteCommand;
import kr.me.seesaw.core.authentication.PrincipalProvider;
import kr.me.seesaw.model.SiteModel;
import kr.me.seesaw.service.SiteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
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
@WebMvcTest(controllers = {SiteApiController.class})
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class SiteApiControllerTest {

    private final MockMvc mockMvc;

    @MockitoBean
    private SiteService siteService;

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

        List<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList("ROLE_ADMIN");
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
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode body = objectMapper.createObjectNode();
        body.put("name", "site-name");
        body.put("domainName", "example.com");
        body.put("description", "desc");
        body.put("distributionCode", "code");
        body.put("searchEngineExposed", true);
        body.put("imageExposed", true);
        body.put("tags", "tag1,tag2");
        ObjectNode address = objectMapper.createObjectNode();
        address.put("zipcode", "");
        address.put("value", "");
        body.set("address", address);
        body.put("contactNumber", "010-0000-0000");
        body.put("intro", "intro");
        body.put("content", "content");

        Mockito.when(siteService.createSite(Mockito.any(CreateSiteCommand.class))).thenReturn(Mockito.mock(SiteModel.class));

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/sites")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(body))
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
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode body = objectMapper.createObjectNode();
        body.put("name", "updated");
        body.put("domainName", "example.com");
        body.put("description", "desc");
        body.put("distributionCode", "code");
        body.put("searchEngineExposed", true);
        body.put("imageExposed", true);
        body.put("tags", "tag1");
        ObjectNode address = objectMapper.createObjectNode();
        address.put("zipcode", "");
        address.put("value", "");
        body.set("address", address);
        body.put("contactNumber", "010");
        body.put("intro", "intro");
        body.put("content", "content");

        Mockito.when(siteService.updateSite(Mockito.eq("id-1"), Mockito.any(CreateSiteCommand.class))).thenReturn(Mockito.mock(SiteModel.class));

        mockMvc.perform(
                        MockMvcRequestBuilders.put("/api/sites/{id}", "id-1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(body))
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

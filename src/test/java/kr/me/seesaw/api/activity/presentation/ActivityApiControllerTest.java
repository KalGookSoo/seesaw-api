package kr.me.seesaw.api.activity.presentation;

import jakarta.persistence.EntityManager;
import kr.me.seesaw.core.domain.article.Article;
import kr.me.seesaw.core.domain.category.Category;
import kr.me.seesaw.core.domain.site.Site;
import kr.me.seesaw.framework.TestDataInitializerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(TestDataInitializerConfig.class)
@SpringBootTest
@Transactional
class ActivityApiControllerTest {
    @Autowired MockMvc mvc;
    @Autowired EntityManager em;

    @Test
    void 비로그인_사용자의_조회는_거부한다() throws Exception {
        mvc.perform(get("/api/me/activity/articles").param("siteId", site("test1.local").getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void 사이트_권한이_없는_사이트의_조회는_거부한다() throws Exception {
        mvc.perform(get("/api/me/activity/articles").param("siteId", site("test2.local").getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void 로그인_사용자에게_본인의_공개_게시글을_반환한다() throws Exception {
        Category category = em.createQuery("select c from Category c where c.name = :name", Category.class)
                .setParameter("name", "자유").getSingleResult();
        Article article = new Article(); article.setCategory(category); article.setTitle("내 활동 테스트");
        article.setContent("<p>본문</p>"); article.setCreatedBy("user"); article.setExposed(true); em.persist(article); em.flush();
        mvc.perform(get("/api/me/activity/articles").param("siteId", site("test1.local").getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("내 활동 테스트"))
                .andExpect(jsonPath("$.content[0].preview").value("본문"));
    }

    private Site site(String domain) {
        return em.createQuery("select s from Site s where s.domainName = :domain", Site.class)
                .setParameter("domain", domain).getSingleResult();
    }
}

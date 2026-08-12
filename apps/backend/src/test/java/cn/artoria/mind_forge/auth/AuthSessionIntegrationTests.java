package cn.artoria.mind_forge.auth;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import cn.artoria.mind_forge.support.AuthTestHelper;
import cn.artoria.mind_forge.support.AuthTestHelper.CsrfSession;
import cn.artoria.mind_forge.support.AuthTestHelper.TestUser;
import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(AuthTestHelper.class)
public class AuthSessionIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthTestHelper auth;

    @Test
    void loginThenMeUsesTheSameSession() throws Exception {
        TestUser user = auth.createUser();

        MockHttpSession session = auth.login(user);

        expectCurrentUser(session, user);
    }

    @Test
    void logoutThenMeReturnsJson401() throws Exception {
        TestUser user = auth.createUser();
        MockHttpSession session = auth.login(user);
        expectCurrentUser(session, user);

        mockMvc.perform(post("/api/auth/logout")
                .with(auth.fetchCsrfSession(session)))
                .andExpect(status().isNoContent());

        expectAuthenticationRequired(session);
    }

    @Test
    void loginChangesExistingSessionIdAndPreservesAuthentication() throws Exception {
        MockHttpSession anonymousSession = auth.createSession();
        String sessionIdBeforeLogin = anonymousSession.getId();
        TestUser user = auth.createUser();

        MockHttpSession authenticatedSession = auth.login(user, anonymousSession);

        assertNotEquals(sessionIdBeforeLogin, authenticatedSession.getId());
        expectCurrentUser(authenticatedSession, user);
    }

    @Test
    void csrfTokenLifecycleAcrossLoginAndLogout() throws Exception {
        TestUser user = auth.createUser();
        CsrfSession anonymousCsrf = auth.fetchCsrfSession();

        expectCsrfForbidden(mockMvc.perform(auth.loginRequest(user, anonymousCsrf.session())));
        expectCsrfForbidden(mockMvc.perform(
                auth.loginRequest(user, anonymousCsrf.session())
                        .header(anonymousCsrf.headerName(), "invalid-csrf-token")));

        auth.login(user, anonymousCsrf);

        expectCsrfForbidden(mockMvc.perform(
                anonymousCsrf.applyTo(post("/api/auth/logout"))));

        CsrfSession authenticatedCsrf = auth.fetchCsrfSession(anonymousCsrf.session());
        mockMvc.perform(authenticatedCsrf.applyTo(post("/api/auth/logout")))
                .andExpect(status().isNoContent());

        CsrfSession postLogoutCsrf = auth.fetchCsrfSession();
        expectCsrfForbidden(mockMvc.perform(
                auth.loginRequest(user, postLogoutCsrf.session())
                        .header(postLogoutCsrf.headerName(), authenticatedCsrf.token())));

        auth.login(user, postLogoutCsrf);
    }

    private void expectCurrentUser(MockHttpSession session, TestUser user) throws Exception {
        mockMvc.perform(get("/api/auth/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(user.email()))
                .andExpect(jsonPath("$.username").value(user.username()));
    }

    private void expectAuthenticationRequired(MockHttpSession session) throws Exception {
        mockMvc.perform(get("/api/auth/me").session(session))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"))
                .andExpect(jsonPath("$.message").value("请先登录"));
    }

    private static void expectCsrfForbidden(ResultActions resultActions) throws Exception {
        resultActions
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("CSRF_TOKEN_INVALID"))
                .andExpect(jsonPath("$.message").value("CSRF token 缺失或无效"));
    }
}

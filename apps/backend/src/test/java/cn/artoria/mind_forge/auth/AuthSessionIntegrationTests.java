package cn.artoria.mind_forge.auth;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import jakarta.transaction.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AuthSessionIntegrationTests {

    private static final String TEST_PASSWORD = "correct-password";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void loginThenMeUsesTheSameSession() throws Exception {
        TestUser user = createTestUser();

        MockHttpSession session = loginWithTestCsrf(user);

        expectCurrentUser(session, user);
    }

    @Test
    void logoutThenMeReturnsJson401() throws Exception {
        TestUser user = createTestUser();
        MockHttpSession session = loginWithTestCsrf(user);
        expectCurrentUser(session, user);

        mockMvc.perform(post("/api/auth/logout")
                .with(csrf())
                .session(session))
                .andExpect(status().isNoContent());

        expectAuthenticationRequired(session);
    }

    @Test
    void loginChangesExistingSessionIdAndPreservesAuthentication() throws Exception {
        MockHttpSession anonymousSession = new MockHttpSession();
        String sessionIdBeforeLogin = anonymousSession.getId();
        TestUser user = createTestUser();

        MockHttpSession authenticatedSession = loginWithTestCsrf(user, anonymousSession);

        assertNotEquals(sessionIdBeforeLogin, authenticatedSession.getId());
        expectCurrentUser(authenticatedSession, user);
    }

    @Test
    void csrfTokenLifecycleAcrossLoginAndLogout() throws Exception {
        TestUser user = createTestUser();
        CsrfSession anonymousCsrf = fetchCsrfSession();

        expectCsrfForbidden(mockMvc.perform(loginRequest(user, anonymousCsrf.session())));
        expectCsrfForbidden(mockMvc.perform(
                loginRequest(user, anonymousCsrf.session())
                        .header(anonymousCsrf.headerName(), "invalid-csrf-token")));

        loginSuccessfully(anonymousCsrf.applyTo(loginRequest(user)), user);

        expectCsrfForbidden(mockMvc.perform(
                anonymousCsrf.applyTo(post("/api/auth/logout"))));

        CsrfSession authenticatedCsrf = fetchCsrfSession(anonymousCsrf.session());
        mockMvc.perform(authenticatedCsrf.applyTo(post("/api/auth/logout")))
                .andExpect(status().isNoContent());

        CsrfSession postLogoutCsrf = fetchCsrfSession();
        expectCsrfForbidden(mockMvc.perform(
                loginRequest(user, postLogoutCsrf.session())
                        .header(postLogoutCsrf.headerName(), authenticatedCsrf.token())));

        loginSuccessfully(postLogoutCsrf.applyTo(loginRequest(user)), user);
    }

    private TestUser createTestUser() {
        String suffix = UUID.randomUUID().toString();
        TestUser user = new TestUser(
                "test" + suffix + "@example.com",
                "test" + suffix,
                TEST_PASSWORD);

        jdbcTemplate.update(
                """
                        INSERT INTO users (email, username, password_hash)
                        VALUES (?, ?, ?)
                        """,
                user.email(),
                user.username(),
                passwordEncoder.encode(user.password()));

        return user;
    }

    private MockHttpSession loginWithTestCsrf(TestUser user) throws Exception {
        return sessionFrom(loginSuccessfully(loginRequest(user).with(csrf()), user));
    }

    private MockHttpSession loginWithTestCsrf(TestUser user, MockHttpSession session) throws Exception {
        return sessionFrom(loginSuccessfully(loginRequest(user, session).with(csrf()), user));
    }

    private MvcResult loginSuccessfully(MockHttpServletRequestBuilder request, TestUser user) throws Exception {
        return mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(user.email()))
                .andExpect(jsonPath("$.username").value(user.username()))
                .andReturn();
    }

    private CsrfSession fetchCsrfSession() throws Exception {
        return fetchCsrfSession(get("/api/auth/csrf"));
    }

    private CsrfSession fetchCsrfSession(MockHttpSession session) throws Exception {
        return fetchCsrfSession(get("/api/auth/csrf").session(session));
    }

    private CsrfSession fetchCsrfSession(MockHttpServletRequestBuilder request) throws Exception {
        MvcResult result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.headerName").value("X-CSRF-TOKEN"))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return new CsrfSession(
                body.get("headerName").asString(),
                body.get("token").asString(),
                sessionFrom(result));
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

    private static MockHttpServletRequestBuilder loginRequest(TestUser user) {
        return post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                            {
                                "email": "%s",
                                "password": "%s"
                            }
                        """.formatted(user.email(), user.password()));
    }

    private static MockHttpServletRequestBuilder loginRequest(TestUser user, MockHttpSession session) {
        return loginRequest(user).session(session);
    }

    private static MockHttpSession sessionFrom(MvcResult result) {
        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
        assertNotNull(session);
        return session;
    }

    private static void expectCsrfForbidden(ResultActions resultActions) throws Exception {
        resultActions
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("CSRF_TOKEN_INVALID"))
                .andExpect(jsonPath("$.message").value("CSRF token 缺失或无效"));
    }

    private record TestUser(String email, String username, String password) {
    }

    private record CsrfSession(String headerName, String token, MockHttpSession session) {

        MockHttpServletRequestBuilder applyTo(MockHttpServletRequestBuilder request) {
            return request
                    .header(headerName, token)
                    .session(session);
        }
    }
}

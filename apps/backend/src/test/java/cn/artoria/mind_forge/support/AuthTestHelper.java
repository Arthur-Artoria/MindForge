package cn.artoria.mind_forge.support;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

public final class AuthTestHelper {

    private static final String TEST_PASSWORD = "correct-password";

    private final MockMvc mockMvc;
    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    public AuthTestHelper(
            MockMvc mockMvc,
            JdbcTemplate jdbcTemplate,
            PasswordEncoder passwordEncoder,
            ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper;
    }

    public TestUser createUser() {
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

    public MockHttpSession createSession() {
        return new MockHttpSession();
    }

    public MockHttpSession login(TestUser user) throws Exception {
        return login(user, fetchCsrfSession());
    }

    public MockHttpSession login(TestUser user, MockHttpSession session) throws Exception {
        return login(user, fetchCsrfSession(session));
    }

    public MockHttpSession login(TestUser user, CsrfSession csrfSession) throws Exception {
        MvcResult result = mockMvc.perform(csrfSession.applyTo(loginRequest(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(user.email()))
                .andExpect(jsonPath("$.username").value(user.username()))
                .andReturn();

        return sessionFrom(result);
    }

    public CsrfSession fetchCsrfSession() throws Exception {
        return fetchCsrfSession(get("/api/auth/csrf"));
    }

    public CsrfSession fetchCsrfSession(MockHttpSession session) throws Exception {
        return fetchCsrfSession(get("/api/auth/csrf").session(session));
    }

    public MockHttpServletRequestBuilder loginRequest(TestUser user) {
        return post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                            {
                                "email": "%s",
                                "password": "%s"
                            }
                        """.formatted(user.email(), user.password()));
    }

    public MockHttpServletRequestBuilder loginRequest(TestUser user, MockHttpSession session) {
        return loginRequest(user).session(session);
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

    private static MockHttpSession sessionFrom(MvcResult result) {
        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
        assertNotNull(session);
        return session;
    }

    public record TestUser(String email, String username, String password) {
    }

    public record CsrfSession(String headerName, String token, MockHttpSession session)
            implements RequestPostProcessor {

        public MockHttpServletRequestBuilder applyTo(MockHttpServletRequestBuilder request) {
            return request.with(this);
        }

        @Override
        public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
            request.setSession(session);
            request.addHeader(headerName, token);
            return request;
        }
    }
}

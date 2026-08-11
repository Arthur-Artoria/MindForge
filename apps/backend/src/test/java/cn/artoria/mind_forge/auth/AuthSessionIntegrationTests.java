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

import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AuthSessionIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void loginThenMeUsesTheSameSession() throws Exception {
        String suffix = UUID.randomUUID().toString();
        String email = "test" + suffix + "@example.com";
        String username = "test" + suffix;
        String password = "correct-password";

        jdbcTemplate.update(
                """
                        INSERT INTO users (email, username, password_hash)
                        VALUES (?, ?, ?)
                        """,
                email,
                username,
                passwordEncoder.encode(password));

        // 登录
        MvcResult loginResult = mockMvc.perform(
                post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                    {
                                        "email": "%s",
                                        "password": "%s"
                                    }
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.username").value(username))
                .andReturn();

        // 取得登录后的 Session
        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);

        assertNotNull(session);

        // 携带登录产生的同一个 Session 访问 /api/auth/me
        mockMvc.perform(get("/api/auth/me")
                .session(session)).andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.username").value(username));

    }

    @Test
    void logoutThenMeReturnsJson401() throws Exception {
        String suffix = UUID.randomUUID().toString();
        String email = "test" + suffix + "@example.com";
        String username = "test" + suffix;
        String password = "correct-password";

        jdbcTemplate.update(
                """
                        INSERT INTO users (email, username, password_hash)
                        VALUES (?, ?, ?)
                        """,
                email,
                username,
                passwordEncoder.encode(password));

        // 登录
        MvcResult loginResult = mockMvc.perform(
                post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                    {
                                        "email": "%s",
                                        "password": "%s"
                                    }
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.username").value(username))
                .andReturn();

        // 取得登录后的 Session
        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);

        assertNotNull(session);

        // 携带登录产生的同一个 Session 访问 /api/auth/me
        mockMvc.perform(get("/api/auth/me")
                .session(session)).andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.username").value(username));

        // 登出
        mockMvc.perform(post("/api/auth/logout")
                .with(csrf())
                .session(session))
                .andExpect(status().isNoContent());

        // 携带登出后的 Session 访问 /api/auth/me
        mockMvc.perform(get("/api/auth/me")
                .session(session)).andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"))
                .andExpect(jsonPath("$.message").value("请先登录"));

    }

    /**
     * 登录前已有匿名 session，登录成功后轮换已有 Session 的 ID，并保留认证状态，并且能够使用 /api/auth/me 获取到用户信息
     */
    @Test
    void loginChangesExistingSessionIdAndPreservesAuthentication() throws Exception {
        // 创建一个匿名 session
        MockHttpSession anonymousSession = new MockHttpSession();
        String sessionIdBeforeLogin = anonymousSession.getId();

        // 创建一个用户
        String suffix = UUID.randomUUID().toString();
        String email = "test" + suffix + "@example.com";
        String username = "test" + suffix;
        String password = "correct-password";

        jdbcTemplate.update(
                """
                        INSERT INTO users (email, username, password_hash)
                        VALUES (?, ?, ?)
                        """,
                email,
                username,
                passwordEncoder.encode(password));

        // 携带匿名 session 访问 login
        // 登录
        MvcResult loginResult = mockMvc.perform(
                post("/api/auth/login")
                        .with(csrf())
                        .session(anonymousSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                    {
                                        "email": "%s",
                                        "password": "%s"
                                    }
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.username").value(username))
                .andReturn();

        // 取得登录后的 Session
        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);

        // 断言登录后的新 session 不为空
        assertNotNull(session);
        String sessionIdAfterLogin = session.getId();

        // 断言登录后的新 session 与匿名 session 不同
        assertNotEquals(sessionIdBeforeLogin, sessionIdAfterLogin);

        // 携带登录后的 Session 访问 /api/auth/me
        mockMvc.perform(get("/api/auth/me")
                .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.username").value(username));
    }
}

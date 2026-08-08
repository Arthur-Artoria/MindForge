package cn.artoria.mind_forge.auth;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
}

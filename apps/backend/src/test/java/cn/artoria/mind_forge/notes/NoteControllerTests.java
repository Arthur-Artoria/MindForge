package cn.artoria.mind_forge.notes;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import cn.artoria.mind_forge.auth.CurrentUserResponse;
import cn.artoria.mind_forge.note.NoteResponse;
import cn.artoria.mind_forge.support.AuthTestHelper;
import cn.artoria.mind_forge.support.AuthTestHelper.CsrfSession;
import cn.artoria.mind_forge.support.AuthTestHelper.TestUser;
import jakarta.transaction.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(AuthTestHelper.class)
public class NoteControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthTestHelper auth;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 未登录创建 Note 返回 401
     */
    @Test
    void createNoteWithoutLoginReturnJson401() throws Exception {
        auth.expectAuthenticationRequired(mockMvc.perform(post("/api/notes").with(csrf())));
    }

    /**
     * 已登录但缺少 csrf 返回 403
     */
    @Test
    void createNoteWithoutCsrfReturnJson403() throws Exception {
        TestUser user = auth.createUser();
        CsrfSession csrfSession = auth.fetchCsrfSession();
        auth.login(user, csrfSession);
        auth.expectCsrfForbidden(mockMvc.perform(post("/api/notes").session(csrfSession.session())));
    }

    /**
     * 错误请求体返回 400
     */
    @Test
    void createNoteWithInvalidRequestBodyReturnJson400() throws Exception {
        TestUser user = auth.createUser();
        MockHttpSession session = auth.login(user);
        CsrfSession authenticatedCsrf = auth.fetchCsrfSession(session);

        mockMvc.perform(authenticatedCsrf.applyTo(post("/api/notes").contentType(MediaType.APPLICATION_JSON).content("""
                {
                    "title": "",
                    "content": "This is a test note"
                }
                """)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value("请求参数无效"));
    }

    /**
     * 已登录且 CSRF token 有效：创建成功，返回 201
     */
    @Test
    void createNoteWithValidCsrfReturnJson201() throws Exception {
        TestUser user = auth.createUser();
        MockHttpSession session = auth.login(user);
        CsrfSession authenticatedCsrf = auth.fetchCsrfSession(session);

        createNote(authenticatedCsrf)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.title").value("Test Note"))
                .andExpect(jsonPath("$.content").value("This is a test note"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.updatedAt").isNotEmpty())
                .andExpect(jsonPath("$.userId").doesNotExist());
    }

    /**
     * 当前用户读取自己的 Note：200。
     */
    @Test
    void getNoteByCurrentUserReturnJson200() throws Exception {
        TestUser user = auth.createUser();
        MockHttpSession session = auth.login(user);
        CsrfSession authenticatedCsrf = auth.fetchCsrfSession(session);

        MvcResult createNoteResult = createNote(authenticatedCsrf).andReturn();
        NoteResponse noteResponse = objectMapper.readValue(createNoteResult.getResponse().getContentAsString(),
                NoteResponse.class);
        Long noteId = noteResponse.id();

        mockMvc.perform(authenticatedCsrf.applyTo(get("/api/notes/{noteId}", noteId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(noteId))
                .andExpect(jsonPath("$.title").value(noteResponse.title()))
                .andExpect(jsonPath("$.content").value(noteResponse.content()));
    }

    /**
     * 用户 B 读取用户 A 的 Note：404。
     */
    @Test
    void getNoteByOtherUserReturnJson404() throws Exception {
        TestUser user = auth.createUser();
        MockHttpSession session = auth.login(user);
        CsrfSession authenticatedCsrf = auth.fetchCsrfSession(session);

        MvcResult createNoteResult = createNote(authenticatedCsrf).andReturn();
        NoteResponse noteResponse = objectMapper.readValue(createNoteResult.getResponse().getContentAsString(),
                NoteResponse.class);
        Long noteId = noteResponse.id();

        TestUser userB = auth.createUser();
        MockHttpSession sessionB = auth.login(userB);
        CsrfSession authenticatedCsrfB = auth.fetchCsrfSession(sessionB);

        mockMvc.perform(authenticatedCsrfB.applyTo(get("/api/notes/{noteId}", noteId)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Note 不存在"));
    }

    /**
     * 数据库中的 `user_id` 来自登录用户，而不是请求体。
     */
    @Test
    void noteUserIdShouldBeFromLoggedInUser() throws Exception {
        // 创建一个其他用户，其 ID 将被恶意放入请求体
        TestUser otherUser = auth.createUser();
        Long forgedUserId = jdbcTemplate.queryForObject(
                "SELECT id FROM users WHERE email = ?",
                Long.class,
                otherUser.email());

        // 创建并登录真正发起请求的用户
        TestUser loggedInUser = auth.createUser();
        MockHttpSession session = auth.login(loggedInUser);
        CsrfSession authenticatedCsrf = auth.fetchCsrfSession(session);

        Long loggedInUserId = jdbcTemplate.queryForObject(
                "SELECT id FROM users WHERE email = ?",
                Long.class,
                loggedInUser.email());

        // 请求体恶意提交其他用户的 ID
        MvcResult result = mockMvc
                .perform(authenticatedCsrf.applyTo(post("/api/notes").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "Test Note",
                                    "content": "This is a test note",
                                    "userId": %d
                                }
                                """
                                .formatted(forgedUserId))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").doesNotExist())
                .andReturn();

        // 响应只用来取得新创建的 Note ID
        NoteResponse noteResponse = objectMapper.readValue(result.getResponse().getContentAsString(),
                NoteResponse.class);

        // 直接查询最终持久化结果
        Long storedUserId = jdbcTemplate.queryForObject(
                "SELECT user_id FROM notes WHERE id = ?",
                Long.class,
                noteResponse.id());

        assertEquals(loggedInUserId, storedUserId);
        assertNotEquals(forgedUserId, storedUserId);
    }

    private ResultActions createNote(CsrfSession authenticatedCsrf) throws Exception {
        return mockMvc.perform(
                authenticatedCsrf.applyTo(post("/api/notes").contentType(MediaType.APPLICATION_JSON).content("""
                        {
                            "title": "Test Note",
                            "content": "This is a test note"
                        }
                        """)));
    }
}

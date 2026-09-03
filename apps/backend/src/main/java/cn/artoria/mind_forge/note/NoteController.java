package cn.artoria.mind_forge.note;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.artoria.mind_forge.auth.AuthenticatedUser;
import cn.artoria.mind_forge.common.web.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @Operation(summary = "获取笔记列表", security = {
            @SecurityRequirement(name = "sessionCookie")
    })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "获取笔记列表成功", content = @Content(mediaType = "application/json", schema = @Schema(implementation = NoteResponse[].class))),
            @ApiResponse(responseCode = "401", description = "未登录", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("")
    public List<NoteResponse> get(@AuthenticationPrincipal AuthenticatedUser user) {
        return noteService.list(user.id());
    }

    @Operation(summary = "获取笔记", security = {
            @SecurityRequirement(name = "sessionCookie")
    }, parameters = {
            @Parameter(name = "noteId", description = "笔记ID", in = ParameterIn.PATH)
    })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "获取笔记成功", content = @Content(mediaType = "application/json", schema = @Schema(implementation = NoteResponse.class))),
            @ApiResponse(responseCode = "401", description = "未登录", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "笔记不存在", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/{noteId}")
    public NoteResponse get(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long noteId) {
        return noteService.getById(user.id(), noteId);
    }

    @Operation(summary = "创建笔记", security = {
            @SecurityRequirement(name = "sessionCookie")
    }, parameters = {
            @Parameter(ref = "#/components/parameters/csrfHeader"),
    })
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "创建笔记成功", content = @Content(mediaType = "application/json", schema = @Schema(implementation = NoteResponse.class))),
            @ApiResponse(responseCode = "400", description = "请求参数无效", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "未登录", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "CSRF token 无效", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("")
    public ResponseEntity<NoteResponse> create(@AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody CreateNoteRequest request) {
        NoteResponse result = noteService.create(user.id(), request);

        return ResponseEntity.created(URI.create("/api/notes/" + result.id()))
                .body(result);
    }

    @Operation(summary = "删除笔记", security = {
            @SecurityRequirement(name = "sessionCookie")
    }, parameters = {
            @Parameter(name = "noteId", description = "笔记ID", in = ParameterIn.PATH),
            @Parameter(ref = "#/components/parameters/csrfHeader")
    })
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "删除笔记成功"),
            @ApiResponse(responseCode = "401", description = "未登录", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "CSRF token 无效", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "笔记不存在", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @DeleteMapping("/{noteId}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long noteId) {
        noteService.delete(user.id(), noteId);

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "更新笔记", security = {
            @SecurityRequirement(name = "sessionCookie")
    }, parameters = {
            @Parameter(name = "noteId", description = "笔记ID", in = ParameterIn.PATH),
            @Parameter(ref = "#/components/parameters/csrfHeader")
    })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "更新笔记成功", content = @Content(mediaType = "application/json", schema = @Schema(implementation = NoteResponse.class))),
            @ApiResponse(responseCode = "400", description = "请求参数无效", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "未登录", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "CSRF token 无效", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "笔记不存在", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PutMapping("/{noteId}")
    public NoteResponse update(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long noteId,
            @Valid @RequestBody UpdateNoteRequest request) {
        return noteService.update(user.id(), noteId, request);
    }
}

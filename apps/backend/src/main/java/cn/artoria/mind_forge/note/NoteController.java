package cn.artoria.mind_forge.note;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.artoria.mind_forge.auth.AuthenticatedUser;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @GetMapping("/{noteId}")
    public NoteResponse get(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long noteId) {
        return noteService.get(user.id(), noteId);
    }

    @PostMapping("")
    public ResponseEntity<NoteResponse> create(@AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody CreateNoteRequest request) {
        NoteResponse result = noteService.create(user.id(), request);

        return ResponseEntity.created(URI.create("/api/notes/" + result.id()))
                .body(result);
    }
}

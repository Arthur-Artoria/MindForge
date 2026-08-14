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
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @GetMapping("")
    public List<NoteResponse> get(@AuthenticationPrincipal AuthenticatedUser user) {
        return noteService.list(user.id());
    }

    @GetMapping("/{noteId}")
    public NoteResponse get(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long noteId) {
        return noteService.getById(user.id(), noteId);
    }

    @PostMapping("")
    public ResponseEntity<NoteResponse> create(@AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody CreateNoteRequest request) {
        NoteResponse result = noteService.create(user.id(), request);

        return ResponseEntity.created(URI.create("/api/notes/" + result.id()))
                .body(result);
    }

    @DeleteMapping("/{noteId}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long noteId) {
        noteService.delete(user.id(), noteId);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{noteId}")
    public NoteResponse update(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long noteId,
            @Valid @RequestBody UpdateNoteRequest request) {
        return noteService.update(user.id(), noteId, request);
    }
}

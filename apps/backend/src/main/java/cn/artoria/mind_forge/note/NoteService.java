package cn.artoria.mind_forge.note;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NoteService {
    private final NoteRepository noteRepository;

    public NoteService(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    @Transactional
    public NoteResponse create(Long currentUserId, CreateNoteRequest request) {
        Note note = Note.create(currentUserId, request.title(), request.content());

        return NoteResponse.from(noteRepository.save(note));
    }

    @Transactional(readOnly = true)
    public NoteResponse get(Long currentUserId, Long noteId) {
        Note note = noteRepository.findByIdAndUserId(noteId, currentUserId).orElseThrow(NoteNotFoundException::new);

        return NoteResponse.from(note);
    }

}

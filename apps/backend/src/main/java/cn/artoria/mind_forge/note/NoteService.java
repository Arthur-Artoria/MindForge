package cn.artoria.mind_forge.note;

import java.util.List;

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
    public NoteResponse getById(Long currentUserId, Long noteId) {
        Note note = findNote(currentUserId, noteId);

        return NoteResponse.from(note);
    }

    @Transactional(readOnly = true)
    public List<NoteResponse> list(Long currentUserId) {
        List<Note> notes = noteRepository.findAllByUserIdAndDeletedAtIsNullOrderByUpdatedAtDesc(currentUserId);

        return notes.stream().map(NoteResponse::from).toList();
    }

    @Transactional
    public void delete(Long currentUserId, Long noteId) {
        Note note = findNote(currentUserId, noteId);
        note.delete();
        noteRepository.save(note);
    }

    @Transactional
    public NoteResponse update(Long currentUserId, Long noteId, UpdateNoteRequest request) {
        Note note = findNote(currentUserId, noteId);
        note.update(request.title(), request.content());
        return NoteResponse.from(noteRepository.save(note));
    }

    private Note findNote(Long currentUserId, Long noteId) {
        return noteRepository.findByIdAndUserIdAndDeletedAtIsNull(noteId, currentUserId)
                .orElseThrow(NoteNotFoundException::new);
    }
}

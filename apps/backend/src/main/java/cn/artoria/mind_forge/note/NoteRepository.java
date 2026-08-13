package cn.artoria.mind_forge.note;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NoteRepository extends JpaRepository<Note, Long> {
    Optional<Note> findByIdAndUserId(Long id, Long userId);

    List<Note> findAllByUserIdOrderByUpdatedAtDesc(Long userId);
}

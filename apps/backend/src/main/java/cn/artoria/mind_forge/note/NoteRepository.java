package cn.artoria.mind_forge.note;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NoteRepository extends JpaRepository<Note, Long> {
    Optional<Note> findByIdAndUserIdAndDeletedAtIsNull(Long id, Long userId);

    List<Note> findAllByUserIdAndDeletedAtIsNullOrderByUpdatedAtDesc(Long userId);
}

package cn.artoria.mind_forge.note;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateNoteRequest(
        @NotBlank
        @Size(max = 255)
        String title,
                        
        @NotNull
        String content) {
}

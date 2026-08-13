
package cn.artoria.mind_forge.note;

public class NoteNotFoundException extends RuntimeException {
    public NoteNotFoundException() {
        super("Note not found");
    }
}
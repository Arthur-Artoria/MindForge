package cn.artoria.mind_forge.note;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import cn.artoria.mind_forge.common.web.ApiErrorCode;
import cn.artoria.mind_forge.common.web.ApiErrorResponse;

@RestControllerAdvice(assignableTypes = NoteController.class)
public class NoteExceptionHandler {

    @ExceptionHandler(NoteNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoteNotFoundException(NoteNotFoundException ex) {
        ApiErrorResponse errorResponse = new ApiErrorResponse(ApiErrorCode.RESOURCE_NOT_FOUND, "Note 不存在");

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

}

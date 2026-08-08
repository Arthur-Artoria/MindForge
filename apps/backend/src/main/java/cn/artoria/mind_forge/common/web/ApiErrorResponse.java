package cn.artoria.mind_forge.common.web;

public record ApiErrorResponse(
        ApiErrorCode code,
        String message) {

}

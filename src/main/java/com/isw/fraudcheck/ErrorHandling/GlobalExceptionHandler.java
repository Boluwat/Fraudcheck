package com.isw.fraudcheck.ErrorHandling;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorHandlingDTO> handleNotFound(ResourceNotFoundException ex){
        ErrorHandlingDTO error = new ErrorHandlingDTO(ex.getMessage(), 404);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorHandlingDTO> handleBadRequest(BadRequestException ex){
        ErrorHandlingDTO error = new ErrorHandlingDTO(ex.getMessage(), 400);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorHandlingDTO> handleException(Exception ex){
        ErrorHandlingDTO error = new ErrorHandlingDTO(ex.getMessage(), 500);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(DuplicateRequestException.class)
    public ResponseEntity<ErrorHandlingDTO> handleDuplicateRequest(DuplicateRequestException ex){
        ErrorHandlingDTO error = new ErrorHandlingDTO(ex.getMessage(), 409);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(ForbiddenRequestException.class)
    public ResponseEntity<ErrorHandlingDTO> handleForbiddenRequest(ForbiddenRequestException ex){
        ErrorHandlingDTO error = new ErrorHandlingDTO(ex.getMessage(), 403);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorHandlingDTO> handleUnauthorizedException(UnauthorizedException e) {
        ErrorHandlingDTO error =new ErrorHandlingDTO(e.getMessage(), 401);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

}

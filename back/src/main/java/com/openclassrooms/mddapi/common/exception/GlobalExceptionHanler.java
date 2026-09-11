package com.openclassrooms.mddapi.common.exception;

import com.openclassrooms.mddapi.common.dto.ErrorResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHanler {

    // Not found exception
    @ExceptionHandler(value = NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponseDto handleNotFoundException(NotFoundException e) {
        log.warn(e.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, e.getMessage());
    }

    /**
     * Response builder
     *
     * @param status http status
     * @param message string
     * @return ErrorResponseDto
     */
    private ErrorResponseDto buildResponse(HttpStatus status, String message) {
        return new ErrorResponseDto(
                status,
                message,
                Instant.now().toString()
        );
    }

}

package com.openclassrooms.mddapi.common.exception;

import com.openclassrooms.mddapi.auth.exception.InvalidTokenException;
import com.openclassrooms.mddapi.auth.exception.UnauthorizedException;
import com.openclassrooms.mddapi.common.dto.ErrorResponseDto;
import com.openclassrooms.mddapi.topic.exception.TopicNotFoundException;
import com.openclassrooms.mddapi.topic.exception.TopicSlugAlreadyExists;
import com.openclassrooms.mddapi.user.exceptions.EmailAlreadyExistsException;
import com.openclassrooms.mddapi.user.exceptions.UserAlreadyExistsException;
import com.openclassrooms.mddapi.user.exceptions.UserNotFoundException;
import com.openclassrooms.mddapi.user.exceptions.UsernameAlreadyExistsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHanler {

    // Not found exceptions

    @ExceptionHandler(value = NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponseDto handleNotFoundException(NotFoundException e) {
        log.warn(e.getMessage(), e);
        return buildResponse(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(value = UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponseDto handleUserNotFoundException(UserNotFoundException e) {
        log.warn(e.getMessage(), e);
        return buildResponse(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(value = TopicNotFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public ErrorResponseDto handleTopicNotFoundException(TopicNotFoundException e) {
        log.warn(e.getMessage(), e);
        return buildResponse(HttpStatus.NOT_FOUND, e.getMessage());
    }

    // Conflict exceptions

    @ExceptionHandler(value = EmailAlreadyExistsException.class)
    @ResponseStatus(value = HttpStatus.CONFLICT)
    public ErrorResponseDto handleEmailAlreadyExistsException(EmailAlreadyExistsException e) {
        log.warn(e.getMessage(), e);
        return buildResponse(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(value = UserAlreadyExistsException.class)
    @ResponseStatus(value = HttpStatus.CONFLICT)
    public ErrorResponseDto handleUserAlreadyExistsException(UserAlreadyExistsException e) {
        log.warn(e.getMessage(), e);
        return buildResponse(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(value = UsernameAlreadyExistsException.class)
    @ResponseStatus(value = HttpStatus.CONFLICT)
    public ErrorResponseDto handleUsernameAlreadyExistsException(UsernameAlreadyExistsException e) {
        log.warn(e.getMessage(), e);
        return buildResponse(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(value = TopicSlugAlreadyExists.class)
    @ResponseStatus(value = HttpStatus.CONFLICT)
    public ErrorResponseDto handleTopicSlugAlreadyExists(TopicSlugAlreadyExists e) {
        log.warn(e.getMessage(), e);
        return buildResponse(HttpStatus.CONFLICT, e.getMessage());
    }

    // unauthorized

    @ExceptionHandler(value = UnauthorizedException.class)
    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    public ErrorResponseDto handleUnauthorizedException(UnauthorizedException e) {
        log.warn(e.getMessage(), e);
        return buildResponse(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(value = InvalidTokenException.class)
    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    public ErrorResponseDto handleInvalidTokenException(InvalidTokenException e) {
        log.warn(e.getMessage(), e);
        return buildResponse(HttpStatus.CONFLICT, e.getMessage());
    }

    // Internal server error exceptions

    @ExceptionHandler(value = Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponseDto handleException(Exception e) {
        log.error(e.getMessage(), e);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
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

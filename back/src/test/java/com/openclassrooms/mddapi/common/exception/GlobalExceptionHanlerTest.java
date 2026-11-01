
package com.openclassrooms.mddapi.common.exception;

import com.openclassrooms.mddapi.article.exception.ArticleNotFoundException;
import com.openclassrooms.mddapi.auth.exception.InvalidTokenException;
import com.openclassrooms.mddapi.auth.exception.UnauthorizedException;
import com.openclassrooms.mddapi.common.dto.ErrorResponseDto;
import com.openclassrooms.mddapi.topic.exception.TopicNotFoundException;
import com.openclassrooms.mddapi.user.exceptions.EmailAlreadyExistsException;
import com.openclassrooms.mddapi.user.exceptions.UserAlreadyExistsException;
import com.openclassrooms.mddapi.user.exceptions.UserNotFoundException;
import com.openclassrooms.mddapi.user.exceptions.UsernameAlreadyExistsException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;

import javax.naming.AuthenticationException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHanlerTest {

    private final GlobalExceptionHanler handler = new GlobalExceptionHanler();

    private void assertResponse(
            ErrorResponseDto actual,
            HttpStatus expectedStatus,
            String expectedMessage
    ) {
        assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields("timestamp")
                .isEqualTo(new ErrorResponseDto(
                        expectedStatus,
                        expectedMessage,
                        null
                ));
    }

    @Test
    void shouldHandleNotFoundException() {
        NotFoundException exception = mock(NotFoundException.class);
        when(exception.getMessage()).thenReturn("Resource not found");

        ErrorResponseDto response = handler.handleNotFoundException(exception);

        assertResponse(response, HttpStatus.NOT_FOUND, "Resource not found");
    }

    @Test
    void shouldHandleArticleNotFoundException() {
        ArticleNotFoundException exception = mock(ArticleNotFoundException.class);
        when(exception.getMessage()).thenReturn("Article not found");

        ErrorResponseDto response = handler.handleArticleNotFoundException(exception);

        assertResponse(response, HttpStatus.NOT_FOUND, "Article not found");
    }

    @Test
    void shouldHandleUserNotFoundException() {
        UserNotFoundException exception = mock(UserNotFoundException.class);
        when(exception.getMessage()).thenReturn("User not found");

        ErrorResponseDto response = handler.handleUserNotFoundException(exception);

        assertResponse(response, HttpStatus.NOT_FOUND, "User not found");
    }

    @Test
    void shouldHandleTopicNotFoundException() {
        TopicNotFoundException exception = mock(TopicNotFoundException.class);
        when(exception.getMessage()).thenReturn("Topic not found");

        ErrorResponseDto response = handler.handleTopicNotFoundException(exception);

        assertResponse(response, HttpStatus.NOT_FOUND, "Topic not found");
    }

    @Test
    void shouldHandleMissingServletRequestParameter() {
        MissingServletRequestParameterException exception =
                new MissingServletRequestParameterException("page", "Integer");

        ErrorResponseDto response =
                handler.handleMissingServletRequestParameter(exception);

        assertResponse(
                response,
                HttpStatus.BAD_REQUEST,
                exception.getMessage()
        );
    }

    @Test
    void shouldHandleEmailAlreadyExistsException() {
        EmailAlreadyExistsException exception = mock(EmailAlreadyExistsException.class);
        when(exception.getMessage()).thenReturn("Email already exists");

        ErrorResponseDto response =
                handler.handleEmailAlreadyExistsException(exception);

        assertResponse(response, HttpStatus.CONFLICT, "Email already exists");
    }

    @Test
    void shouldHandleUserAlreadyExistsException() {
        UserAlreadyExistsException exception = mock(UserAlreadyExistsException.class);
        when(exception.getMessage()).thenReturn("User already exists");

        ErrorResponseDto response =
                handler.handleUserAlreadyExistsException(exception);

        assertResponse(response, HttpStatus.CONFLICT, "User already exists");
    }

    @Test
    void shouldHandleUsernameAlreadyExistsException() {
        UsernameAlreadyExistsException exception =
                mock(UsernameAlreadyExistsException.class);
        when(exception.getMessage()).thenReturn("Username already exists");

        ErrorResponseDto response =
                handler.handleUsernameAlreadyExistsException(exception);

        assertResponse(response, HttpStatus.CONFLICT, "Username already exists");
    }

    @Test
    void shouldHandleAuthorizationDeniedException() {
        AuthorizationDeniedException exception =
                mock(AuthorizationDeniedException.class);
        when(exception.getMessage()).thenReturn("Access denied");

        ErrorResponseDto response = handler.handleAuthorizationDenied(exception);

        assertResponse(response, HttpStatus.FORBIDDEN, "Access denied");
    }

    @Test
    void shouldHandleAuthenticationException() {
        AuthenticationException exception = mock(AuthenticationException.class);
        when(exception.getMessage()).thenReturn("Authentication failed");

        ErrorResponseDto response =
                handler.handleAuthenticationException(exception);

        assertResponse(
                response,
                HttpStatus.UNAUTHORIZED,
                "Identifiants invalides"
        );
    }

    @Test
    void shouldHandleUnauthorizedException() {
        UnauthorizedException exception = mock(UnauthorizedException.class);
        when(exception.getMessage()).thenReturn("Unauthorized");

        ErrorResponseDto response =
                handler.handleUnauthorizedException(exception);

        assertResponse(response, HttpStatus.UNAUTHORIZED, "Unauthorized");
    }

    @Test
    void shouldHandleInvalidTokenException() {
        InvalidTokenException exception = mock(InvalidTokenException.class);
        when(exception.getMessage()).thenReturn("Invalid token");

        ErrorResponseDto response =
                handler.handleInvalidTokenException(exception);

        assertResponse(response, HttpStatus.UNAUTHORIZED, "Invalid token");
    }

    @Test
    void shouldHandleValidationException() {
        MethodArgumentNotValidException exception =
                mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(
                new FieldError("userDto", "email", "must be valid"),
                new FieldError("userDto", "username", "must not be blank")
        ));

        ErrorResponseDto response = handler.handleValidationException(exception);

        assertResponse(
                response,
                HttpStatus.BAD_REQUEST,
                "email: must be valid, username: must not be blank"
        );
    }

    @Test
    void shouldHandleUnexpectedException() {
        Exception exception = new Exception("Unexpected error");

        ErrorResponseDto response = handler.handleException(exception);

        assertResponse(
                response,
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Unexpected error"
        );
    }
}

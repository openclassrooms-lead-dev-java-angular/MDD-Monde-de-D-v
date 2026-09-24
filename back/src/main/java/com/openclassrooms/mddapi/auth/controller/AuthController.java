package com.openclassrooms.mddapi.auth.controller;

import com.openclassrooms.mddapi.auth.service.AuthService;
import com.openclassrooms.mddapi.auth.dto.LoginRequestDto;
import com.openclassrooms.mddapi.auth.dto.RegisterRequestDto;
import com.openclassrooms.mddapi.auth.dto.UsernameAvailableDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public void login(
            @Valid @RequestBody LoginRequestDto dto,
            HttpServletResponse response
    ) {
        Map<String, ResponseCookie> cookieMap = authService.authenticate(dto);

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                cookieMap.get("accessTokenCookie").toString()
        );

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                cookieMap.get("refreshTokenCookie").toString()
        );
    }

    @PostMapping(
            name = "/register",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void register(
            @Valid @RequestPart("register") RegisterRequestDto dto,
            @RequestPart(value = "media", required = false) MultipartFile media
    ) {
        authService.register(dto, media);
    }

    @PostMapping("/refresh")
    public void refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        ResponseCookie accessTokenCookie = authService.refreshAccessToken(request);

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                accessTokenCookie.toString()
        );
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(
            HttpServletResponse response
    ) {
        Map<String, ResponseCookie> cookies = authService.logout();

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                cookies.get("accessCookie").toString()
        );

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                cookies.get("refreshCookie").toString()
        );
    }

    @GetMapping("/username-available/{username}")
    public UsernameAvailableDto usernameAvailable(
            @PathVariable String username
    ) {
        return authService.usernameAvailable(username);
    }
}

package com.openclassrooms.mddapi.auth.security.jwt;

import com.openclassrooms.mddapi.auth.security.userDetails.UserDetailsImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtTokenServiceTest {

    @Mock
    private JwtEncoder jwtEncoder;

    @Mock
    private JwtDecoder jwtDecoder;

    @Mock
    private Jwt encodedJwt;

    @Mock
    private UserDetailsImpl userDetails;

    private JwtTokenService jwtTokenService;

    @BeforeEach
    void setUp() {
        jwtTokenService = new JwtTokenService(jwtEncoder, jwtDecoder);
        ReflectionTestUtils.setField(jwtTokenService, "accessTokenExpiration", 15);
        ReflectionTestUtils.setField(jwtTokenService, "refreshTokenExpiration", 30);
    }

    @Test
    void shouldGenerateAccessToken() {
        // Given
        when(userDetails.getId())
                .thenReturn(123L);
        when(userDetails.getUsername())
                .thenReturn("john.doe@example.com");
        when(encodedJwt.getTokenValue())
                .thenReturn("access.jwt.token");
        when(jwtEncoder.encode(any(JwtEncoderParameters.class)))
                .thenReturn(encodedJwt);

        // When
        String token = jwtTokenService.generateAccessToken(userDetails);

        // Then
        assertThat(token)
                .isEqualTo("access.jwt.token");

        ArgumentCaptor<JwtEncoderParameters> captor = ArgumentCaptor.forClass(JwtEncoderParameters.class);

        verify(jwtEncoder).encode(captor.capture());

        JwtEncoderParameters parameters = captor.getValue();
        JwsHeader header = parameters.getJwsHeader();

        assertThat(header).isNotNull();
        Assertions.assertNotNull(header);
        assertThat(header.getAlgorithm())
                .isEqualTo(SignatureAlgorithm.RS256);
        assertThat((Object)parameters.getClaims().getClaim("iss"))
                .isEqualTo("mdd-api");
        assertThat(parameters.getClaims().getSubject())
                .isEqualTo("123");
        assertThat(parameters.getClaims().getClaimAsString("email"))
                .isEqualTo("john.doe@example.com");
        assertThat(parameters.getClaims().getClaimAsString("type"))
                .isEqualTo("access");
        assertThat(parameters.getClaims().getIssuedAt())
                .isNotNull();
        assertThat(parameters.getClaims().getExpiresAt())
                .isNotNull();
        assertThat(parameters.getClaims().getExpiresAt())
                .isAfter(parameters.getClaims().getIssuedAt());
    }

    @Test
    void shouldGenerateRefreshToken() {
        // Given
        when(userDetails.getId())
                .thenReturn(123L);
        when(encodedJwt.getTokenValue())
                .thenReturn("refresh.jwt.token");
        when(jwtEncoder.encode(any(JwtEncoderParameters.class)))
                .thenReturn(encodedJwt);

        // When
        String token = jwtTokenService.generateRefreshToken(userDetails);

        // Then
        assertThat(token).isEqualTo("refresh.jwt.token");

        ArgumentCaptor<JwtEncoderParameters> captor = ArgumentCaptor.forClass(JwtEncoderParameters.class);

        verify(jwtEncoder).encode(captor.capture());

        JwtEncoderParameters parameters = captor.getValue();

        assertThat(parameters.getJwsHeader().getAlgorithm().getName())
                .isEqualTo("RS256");
        assertThat((Object)parameters.getClaims().getClaim("iss"))
                .isEqualTo("mdd-api");
        assertThat(parameters.getClaims().getSubject())
                .isEqualTo("123");
        assertThat(parameters.getClaims().getClaimAsString("type"))
                .isEqualTo("refresh");
        assertThat((Object)parameters.getClaims().getClaim("email"))
                .isNull();
        assertThat(parameters.getClaims().getIssuedAt())
                .isNotNull();
        assertThat(parameters.getClaims().getExpiresAt())
                .isNotNull();
        assertThat(parameters.getClaims().getExpiresAt())
                .isAfter(parameters.getClaims().getIssuedAt());
    }

    @Test
    void shouldDecodeAndValidateToken() {
        // Given
        String token = "jwt.token";
        Jwt decodedJwt = mock(Jwt.class);
        when(jwtDecoder.decode(token)).thenReturn(decodedJwt);

        // When
        Jwt result = jwtTokenService.decodeAndValidate(token);

        // Then
        assertThat(result)
                .isSameAs(decodedJwt);
        verify(jwtDecoder).decode(token);
    }

    @Test
    void shouldPropagateJwtDecoderException() {
        // Given
        String token = "invalid.jwt.token";
        JwtException exception = new JwtException("Invalid JWT");
        when(jwtDecoder.decode(token)).thenThrow(exception);

        // When / Then
        assertThatThrownBy(() -> jwtTokenService.decodeAndValidate(token))
                .isSameAs(exception);
        verify(jwtDecoder).decode(token);

    }
}
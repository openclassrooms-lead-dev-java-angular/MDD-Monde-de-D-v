package com.openclassrooms.mddapi.auth.security.jwt;

import com.openclassrooms.mddapi.auth.security.userDetails.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JwtTokenService {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    @Value("${app.jwt.expiration.access-token}")
    private int accessTokenExpiration;

    @Value("${app.jwt.expiration.refresh-token}")
    private int refreshTokenExpiration;

    /**
     * Generates a short-lived access token for the authenticated user.
     *
     * <p>The token contains the user's identifier as the subject, as well as
     * their email, roles and token type. The token is signed using the RSA
     * private key configured in the application's {@link JwtEncoder}.</p>
     *
     * @param userDetailsImpl authenticated user information
     * @return the encoded and signed JWT access token
     */
    public String generateAccessToken(UserDetailsImpl userDetailsImpl) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("mdd-api")
                .subject(userDetailsImpl.getId().toString())
                .issuedAt(now)
                .expiresAt(now.plus(Duration.ofMinutes(accessTokenExpiration)))
                .claim("email", userDetailsImpl.getUsername())
                .claim("roles", List.of(userDetailsImpl.getRole().name()))
                .claim("type", "access")
                .build();

        return encode(claims);
    }

    /**
     * Generates a long-lived refresh token for the authenticated user.
     *
     * <p>The refresh token intentionally contains fewer claims than the access
     * token. It only contains the user identifier and its token type.</p>
     *
     * @param userDetailsImpl authenticated user information
     * @return the encoded and signed JWT refresh token
     */
    public String generateRefreshToken(UserDetailsImpl userDetailsImpl) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("mdd-api")
                .subject(userDetailsImpl.getId().toString())
                .issuedAt(now)
                .expiresAt(now.plus(Duration.ofDays(refreshTokenExpiration)))
                .claim("type", "refresh")
                .build();

        return encode(claims);
    }

    /**
     * Decodes and validates a JWT.
     *
     * <p>The configured {@link JwtDecoder} verifies the JWT signature using
     * the configured RSA public key and validates the standard JWT claims
     * supported by the decoder, such as the token expiration.</p>
     *
     * @param token the encoded JWT
     * @return the decoded and validated JWT
     * @throws org.springframework.security.oauth2.jwt.JwtException
     * if the token is invalid, malformed, expired or has an invalid signature
     * */
    public Jwt decodeAndValidate(String token) {

        return jwtDecoder.decode(token);
    }

    /**
     * Encodes and signs the provided JWT claims.
     *
     * <p>The token is signed using the RSA SHA-256 (RS256) algorithm.</p>
     *
     * @param claims JWT claims to encode
     * @return the encoded and signed JWT
     */
    private String encode(JwtClaimsSet claims) {
        JwsHeader header = JwsHeader
                .with(SignatureAlgorithm.RS256)
                .build();

        return jwtEncoder
                .encode(JwtEncoderParameters.from(header, claims))
                .getTokenValue();
    }
}

package com.openclassrooms.mddapi.auth.security.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

public class JwtKeyConfigTest {

    private JwtKeyConfig jwtKeyConfig;
    private KeyPair keyPair;
    private Path privateKeyFile;
    private Path publicKeyFile;

    @BeforeEach
    void setUp() throws Exception {
        jwtKeyConfig = new JwtKeyConfig();

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);

        keyPair = keyPairGenerator.generateKeyPair();
        privateKeyFile = Files.createTempFile("jwt-private", ".pem");
        publicKeyFile = Files.createTempFile("jwt-public", ".pem");

        writePrivateKey(privateKeyFile);
        writePublicKey(publicKeyFile);

        ReflectionTestUtils.setField(jwtKeyConfig, "privateKeyPath", privateKeyFile.toString());
        ReflectionTestUtils.setField(jwtKeyConfig, "publicKeyPath", publicKeyFile.toString());
    }

    @Test
    void shouldLoadRsaPublicKey() throws Exception {
        // When
        RSAPublicKey publicKey = jwtKeyConfig.rsaPublicKey();

        // Then
        assertThat(publicKey)
                .isNotNull();
        assertThat(publicKey.getAlgorithm())
                .isEqualTo("RSA");
        assertThat(publicKey.getModulus())
                .isEqualTo(((RSAPublicKey) keyPair.getPublic()).getModulus());
        assertThat(publicKey.getPublicExponent())
                .isEqualTo(((RSAPublicKey) keyPair.getPublic()).getPublicExponent());
    }

    @Test
    void shouldLoadRsaPrivateKey() throws Exception {
        // When
        RSAPrivateKey privateKey = jwtKeyConfig.rsaPrivateKey();

        // Then
        assertThat(privateKey)
                .isNotNull();
        assertThat(privateKey.getAlgorithm())
                .isEqualTo("RSA");
        assertThat(privateKey.getModulus())
                .isEqualTo(((RSAPrivateKey) keyPair.getPrivate()).getModulus());
    }

    @Test
    void shouldCreateJwtEncoder() throws Exception {
        // Given
        RSAPublicKey publicKey = jwtKeyConfig.rsaPublicKey();
        RSAPrivateKey privateKey = jwtKeyConfig.rsaPrivateKey();

        // When
        JwtEncoder encoder = jwtKeyConfig.jwtEncoder(publicKey, privateKey);

        // Then
        assertThat(encoder)
                .isNotNull();
        assertThat(encoder)
                .isInstanceOf(NimbusJwtEncoder.class);
    }

    @Test
    void shouldCreateJwtDecoder() throws Exception {
        // Given
        RSAPublicKey publicKey = jwtKeyConfig.rsaPublicKey();

        // When
        JwtDecoder decoder = jwtKeyConfig.jwtDecoder(publicKey);

        // Then
        assertThat(decoder).isNotNull();
    }

    private void writePrivateKey(Path path) throws Exception {
        String encoded = Base64.getEncoder()
                .encodeToString(keyPair.getPrivate().getEncoded());

        String pem = """ 
                -----BEGIN PRIVATE KEY----- %s -----END PRIVATE KEY----- 
                """
                .formatted(encoded);

        Files.writeString(path, pem);
    }

    private void writePublicKey(Path path) throws Exception {
        String encoded = Base64.getEncoder()
                .encodeToString(keyPair.getPublic().getEncoded());

        String pem = """ 
                -----BEGIN PUBLIC KEY----- %s -----END PUBLIC KEY----- 
                """
                .formatted(encoded);

        Files.writeString(path, pem);

    }
}

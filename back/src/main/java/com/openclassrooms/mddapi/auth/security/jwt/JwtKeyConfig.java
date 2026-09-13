package com.openclassrooms.mddapi.auth.security.jwt;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration
public class JwtKeyConfig {

    @Value("${app.jwt.private-key-path}")
    private String privateKeyPath;

    @Value("${app.jwt.public-key-path}")
    private String publicKeyPath;

    /**
     * Creates the RSA public key used to verify JWT signatures.
     *
     * @return the configured RSA public key
     * @throws GeneralSecurityException if the key cannot be parsed
     * @throws IOException if the key file cannot be read
     */
    @Bean
    public RSAPublicKey rsaPublicKey() throws GeneralSecurityException, IOException {
        return loadPublicKey(publicKeyPath);
    }

    /**
     * Creates the RSA private key used to sign JWT tokens.
     *
     * @return the configured RSA private key
     * @throws GeneralSecurityException if the key cannot be parsed
     * @throws IOException if the key file cannot be read
     */
    @Bean
    public RSAPrivateKey rsaPrivateKey() throws GeneralSecurityException, IOException {
        return loadPrivateKey(privateKeyPath);
    }

    /**
     * Creates the JWT encoder used to sign JWT tokens with the configured
     * RSA key pair.
     *
     * @param publicKey RSA public key
     * @param privateKey RSA private key
     * @return a configured JWT encoder
     */
    @Bean
    public JwtEncoder jwtEncoder(RSAPublicKey publicKey, RSAPrivateKey privateKey) {
        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .build();

        JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(new JWKSet(rsaKey));

        return new NimbusJwtEncoder(jwkSource);
    }

    /**
     * Creates the JWT decoder used to verify incoming JWT signatures.
     *
     * @param publicKey RSA public key used to verify JWT signatures
     * @return a configured JWT decoder
     */
    @Bean
    public JwtDecoder jwtDecoder(RSAPublicKey publicKey) {
        return NimbusJwtDecoder.withPublicKey(publicKey).build();
    }

    /**
     * Loads an RSA private key from a PKCS#8 PEM file.
     *
     * @param path path to the private key file
     * @return the parsed RSA private key
     * @throws IOException if the file cannot be read
     * @throws GeneralSecurityException if the key cannot be parsed
     */
    private static RSAPrivateKey loadPrivateKey(String path) throws IOException, GeneralSecurityException {
        String pem = Files.readString(Path.of(path))
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");

        byte[] decoded = Base64.getDecoder().decode(pem);

        return (RSAPrivateKey) KeyFactory.getInstance("RSA")
                .generatePrivate(new PKCS8EncodedKeySpec(decoded));
    }

    /**
     * Loads an RSA public key from an X.509 PEM file.
     *
     * @param path path to the public key file
     * @return the parsed RSA public key
     * @throws IOException if the file cannot be read
     * @throws GeneralSecurityException if the key cannot be parsed
     */
    private static RSAPublicKey loadPublicKey(String path) throws IOException, GeneralSecurityException {

        String pem = Files.readString(Path.of(path))
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");

        byte[] decoded = Base64.getDecoder().decode(pem);

        return (RSAPublicKey) KeyFactory.getInstance("RSA")
                .generatePublic(new X509EncodedKeySpec(decoded));
    }
}

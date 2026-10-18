package com.civictransparency.auth.service;

import com.civictransparency.auth.exception.InvalidAccessTokenException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class HmacAccessTokenService implements AccessTokenService {

    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();

    private final ObjectMapper objectMapper;
    private final SecretKeySpec signingKey;

    public HmacAccessTokenService(ObjectMapper objectMapper,
            @Value("${civic.auth.token-secret}") String encodedSecret) {
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
        this.signingKey = new SecretKeySpec(decodeSecret(encodedSecret), "HmacSHA256");
    }

    @Override
    public String issue(UUID userId, List<String> roles, OffsetDateTime expiresAt) {
        return issueToken(userId, roles, expiresAt, "access");
    }

    @Override
    public String issueRefresh(UUID userId, List<String> roles, OffsetDateTime expiresAt) {
        return issueToken(userId, roles, expiresAt, "refresh");
    }

    @Override
    public UUID verify(String token) {
        try {
            return getClaims(token, "access").userId();
        } catch (Exception ex) {
            throw new InvalidAccessTokenException();
        }
    }

    @Override
    public UUID verifyRefresh(String token) {
        try {
            return getClaims(token, "refresh").userId();
        } catch (Exception ex) {
            throw new InvalidAccessTokenException();
        }
    }

    @Override
    public List<String> getAccessRoles(String token) {
        try {
            return getClaims(token, "access").roles();
        } catch (Exception ex) {
            throw new InvalidAccessTokenException();
        }
    }

    @Override
    public List<String> getRoles(String token) {
        try {
            return getClaims(token, "refresh").roles();
        } catch (Exception ex) {
            throw new InvalidAccessTokenException();
        }
    }

    private TokenClaims getClaims(String token, String expectedType) {
        String[] parts = token.split("\\.", -1);
        if (parts.length != 3) {
            throw new IllegalArgumentException("Malformed access token");
        }

        try {
            byte[] expectedSignature = sign(parts[0] + "." + parts[1]);
            byte[] actualSignature = URL_DECODER.decode(parts[2]);
            if (!MessageDigest.isEqual(expectedSignature, actualSignature)) {
                throw new IllegalArgumentException("Invalid access token signature");
            }

            var payload = objectMapper.readTree(URL_DECODER.decode(parts[1]));
            if (payload.path("exp").asLong(0) <= OffsetDateTime.now().toEpochSecond()) {
                throw new IllegalArgumentException("Expired access token");
            }
            if (!expectedType.equals(payload.path("type").asText())) {
                throw new IllegalArgumentException("Unexpected token type");
            }

            List<String> roles = objectMapper.readerForListOf(String.class)
                    .readValue(payload.path("roles").toString());

            return new TokenClaims(UUID.fromString(payload.path("sub").asText()), roles);
        } catch (IOException | IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid access token payload", ex);
        }
    }

    private String issueToken(UUID userId, List<String> roles, OffsetDateTime expiresAt, String type) {
        String header = encodeJson(Map.of("alg", "HS256", "typ", "JWT"));
        String payload = encodeJson(Map.of(
                "sub", userId.toString(),
                "roles", roles,
                "iat", OffsetDateTime.now().toEpochSecond(),
                "exp", expiresAt.toEpochSecond(),
                "type", type));
        String unsignedToken = header + "." + payload;
        return unsignedToken + "." + URL_ENCODER.encodeToString(sign(unsignedToken));
    }

    private record TokenClaims(UUID userId, List<String> roles) {
    }

    private byte[] decodeSecret(String encodedSecret) {
        try {
            byte[] secret = Base64.getDecoder().decode(encodedSecret);
            if (secret.length < 32) {
                throw new IllegalStateException("CIVIC_AUTH_TOKEN_SECRET must decode to at least 32 bytes");
            }
            return secret;
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("CIVIC_AUTH_TOKEN_SECRET must be Base64 encoded", ex);
        }
    }

    private String encodeJson(Map<String, ?> value) {
        try {
            return URL_ENCODER.encodeToString(objectMapper.writeValueAsBytes(value));
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to issue access token", ex);
        }
    }

    private byte[] sign(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(signingKey);
            return mac.doFinal(value.getBytes(StandardCharsets.US_ASCII));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to sign access token", ex);
        }
    }
}

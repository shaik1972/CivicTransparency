package com.civictransparency.auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class HmacAccessTokenService implements AccessTokenService {

    private static final Base64.Encoder URL_ENCODER =
            Base64.getUrlEncoder().withoutPadding();

    private final ObjectMapper objectMapper;
    private final SecretKeySpec signingKey;

    public HmacAccessTokenService(ObjectMapper objectMapper,
                                  @Value("${civic.auth.token-secret}") String encodedSecret) {
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
        this.signingKey = new SecretKeySpec(decodeSecret(encodedSecret), "HmacSHA256");
    }

    @Override
    public String issue(UUID userId, List<String> roles, OffsetDateTime expiresAt) {
        String header = encodeJson(Map.of("alg", "HS256", "typ", "JWT"));
        String payload = encodeJson(Map.of(
                "sub", userId.toString(),
                "roles", roles,
                "iat", OffsetDateTime.now().toEpochSecond(),
                "exp", expiresAt.toEpochSecond()));
        String unsignedToken = header + "." + payload;
        return unsignedToken + "." + URL_ENCODER.encodeToString(sign(unsignedToken));
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

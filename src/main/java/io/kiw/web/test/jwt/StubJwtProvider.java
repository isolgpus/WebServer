package io.kiw.web.test.jwt;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kiw.result.Result;
import io.kiw.web.jwt.JwtClaims;
import io.kiw.web.jwt.JwtProvider;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

public class StubJwtProvider implements JwtProvider {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String JWT_HEADER = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";

    private final String secret;

    public StubJwtProvider(String secret) {
        this.secret = secret;
    }

    @Override
    public String generateToken(Map<String, Object> claims) {
        try {
            String header = base64UrlEncode(JWT_HEADER);
            String payload = base64UrlEncode(MAPPER.writeValueAsString(claims));
            String signingInput = header + "." + payload;
            return signingInput + "." + sign(signingInput);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate token", e);
        }
    }

    @Override
    public Result<String, JwtClaims> validateToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return Result.error("Invalid token format");
            }

            String signingInput = parts[0] + "." + parts[1];
            if (!sign(signingInput).equals(parts[2])) {
                return Result.error("Invalid token signature");
            }

            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            Map<String, Object> claims = MAPPER.readValue(payloadJson, new TypeReference<>() {});

            Object exp = claims.get("exp");
            if (exp != null && System.currentTimeMillis() / 1000 > ((Number) exp).longValue()) {
                return Result.error("Token has expired");
            }

            return Result.success(new JwtClaims(claims));
        } catch (Exception e) {
            return Result.error("Token validation failed");
        }
    }

    private String sign(String input) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] bytes = mac.doFinal(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to sign token", e);
        }
    }

    private String base64UrlEncode(String input) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(input.getBytes(StandardCharsets.UTF_8));
    }
}

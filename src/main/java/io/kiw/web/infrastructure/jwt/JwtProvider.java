package io.kiw.web.infrastructure.jwt;

import io.kiw.result.Result;

import java.util.Map;

public interface JwtProvider {

    String generateToken(Map<String, Object> claims);

    Result<String, JwtClaims> validateToken(String token);
}

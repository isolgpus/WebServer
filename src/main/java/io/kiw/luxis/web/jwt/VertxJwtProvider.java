package io.kiw.luxis.web.jwt;

import io.kiw.luxis.result.Result;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authentication.TokenCredentials;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class VertxJwtProvider implements JwtProvider {

    private final JWTAuth jwtAuth;

    public VertxJwtProvider(Vertx vertx, String secret) {
        this(vertx, secret, "HS256");
    }

    public VertxJwtProvider(Vertx vertx, String secret, String algorithm) {
        JWTAuthOptions config = new JWTAuthOptions()
                .addPubSecKey(new PubSecKeyOptions()
                .setAlgorithm(algorithm)
                .setBuffer(secret));
        this.jwtAuth = JWTAuth.create(vertx, config);
    }

    @Override
    public String generateToken(Map<String, Object> claims) {
        return jwtAuth.generateToken(new JsonObject(claims), new JWTOptions());
    }

    @Override
    public Result<String, JwtClaims> validateToken(String token) {
        try {
            Future<User> future = jwtAuth.authenticate(new TokenCredentials(token));
            CompletableFuture<User> cf = future.toCompletionStage().toCompletableFuture();
            User user = cf.get(5, TimeUnit.SECONDS);
            return Result.success(new JwtClaims(user.principal().getMap()));
        } catch (ExecutionException e) {
            return Result.error("Invalid token");
        } catch (InterruptedException | TimeoutException e) {
            Thread.currentThread().interrupt();
            return Result.error("Token validation failed");
        }
    }
}

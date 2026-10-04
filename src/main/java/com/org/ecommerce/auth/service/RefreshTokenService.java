package com.org.ecommerce.auth.service;

import com.org.ecommerce.auth.exception.InvalidRefreshTokenException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class RefreshTokenService {
    // PASSWORD_ENCODER BEAN WITH BCRYPTENCODER IS NOT AUTOMATICALLY CREATED BY SPRING. SO WE USED @BEAN TO CREATE IT.
    // BUT SPRING'S AUTO CONFIGURATION CREATES THE BELOW REDIS BEAN BASED ON THE POM DEPENDENCY & PROPERTIES IN YAML FILE.
    private final StringRedisTemplate stringRedisTemplate;

    public RefreshTokenService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public String generateRefreshToken() {
        return UUID.randomUUID().toString();
    }

    public void storeRefreshToken(String refreshToken, Long userId) {
        stringRedisTemplate.opsForValue()
                .set(
                        refreshToken, // HERE WE ARE STORING RAW TOKEN. BUT WE CAN HASH IT WITH HS256 OR SOMETHING.
                        userId.toString(),
                        7,
                        TimeUnit.DAYS);
    }

    public Long getUserId(String refreshToken) {
        String userId = stringRedisTemplate.opsForValue().get(refreshToken);

        if(userId == null){
            throw new InvalidRefreshTokenException();
        }

        return Long.valueOf(userId);
    }

    public void deleteRefreshToken(String refreshToken) {
        stringRedisTemplate.delete(refreshToken);
    }

    /**
     * On login, we issue two tokens: a JWT access token with a 1-hour TTL, and a random UUID refresh token with a 7-day TTL,
     *      stored in Redis mapped to the user ID.
     * The access token is what's sent on every regular API call.
     * When the access token expires, the client gets a 401 and calls a dedicated refresh endpoint with the refresh token.
     * That endpoint validates the refresh token against Redis, then issues a new access token and a new refresh token,
     *      and deletes the old refresh token from Redis — so refresh tokens rotate on every use rather than being reused indefinitely.
     *
     * A couple of things I'd add to make it production-grade:
     * 1. Hash the refresh token before storing in Redis - using SHA 256 to hash
     * 2. Act on reuse detection - suppose if an old refresh token is reused, then I would consider it as a compromise in refresh token.
     *      Revoke all the user's active sessions and force re-login - this I would do by deleting all the refresh tokens.
     * 3. But the current code has only refresh token as key. So if I have to delete all the tokens as mentioned in step 2,
     *      then I would also need to include user id in the key.
     */
}

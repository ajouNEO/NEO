package com.neo.back.authorization.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@RequiredArgsConstructor
@Service
public class RedisUtil {
    private final StringRedisTemplate template;

    public String getData(String key) {
        ValueOperations<String, String> valueOperations = template.opsForValue();
        return valueOperations.get(key);
    }

    public boolean existData(String key) {
        return Boolean.TRUE.equals(template.hasKey(key));
    }

    public void setValue(String key, String value) {
        template.opsForValue().set(key, value);
    }

    public void setDataExpire(String key, String value, long duration) {
        ValueOperations<String, String> valueOperations = template.opsForValue();
        Duration expireDuration = Duration.ofSeconds(duration);
        valueOperations.set(key, value, expireDuration);
    }

    public void incrementValueBy(String key, long delta) {
        template.opsForValue().increment(key, delta);
    }



    public void deleteData(String key) {
        template.delete(key);
    }



    //User number 및 list 저장 관련
    public void updateUserNumberInRedis(Long dockerId, Integer userNumber) {
        template.opsForValue().set("docker:" + dockerId + ":userNumber", userNumber.toString());
    }

    public Integer getUserNumberFromRedis(Long dockerId) {
        String value = template.opsForValue().get("docker:" + dockerId + ":userNumber");
        return value != null ? Integer.valueOf(value) : null;
    }
    public void setUsernames(String dockerId, List<String> usernames) {
        String usernamesString = String.join(",", usernames);
        template.opsForValue().set("docker:" + dockerId + ":usernames", usernamesString);
    }

    public List<String> getUsernames(Long dockerId) {
        String value = template.opsForValue().get("docker:" + dockerId + ":usernames");
        return value != null ? List.of(value.split(",")) : null;
    }




}
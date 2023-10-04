package com.tenpo.apirest.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class PercentageCacheImpl implements PercentageCache{
    private static final Logger logger = LoggerFactory.getLogger(PercentageCacheImpl.class);

    @Autowired
    private RedisTemplate<String, Double> redisTemplate;

    private String percentageKey = "externalPercentageService";



    public void savePercentageInCache(Double percentage) {
        redisTemplate.opsForValue().set(percentageKey, percentage, 30, TimeUnit.MINUTES);
    }

    public Double getPercentageCachedValue() {
        return redisTemplate.opsForValue().get(percentageKey);
    }

    public void invalidateCache() {
        redisTemplate.opsForValue().getAndDelete(percentageKey);
    }
}

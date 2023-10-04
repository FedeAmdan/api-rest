package com.tenpo.apirest.ratelimit;

import com.tenpo.apirest.controller.ApiController;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Refill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class RequestRateLimiterImpl implements RequestRateLimiter{

    private static final Logger logger = LoggerFactory.getLogger(RequestRateLimiterImpl.class);

    Bucket bucket;
    public RequestRateLimiterImpl(int amountOfRequest, int minutesInterval) {
        Bandwidth limit = Bandwidth.classic(amountOfRequest, Refill.intervally(amountOfRequest, Duration.ofMinutes(minutesInterval)));
        bucket = Bucket4j.builder()
                .addLimit(limit)
                .build();
    }

    public boolean canHandleRequest(String path) {
        if (ApiController.SERVICE_PATH.equals(path)) {
            logger.debug("Available amount of requests in this minute: " + bucket.getAvailableTokens());
            return bucket.tryConsume(1);
        }
        return true;
    }
}

package com.tenpo.apirest.ratelimit;

public interface RequestRateLimiter {
    boolean canHandleRequest(String path);
}

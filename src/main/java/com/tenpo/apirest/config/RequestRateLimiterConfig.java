package com.tenpo.apirest.config;

import com.tenpo.apirest.ratelimit.RequestRateLimiter;
import com.tenpo.apirest.ratelimit.RequestRateLimiterImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RequestRateLimiterConfig {

    @Bean
    RequestRateLimiter requestRateLimiter(){
        return new RequestRateLimiterImpl(3, 1);
    }
}

package com.tenpo.apirest.interceptor;

import com.tenpo.apirest.exception.TooManyRequestsException;
import com.tenpo.apirest.ratelimit.RequestRateLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class ApiInterceptor implements HandlerInterceptor {

    @Autowired
    RequestRateLimiter requestRateLimiter;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!requestRateLimiter.canHandleRequest(request.getRequestURI())) {
            throw new TooManyRequestsException("Exceeded Rate Limit: Maximum of 3 requests per minute allowed.");
        }
        return true;
    }
}

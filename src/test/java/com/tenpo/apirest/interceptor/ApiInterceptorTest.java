package com.tenpo.apirest.interceptor;

import com.tenpo.apirest.controller.ApiController;
import com.tenpo.apirest.exception.TooManyRequestsException;
import com.tenpo.apirest.ratelimit.RequestRateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ApiInterceptorTest {
    @InjectMocks
    ApiInterceptor apiInterceptor;

    @Mock
    RequestRateLimiter requestRateLimiter;

    @Test
    public void allowRequestToBeHandledIfRateLimiterAllowsIt() throws Exception {
        HttpServletRequest request = new MockHttpServletRequest("POST", ApiController.SERVICE_PATH);
        HttpServletResponse response = new MockHttpServletResponse();
        when(requestRateLimiter.canHandleRequest(ApiController.SERVICE_PATH)).thenReturn(true);
        assertTrue(apiInterceptor.preHandle(request, response, null));
    }

    @Test
    public void doNotAllowRequestToBeHandledIfRateLimiterDoesntAllowIt() {
        HttpServletRequest request = new MockHttpServletRequest("POST", ApiController.SERVICE_PATH);
        HttpServletResponse response = new MockHttpServletResponse();
        when(requestRateLimiter.canHandleRequest(ApiController.SERVICE_PATH)).thenReturn(false);
        assertThrows(TooManyRequestsException.class, () -> apiInterceptor.preHandle(request, response, null));
    }
}

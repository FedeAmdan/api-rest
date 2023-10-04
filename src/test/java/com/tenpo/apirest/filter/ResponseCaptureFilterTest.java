package com.tenpo.apirest.filter;

import com.tenpo.apirest.entity.ApiCall;
import com.tenpo.apirest.repository.ApiCallRepository;
import jakarta.servlet.ServletOutputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.util.ContentCachingResponseWrapper;
import jakarta.servlet.ServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class ResponseCaptureFilterTest {

    @InjectMocks
    private ResponseCaptureFilter filter;

    @Mock
    private ApiCallRepository apiCallRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    String endpoint = "/api/resource";
    String method = "GET";
    String response = "Response content";

    @Test
    public void testStoreCall() throws Exception {
        when(apiCallRepository.save(any(ApiCall.class))).thenReturn(new ApiCall());

        filter.storeCall(endpoint, method, response);

        CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.initialize();
        executor.execute(() -> {
            completableFuture.complete(null);
        });

        completableFuture.get();

        verify(apiCallRepository).save(argThat(apiCall ->
                apiCall.getEndpoint().equals(endpoint) &&
                        apiCall.getMethod().equals(method) &&
                        apiCall.getResponse().equals(response)
        ));
    }

    @Test
    public void testFailingStoreCall() throws InterruptedException, ExecutionException {
        when(apiCallRepository.save(any(ApiCall.class))).thenThrow(new RuntimeException("Simulated exception"));

        filter.storeCall(endpoint, method, response);

        CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.initialize();
        executor.execute(() -> {
            completableFuture.complete(null);
        });

        completableFuture.get();

        verify(apiCallRepository).save(any(ApiCall.class));
    }

    @Test
    public void testGetResponseContent() throws IOException {
        ContentCachingResponseWrapper responseWrapper = mock(ContentCachingResponseWrapper.class);
        ServletResponse response = mock(ServletResponse.class);

        String responseContent = "Hello, World!";
        byte[] responseBody = responseContent.getBytes(StandardCharsets.UTF_8);

        when(responseWrapper.getContentAsByteArray()).thenReturn(responseBody);
        when(responseWrapper.getCharacterEncoding()).thenReturn("UTF-8");

        ServletOutputStream outputStream = mock(ServletOutputStream.class);
        when(response.getOutputStream()).thenReturn(outputStream);

        String result = filter.getResponseWrapperContentAndCopyItToResponse(response, responseWrapper);

        verify(responseWrapper, times(1)).getContentAsByteArray();
        verify(responseWrapper, times(1)).getCharacterEncoding();
        verify(outputStream, times(1)).write(responseBody);
        assertEquals(responseContent, result);
    }
}


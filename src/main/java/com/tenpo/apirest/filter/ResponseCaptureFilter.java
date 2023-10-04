package com.tenpo.apirest.filter;

import com.tenpo.apirest.controller.ApiController;
import com.tenpo.apirest.entity.ApiCall;
import com.tenpo.apirest.repository.ApiCallRepository;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.apache.catalina.connector.RequestFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import jakarta.servlet.http.HttpServletResponse;

@Component
public class ResponseCaptureFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(ResponseCaptureFilter.class);

    @Autowired
    private ApiCallRepository apiCallRepository;

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String requestUri = request instanceof RequestFacade? ((RequestFacade) request).getRequestURI() : "";

        if (ApiController.SERVICE_PATH.equals(requestUri)) {
            ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper((HttpServletResponse) response);
            chain.doFilter(request, responseWrapper);
            int responseStatus = ((HttpServletResponse) response).getStatus();
            String requestMethod = ((RequestFacade) request).getMethod();
            String responseContent = getResponseWrapperContentAndCopyItToResponse(response, responseWrapper);
            if (responseStatus >= 200 && responseStatus < 300) {
                storeCall(requestUri, requestMethod, responseContent);
            }
        } else {
            chain.doFilter(request, response);
        }
    }

    protected String getResponseWrapperContentAndCopyItToResponse(ServletResponse response, ContentCachingResponseWrapper responseWrapper) throws IOException {
        byte[] responseBody = responseWrapper.getContentAsByteArray();
        String responseContent = new String(responseBody, responseWrapper.getCharacterEncoding());
        response.getOutputStream().write(responseBody);
        return responseContent;
    }


    protected void storeCall(String endpoint, String method, String response) {
        CompletableFuture.runAsync(() -> {
            ApiCall apiCall = new ApiCall();
            apiCall.setEndpoint(endpoint);
            apiCall.setMethod(method);
            apiCall.setResponse(response);
            try {
                apiCallRepository.save(apiCall);
            } catch (Exception e) {
                logger.error("ResponseCaptureFilter - Error saving api call into DB. Exception: " + e.toString());
            }
        });
    }
}

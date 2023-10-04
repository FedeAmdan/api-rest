package com.tenpo.apirest.controller;

import com.tenpo.apirest.entity.ApiCall;
import com.tenpo.apirest.service.ApiCallService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class HistoryControllerTest {

    @InjectMocks
    private HistoryController historyController;

    @Mock
    private ApiCallService apiCallService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetApiCallHistory() {
        ApiCall apiCall = new ApiCall();
        apiCall.setId(1L);
        apiCall.setMethod("POST");
        apiCall.setEndpoint("/v1/calculate");
        apiCall.setResponse("{\"result\": 33.0}");

        List<ApiCall> apiCalls = List.of(apiCall);
        Page<ApiCall> apiCallPage = new PageImpl<>(apiCalls);
        when(apiCallService.getApiCallHistory(any(Pageable.class))).thenReturn(apiCallPage);

        ResponseEntity<Page<ApiCall>> response = historyController.getApiCallHistory(0, 10, "id", "desc");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(apiCallPage, response.getBody());
        verify(apiCallService).getApiCallHistory(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id")));
    }
}

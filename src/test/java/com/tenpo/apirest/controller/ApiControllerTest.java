package com.tenpo.apirest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenpo.apirest.entity.CalculationRequest;
import com.tenpo.apirest.exception.ExternalServiceException;
import com.tenpo.apirest.exception.TooManyRequestsException;
import com.tenpo.apirest.service.CalculationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.anyDouble;
import static org.mockito.Mockito.when;

public class ApiControllerTest {

    @InjectMocks
    private ApiController apiController;

    @Mock
    private CalculationService calculationService;

    private ObjectMapper objectMapper;

    private CalculationRequest request;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        request = new CalculationRequest();
        request.setNum1(10.0);
        request.setNum2(20.0);
    }

    @Test
    public void testCalculate() {
        when(calculationService.calculateSumWithPercentage(10.0, 20.0)).thenReturn(30.0);
        ResponseEntity<Object> response = apiController.calculate(request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Map.of("result", 30.0), response.getBody());
    }

    @Test
    public void testThrowTooManyRequestsException() {
        when(calculationService.calculateSumWithPercentage(anyDouble(), anyDouble()))
                .thenThrow(new TooManyRequestsException("too many requests"));
        assertThrows(TooManyRequestsException.class, () -> apiController.calculate(request));
    }

    @Test
    public void testThrowExternalServiceException() {
        when(calculationService.calculateSumWithPercentage(10.0, 20.0))
                .thenThrow(new ExternalServiceException("External service failed"));
        assertThrows(ExternalServiceException.class, () -> apiController.calculate(request));
    }

}

package com.tenpo.apirest.service;

import com.tenpo.apirest.cache.PercentageCache;
import com.tenpo.apirest.exception.ExternalServiceException;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import com.tenpo.apirest.gateway.ExternalServiceClient;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CalculationServiceTest {

    @InjectMocks
    CalculationService calculationService;

    @Mock
    ExternalServiceClient externalServiceClient;

    @Mock
    PercentageCache percentageCache;

    @Test
    public void sumBetweenTwoNumbersWithZeroPercentageShouldWork() {
        Double num1 =3.0;
        Double num2= 4.0;
        Double expectedResponse = 7.0;
        when(percentageCache.getPercentageCachedValue()).thenReturn(0.0);

        Double response = calculationService.calculateSumWithPercentage(num1, num2);
        assertEquals(expectedResponse, response);
    }

    @Test
    public void sumBetweenTwoNumbersWithPercentageNumberShouldWork() {
        Double num1 =3.0;
        Double num2= 4.0;
        Double expectedResponse = 7.7;

        when(percentageCache.getPercentageCachedValue()).thenReturn(10.0);

        Double response = calculationService.calculateSumWithPercentage(num1, num2);
        assertEquals(expectedResponse, response);
    }


    @Test
    public void sendingNullAsSecondNumberShouldFail() {
        Double num1 =3.0;
        Double num2= null;
        assertThrows(IllegalArgumentException.class, () -> {
            calculationService.calculateSumWithPercentage(num1, num2);
        });
    }

    @Test
    public void sendingNullAsFirstNumberShouldFail() {
        Double num1 = null;
        Double num2 = 4.0;
        assertThrows(IllegalArgumentException.class, () -> {
            calculationService.calculateSumWithPercentage(num1, num2);
        });
    }

    @Test
    public void sumBetweenNullNumbersShouldFail() {
        Double num1 = null;
        Double num2 = null;
        assertThrows(IllegalArgumentException.class, () -> {
            calculationService.calculateSumWithPercentage(num1, num2);
        });
    }

    @Test
    public void getPercentageFromCache() {
        Double expectedPercentage = 10.0;
        when(percentageCache.getPercentageCachedValue()).thenReturn(expectedPercentage);

        Double actualValue = calculationService.getPercentage();

        assertEquals(expectedPercentage, actualValue);
    }

    @Test
    public void getPercentageFromExternalServiceAfterCacheMiss() {
        Double expectedPercentage = 10.0;
        when(percentageCache.getPercentageCachedValue()).thenReturn(null);
        when(externalServiceClient.fetchPercentage()).thenReturn(expectedPercentage);
        doNothing().when(percentageCache).savePercentageInCache(expectedPercentage);

        Double actualValue = calculationService.getPercentage();

        verify(percentageCache, times(1)).getPercentageCachedValue();
        verify(percentageCache, times(1)).savePercentageInCache(expectedPercentage);
        assertEquals(expectedPercentage, actualValue);
    }

    @Test
    public void getPercentageFromExternalServiceAfterCallingItTwoTimes() {
        Double expectedPercentage = 10.0;
        when(percentageCache.getPercentageCachedValue()).thenReturn(null);
        when(externalServiceClient.fetchPercentage())
                .thenThrow(new ExternalServiceException("Failed to retrieve percentage"))
                .thenReturn(10.0);

        Double actualValue = calculationService.getPercentage();

        verify(externalServiceClient, times(2)).fetchPercentage();
        verify(percentageCache, times(1)).getPercentageCachedValue();
        verify(percentageCache, times(1)).savePercentageInCache(expectedPercentage);
        assertEquals(expectedPercentage, actualValue);
    }

    @Test
    public void getPercentageFromExternalServiceAfterCallingItThreeTimes() {
        Double expectedPercentage = 10.0;
        when(percentageCache.getPercentageCachedValue()).thenReturn(null);
        when(externalServiceClient.fetchPercentage())
                .thenThrow(new ExternalServiceException("Failed to retrieve percentage"))
                .thenThrow(new ExternalServiceException("Failed to retrieve percentage"))
                .thenReturn(10.0);

        Double actualValue = calculationService.getPercentage();

        verify(externalServiceClient, times(3)).fetchPercentage();
        verify(percentageCache, times(1)).getPercentageCachedValue();
        verify(percentageCache, times(1)).savePercentageInCache(expectedPercentage);
        assertEquals(expectedPercentage, actualValue);
    }

    @Test
    public void failToGetPercentageAfterCacheMissAndFailingToCallExternalServiceErrorThreeTimes() {
        when(percentageCache.getPercentageCachedValue()).thenReturn(null);
        when(externalServiceClient.fetchPercentage())
                .thenThrow(new ExternalServiceException("Failed to retrieve percentage"))
                .thenThrow(new ExternalServiceException("Failed to retrieve percentage"))
                .thenThrow(new ExternalServiceException("Failed to retrieve percentage"));

        assertThrows(ExternalServiceException.class, () -> {
            calculationService.getPercentage();
        });

        verify(externalServiceClient, times(3)).fetchPercentage();
        verify(percentageCache, times(1)).getPercentageCachedValue();
        verify(percentageCache, times(0)).savePercentageInCache(anyDouble());
    }
    @Test
    public void getOldPercentageValueAfterCacheMissAndFailingToCallExternalServiceErrorThreeTimesButUsingOldValue() {
        Double expectedPercentage = 10.0;
        // First, get the value from the external service. It should save the value in case everything fails.
        when(percentageCache.getPercentageCachedValue()).thenReturn(null);
        when(externalServiceClient.fetchPercentage()).thenReturn(expectedPercentage);
        doNothing().when(percentageCache).savePercentageInCache(expectedPercentage);

        Double actualValue = calculationService.getPercentage();

        verify(percentageCache, times(1)).getPercentageCachedValue();
        verify(percentageCache, times(1)).savePercentageInCache(expectedPercentage);
        assertEquals(expectedPercentage, actualValue);

        // Then, call the method again, perform a cache miss,
        // fail to get the percentage from the external service 3 times,
        // and it should use the saved value.
        when(percentageCache.getPercentageCachedValue()).thenReturn(null);
        when(externalServiceClient.fetchPercentage())
                .thenThrow(new ExternalServiceException("Failed to retrieve percentage"))
                .thenThrow(new ExternalServiceException("Failed to retrieve percentage"))
                .thenThrow(new ExternalServiceException("Failed to retrieve percentage"));

        actualValue = calculationService.getPercentage();

        verify(externalServiceClient, times(4)).fetchPercentage();
        verify(percentageCache, times(2)).getPercentageCachedValue();
        assertEquals(expectedPercentage, actualValue);
    }
}

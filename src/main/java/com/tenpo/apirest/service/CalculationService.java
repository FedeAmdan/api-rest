package com.tenpo.apirest.service;

import com.tenpo.apirest.cache.PercentageCache;
import com.tenpo.apirest.exception.ExternalServiceException;
import com.tenpo.apirest.gateway.ExternalServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CalculationService {

    @Autowired
    private ExternalServiceClient externalServiceClient;

    @Autowired
    PercentageCache percentageCache;

    private int externalServiceAttempts = 3;
    private static final Logger logger = LoggerFactory.getLogger(CalculationService.class);

    public Double calculateSumWithPercentage(Double num1, Double num2) {
        if (num1 == null || num2 == null) {
            throw new IllegalArgumentException("Provided numbers should not be null");
        }
        Double percentage = getPercentage();
        return (num1 + num2) * (100 + percentage)/100;
    }

    private Double previousPercentage = null;

    protected Double getPercentage() {
        Double percentage = percentageCache.getPercentageCachedValue();
        if (percentage != null) {
            return percentage;
        }
        for (int i = 0; i<externalServiceAttempts; i++) {
            try {
                percentage = externalServiceClient.fetchPercentage();
                previousPercentage = percentage;
                percentageCache.savePercentageInCache(percentage);
                logger.debug("External service call - Percentage retrieved from external service");
                return percentage;
            } catch (ExternalServiceException e) {
                logger.debug("External service call - Attempt "+ (i+1) + " failed.");
            }
        }
        if (previousPercentage == null) {
            throw new ExternalServiceException("Unable to get percentage from external service");
        }
        logger.debug("External service call - Using old value: " + previousPercentage);
        return previousPercentage;
    }


}

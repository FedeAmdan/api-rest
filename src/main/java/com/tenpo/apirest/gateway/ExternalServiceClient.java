package com.tenpo.apirest.gateway;

import org.springframework.stereotype.Service;

@Service
public interface ExternalServiceClient {
    Double fetchPercentage();
}
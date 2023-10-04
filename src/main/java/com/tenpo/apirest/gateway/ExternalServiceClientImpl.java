package com.tenpo.apirest.gateway;

import com.tenpo.apirest.entity.PercentageResponse;
import com.tenpo.apirest.exception.ExternalServiceException;
import retrofit2.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class ExternalServiceClientImpl implements ExternalServiceClient {

    @Autowired
    private ExternalServiceApi externalServiceApi;

    @Override
    public Double fetchPercentage() {
        try {
            Response<PercentageResponse> response = externalServiceApi.fetchPercentage().execute();
            if (response.isSuccessful()) {
                return response.body().getPercentage();
            } else {
                throw new ExternalServiceException("Error: " + response.errorBody().string());
            }
        } catch (IOException e) {
            throw new ExternalServiceException("Failed to retrieve percentage", e);
        }
    }
}

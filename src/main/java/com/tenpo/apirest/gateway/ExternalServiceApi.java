package com.tenpo.apirest.gateway;

import com.tenpo.apirest.entity.PercentageResponse;
import retrofit2.Call;
import retrofit2.http.GET;

public interface ExternalServiceApi {
    @GET("/v1/external-service")
    Call<PercentageResponse> fetchPercentage();
}
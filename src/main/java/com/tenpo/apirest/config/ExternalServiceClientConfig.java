package com.tenpo.apirest.config;

import com.tenpo.apirest.gateway.ExternalServiceApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

@Configuration
public class ExternalServiceClientConfig {

    @Value("${external.service.base.url}")
    private String externalServiceUrl;

    @Bean
    public Retrofit retrofit() {
        return new Retrofit.Builder()
            .baseUrl(externalServiceUrl)
            .addConverterFactory(JacksonConverterFactory.create())
            .build();
    }

    @Bean
    public ExternalServiceApi externalServiceApi(Retrofit retrofit) {
        return retrofit.create(ExternalServiceApi.class);
    }
}

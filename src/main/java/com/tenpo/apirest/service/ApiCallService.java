package com.tenpo.apirest.service;

import com.tenpo.apirest.entity.ApiCall;
import com.tenpo.apirest.repository.ApiCallRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
@Service
public class ApiCallService {

    @Autowired
    private ApiCallRepository apiCallRepository;

    public Page<ApiCall> getApiCallHistory(Pageable pageable) {
        return apiCallRepository.findAll(pageable);
    }
}


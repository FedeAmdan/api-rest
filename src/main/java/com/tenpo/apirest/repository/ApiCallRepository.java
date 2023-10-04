package com.tenpo.apirest.repository;

import com.tenpo.apirest.entity.ApiCall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApiCallRepository extends JpaRepository<ApiCall, Long> {
}
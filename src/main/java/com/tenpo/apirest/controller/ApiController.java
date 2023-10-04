package com.tenpo.apirest.controller;

import com.tenpo.apirest.entity.CalculationRequest;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.tenpo.apirest.service.CalculationService;
import com.tenpo.apirest.exception.ExternalServiceException;
import com.tenpo.apirest.exception.TooManyRequestsException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.InvalidParameterException;
import java.util.Map;

import static com.tenpo.apirest.controller.ApiController.SERVICE_PATH;

@RestController
@RequestMapping(SERVICE_PATH)
@Tag(name = "Calculation API", description = "")
public class ApiController {
    public static final String SERVICE_PATH = "/v1/calculate";

    @Autowired
    private CalculationService calculationService;

    @Operation(
            summary = "Sum",
            description = "Sums two numbers and applies a percentage increase acquired from an external service",
            tags = {})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful response", content = { @Content(examples = { @ExampleObject("{ \"result\": 33.3 }")}, mediaType = "application/json") }),
            @ApiResponse(responseCode = "400", description = "The request was malformed. Only 2 numbers are allowed.", content = { @Content(examples = { @ExampleObject("{ \"error\": \"Invalid argument. Only 2 numbers are allowed.\" }")}, mediaType = "application/json")  }),
            @ApiResponse(responseCode = "429", description = "The request was rate limited. Only 3 requests per minute are allowed.", content = { @Content(examples = { @ExampleObject("{ \"error\": \"Too many requests. Only 3 requests per minute are allowed.\" }")}, mediaType = "application/json") }),
            @ApiResponse(responseCode = "500", description = "Internal service error", content = { @Content(examples = { @ExampleObject("{ \"error\": \"Unexpected error occurred\" }")}, mediaType = "application/json") }),
            @ApiResponse(responseCode = "503", description = "Service unavailable. This is thrown when an external service fails.", content = { @Content(examples = { @ExampleObject("{ \"error\": \"External service failed\" }")}, mediaType = "application/json") })
    })
    @PostMapping
    public ResponseEntity<Object> calculate(
            @Parameter(name = "request", description = "Json that contains the two numbers that will be used in the sum.", required = true,  content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CalculationRequest.class)
            ))
            @RequestBody CalculationRequest request
    ) {
        Double result = calculationService.calculateSumWithPercentage(request.getNum1(), request.getNum2());
        return ResponseEntity.ok(Map.of("result", result));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, InvalidParameterException.class})
    public ResponseEntity<Object> handleHttpMessageNotReadableException() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Invalid argument. Only 2 numbers are allowed."));
    }
    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<Object> handleTooManyRequests() {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of("error", "Too many requests. Only 3 requests per minute are allowed."));
    }
    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<Object> handleExternalServiceError() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("error", "External service failed"));
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnexpectedError() {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Unexpected error occurred"));
    }
}

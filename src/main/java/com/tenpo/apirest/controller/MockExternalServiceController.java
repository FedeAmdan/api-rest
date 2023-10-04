package com.tenpo.apirest.controller;

import com.tenpo.apirest.cache.PercentageCache;
import com.tenpo.apirest.entity.ExternalServiceBehaviour;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.security.InvalidParameterException;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/v1/external-service")
@Tag(name = "External Service Mock Controller", description = "For QA purposes")
public class MockExternalServiceController {

    @Autowired
    PercentageCache percentageCache;
    private ExternalServiceBehaviour behaviour = ExternalServiceBehaviour.RESPONSE_AT_FIRST_ATTEMPT;
    @Operation(
            summary = "Percentage Service",
            description = "Service that is called by the main API",
            tags = {})
    @GetMapping
    public ResponseEntity<Object> getPercentage() {
        ResponseEntity<Object> successfulResponse = ResponseEntity.ok(Map.of("percentage", 10.0));
        ResponseEntity<Object> failingResponse = ResponseEntity.internalServerError().build();
        return switch (behaviour){
            case NO_RESPONSE -> failingResponse;
            case RANDOMLY_RESPOND_OR_NOT -> new Random().nextBoolean() ?  successfulResponse: failingResponse;
            default -> successfulResponse;
        };
    }

    @Operation(
            summary = "Behaviour changer",
            description = "Service that changes the response of the GET method. Available inputs: \"RESPONSE_AT_FIRST_ATTEMPT\", \"RANDOMLY_RESPOND_OR_NOT\",\"NO_RESPONSE\"",
            tags = {})
    @ApiResponses({
        @ApiResponse(responseCode = "200", content = { @Content(examples = {@ExampleObject("{\"behaviour\": \"RESPONSE_AT_FIRST_ATTEMPT\"}")}, mediaType = "application/json") }),
        @ApiResponse(responseCode = "400", description = "The request was malformed. Only 2 numbers are allowed.", content = { @Content(examples = { @ExampleObject("{ \"error\": \"Invalid argument. Use one of the following: [\"RESPONSE_AT_FIRST_ATTEMPT\", \"RANDOMLY_RESPOND_OR_NOT\",\"NO_RESPONSE\"]\" }")}, mediaType = "application/json")  }),
        @ApiResponse(responseCode = "500", description = "Internal service error", content = { @Content(examples = { @ExampleObject("{ \"error\": \"Unexpected error occurred\" }")}, mediaType = "application/json") }),
    })
    @PostMapping
    public ResponseEntity<Object> setBehaviour(

            @Schema(enumAsRef = true, allowableValues = {"RESPONSE_AT_FIRST_ATTEMPT", "RANDOMLY_RESPOND_OR_NOT","NO_RESPONSE"}, example = "RESPONSE_AT_FIRST_ATTEMPT", required = true)
            @RequestBody ExternalServiceBehaviour behaviour) {
        this.behaviour = behaviour;
        percentageCache.invalidateCache();
        return ResponseEntity.ok(Map.of("behaviour", this.behaviour.toString()));
    }


    @ExceptionHandler({HttpMessageNotReadableException.class, InvalidParameterException.class})
    public ResponseEntity<Object> handleHttpMessageNotReadableException() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Invalid argument. Use one of the following: [" + ExternalServiceBehaviour.values().toString() + "]"));
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnexpectedError() {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Unexpected error occurred"));
    }
}

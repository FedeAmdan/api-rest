package com.tenpo.apirest.controller;

import com.tenpo.apirest.entity.ApiCall;
import com.tenpo.apirest.service.ApiCallService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.tenpo.apirest.controller.HistoryController.SERVICE_PATH;

@Tag(name = "History API", description = "")
@RestController
@RequestMapping(SERVICE_PATH)
public class HistoryController {
    public static final String SERVICE_PATH = "/v1/history";

    @Autowired
    private ApiCallService apiCallService;

    @Operation(
            summary = "History",
            description = "Gets all the calls, paginated.",
            tags = {})
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = { @Content(examples = {@ExampleObject("{\"content\": [{\"id\": 26, \"endpoint\": \"/v1/calculate\", \"method\": \"POST\", \"response\": \"{\\\"result\\\":0.0}\"}], \"pageable\": { \"pageNumber\": 0, \"pageSize\": 1, \"sort\": { \"sorted\": true, \"empty\": false, \"unsorted\": false }, \"offset\": 0, \"paged\": true, \"unpaged\": false }, \"totalPages\": 26, \"totalElements\": 26, \"last\": false, \"size\": 1, \"number\": 0, \"sort\": { \"sorted\": true, \"empty\": false, \"unsorted\": false}, \"numberOfElements\": 1, \"first\": true, \"empty\": false}")}, schema = @Schema(implementation = Page.class), mediaType = "application/json") }),
            @ApiResponse(responseCode = "400", description = "The request was malformed. Use positive numbers where appropriate. In sort use a valid column name, in direction use desc or asc", content = { @Content(examples = { @ExampleObject("{ \"error\": \"Invalid provided argument.\" }")}, mediaType = "application/json")  }),
            @ApiResponse(responseCode = "500", description = "Internal service error", content = { @Content(examples = { @ExampleObject("{ \"error\": \"Unexpected error occurred\" }")}, mediaType = "application/json") }),
    })
    @GetMapping
    public ResponseEntity<Page<ApiCall>> getApiCallHistory(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "id") String sort,
            @RequestParam(value = "direction", defaultValue = "desc") String direction) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sort));

        Page<ApiCall> apiCallHistoryPage = apiCallService.getApiCallHistory(pageable);
        return ResponseEntity.ok(apiCallHistoryPage);
    }

    @ExceptionHandler({IllegalArgumentException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<Object> handleIllegalArgument() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Invalid provided argument"));
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnexpectedError() {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Unexpected error occurred"));
    }
}

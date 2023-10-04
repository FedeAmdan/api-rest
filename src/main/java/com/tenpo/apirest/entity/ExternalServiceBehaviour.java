package com.tenpo.apirest.entity;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ExternalServiceBehaviour {
    RESPONSE_AT_FIRST_ATTEMPT("RESPONSE_AT_FIRST_ATTEMPT"),
    RANDOMLY_RESPOND_OR_NOT("RANDOMLY_RESPOND_OR_NOT"),
    NO_RESPONSE("NO_RESPONSE");

    private final String value;

    ExternalServiceBehaviour(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}

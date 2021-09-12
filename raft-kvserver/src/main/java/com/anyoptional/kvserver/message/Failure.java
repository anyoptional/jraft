package com.anyoptional.kvserver.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 错误
 */
public class Failure {

    private final int code;
    private final String message;

    @JsonCreator
    public Failure(@JsonProperty("code") int code,
                   @JsonProperty("message") String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "Failure{" +
                "code=" + code +
                ", message='" + message + '\'' +
                '}';
    }

}

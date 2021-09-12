package com.anyoptional.kvserver.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.UUID;

public class SetCommand {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final String requestId;
    private final String key;
    private final byte[] value;

    public SetCommand(String key, byte[] value) {
        this(UUID.randomUUID().toString(), key, value);
    }

    @JsonCreator
    public SetCommand(@JsonProperty("requestId") String requestId,
                      @JsonProperty("key") String key,
                      @JsonProperty("value") byte[] value) {
        this.requestId = requestId;
        this.key = key;
        this.value = value;
    }

    public static SetCommand fromBytes(byte[] bytes) {
        try {
            return OBJECT_MAPPER.readValue(bytes, SetCommand.class);
        } catch (IOException e) {
            throw new IllegalStateException("failed to deserialize set command", e);
        }
    }

    public String getRequestId() {
        return requestId;
    }

    public String getKey() {
        return key;
    }

    public byte[] getValue() {
        return value;
    }

    public byte[] toBytes() {
        try {
            return OBJECT_MAPPER.writeValueAsBytes(this);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("failed to serialize set command", e);
        }
    }

    @Override
    public String toString() {
        return "SetCommand{" +
                "key='" + key + '\'' +
                ", requestId='" + requestId + '\'' +
                '}';
    }

}

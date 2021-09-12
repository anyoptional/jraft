package com.anyoptional.kvserver.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.lang.Nullable;

public class GetCommandResponse {

    /**
     * 是否找到数据，避免与 value 本来就
     * 为空产生冲突
     */
    private final boolean found;

    /**
     * 对应的数据
     */
    @Nullable
    private final byte[] value;

    public GetCommandResponse(@Nullable byte[] value) {
        this(value != null, value);
    }

    @JsonCreator
    public GetCommandResponse(@JsonProperty("found") boolean found,
                              @Nullable @JsonProperty("value") byte[] value) {
        this.found = found;
        this.value = value;
    }

    public boolean isFound() {
        return found;
    }

    @Nullable
    public byte[] getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "GetCommandResponse{found=" + found + '}';
    }

}

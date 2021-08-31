package com.anyoptional.raft.core;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.ToString;

/**
 * 节点ip地址
 */
@Getter
@ToString
public class Address {

    private final String host;

    private final int port;

    public Address(String host, int port) {
        Preconditions.checkNotNull(host);
        this.host = host;
        this.port = port;
    }

}

package com.anyoptional.raft.core.rpc;

import com.google.common.base.Preconditions;

import javax.annotation.concurrent.Immutable;

/**
 * Address.
 */
@Immutable
public class Address {

    private final String host;
    private final int port;

    /**
     * Create.
     *
     * @param host host
     * @param port port
     */
    public Address(String host, int port) {
        Preconditions.checkNotNull(host);
        this.host = host;
        this.port = port;
    }

    /**
     * Get host.
     *
     * @return host
     */
    public String getHost() {
        return host;
    }

    /**
     * Get port.
     *
     * @return port
     */
    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return "Address{" +
                "host='" + host + '\'' +
                ", port=" + port +
                '}';
    }

}
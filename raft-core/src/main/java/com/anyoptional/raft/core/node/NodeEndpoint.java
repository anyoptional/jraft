package com.anyoptional.raft.core.node;

import com.anyoptional.raft.core.rpc.Address;
import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.ToString;

/**
 * 节点连接信息
 */
@Getter
@ToString
public class NodeEndpoint {

    /**
     * 节点id
     */
    private final NodeId id;

    /**
     * 节点地址
     */
    private final Address address;

    public NodeEndpoint(String id, String host, int port) {
        this(new NodeId(id), new Address(host, port));
    }

    public NodeEndpoint(NodeId id, Address address) {
        Preconditions.checkNotNull(id);
        Preconditions.checkNotNull(address);
        this.id = id;
        this.address = address;
    }


    /**
     * Get host.
     *
     * @return host
     */
    public String getHost() {
        return this.address.getHost();
    }

    /**
     * Get port.
     *
     * @return port
     */
    public int getPort() {
        return this.address.getPort();
    }
}

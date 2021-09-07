package com.anyoptional.raft.core.rpc.nio;


import com.anyoptional.raft.core.rpc.ChannelException;
import com.anyoptional.raft.core.rpc.message.AppendEntriesResult;
import com.anyoptional.raft.core.rpc.message.AppendEntriesRpc;
import com.anyoptional.raft.core.rpc.message.RequestVoteResult;
import com.anyoptional.raft.core.rpc.message.RequestVoteRpc;
import io.netty.channel.Channel;

class NioChannel implements com.anyoptional.raft.core.rpc.Channel {

    private final Channel nettyChannel;

    NioChannel(Channel nettyChannel) {
        this.nettyChannel = nettyChannel;
    }

    @Override
    public void writeRequestVoteRpc(RequestVoteRpc rpc) {
        nettyChannel.writeAndFlush(rpc);
    }

    @Override
    public void writeRequestVoteResult(RequestVoteResult result) {
        nettyChannel.writeAndFlush(result);
    }

    @Override
    public void writeAppendEntriesRpc(AppendEntriesRpc rpc) {
        nettyChannel.writeAndFlush(rpc);
    }

    @Override
    public void writeAppendEntriesResult(AppendEntriesResult result) {
        nettyChannel.writeAndFlush(result);
    }
    
    @Override
    public void close() {
        try {
            nettyChannel.close().sync();
        } catch (InterruptedException e) {
            throw new ChannelException("failed to close", e);
        }
    }

    Channel getDelegate() {
        return nettyChannel;
    }
    
}

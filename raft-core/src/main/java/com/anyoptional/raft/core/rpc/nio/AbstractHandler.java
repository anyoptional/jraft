package com.anyoptional.raft.core.rpc.nio;

import com.anyoptional.raft.core.node.NodeId;
import com.anyoptional.raft.core.rpc.Channel;
import com.anyoptional.raft.core.rpc.message.*;
import com.google.common.eventbus.EventBus;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

abstract class AbstractHandler extends ChannelDuplexHandler {

    private static final Logger logger = LoggerFactory.getLogger(AbstractHandler.class);
    protected final EventBus eventBus;
    NodeId remoteId;
    protected Channel channel;
    private static AppendEntriesRpc lastAppendEntriesRpc;

    AbstractHandler(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        assert remoteId != null;
        assert channel != null;

        if (msg instanceof RequestVoteRpc) {
            RequestVoteRpc rpc = (RequestVoteRpc) msg;
            eventBus.post(new RequestVoteRpcMessage(rpc, remoteId, channel));
        } else if (msg instanceof RequestVoteResult) {
            eventBus.post(msg);
        } else if (msg instanceof AppendEntriesRpc) {
            AppendEntriesRpc rpc = (AppendEntriesRpc) msg;
            eventBus.post(new AppendEntriesRpcMessage(rpc, remoteId, channel));
        } else if (msg instanceof AppendEntriesResult) {
            AppendEntriesResult result = (AppendEntriesResult) msg;
            if (getLastAppendEntriesRpc() == null) {
                logger.warn("no last append entries rpc");
            } else {
                if (!Objects.equals(result.getRpcMessageId(), getLastAppendEntriesRpc().getMessageId())) {
                    logger.warn("incorrect append entries rpc message id {}, expected {}", result.getRpcMessageId(), getLastAppendEntriesRpc().getMessageId());
                } else {
                    eventBus.post(new AppendEntriesResultMessage(result, remoteId, getLastAppendEntriesRpc()));
                    setLastAppendEntriesRpc(null);
                }
            }
        }
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof AppendEntriesRpc) {
            setLastAppendEntriesRpc((AppendEntriesRpc) msg);
        }
        super.write(ctx, msg, promise);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.warn(cause.getMessage(), cause);
        ctx.close();
    }

    public AppendEntriesRpc getLastAppendEntriesRpc() {
        return lastAppendEntriesRpc;
    }

    public void setLastAppendEntriesRpc(AppendEntriesRpc lastAppendEntriesRpc) {
        if (lastAppendEntriesRpc == null) {
//            logger.info("clearing lastAppendEntriesRpc");
        } else {
//            logger.info("record lastAppendEntriesRpc: {}", lastAppendEntriesRpc);
        }
        this.lastAppendEntriesRpc = lastAppendEntriesRpc;
    }
}
